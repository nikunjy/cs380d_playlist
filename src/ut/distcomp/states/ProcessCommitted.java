package ut.distcomp.states;

import java.util.List;
import java.util.Map;

import ut.distcomp.application.ApplicationMessage;
import ut.distcomp.application.PlayListProcess;
import ut.distcomp.application.WaitUtil;
import ut.distcomp.framework.Config;
import ut.distcomp.framework.NetController;

public class ProcessCommitted implements State {
	Map<String, Object> ctx;
	private void sendCommitAll(NetController serverImpl,Config config) { 
		//replace with UPSET
		ApplicationMessage reply = new ApplicationMessage(config.procNum);
		reply.operation = ApplicationMessage.MessageTypes.COMMIT.value();
		for(int i=0;i<config.numProcesses;i++)
			serverImpl.sendMsg(i,reply.toString());

	}
	public String operate() {
		NetController serverImpl = (NetController)ctx.get("serverImpl");
		Config config = (Config)ctx.get("config");
		config.logger.info("Process Commited");
		PlayListProcess pprocess = (PlayListProcess)ctx.get("process");
		pprocess.saveDecision("Commit");
		if (config.procNum == 0) { 
			return "BECOMECOORD";
		}
		
		ApplicationMessage coordinatorMessage = new ApplicationMessage(config.procNum);
		coordinatorMessage.operation = ApplicationMessage.MessageTypes.COMMIT.value();
		serverImpl.sendMsg(0,coordinatorMessage.toString());
		while(true) {
			WaitUtil.waitUntilTimeout();
			List<String> messages = serverImpl.getReceivedMsgs();
			for(String msg : messages) {
				ApplicationMessage message = ApplicationMessage.getApplicationMsg(msg);
				if (message.isCompleteMessage()) {
					return "DONE";
				} else {
					sendCommitAll(serverImpl,config);
				}
			}
			if (pprocess.getLiveSet().size() == config.numProcesses) { 
				return "DONE";
			}
		}
	}
	public String getName() { 
		return "processCommitted";
	}
	public void setContext(Map<String,Object> context) { 
		ctx = context;
	}
}

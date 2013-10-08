package ut.distcomp.states;

import java.util.List;
import java.util.Map;

import ut.distcomp.application.ApplicationMessage;
import ut.distcomp.framework.Config;
import ut.distcomp.framework.NetController;

public class ProcessAborted implements State{
	Map<String, Object> ctx;
	private void sendAbortAll(NetController serverImpl,Config config) { 
		//replace with UPSET
		ApplicationMessage reply = new ApplicationMessage(config.procNum);
		reply.operation = ApplicationMessage.MessageTypes.ABORT.value();
		for(int i=0;i<config.numProcesses;i++)
			serverImpl.sendMsg(i,reply.toString());
		
	}
	public String operate() {
		NetController serverImpl = (NetController)ctx.get("serverImpl");
		Config config = (Config)ctx.get("config");
		ApplicationMessage coordinatorMessage = new ApplicationMessage(config.procNum);
		coordinatorMessage.operation = ApplicationMessage.MessageTypes.ABORT.value();
		serverImpl.sendMsg(0,coordinatorMessage.toString());
		while(true) {
			List<String> messages = serverImpl.getReceivedMsgs();
			for(String msg : messages) {
				ApplicationMessage message = ApplicationMessage.getApplicationMsg(msg);
				if (message.isCompleteMessage()) {
					return "DONE";
				} else {
					sendAbortAll(serverImpl,config);
				}
			}
		}
	}
	public String getName() { 
		return "processAborted";
	}
	public void setContext(Map<String,Object> context) { 
		ctx = context;
	}
}

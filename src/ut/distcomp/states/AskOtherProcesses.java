package ut.distcomp.states;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ut.distcomp.application.ApplicationMessage;
import ut.distcomp.application.PlayListProcess;
import ut.distcomp.application.WaitUtil;
import ut.distcomp.framework.Config;
import ut.distcomp.framework.NetController;

public class AskOtherProcesses implements State{
	private Map<String,Object> ctx;
	public String operate() { 
		Config config = (Config)ctx.get("config");
		NetController serverImpl = (NetController)ctx.get("serverImpl");
		PlayListProcess pprocess = (PlayListProcess)ctx.get("process");
		Set<Integer> liveSet = pprocess.getLiveSet(); 
		if (liveSet.size() == 0) {
			return "TOTALFAILURE";
		} else { 
			WaitUtil.waitUntilTimeout(4000);
			ApplicationMessage stateReq = new ApplicationMessage(config.procNum); 
			stateReq.operation = ApplicationMessage.MessageTypes.STATEREQ.value();
			for (Integer liveProc : liveSet) { 
				serverImpl.sendMsg(liveProc, stateReq.toString());
			}
			List<String> messages = serverImpl.getReceivedMsgs();
			for(String msg : messages) { 
				ApplicationMessage message = ApplicationMessage.getApplicationMsg(msg); 
				if(message.isCommit()) {
					return "COMMIT";
				}
				if(message.isAbort()) { 
					return "ABORT";
				}
			}
			return "NOOP";
		}
	}
	public String getName() { 
		return "askOtherProcesses";
	}
	public void setContext(Map<String,Object> ctx) {
		this.ctx = ctx;
	}

}

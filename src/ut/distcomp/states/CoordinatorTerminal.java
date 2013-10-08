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

public class CoordinatorTerminal implements State {
	private Map<String,Object> ctx;
	public String operate() {
		Config config = (Config)ctx.get("config");
		NetController serverImpl = (NetController)ctx.get("serverImpl");
		PlayListProcess pprocess = (PlayListProcess)ctx.get("process");
		Set<Integer> processSet = new HashSet<Integer>();
		processSet.add(0);
		while(processSet.size() != config.numProcesses) {
			WaitUtil.waitUntilTimeout();
			List<String> messages = serverImpl.getReceivedMsgs();
			for (String msg : messages) { 
				ApplicationMessage message = ApplicationMessage.getApplicationMsg(msg); 
				if (message.isCommit() || message.isAbort()) {
					processSet.add(message.sender);
				}
				if(message.isStateReq()) {
					//For recovery
					ApplicationMessage reply = new ApplicationMessage(config.procNum); 
					reply.operation = (String)ctx.get("lastDecision");
					serverImpl.sendMsg(message.sender, reply.toString());
				}
			}
		}
		ApplicationMessage completeMessage = new ApplicationMessage(config.procNum); 
		completeMessage.operation = ApplicationMessage.MessageTypes.COMPLETE.value();
		pprocess.broadCast(completeMessage);
		return "SUCCESS";
	}
	public String getName() { 
		return "coordinatorTerminal";
	}
	public void setContext(Map<String,Object> context) { 
		ctx = context;
	}

}

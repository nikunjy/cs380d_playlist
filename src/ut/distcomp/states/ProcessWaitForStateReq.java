package ut.distcomp.states;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import ut.distcomp.application.ApplicationMessage;
import ut.distcomp.application.InstructionUtils;
import ut.distcomp.application.PlayListProcess;
import ut.distcomp.application.WaitUtil;
import ut.distcomp.framework.Config;
import ut.distcomp.framework.NetController;

public class ProcessWaitForStateReq implements State{
	Map<String, Object> ctx;
	public String operate() {
		Config config = (Config)ctx.get("config");
		NetController serverImpl = (NetController)ctx.get("serverImpl");
		String lastState = (String)ctx.get("lastState");
		WaitUtil.waitUntilTimeout();
		List<String> messages = serverImpl.getReceivedMsgs();
		System.out.println(messages);
		for(String msg : messages) {
			ApplicationMessage message = ApplicationMessage.getApplicationMsg(msg);
			if(message.isStateReq()) {
				ApplicationMessage reply = new ApplicationMessage(config.procNum);
				reply.operation = ApplicationMessage.MessageTypes.STATERESP.value();
				reply.message = lastState;
				config.logger.info("Sending last state "+reply.message+" to new coordinator");
				serverImpl.sendMsg(message.sender, reply.toString());
				if (lastState.equalsIgnoreCase("processWaitForDecision"))  {
					ctx.put("lastState","processWaitForPreCommit");
				}
				return "STATE-SENT";
			}
		}
		return "REELECT";
	}
	public String getName() { 
		return "processWaitForStateReq"; 
	}
	public void setContext(Map<String,Object> context) { 
		ctx = context;
	}
}

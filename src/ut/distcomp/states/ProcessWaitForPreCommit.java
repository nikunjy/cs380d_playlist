package ut.distcomp.states;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import ut.distcomp.application.ApplicationMessage;
import ut.distcomp.application.PlayListProcess;
import ut.distcomp.application.WaitUtil;
import ut.distcomp.framework.Config;
import ut.distcomp.framework.NetController;
import ut.distcomp.application.InstructionUtils;
public class ProcessWaitForPreCommit implements State{
	private Map<String,Object> ctx;
	public String operate() { 
		Config config = (Config)ctx.get("config");
		NetController serverImpl = (NetController)ctx.get("serverImpl");
		PlayListProcess pprocess = (PlayListProcess)ctx.get("process");
		ctx.put("lastState",getName()); 
		WaitUtil.waitUntilTimeout();
		List<String> messages = serverImpl.getReceivedMsgs();
		boolean error = false;
		if (messages.size() != 0) {
			System.out.println(messages);
			for (String msg : messages) {
				ApplicationMessage message = ApplicationMessage
						.getApplicationMsg(msg);
				if (message.isPreCommit()) {
					config.logger.info("Received precommit");
					Properties props = pprocess.getProperties();
					props.setProperty(
							PlayListProcess.LogCategories.LASTSTATE.value(),
							message.operation);
					pprocess.writeProperties(props);
					if (InstructionUtils.sendAck()) {
						ApplicationMessage reply = new ApplicationMessage(
								config.procNum);
						reply.operation = ApplicationMessage.MessageTypes.ACK
								.value();
						config.logger.info("Sent an ACK");
						serverImpl.sendMsg(message.sender, reply.toString());
					}
					return "SUCCESS";
				}
				if (message.isAbort()) {
					Properties props = pprocess.getProperties();
					props.setProperty(
							PlayListProcess.LogCategories.OPERATION.value(), "");
					props.setProperty(
							PlayListProcess.LogCategories.DECISION.value(),
							message.operation);
					pprocess.writeProperties(props);
					return "ABORT";
				}
			}
		}
		return "REELECT";
	}
	public String getName() { 
		return "processWaitForPreCommit";
	}
	public void setContext(Map<String,Object> context) { 
		ctx = context;
	}

}

package ut.distcomp.states;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import ut.distcomp.application.ApplicationMessage;
import ut.distcomp.application.PlayListProcess;
import ut.distcomp.application.WaitUtil;
import ut.distcomp.framework.Config;
import ut.distcomp.framework.NetController;

public class CoordinatorWaitForAck implements State{
	private Map<String,Object> ctx;
	public String operate() { 
		Config config = (Config)ctx.get("config");
		NetController serverImpl = (NetController)ctx.get("serverImpl");
		PlayListProcess pprocess = (PlayListProcess)ctx.get("process");
		WaitUtil.waitUntilTimeout();
		ApplicationMessage reply = new ApplicationMessage(config.procNum);
		reply.operation = ApplicationMessage.MessageTypes.COMMIT.value();
		List<String> messages = serverImpl.getReceivedMsgs();
		int count = 0;
		for(String msg : messages) { 
			ApplicationMessage message = ApplicationMessage.getApplicationMsg(msg); 
			if(message.isAck()) { 
				int sender = message.sender;
				
				if (count == 0) {
					Properties props = pprocess.getProperties();
					props.setProperty(PlayListProcess.LogCategories.DECISION.value(), reply.operation);	
					pprocess.writeProperties(props);
					count++;
				}
				serverImpl.sendMsg(sender,reply.toString());
			}
		}

		Set<Integer> liveSet = pprocess.getLiveSet();
		for(Integer proc : liveSet) { 
			serverImpl.sendMsg(proc, reply.toString());
		}
		if (config.procNum != 0) {
			return "COMMIT";
		} else {
			ctx.put("lastDecision", "COMMIT");
			return "TERMINAL";
		}

		/*
		ApplicationMessage reply = new ApplicationMessage(config.procNum); 
		reply.operation = ApplicationMessage.MessageTypes.ABORT.value();
		pprocess.broadCast(reply);
		if (config.procNum != 0) { 		
			return "ABORT";
		} else {
			ctx.put("lastDecision", "ABORT");
			return "TERMINAL";
		}*/
	}
	public String getName() { 
		return "coordinatorWaitForAck";
	}
	public void setContext(Map<String,Object> context) { 
		ctx = context;
	}

}

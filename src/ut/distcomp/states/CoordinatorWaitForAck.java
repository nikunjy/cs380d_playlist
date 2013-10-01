package ut.distcomp.states;

import java.util.List;
import java.util.Map;
import java.util.Properties;

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
		List<String> messages = serverImpl.getReceivedMsgs();
		System.out.println(messages);
		for(String msg : messages) { 
			ApplicationMessage message = ApplicationMessage.getApplicationMsg(msg); 
			if(message.isAck()) { 
				Properties props = pprocess.getProperties();
				
				int sender = message.sender;
				ApplicationMessage reply = new ApplicationMessage(config.procNum);
				reply.operation = ApplicationMessage.MessageTypes.COMMIT.value();
				props.setProperty(PlayListProcess.LogCategories.DECISION.value(), reply.operation);	
				//TODO write this once
				pprocess.writeProperties(props);
				serverImpl.sendMsg(sender,reply.toString());
			}
		}
		if(messages.size()>0) { 
			return "SUCCESS"; 
		}
		ApplicationMessage reply = new ApplicationMessage(config.procNum); 
		reply.operation = ApplicationMessage.MessageTypes.ABORT.value();
		pprocess.broadCast(reply);
		return "ABORT";		
	}
	public String getName() { 
		return "coordinatorWaitForAck";
	}
	public void setContext(Map<String,Object> context) { 
		ctx = context;
	}

}

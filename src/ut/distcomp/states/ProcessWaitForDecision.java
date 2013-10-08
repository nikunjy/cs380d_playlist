package ut.distcomp.states;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import ut.distcomp.application.ApplicationMessage;
import ut.distcomp.application.PlayListProcess;
import ut.distcomp.application.WaitUtil;
import ut.distcomp.framework.Config;
import ut.distcomp.framework.NetController;

public class ProcessWaitForDecision implements State{
	private Map<String,Object> ctx;
	public String operate() { 
		Config config = (Config)ctx.get("config");
		NetController serverImpl = (NetController)ctx.get("serverImpl");
		PlayListProcess pprocess = (PlayListProcess)ctx.get("process");
		ctx.put("lastState", getName());
		WaitUtil.waitUntilTimeout();
		List<String> messages = serverImpl.getReceivedMsgs();
		System.out.println(messages);
		for(String msg : messages) { 
			ApplicationMessage message = ApplicationMessage.getApplicationMsg(msg); 
			if(message.isCommit() || message.isAbort()) { 
				Properties props = pprocess.getProperties();
				props.setProperty(PlayListProcess.LogCategories.DECISION.value(), message.operation);
				pprocess.writeProperties(props); 
				return "SUCCESS";
			}
		}
		ApplicationMessage pingMessage = new ApplicationMessage(config.procNum);
		pingMessage.operation = ApplicationMessage.MessageTypes.PING.value(); 
		boolean isSuccess = serverImpl.sendMsg(pprocess.coordinator, pingMessage.toString());
		if (isSuccess) { 
			return operate();
		} 
		return "REELECT";
	}
	public String getName() { 
		return "processWaitForDecision";
	}
	public void setContext(Map<String,Object> context) { 
		ctx = context;
	}

}

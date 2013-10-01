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
		WaitUtil.waitUntilTimeout();
		List<String> messages = serverImpl.getReceivedMsgs();
		for(String msg : messages) { 
			ApplicationMessage message = ApplicationMessage.getApplicationMsg(msg); 
			if(message.isCommit()) { 
				Properties props = pprocess.getProperties();
				props.setProperty(PlayListProcess.LogCategories.DECISION.value(), message.operation);
				pprocess.writeProperties(props); 
				return "SUCCESS";
			}
		}
		return "ERROR";
	}
	public String getName() { 
		return "processWaitForDecision";
	}
	public void setContext(Map<String,Object> context) { 
		ctx = context;
	}

}

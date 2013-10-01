package ut.distcomp.states;

import java.util.Map;
import java.util.Properties;

import ut.distcomp.application.ApplicationMessage;
import ut.distcomp.application.InstructionUtils;
import ut.distcomp.application.PlayListProcess;
import ut.distcomp.framework.Config;
import ut.distcomp.framework.NetController;

public class CoordinatorInitiate implements State{
	private Map<String,Object> ctx;
	public String operate() { 
		Config config = (Config)ctx.get("config");
		NetController serverImpl = (NetController)ctx.get("serverImpl");
		if(config == null) { 
			return "Error";
		}
		PlayListProcess pprocess = (PlayListProcess)ctx.get("process");
		ApplicationMessage initMessage = InstructionUtils.getPlayListOperation(config);
		Properties props = pprocess.getProperties(); 
		props.setProperty(PlayListProcess.LogCategories.OPERATION.value(),initMessage.toString());
		pprocess.writeProperties(props);
		pprocess.broadCast(initMessage);
		config.logger.info("Sent broadcast for "+initMessage.toString());
		boolean success = true;
		for(int i=0;i<config.addresses.length;i++) {
			boolean sentSuccess = serverImpl.sendMsg(i,initMessage.toString());
			success = success & sentSuccess;
		}
		if (!success) {
			ApplicationMessage abortMessage = new ApplicationMessage(config.procNum);
			abortMessage.operation = ApplicationMessage.MessageTypes.ABORT.value();
			props = pprocess.getProperties(); 
			props.setProperty(PlayListProcess.LogCategories.DECISION.value(),abortMessage.operation);
			pprocess.writeProperties(props); 
			pprocess.broadCast(abortMessage);
			return "ABORT";
		}
		return "SUCCESS";
		
	}
	public String getName() { 
		return "coordinatorInitiate";
	}
	public void setContext(Map<String,Object> ctx) {
		this.ctx = ctx;
	}
}

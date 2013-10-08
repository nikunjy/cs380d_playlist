package ut.distcomp.states;

import java.util.Map;

import ut.distcomp.application.ApplicationMessage;
import ut.distcomp.application.PlayListProcess;
import ut.distcomp.framework.Config;
import ut.distcomp.framework.NetController;

public class ProcessElect implements State{
	private Map<String,Object> ctx;
	public String operate() { 
		//select new coordinator
		Config config = (Config)ctx.get("config");
		NetController serverImpl = (NetController)ctx.get("serverImpl");
		PlayListProcess pprocess = (PlayListProcess)ctx.get("process");	
		for(int i = pprocess.coordinator + 1;i < config.procNum ;i++) {  
			ApplicationMessage message = new ApplicationMessage(config.procNum);
			message.operation = ApplicationMessage.MessageTypes.ELECT.value();
			boolean success = serverImpl.sendMsg(i, message.toString());
			if(success) {
				config.logger.info("I am still a participant");
				pprocess.removeFromLiveSet(i-1);
				pprocess.coordinator = i;
				return "PARTICIPANT";
			}
		}
		config.logger.info("I am the new Coordinator");
		return "ELECTED";
	}
	public String getName() { 
		return "processElect";
	}
	public void setContext(Map<String,Object> context) {
		this.ctx = context;
	}

}

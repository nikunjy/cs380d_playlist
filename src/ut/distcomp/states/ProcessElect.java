package ut.distcomp.states;

import java.util.Map;
import java.util.Set;

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
		Set<Integer> liveProcesses = pprocess.getLiveSet();
		pprocess.writeLiveSet(liveProcesses);
		
		for(Integer liveProcess : liveProcesses) {  
			ApplicationMessage message = new ApplicationMessage(config.procNum);
			message.operation = ApplicationMessage.MessageTypes.ELECT.value();
			boolean success = serverImpl.sendMsg(liveProcess, message.toString());
			if(success) {
				pprocess.coordinator = liveProcess;
				if(liveProcess == config.procNum)
					break;
				config.logger.info("I am still a participant");
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

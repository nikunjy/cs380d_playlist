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
		/*Need logic to determine just by looking at UP set, who the new co-ordinator is*/
		for(int i = 0;i<config.procNum ;i++) { 
			//if(i!=pprocess.coordinator) { 
				ApplicationMessage message = new ApplicationMessage(config.procNum);
				message.operation = ApplicationMessage.MessageTypes.ELECT.value();
				boolean success = serverImpl.sendMsg(i, message.toString());
				if(success) {
					return "PARTICIPANT";
				}
			//}
		}
		return "ELECTED";
	}
	public String getName() { 
		return "processElect";
	}
	public void setContext(Map<String,Object> context) {
		this.ctx = context;
	}

}

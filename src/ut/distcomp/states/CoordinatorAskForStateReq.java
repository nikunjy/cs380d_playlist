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

public class CoordinatorAskForStateReq implements State{

	private Map<String,Object> ctx;
	
	public String operate() {
		Config config = (Config)ctx.get("config");
		NetController serverImpl = (NetController)ctx.get("serverImpl");
		//PlayListProcess pprocess = (PlayListProcess)ctx.get("process");
		
		ApplicationMessage msg = new ApplicationMessage(config.procNum);
		msg.operation = ApplicationMessage.MessageTypes.STATEREQ.value();
		/*Need UP set here to, to broadcast*/
		for(int i=0;i<config.numProcesses;i++) {
			if(i != config.procNum)
				serverImpl.sendMsg(i,msg.toString());
		}
		return "DONE";
	}

	public String getName() { 
		return "CoordinatorAskForStateReq"; 
	}
	public void setContext(Map<String,Object> context) { 
		ctx = context;
	}

}

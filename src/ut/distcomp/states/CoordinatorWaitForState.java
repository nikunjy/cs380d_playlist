package ut.distcomp.states;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import ut.distcomp.application.ApplicationMessage;
import ut.distcomp.application.InstructionUtils;
import ut.distcomp.application.PlayListProcess;
import ut.distcomp.application.WaitUtil;
import ut.distcomp.framework.Config;
import ut.distcomp.framework.NetController;


public class CoordinatorWaitForState implements State{
	private Map<String,Object> ctx;
	int decision=-1; /*1-Committed, 2-Aborted, 3-Committable*/
	public String operate() {
		Config config = (Config)ctx.get("config");
		NetController serverImpl = (NetController)ctx.get("serverImpl");
		PlayListProcess pprocess = (PlayListProcess)ctx.get("process");
		
		List<Integer> procs = new ArrayList<Integer>();
		WaitUtil.waitUntilTimeout(4000);
		List<String> messages = serverImpl.getReceivedMsgs();
		/*Add its own state*/
		ApplicationMessage mesg = new ApplicationMessage(config.procNum);
		mesg.operation = ApplicationMessage.MessageTypes.STATERESP.value();
		mesg.message = (String)ctx.get("lastState");
		messages.add(mesg.toString());
		boolean committableProcess = true;
		for(String msg : messages) {
			ApplicationMessage message = ApplicationMessage.getApplicationMsg(msg);
			if(message.isStateResp()) {
				if(message.message.equalsIgnoreCase("processCommitted")) {
					decision = 1;
					break;
				}
				if(message.message.equalsIgnoreCase("processAborted")) {
					decision = 2;
					break;
				}
				if(message.message.equalsIgnoreCase("processWaitForDecision")) {
					committableProcess = true;
				}
			}
		}
		ApplicationMessage reply = new ApplicationMessage(config.procNum);
		Properties p = pprocess.getProperties();
		p.setProperty(PlayListProcess.LogCategories.LASTSTATE.value(),
				"waitForState");
		
		
		if(!committableProcess) {
			
			if (decision == 1) {
				p.setProperty(PlayListProcess.LogCategories.DECISION.value(),
						"commit");
				pprocess.writeProperties(p);
				reply.operation = ApplicationMessage.MessageTypes.COMMIT
						.value();

			}
			else {
				p.setProperty(PlayListProcess.LogCategories.DECISION.value(),
						"abort");
				pprocess.writeProperties(p);
				reply.operation = ApplicationMessage.MessageTypes.ABORT
						.value();

			}
			for(int i=0;i<config.numProcesses;i++)
				serverImpl.sendMsg(i,reply.toString());
			return "DONE";
		}
		else {
			config.logger.info("Found a process comittable");
			p.setProperty(PlayListProcess.LogCategories.DECISION.value(),
					"precommit");
			pprocess.writeProperties(p);
			reply.operation = ApplicationMessage.MessageTypes.PRECOMMIT.value();
			for(int j = config.procNum + 1; j < config.addresses.length; j++) 
				serverImpl.sendMsg(j, reply.toString());
			return "PRECOMMIT";
		}
		
	}
	public String getName() { 
		return "coordinatorWaitForState"; 
	}
	public void setContext(Map<String,Object> context) { 
		ctx = context;
	}
}

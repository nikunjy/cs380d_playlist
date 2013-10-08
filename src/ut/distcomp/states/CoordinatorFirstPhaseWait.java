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

public class CoordinatorFirstPhaseWait implements State {
	private Map<String,Object> ctx;
	/*
	 * (non-Javadoc)
	 * @see ut.distcomp.states.State#operate()
	 * This state is reached when the processes have been sent messages successfullly.
	 */
	public String operate() { 
		Config config = (Config)ctx.get("config");
		NetController serverImpl = (NetController)ctx.get("serverImpl");
		PlayListProcess pprocess = (PlayListProcess)ctx.get("process");
		
		int yesVotes = 1;
		int totalVotes = 1;
		while (true) {
			WaitUtil.waitUntilTimeout(500);
			List<String> messages = serverImpl.getReceivedMsgs();
			for(String msg : messages) { 
				ApplicationMessage message = ApplicationMessage.getApplicationMsg(msg);
				if(message.isVote()) {
					if(message.getVote().equalsIgnoreCase("Yes")) {
						yesVotes++;
					} 
					totalVotes++;
				}
			}
			Set<Integer> liveSet = pprocess.getLiveSet();
			if (liveSet.size() == totalVotes) {
				break;
			}
		}
		if (yesVotes == config.numProcesses) {
			//Send pre commit to all
			config.logger.info("Sending pre commit");
			Properties p = pprocess.getProperties();
			p.setProperty(PlayListProcess.LogCategories.LASTSTATE.value(),
					"Precommit");
			pprocess.writeProperties(p);
			ApplicationMessage reply = new ApplicationMessage(config.procNum);
			reply.operation = ApplicationMessage.MessageTypes.PRECOMMIT.value();
			pprocess.broadCast(reply);
			return "PRECOMMIT";
		} else {
			config.logger.info("Going to send an abort");
			Properties p = pprocess.getProperties();
			p.setProperty(PlayListProcess.LogCategories.LASTSTATE.value(),
					"Firstphasewait");
			p.setProperty(PlayListProcess.LogCategories.DECISION.value(),
					"Abort");
			pprocess.writeProperties(p);
			
			ApplicationMessage reply = new ApplicationMessage(config.procNum); 
			reply.operation = ApplicationMessage.MessageTypes.ABORT.value();
			pprocess.broadCast(reply);
			ctx.put("lastDecision", "ABORT");
			return "ABORT";
		}
	}
	public String getName() { 
		return "coordinatorFirstPhaseWait";
	}
	public void setContext(Map<String,Object> ctx) {
		this.ctx = ctx;
	}
}

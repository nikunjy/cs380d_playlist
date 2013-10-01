package ut.distcomp.states;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import ut.distcomp.application.ApplicationMessage;
import ut.distcomp.application.InstructionUtils;
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
		Integer[] votes = new Integer[config.addresses.length];
		for(int i=0;i<votes.length;i++)
			votes[i] = -1;
		votes[config.procNum] = 1;
		int count = 0;
		WaitUtil.waitUntilTimeout(500);
		List<String> messages = serverImpl.getReceivedMsgs();
		System.out.println(messages);
		long now = (new Date()).getTime();
		for(String msg : messages) { 
			ApplicationMessage message = ApplicationMessage.getApplicationMsg(msg);
			if(message.isVote()) {
				if(message.getVote().equalsIgnoreCase("Yes")) {
					votes[message.sender] = 1;
				} else {
					votes[message.sender] = 0;
				}
			}
		}
		
		for(int i=0;i<votes.length;i++) { 
			count+=votes[i];
		}
		if(count == votes.length) {
			//Send pre commit to all
			config.logger.info("Sending pre commit");
			Properties p = pprocess.getProperties();
			p.setProperty(PlayListProcess.LogCategories.LASTSTATE.value(),
					"precommit");
			pprocess.writeProperties(p);
			ApplicationMessage reply = new ApplicationMessage(config.procNum);
			reply.operation = ApplicationMessage.MessageTypes.PRECOMMIT.value();
			pprocess.broadCast(reply);
			return "PRECOMMIT";
		} else {
			config.logger.info("Going to send an abort");
			Properties p = pprocess.getProperties();
			p.setProperty(PlayListProcess.LogCategories.LASTSTATE.value(),
					"firstphasewait");
			p.setProperty(PlayListProcess.LogCategories.DECISION.value(),
					"abort");
			pprocess.writeProperties(p);

			for (int i = 0 ;i<votes.length;i++) { 
				if( i !=config.procNum && votes[i] == 1) {
					ApplicationMessage reply = new ApplicationMessage(config.procNum); 
					reply.operation = ApplicationMessage.MessageTypes.ABORT.value();
					serverImpl.sendMsg(i,reply.toString());
				}
			}
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

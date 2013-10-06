package ut.distcomp.states;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import ut.distcomp.application.ApplicationMessage;
import ut.distcomp.application.InstructionUtils;
import ut.distcomp.application.PlayListProcess;
import ut.distcomp.framework.Config;
import ut.distcomp.framework.NetController;

public class ProcessWaitForInitiate  implements State{
	private Map<String,Object> ctx;
	public String operate() { 
		Config config = (Config)ctx.get("config");
		NetController serverImpl = (NetController)ctx.get("serverImpl");
		PlayListProcess pprocess = (PlayListProcess)ctx.get("process");
		List<String> messages = serverImpl.getReceivedMsgs();
		for(String msg : messages) { 
			ApplicationMessage message = ApplicationMessage.getApplicationMsg(msg); 
			if(message.isPlayListOperation()) {
				Properties p = pprocess.getProperties();
				p.setProperty(PlayListProcess.LogCategories.OPERATION.value(),message.toString());
				//send back a vote
				ApplicationMessage reply = new ApplicationMessage(config.procNum);
				reply.operation = ApplicationMessage.MessageTypes.VOTE.value();
				reply.vote = InstructionUtils.getVoteInstruction();
				p.setProperty(PlayListProcess.LogCategories.DECISION.value(), reply.vote);
				pprocess.writeProperties(p);
				serverImpl.sendMsg(message.sender, reply.toString());
				return "VoteSent";
			}
			if(message.isNewCoordinatorMessage()) {
				//I'm the new Coordinator, get my last state
				String lastState = (String)ctx.get("lastState");
				ApplicationMessage reply = new ApplicationMessage(config.procNum);
				if(lastState.equalsIgnoreCase("processAborted"))
					reply.operation = ApplicationMessage.MessageTypes.ABORT
						.value();
				else
					reply.operation = ApplicationMessage.MessageTypes.COMMIT
						.value();
				for(int i=0;i<config.numProcesses;i++)
					serverImpl.sendMsg(i,reply.toString());
				return "NOOP";
			}
		}
		return "NOOP";
	}
	public String getName() { 
		return "processWaitForInitiate"; 
	}
	public void setContext(Map<String,Object> context) { 
		ctx = context;
	}

}

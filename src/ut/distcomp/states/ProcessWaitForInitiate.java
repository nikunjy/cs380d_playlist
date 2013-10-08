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
				System.out.println(message.toString());
				Properties p = pprocess.getProperties();
				p.setProperty(PlayListProcess.LogCategories.OPERATION.value(),message.toString());
				//send back a vote
				ApplicationMessage reply = new ApplicationMessage(config.procNum);
				reply.operation = ApplicationMessage.MessageTypes.VOTE.value();
				reply.vote = InstructionUtils.getVoteInstruction();
				p.setProperty(PlayListProcess.LogCategories.LASTSTATE.value(), reply.vote);
				pprocess.writeProperties(p);
				serverImpl.sendMsg(message.sender, reply.toString());
				System.out.println(reply.toString());
				return "VoteSent";
			}
		}
		WaitUtil.waitUntilTimeout();
		return "NOOP";
	}
	public String getName() { 
		return "processWaitForInitiate"; 
	}
	public void setContext(Map<String,Object> context) { 
		ctx = context;
	}

}

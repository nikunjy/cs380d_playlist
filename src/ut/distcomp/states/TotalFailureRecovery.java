package ut.distcomp.states;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ut.distcomp.application.ApplicationMessage;
import ut.distcomp.application.PlayListProcess;
import ut.distcomp.application.WaitUtil;
import ut.distcomp.framework.Config;
import ut.distcomp.framework.NetController;

public class TotalFailureRecovery implements State{
	private Map<String,Object> ctx;
	private Set<Integer> interSection(Set<Integer> set1, Set<Integer> set2)  {
		Set<Integer> intersection = new HashSet<Integer>();
		for(Integer i : set1) { 
			if(set2.contains(i)) { 
				intersection.add(i);
			}
		}
		return intersection;
	}
	private Set<Integer> sendLiveSetMessage(Set <Integer> logLiveSet) {
		Config config = (Config)ctx.get("config");
		NetController serverImpl = (NetController)ctx.get("serverImpl");
		PlayListProcess pprocess = (PlayListProcess)ctx.get("process");
		ApplicationMessage liveSetMessage = new ApplicationMessage(config.procNum); 
		liveSetMessage.operation = ApplicationMessage.MessageTypes.UPSET.value();
		liveSetMessage.message = liveSetMessage.serializeLiveSet(logLiveSet);
		Set<Integer> recoveringProcs = new HashSet<Integer>();
		for(int i = 0;i < config.numProcesses; i++) { 
			boolean success = serverImpl.sendMsg(i,liveSetMessage.toString());
			if(success) { 
				recoveringProcs.add(i);
			}
		}
		return recoveringProcs;
	}
	private boolean isSubset(Set<Integer> set1, Set<Integer> set2) {
		return set1.equals(interSection(set1,set2));
	}
	public String operate() { 
		Config config = (Config)ctx.get("config");
		NetController serverImpl = (NetController)ctx.get("serverImpl");
		PlayListProcess pprocess = (PlayListProcess)ctx.get("process");
		Set<Integer> recoveringProcesses = new HashSet<Integer>();
		WaitUtil.waitUntilTimeout();
		Set<Integer> recoveringProcs = sendLiveSetMessage(pprocess.liveSet);
		Set<Integer> intersectionSet =  pprocess.liveSet;
		 
		List<String> messages = serverImpl.getReceivedMsgs(); 
		for(String msg : messages) {  
			ApplicationMessage message = ApplicationMessage.getApplicationMsg(msg);
			if (message.isUpSetMessage())  {
				Set<Integer> recvLiveSet = message.getLiveSetFromMessage(message.message);
				interSection(intersectionSet,recvLiveSet);
			}
		}
		if (isSubset(recoveringProcs,intersectionSet)) { 
			pprocess.writeLiveSet(recoveringProcs);
			pprocess.coordinator = 0;
			serverImpl.openPing();
			return "REELECT";
		}
		return "TOTALFAILURE";
		
	}
	public String getName() { 
		return "totalFailureRecovery";
	}
	public void setContext(Map<String,Object> ctx) {
		this.ctx = ctx;
	}
}

package ut.distcomp.application;

import java.util.HashSet;
import java.util.Set;

import com.google.gson.Gson;

public class ApplicationMessage {
	
	public enum MessageTypes {
		ADD("Add"),DELETE("Delete"),EDIT("Edit"),ELECT("NewLeader"),VOTE("Vote"),COMMIT("Commit"),ABORT("Abort"),
		PRECOMMIT("precommit"),ACK("ack"),STATEREQ("stateReq"),STATERESP("stateResp"),COMPLETE("transactionComplete"),
		RSTATEREQ("recoverStateReq"),UPSET("upSet"),PING("ping");
		public String message;
		public String value() { 
			return message;
		}
		MessageTypes(String message) {
			this.message = message;
		}
	}
	/*
	 * All the operations are same except Edit for now. 
	 * This will also hold the operations for recovery and as such. 
	 */
	public String message;
	public String operation; 
	public String oldplayListName; 
	public String newPlayListName;
	public int coordinator;
	public String vote;
	public String playListOpTarget;
	public int sender;
	public int receiver;
	public ApplicationMessage(int sender) { 
		this.sender = sender;
	}
	public static ApplicationMessage getApplicationMsg(String message) { 
		Gson g = new Gson();
		return g.fromJson(message, ApplicationMessage.class);
	}
	public boolean isNewCoordinatorMessage() { 
		return operation.equalsIgnoreCase("NewLeader");
	}
	public boolean isPing() { 
		return operation.equalsIgnoreCase("Ping");
	}
	public boolean isCommit() { 
		return operation.equalsIgnoreCase("Commit");
	}
	public boolean isAbort() {
		return operation.equalsIgnoreCase("Abort");
	}
	public boolean isPreCommit() { 
		return operation.equalsIgnoreCase("PreCommit");
	}
	public boolean isAdd() { 
		return operation.equalsIgnoreCase("Add");
	}
	public boolean isEdit() { 
		return operation.equalsIgnoreCase("Edit"); 
	}
	public boolean isDelete() { 
		return operation.equalsIgnoreCase("Delete");
	}
	public boolean isAck() { 
		return operation.equalsIgnoreCase(MessageTypes.ACK.value());
	}
	public boolean isStateReq() {
		return operation.equalsIgnoreCase("stateReq");
	}
	public boolean isStateResp() {
		return operation.equalsIgnoreCase("stateResp");
	}
	public String getName() { 
		return oldplayListName;
	}
	public String getNewName() { 
		return newPlayListName;
	}

	public int getNewCoordinator() throws Exception { 
		if(isNewCoordinatorMessage()) { 
			return coordinator;
		} else { 
			throw new Exception("Not a new coordinator message");
		}
	}
	public boolean isPlayListOperation() { 
		return (isAdd() || isEdit() || isDelete());
	}
	public boolean isUpSetMessage() { 
		return operation.equalsIgnoreCase(ApplicationMessage.MessageTypes.UPSET.value());
	}
	public Set<Integer> getLiveSetFromMessage(String message) { 
		String[] procs = message.split(" ");
		Set<Integer> liveSet = new HashSet<Integer>();
		for(String proc : procs) {
			if(proc.equals("") || proc == null)
				continue;
			liveSet.add(Integer.parseInt(proc));
		}
		return liveSet;
	}
	public String serializeLiveSet(Set<Integer> liveSet) { 
		String ret = "";
		int count = 0;
		for(Integer liveProcs : liveSet) { 
			ret+=liveProcs;
			if (count != liveSet.size()-1) {
				ret +=" ";
			}
			count++;
		}
		return ret;
	}
	public boolean isRStateReq() { 
		return operation.equalsIgnoreCase(ApplicationMessage.MessageTypes.RSTATEREQ.value()); 
	}
	public String getVote() { 
		return vote;
	}
	public boolean isVote() { 
		return operation.equalsIgnoreCase(ApplicationMessage.MessageTypes.VOTE.value());
	}
	public boolean isCompleteMessage() { 
		return operation.equalsIgnoreCase(ApplicationMessage.MessageTypes.COMPLETE.value());
	}
	public String toString() { 
		Gson g = new Gson(); 
		return g.toJson(this);
	}

}

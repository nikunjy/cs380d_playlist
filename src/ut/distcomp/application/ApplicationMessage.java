package ut.distcomp.application;

import com.google.gson.Gson;

public class ApplicationMessage {
	
	public enum MessageTypes {
		ADD("Add"),DELETE("Delete"),EDIT("Edit"),ELECT("NewLeader"),VOTE("Vote"),COMMIT("Commit"),ABORT("Abort"),
		PRECOMMIT("precommit"),ACK("ack");
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
	public String getVote() { 
		return vote;
	}
	public boolean isVote() { 
		return operation.equalsIgnoreCase(ApplicationMessage.MessageTypes.VOTE.value());
	}
	public String toString() { 
		Gson g = new Gson(); 
		return g.toJson(this);
	}

}

package ut.distcomp.application;

public class ApplicationMessage {
	
	public enum MessageTypes {
		ADD("Add"),DELETE("Delete"),EDIT("Edit"),ELECT("NewLeader"),VOTE("Vote"),COMMIT("Commit"),ABORT("Abort");
		public String message;
		String value() { 
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
	String message;
	String operation; 
	String oldplayListName; 
	String newPlayListName;
	String coordinator;
	String vote;
	String playListOpTarget;
	public ApplicationMessage(String message) throws Exception{ 
		this.message = message;
		String[] subMessages = message.split("\\s+");
		if(subMessages.length<2) {
			//TODO message semantics here
		}
		operation = subMessages[0];
		if(isNewCoordinatorMessage()) {
			coordinator = subMessages[1];
			return;
		}
		if(isAbort() || isCommit()) { 
			return;
		}
		oldplayListName = subMessages[1];
		if(isDelete() || isAdd()) { 
			playListOpTarget = subMessages[2];
			return;
		}
		if(isEdit()) {
			newPlayListName = subMessages[2];
			playListOpTarget = subMessages[3];
		}else if (isVote()) {
			vote = subMessages[1];
		}
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
	public String getName() { 
		return oldplayListName;
	}
	public String getNewName() { 
		return newPlayListName;
	}
	public String getNewCoordinator() throws Exception { 
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
		return operation.equalsIgnoreCase("Vote");
	}

}

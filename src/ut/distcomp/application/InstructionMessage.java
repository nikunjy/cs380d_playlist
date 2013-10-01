package ut.distcomp.application;
//We will save this to a file and do testing based on that file. 
//File will contain the instructions and processes will behave accordingly
public class InstructionMessage {
	public boolean doAck; 
	public boolean sendYes;
	public boolean sendNothing;
	public boolean sendPreCommit; 
	public String operation; 
	public String playListName; 
	public String newPlayListName;
	public boolean failAfterVote; 
}

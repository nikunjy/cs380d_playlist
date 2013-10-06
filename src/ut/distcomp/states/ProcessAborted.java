package ut.distcomp.states;

import java.util.Map;

public class ProcessAborted implements State{
	Map<String, Object> ctx;
	public String operate() {
		return "DONE";
	}
	public String getName() { 
		return "processAborted";
	}
	public void setContext(Map<String,Object> context) { 
		ctx = context;
	}
}

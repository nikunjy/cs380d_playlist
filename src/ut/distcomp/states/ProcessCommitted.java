package ut.distcomp.states;

import java.util.Map;

public class ProcessCommitted {
	Map<String, Object> ctx;
	public String operate() {
		return "DONE";
	}
	public String getName() { 
		return "processCommitted";
	}
	public void setContext(Map<String,Object> context) { 
		ctx = context;
	}
}

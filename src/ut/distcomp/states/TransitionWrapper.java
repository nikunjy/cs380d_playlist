package ut.distcomp.states;

class TransitionWrapper {  
	public String decision;
	public String fromState;
	public String toState;
	public TransitionWrapper(String d,String f, String t) { 
		decision = d; 
		fromState = f; 
		toState = t;
	}
}
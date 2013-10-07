package ut.distcomp.states;

class TransitionWrapper {  
	public String decision;
	public String fromState;
	public String toState;
	public boolean isCoordinator;
	public TransitionWrapper setIsCoordinator(boolean isCoordinator) { 
		this.isCoordinator = isCoordinator;
		return this;
	}
	public TransitionWrapper(String d,String f, String t) { 
		decision = d; 
		fromState = f; 
		toState = t;
		isCoordinator = false;
	}
}
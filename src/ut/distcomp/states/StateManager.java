package ut.distcomp.states;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ut.distcomp.states.TransitionWrapper;
import ut.distcomp.framework.Config;

public class StateManager {
	
	private static List<State> allStates = new ArrayList<State>();
	private static Map<String,State> stateNameMap = new HashMap<String,State>();
	private static List<TransitionWrapper> coordinatorDFA = new ArrayList<TransitionWrapper>();
	private static List<TransitionWrapper> processDFA = new ArrayList<TransitionWrapper>();
	
	static {
		allStates.add(new CoordinatorFirstPhaseWait());
		allStates.add(new CoordinatorInitiate());
		allStates.add(new CoordinatorWaitForAck()); 
		allStates.add(new ProcessWaitForDecision());
		allStates.add(new ProcessWaitForInitiate());  
		allStates.add(new ProcessWaitForPreCommit());
		
		for(State state : allStates) {
			stateNameMap.put(state.getName(), state);
		}
		coordinatorDFA.add(new TransitionWrapper("ABORT","coordinatorInitiate","coordinatorInitiate"));
		coordinatorDFA.add(new TransitionWrapper("SUCCESS","coordinatorInitiate","coordinatorFirstPhaseWait"));
		coordinatorDFA.add(new TransitionWrapper("ABORT","coordinatorFirstPhaseWait","coordinatorInitiate"));
		coordinatorDFA.add(new TransitionWrapper("PRECOMMIT","coordinatorFirstPhaseWait","coordinatorWaitForAck"));
		coordinatorDFA.add(new TransitionWrapper("SUCCESS","coordinatorWaitForAck","coordinatorInitiate"));
		coordinatorDFA.add(new TransitionWrapper("ERROR","coordinatorWaitForAck","coordinatorInitiate"));
		
		
		processDFA.add(new TransitionWrapper("NOOP","processWaitForInitiate","processWaitForInitiate"));
		processDFA.add(new TransitionWrapper("VoteSent","processWaitForInitiate","processWaitForPreCommit"));
		processDFA.add(new TransitionWrapper("ERROR","processWaitForInitiate","processWaitForInitiate"));
		processDFA.add(new TransitionWrapper("SUCCESS","processWaitForPreCommit","processWaitForDecision"));
		processDFA.add(new TransitionWrapper("ABORT","processWaitForPreCommit","processWaitForInitiate"));
		processDFA.add(new TransitionWrapper("SUCCESS","processWaitForDecision","processWaitForInitiate"));
	}
	State makeTransition(List<TransitionWrapper> dfa,State currentState,String transition) {
		for(TransitionWrapper wrapper : dfa) {
			if(wrapper.fromState.equalsIgnoreCase(currentState.getName()) && wrapper.decision.equalsIgnoreCase(transition)) {
				return stateNameMap.get(wrapper.toState);
			}
		}
		return null;
	}
	public void initiateAsCoordinator(Map<String,Object> context) throws Exception{
		for(State state : allStates) { 
			state.setContext(context);
		}
		Config config = (Config)context.get("config");
		config.logger.info("Starting as Initiator");
		State currentState = stateNameMap.get("coordinatorInitiate");
		while(true) { 
			String transition = currentState.operate();
			State nextState = makeTransition(coordinatorDFA,currentState,transition);
			if(nextState == null) { 
				throw new Exception("Tried to make transition "+transition+" "+currentState.getName());
			}
			config.logger.info("Made a transition "+transition+" from "+currentState.getName()+" "+nextState.getName());
			currentState = nextState;
		}
	}
	public void startProcess(Map<String,Object> context) throws Exception { 
		for(State state : allStates) { 
			state.setContext(context);
		}
		Config config = (Config)context.get("config");
		config.logger.info("Starting process "+config.procNum);
		State currentState = stateNameMap.get("processWaitForInitiate");
		while(true) { 
			String transition = currentState.operate();
			State nextState = makeTransition(processDFA,currentState,transition);
			if(nextState == null) { 
				throw new Exception("Tried to make transition "+transition+" "+currentState.getName());
			}
			if (!transition.equals("NOOP"))
			config.logger.info("Made a transition "+transition+" from "+currentState.getName()+" "+nextState.getName());
			currentState = nextState;
		}
	}
	

}

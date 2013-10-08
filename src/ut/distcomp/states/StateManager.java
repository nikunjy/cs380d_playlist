package ut.distcomp.states;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ut.distcomp.application.PlayListProcess;
import ut.distcomp.framework.Config;

public class StateManager {

	private static List<State> allStates = new ArrayList<State>();
	private static Map<String,State> stateNameMap = new HashMap<String,State>();
	private static List<TransitionWrapper> coordinatorDFA = new ArrayList<TransitionWrapper>();
	private static List<TransitionWrapper> processDFA = new ArrayList<TransitionWrapper>();
	private static List<TransitionWrapper> recoveryDFA = new ArrayList<TransitionWrapper>();

	static {
		allStates.add(new AskOtherProcesses());
		allStates.add(new TotalFailureRecovery());
		allStates.add(new CoordinatorAskForStateReq());
		allStates.add(new CoordinatorFirstPhaseWait());
		allStates.add(new CoordinatorInitiate());
		allStates.add(new CoordinatorTerminal());
		allStates.add(new CoordinatorWaitForAck());
		allStates.add(new CoordinatorWaitForState());
		allStates.add(new ProcessAborted());
		allStates.add(new ProcessCommitted());
		allStates.add(new ProcessElect());
		allStates.add(new ProcessWaitForDecision());
		allStates.add(new ProcessWaitForInitiate());  
		allStates.add(new ProcessWaitForPreCommit());
		allStates.add(new ProcessWaitForStateReq());

		for(State state : allStates) {
			stateNameMap.put(state.getName(), state);
		}
		coordinatorDFA.add(new TransitionWrapper("ABORT","coordinatorInitiate","coordinatorTerminal"));
		coordinatorDFA.add(new TransitionWrapper("SUCCESS","coordinatorInitiate","coordinatorFirstPhaseWait"));
		coordinatorDFA.add(new TransitionWrapper("ABORT","coordinatorFirstPhaseWait","coordinatorTerminal"));
		coordinatorDFA.add(new TransitionWrapper("PRECOMMIT","coordinatorFirstPhaseWait","coordinatorWaitForAck"));
		coordinatorDFA.add(new TransitionWrapper("TERMINAL","coordinatorWaitForAck","coordinatorTerminal"));
		coordinatorDFA.add(new TransitionWrapper("COMMIT","coordinatorWaitForAck","processCommitted"));
		coordinatorDFA.add(new TransitionWrapper("ABORT","coordinatorWaitForAck","processAborted"));
		coordinatorDFA.add(new TransitionWrapper("SUCCESS","coordinatorTerminal","coordinatorInitiate"));
		
		processDFA.add(new TransitionWrapper("NOOP","processWaitForInitiate","processWaitForInitiate"));
		processDFA.add(new TransitionWrapper("VoteSent","processWaitForInitiate","processWaitForPreCommit"));
		processDFA.add(new TransitionWrapper("ERROR","processWaitForInitiate","processWaitForInitiate"));
		processDFA.add(new TransitionWrapper("SUCCESS","processWaitForPreCommit","processWaitForDecision"));
		processDFA.add(new TransitionWrapper("ABORT","processWaitForPreCommit","processAborted"));
		processDFA.add(new TransitionWrapper("REELECT","processWaitForPreCommit","processElect"));
		processDFA.add(new TransitionWrapper("REELECT","processWaitForStateReq","processElect"));
		processDFA.add(new TransitionWrapper("DONE","processAborted","processWaitForInitiate"));
		processDFA.add(new TransitionWrapper("BECOMECOORD","processAborted","coordinatorTerminal"));
		processDFA.add(new TransitionWrapper("COMMIT","processWaitForDecision","processCommitted"));
		processDFA.add(new TransitionWrapper("ABORT","processWaitForDecision","processAborted"));
		processDFA.add(new TransitionWrapper("DONE","processCommitted", "processWaitForInitiate"));
		processDFA.add(new TransitionWrapper("BECOMECOORD","processCommitted","coordinatorTerminal"));
		processDFA.add(new TransitionWrapper("REELECT","processWaitForDecision","processElect"));
		processDFA.add(new TransitionWrapper("PARTICIPANT","processElect","processWaitForStateReq"));
		processDFA.add(new TransitionWrapper("ELECTED","processElect","coordinatorAskForStateReq"));
		processDFA.add(new TransitionWrapper("DONE","coordinatorAskForStateReq","coordinatorWaitForState"));
		processDFA.add(new TransitionWrapper("ABORT","coordinatorWaitForState","processAborted"));
		processDFA.add(new TransitionWrapper("COMMIT","coordinatorWaitForState","processCommitted"));
		processDFA.add(new TransitionWrapper("PRECOMMIT", "coordinatorWaitForState","coordinatorWaitForAck"));

		recoveryDFA.add(new TransitionWrapper("DONE","processAborted","processWaitForInitiate"));
		recoveryDFA.add(new TransitionWrapper("DONE","processCommitted", "processWaitForInitiate"));
		recoveryDFA.add(new TransitionWrapper("COMMIT","askOtherProcesses", "processCommitted"));
		recoveryDFA.add(new TransitionWrapper("ABORT","askOtherProcesses", "processAborted"));
		recoveryDFA.add(new TransitionWrapper("NOOP","askOtherProcesses", "askOtherProcesses"));
		recoveryDFA.add(new TransitionWrapper("TOTALFAILURE","askOtherProcesses", "totalFailureRecovery"));
		recoveryDFA.add(new TransitionWrapper("TOTALFAILURE","totalFailureRecovery", "totalFailureRecovery"));
		recoveryDFA.add(new TransitionWrapper("ASKOTHERS","totalFailureRecovery", "askOtherProcesses"));
		//recoveryDFA.add(new TransitionWrapper("REELECT","totalFailureRecovery", "processElect"));
		recoveryDFA.add(new TransitionWrapper("COMMIT","totalFailureRecovery", "processCommitted"));
		recoveryDFA.add(new TransitionWrapper("ABORT","totalFailureRecovery", "processAborted"));


	}
	State makeTransition(List<TransitionWrapper> dfa,State currentState,String transition) {
		for(TransitionWrapper wrapper : dfa) {
			if(wrapper.fromState.equalsIgnoreCase(currentState.getName()) && wrapper.decision.equalsIgnoreCase(transition)) {
				return stateNameMap.get(wrapper.toState);
			}
		}
		if (dfa == recoveryDFA) { 
			for(TransitionWrapper wrapper : processDFA) {
				if(wrapper.fromState.equalsIgnoreCase(currentState.getName()) && wrapper.decision.equalsIgnoreCase(transition)) {
					return stateNameMap.get(wrapper.toState);
				}
			}
		}
		if (dfa != coordinatorDFA) 
			for(TransitionWrapper wrapper : coordinatorDFA) {
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
		PlayListProcess pprocess = (PlayListProcess)context.get("process");
		while(true) { 
			String transition = currentState.operate();
			State nextState = makeTransition(coordinatorDFA,currentState,transition);
			if(nextState == null) { 
				throw new Exception("Tried to make transition "+transition+" "+currentState.getName());
			}
			config.logger.info("Made a transition "+transition+" from "+currentState.getName()+" "+nextState.getName());
			currentState = nextState;
			Set<Integer> liveProcesses = pprocess.getLiveSet();
			pprocess.writeLiveSet(liveProcesses);
		}
	}

	public void startProcessWithRecovery(Map<String,Object> context) throws Exception {
		for(State state : allStates) { 
			state.setContext(context);
		}
		Config config = (Config)context.get("config");
		if(context.get("decidedState") != null) { 
			State currentState = stateNameMap.get((String)context.get("decidedState"));
			while (true) {
				String transition = currentState.operate();
				State nextState = makeTransition(recoveryDFA, currentState,
						transition);
				if (nextState == null) {
					throw new Exception("Tried to make transition "
							+ transition + " " + currentState.getName());
				}
				if (!transition.equals("NOOP"))
					config.logger.info("Made a transition " + transition
							+ " from " + currentState.getName() + " "
							+ nextState.getName());	
				currentState = nextState;
				if(currentState.getName().equalsIgnoreCase("processWaitForInitiate") || currentState.getName().equalsIgnoreCase("coordinatorInitiate")) {
					break;
				}
			}
		} else {
			throw new Exception("Recovery Failed");
		}
	}
	public void startProcess(Map<String, Object> context) throws Exception {
		for (State state : allStates) {
			state.setContext(context);
		}
		Config config = (Config) context.get("config");
		config.logger.info("Starting process " + config.procNum);
		State currentState = stateNameMap.get("processWaitForInitiate");
		PlayListProcess pprocess = (PlayListProcess)context.get("process");
		while (true) {
			String transition = currentState.operate();
			if (currentState.getName()
					.equalsIgnoreCase("processWaitForStateReq")) {

				if(transition.equalsIgnoreCase("REELECT"))
					currentState = stateNameMap.get("processElect");
				else {
					System.out.println(context.get("lastState"));
					currentState = stateNameMap.get(context.get("lastState"));
				}

				config.logger.info("Made a transition " + transition
						+ " from processWaitForStateReq to " + currentState.getName());
			}
			else {
				State nextState = makeTransition(processDFA, currentState,
						transition);
				if (nextState == null) {
					throw new Exception("Tried to make transition "
							+ transition + " " + currentState.getName());
				}
				if (!transition.equals("NOOP"))
					config.logger.info("Made a transition " + transition
							+ " from " + currentState.getName() + " "
							+ nextState.getName());
				
				currentState = nextState;
			}
			if(!transition.equalsIgnoreCase("NOOP")) {
				Set<Integer> liveProcesses = pprocess.getLiveSet();
				pprocess.writeLiveSet(liveProcesses);
			}
		}
	}


}

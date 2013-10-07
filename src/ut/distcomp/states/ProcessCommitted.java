package ut.distcomp.states;

import java.util.Map;

import ut.distcomp.framework.NetController;

public class ProcessCommitted implements State {
	Map<String, Object> ctx;
	public String operate() {
		NetController serverImpl = (NetController)ctx.get("serverImpl");
		serverImpl.purgeMessages();
		return "DONE";
	}
	public String getName() { 
		return "processCommitted";
	}
	public void setContext(Map<String,Object> context) { 
		ctx = context;
	}
}

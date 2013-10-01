package ut.distcomp.states;

import java.util.Map;

//TODO might have to change this to extends thread.
public interface State {
	public String operate(); 
	public String getName();
	public void setContext(Map<String,Object> ctx);
}

package ut.distcomp.states;

import java.util.Map;
public interface State {
	public String operate(); 
	public String getName();
	public void setContext(Map<String,Object> ctx);
}

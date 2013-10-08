package ut.distcomp.application;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import ut.distcomp.framework.Config;
import ut.distcomp.framework.NetController;
import ut.distcomp.states.StateManager;


public class PlayListProcess extends Thread{
	public enum LogCategories {
		DECISION("lastDecision"),PLAYLIST("playList"),LIVESET("liveSet"),OPERATION("operation"),LASTSTATE("lastState");
		public String category;
		public String value() { 
			return category;
		}
		LogCategories(String category) { 
			this.category = category;
		}
	}
	private NetController serverImpl;
	private String dtLog;
	private Writer writer;
	private Reader reader;
	private Config config;
	private volatile boolean isCoordinator;
	public Set<Integer> liveSet;
	public int coordinator;
	public boolean isRecovering;
	public void setIsCoordinator(boolean isCoordinator) { 
		this.isCoordinator = isCoordinator;
	}
	Properties props;
	private List<String> playList;
	public PlayListProcess(Config config) {
		liveSet = new TreeSet<Integer>();
		serverImpl = new NetController(config);
		playList = new ArrayList<String>();
		this.config = config;
		isRecovering = false;
		int processId = config.procNum;
		dtLog = "/tmp/dt_playlist/"+processId;
		props = new Properties();
		LogCategories[] categories = LogCategories.values();
		for(LogCategories category : categories) {
			props.setProperty(category.value(), "");
		}
		if (processId == 0) { 
			isCoordinator = true;
		} else { 
			isCoordinator = false;
		}
		File f = new File(dtLog);
		if(f.exists()) { 
			recovery();
		}else {
			try {
				writer = new PrintWriter(new BufferedWriter(new FileWriter(dtLog,false)));
				writer.flush(); 
				writer.close();
				OutputStream out = new FileOutputStream(f);
				props.store(out, "First Time");
				out.flush();
				out.close();
			} catch (IOException ex){
				config.logger.info("Exception"+ex);
			} finally {
				try {writer.close();} catch (Exception ex) {
					System.out.println(ex);
				}
			}	
		}
		try {
			reader = new BufferedReader(new FileReader(dtLog));
		} catch (Exception e) { 
			config.logger.info("Exception"+e);
		}

	}
	public Properties getProperties() {
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream(dtLog));
		} catch (Exception e) { 
			config.logger.info(e.toString());
		}
		return prop;
	}
	public void removeFromLiveSet(int p) { 
		for (int i=0;i<=p;i++)
			liveSet.remove(i); 
	}
	public void writeLiveSet(Set<Integer> liveSet) {
		this.liveSet = liveSet; 
		Properties props = getProperties();
		props.setProperty(PlayListProcess.LogCategories.LIVESET.value(), serializeLiveSet(liveSet));
		writeProperties(props);
	}
	public String serializeLiveSet(Set<Integer> liveSet) { 
		String ret = "";
		int count = 0;
		for(Integer liveProcs : liveSet) { 
			ret+=liveProcs;
			if (count != liveSet.size()-1) {
				ret +=" ";
			}
			count++;
		}
		return ret;
	}
	public Set<Integer> getUpSetFromLog(Properties props){
		String upsetLog = props.getProperty(PlayListProcess.LogCategories.LIVESET.value());
		String[] procs = upsetLog.split(" ");
		Set<Integer> liveSet = new HashSet<Integer>();
		for(String proc : procs) {
			if(proc.equals("") || proc == null)
				continue;
			liveSet.add(Integer.parseInt(proc));
		}
		return liveSet;
	}

	public List<String> getPlayListFromLog() { 
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream(dtLog));
			String[] playListArray = prop.getProperty(LogCategories.PLAYLIST.value()).split(",");
			playList = Arrays.asList(playListArray);
		} catch (Exception e) { 
			config.logger.info(e.toString());
		}
		return playList;
	}
	public void updateLiveSet() { 
		this.liveSet = getLiveSet();
	}
	public Set<Integer> getLiveSet() { 
		ApplicationMessage pingMessage = new ApplicationMessage(config.procNum); 
		pingMessage.operation = ApplicationMessage.MessageTypes.PING.value();
		Set<Integer> liveSet = new TreeSet<Integer>();
		for (int i = 0 ;i < config.addresses.length; i++) { 
			boolean success = serverImpl.sendMsg(i, pingMessage.toString());
			if (success)
				liveSet.add(i);
		}
		return liveSet;
	}
	private String serializePlayList() { 
		String serializedList = "";
		int i = 0;
		for(String list : playList) { 
			serializedList += list;
			if(i<playList.size()-1)
				serializedList +=",";
			i++;
		}
		return serializedList;
	}
	public void writeProperties(Properties props) {
		try {
			File f = new File(dtLog);
			OutputStream out = new FileOutputStream(f);
			Date updateTime = new Date();
			props.store(out, "Update at "+updateTime);
			out.flush();
			out.close();
		} catch (Exception e) { 
			//we don't care. DT log always works 
			config.logger.info(e.toString());
		}

	}
	public boolean broadCast(ApplicationMessage message) {
		boolean success = true;
		for(int i = 0; i<config.addresses.length;i++) {
			if(i != config.procNum)
				success &= serverImpl.sendMsg(i, message.toString());
		}
		if (success)
			config.logger.info("Sent broadcast"+message+"\n");
		return success;
	}

	public void recovery() {
		serverImpl.pingShutdown();
		
		isRecovering = true;
		Map<String,Object> context = new HashMap<String,Object>();
		context.put("config",config);
		context.put("serverImpl",serverImpl);
		context.put("process", this);
		context.put("lastState", null);
		Properties props = new Properties(); 
		try {
			props.load(new FileInputStream(dtLog));
			this.liveSet = getUpSetFromLog(props);
			String lastDecision = props.getProperty(PlayListProcess.LogCategories.DECISION.value());
			String lastState = props.getProperty(PlayListProcess.LogCategories.LASTSTATE.value());
			if(lastDecision.equalsIgnoreCase("Abort")) {
				serverImpl.openPing();
				if(config.procNum == 0) {
					context.put("lastDecision", "ABORT");
					context.put("decidedState", "coordinatorTerminal");
				} else {
					context.put("decidedState", "processAborted");
				}
			}else if(lastDecision.equalsIgnoreCase("Commit")) {
				serverImpl.openPing();
				if(config.procNum == 0) {
					context.put("lastDecision", "COMMIT");
					context.put("decidedState", "coordinatorTerminal");
				} else {
					context.put("decidedState", "processCommitted");
				}
			}else { 
				context.put("decidedState","askOtherProcesses");
				//last state can Pre Commit or yes
				if (lastState.equalsIgnoreCase("precommit")) { 
					lastState = "processWaitForDecision";
				}
				if(lastState.equalsIgnoreCase("yes")) {
					lastState = "processWaitForPreCommit";
				}
				context.put("lastState",lastState);
			}

			StateManager manager = new StateManager();  
			manager.startProcessWithRecovery(context);
		} catch(Exception e) { 
			e.printStackTrace();
			config.logger.info("Unexpected problem");
		}

	}
	public void run() {
		//Write what happens if the server comes up.
		Map<String,Object> context = new HashMap<String,Object>();
		context.put("config",config);
		context.put("serverImpl",serverImpl);
		context.put("process", this);
		context.put("lastState", null);
		if (isCoordinator) {
			StateManager manager = new StateManager(); 
			try {
				manager.initiateAsCoordinator(context);
			} catch(Exception e) { 
				e.printStackTrace();
				config.logger.info("Unexpected problem");
			}
		} else {
			this.coordinator = 0;
			StateManager manager = new StateManager(); 
			try {
				manager.startProcess(context);
			} catch(Exception e) {
				e.printStackTrace();
				config.logger.info("Unexpected problem"+e);
			}

		}

	}
}

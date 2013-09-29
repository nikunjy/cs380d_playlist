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
import java.util.List;
import java.util.Properties;

import ut.distcomp.framework.Config;
import ut.distcomp.framework.NetController;


public class PlayListProcess extends Thread{
	public enum LogCategories {
		DECISION("lastDecision"),PLAYLIST("playList"),LIVEADDRESS("liveAddress"),LIVEPORTS("livePorts"),OPERATION("operation");
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
	Properties props;
	private List<String> playList;
	public PlayListProcess(Config config) {
		this.config = config;
		int processId = config.procNum;
		dtLog = "f:/dt_playlist/"+processId;
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

		}else {
			try {
				writer = new PrintWriter(new BufferedWriter(new FileWriter(dtLog,false)));
				OutputStream out = new FileOutputStream(f);
				props.store(out, "First Time");
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
		serverImpl = new NetController(config);
		playList = new ArrayList<String>();
	}
	private Properties getProperties() {
		Properties prop = new Properties();
		try {
		prop.load(new FileInputStream(dtLog));
		} catch (Exception e) { 
			config.logger.info(e.toString());
		}
		return prop;
		
	}
	private List<String> getPlayListFromLog() { 
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
	private void writeProperties(Properties props) {
		try {
		File f = new File(dtLog);
		OutputStream out = new FileOutputStream(f);
		Date updateTime = new Date();
		props.store(out, "Update at "+updateTime);
		} catch (Exception e) { 
			//we don't care. DT log always works 
			config.logger.info(e.toString());
		}
		
	}
	
	public void recovery() { 

		//recovery code goes here. 
	}
	public void init() throws Exception{		

	}
	public void run() {
		//Write what happens if the server comes up.
		if (isCoordinator) {
			config.logger.info("I am coordinator");
			for(int i=0;i<config.addresses.length;i++) { 
				serverImpl.sendMsg(i,"Add List1 1");
				config.logger.info("Message sent to "+i);
			}
			while(true) {
			List<String> receivedVotes = serverImpl.getReceivedMsgs();
			if(receivedVotes.size()>0)
				config.logger.info(receivedVotes.toString());
			int totalVotes = 0;
			
			for(String vote : receivedVotes) { 
				try {
					ApplicationMessage msg = new ApplicationMessage(vote);
					if(msg.isVote()) { 
						totalVotes+=1;
						if(msg.getVote().equalsIgnoreCase("No")) {
							props.setProperty(LogCategories.DECISION.value(),"ABORT");
							writeProperties(props);
							config.logger.info("Received a No");
							for(int i=1;i<config.addresses.length;i++) { 
								serverImpl.sendMsg(i,ApplicationMessage.MessageTypes.ABORT.value());
							}
							
							break;
						}
					}
				}catch(Exception e) {
					e.printStackTrace();
				}	
			}
			}
			
		} else { 
			while(true) {
			List<String> messages = serverImpl.getReceivedMsgs();
			if(messages.size()>0)
			config.logger.info(messages.toString());
			/*
			 * Messages received are FIFO. Assumed here. If not we might have to implement sequence number as well.
			 * TODO make sure the messages received are fine. 
			 */
			for (String message : messages) { 
				try {
					ApplicationMessage msg = new ApplicationMessage(message);
					if(msg.isPlayListOperation()) {
						//Make the call of sending yes/no
						props.setProperty(LogCategories.DECISION.value(),"No");
						writeProperties(props);
						boolean result = serverImpl.sendMsg(0,"Vote No");
						if(result)
							config.logger.info("Message sent to coordinator");
					}

				} catch(Exception e) { 
					//TODO take care of this. Maybe just ignore ? 
				}

			}
			}
		}

	}
}

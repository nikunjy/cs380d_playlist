package ut.distcomp.application;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import ut.distcomp.framework.Config;

public class InstructionUtils {
	public static String getVoteInstruction() { 
		try {
			BufferedReader bi = new BufferedReader(new InputStreamReader(System.in));
			String s = bi.readLine();
			return s;
		} catch (Exception e) { 
			return "Yes";
		}
	}
	public static ApplicationMessage getPlayListOperation(Config config) {
		BufferedReader bi = new BufferedReader(new InputStreamReader(System.in));
		String line;
		try {
			while((line = bi.readLine()) !=null && !line.equalsIgnoreCase("GO")) { 
			}
		}catch(Exception e) { 

		}
		ApplicationMessage message = new ApplicationMessage(config.procNum); 
		int seed = (int)(Math.random() * 100);
		if(seed<=30) { 
			message.operation = ApplicationMessage.MessageTypes.ADD.value();
			message.receiver  = (int)(Math.random()*config.addresses.length);
			if(message.receiver == config.procNum) { 
				if(message.receiver == config.addresses.length-1) { 
					message.receiver -=1;
				} else {
					message.receiver +=1;
				}	
			}
			message.oldplayListName = "List"+(message.receiver);

		} else if(seed<=60) { 
			message.operation = ApplicationMessage.MessageTypes.EDIT.value();
			message.receiver  = (int)(Math.random()*config.addresses.length);
			if(message.receiver == config.procNum) { 
				if(message.receiver == config.addresses.length-1) { 
					message.receiver -=1;
				} else {
					message.receiver +=1;
				}	
			}
			message.oldplayListName = "List"+(message.receiver);
			message.newPlayListName = "List"+(message.receiver+1);
		}else {
			message.operation = ApplicationMessage.MessageTypes.DELETE.value();
			message.receiver  = (int)(Math.random()*config.addresses.length);
			if(message.receiver == config.procNum) { 
				if(message.receiver == config.addresses.length-1) { 
					message.receiver -=1;
				} else {
					message.receiver +=1;
				}	
			}
			message.oldplayListName = "List"+(message.receiver);
		}
		config.logger.info(message.toString());
		return message;
	}
	public static boolean sendAck() { 
		return true;
	}
}

package ut.distcomp.application;

import java.util.ArrayList;
import java.util.List;

import ut.distcomp.framework.Config;


public class ApplicationInitializer {
	public static void main(String[] args) { 
		Config config = null;
		
		try { 
		config = new Config(args[0]);
		PlayListProcess process = new PlayListProcess(config);
		process.run();
		}catch(Exception e) { 
			System.out.println(e);
		}
	}

}

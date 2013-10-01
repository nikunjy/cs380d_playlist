package ut.distcomp.application;

import java.util.Date;

public class WaitUtil {
	public static void waitUntilTimeout() {
		long timeOut = 1000 * 2;
		waitUntilTimeout(timeOut);
	}
	public static void waitUntilTimeout(long timeOut) {
		System.out.println("Process waiting for "+timeOut);
		long start = (new Date()).getTime();
		while(true) {
			long now = (new Date()).getTime();
			if(now - start >= timeOut)  {
				break;
			}
		}
	}
}

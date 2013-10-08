/**
 * This code may be modified and used for non-commercial 
 * purposes as long as attribution is maintained.
 * 
 * @author: Isaac Levy
 */

/**
* The sendMsg method has been modified by Navid Yaghmazadeh to fix a bug regarding to send a message to a reconnected socket.
*/

package ut.distcomp.framework;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;

import ut.distcomp.application.ApplicationMessage;

/**
 * Public interface for managing network connections.
 * You should only need to use this and the Config class.
 * @author ilevy
 *
 */
public class NetController {
	private final Config config;
	private final List<IncomingSock> inSockets;
	private final List<IncomingSock> pingInSockets;
	private final OutgoingSock[] outSockets;
	private final OutgoingSock[] pingOutSockets;
	private final ListenServer listener;
	private ListenServer pingListener;

	public NetController(Config config) {
		this.config = config;
		inSockets = Collections.synchronizedList(new ArrayList<IncomingSock>());
		pingInSockets = Collections.synchronizedList(new ArrayList<IncomingSock>());
		listener = new ListenServer(config, inSockets,false);
		pingListener = new ListenServer(config,pingInSockets,true);
		outSockets = new OutgoingSock[config.numProcesses];
		pingOutSockets = new OutgoingSock[config.numProcesses];
		listener.start();
		pingListener.start();
	}
	
	// Establish outgoing connection to a process
	private synchronized void initOutgoingConn(int proc,boolean isPing) throws IOException {
		OutgoingSock sock; 
		if (isPing) 
			sock = pingOutSockets[proc];
		else
			sock = outSockets[proc]; 
		if (sock != null)
			throw new IllegalStateException("proc " + proc + " not null");
		int port = config.ports[proc]; 
		if(isPing) {
			port += 1000;	
		}
		if (isPing) 
			pingOutSockets[proc] = new OutgoingSock(new Socket(config.addresses[proc], port));
		else 
			outSockets[proc] = new OutgoingSock(new Socket(config.addresses[proc], port));
		config.logger.info(String.format("Server %d: Socket to %d established", 
				port, proc));
	}
	
	public synchronized boolean sendPing(int process, String msg) {
		try {
			if (pingOutSockets[process] == null)
				initOutgoingConn(process,true);
			pingOutSockets[process].sendMsg(msg);
		} catch (IOException e) { 
			if (pingOutSockets[process] != null) {
				pingOutSockets[process].cleanShutdown();
				pingOutSockets[process] = null;
				try{
					initOutgoingConn(process,true);
					pingOutSockets[process].sendMsg(msg);	
				} catch(IOException e1){
					if (pingOutSockets[process] != null) {
						pingOutSockets[process].cleanShutdown();
						pingOutSockets[process] = null;
					}
                    return false;
				}
				return true;
			}
			return false;
		}
		return true;
	}
	/**
	 * Send a msg to another process.  This will establish a socket if one is not created yet.
	 * Will fail if recipient has not set up their own NetController (and its associated serverSocket)
	 * @param process int specified in the config file - 0 based
	 * @param msg Do not use the "&" character.  This is hardcoded as a message separator. 
	 *            Sends as ASCII.  Include the sending server ID in the message
	 * @return bool indicating success
	 */
	public synchronized boolean sendMsg(int process, String msg) {
		ApplicationMessage message = ApplicationMessage.getApplicationMsg(msg); 
		if (message.isPing()) { 
			return sendPing(process,msg);
		}
		try {
			if (outSockets[process] == null)
				initOutgoingConn(process,false);
			outSockets[process].sendMsg(msg);
		} catch (IOException e) { 
			if (outSockets[process] != null) {
				outSockets[process].cleanShutdown();
				outSockets[process] = null;
				try{
					initOutgoingConn(process,false);
                        		outSockets[process].sendMsg(msg);	
				} catch(IOException e1){
					if (outSockets[process] != null) {
						outSockets[process].cleanShutdown();
	                	outSockets[process] = null;
					}
					config.logger.info(String.format("Server %d: Msg to %d failed.",
                        config.procNum, process));
        		    config.logger.log(Level.FINE, String.format("Server %d: Socket to %d error",
                        config.procNum, process), e);
                    return false;
				}
				return true;
			}
			config.logger.info(String.format("Server %d: Msg to %d failed.", 
				config.procNum, process));
			config.logger.log(Level.FINE, String.format("Server %d: Socket to %d error", 
				config.procNum, process), e);
			return false;
		}
		return true;
	}
	
	/**
	 * Return a list of msgs received on established incoming sockets
	 * @return list of messages sorted by socket, in FIFO order. *not sorted by time received*
	 */
	public synchronized List<String> getReceivedMsgs() {
		List<String> objs = new ArrayList<String>();
		synchronized(inSockets) {
			ListIterator<IncomingSock> iter  = inSockets.listIterator();
			while (iter.hasNext()) {
				IncomingSock curSock = iter.next();
				try {
					objs.addAll(curSock.getMsgs());
				} catch (Exception e) {
					config.logger.log(Level.INFO, 
							"Server " + config.procNum + " received bad data on a socket", e);
					curSock.cleanShutdown();
					iter.remove();
				}
			}
		}
		
		return objs;
	}
	public synchronized void pingShutdown() {
		pingListener.cleanShutdown();
		if(pingInSockets != null) {
		    for (IncomingSock sock : pingInSockets)
			    if(sock != null)
                    sock.cleanShutdown();
        }
	}
	

	public synchronized void openPing() {
		pingListener = new ListenServer(config,pingInSockets,true);
		pingListener.start();
	}
	
	/**
	 * Shuts down threads and sockets.
	 */
	public synchronized void shutdown() {
		listener.cleanShutdown();
		pingListener.cleanShutdown();
        if(inSockets != null) {
		    for (IncomingSock sock : inSockets)
			    if(sock != null)
                    sock.cleanShutdown();
        }
		if(outSockets != null) {
            for (OutgoingSock sock : outSockets)
			    if(sock != null)
                    sock.cleanShutdown();
        }
		
	}

}

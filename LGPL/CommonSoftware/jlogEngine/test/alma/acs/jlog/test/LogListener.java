/*******************************************************************************
 * ALMA - Atacama Large Millimeter Array
 * Copyright (c) ESO - European Southern Observatory, 2011
 * (in the framework of the ALMA collaboration).
 * All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *******************************************************************************/
package alma.acs.jlog.test;

import com.cosylab.logging.engine.LogEngineException;
import com.cosylab.logging.engine.ACS.ACSRemoteErrorListener;
import com.cosylab.logging.engine.ACS.LCEngine;
import com.cosylab.logging.engine.ACS.ACSLogConnectionListener;
import com.cosylab.logging.engine.ACS.ACSRemoteLogListener;
import com.cosylab.logging.engine.ACS.ACSRemoteRawLogListener;

import com.cosylab.logging.engine.log.ILogEntry;
import com.cosylab.logging.engine.log.LogField;

/**
 * A class to test the receptions of the logs through the listeners.
 * The class instantiate a LCEngine and register itself as listeners.
 * The logs and messages received are printed in the stdout
 * 
 * @author acaproni
 *
 */
public class LogListener implements 
	ACSLogConnectionListener, 
	ACSRemoteLogListener, 
	ACSRemoteRawLogListener,
	ACSRemoteErrorListener {

	/* (non-Javadoc)
	 * @see com.cosylab.logging.engine.ACS.ACSRemoteErrorListener#errorReceived(java.lang.String)
	 */
	@Override
	public void errorReceived(String xml) {
		System.err.println("Error received: "+xml);
	}

	// The engine that connects to the logging client
	private LCEngine engine;
	
	/**
	 * The constructor
	 *
	 */
	public LogListener() throws LogEngineException {
		engine = new LCEngine();
		engine.addLogConnectionListener(this);
		engine.addLogListener(this);
		engine.addRawLogListener(this);
		engine.addLogErrorListener(this);
		engine.connect();
	}
	
	/**
	 * Main
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		LogListener ll = null;
		try {
			ll=new LogListener();
		}  catch (Throwable t) {
			System.err.println("Error instantiating the LogListenerStressTest: "+t.getMessage());
			t.printStackTrace();
			System.exit(-1);
		}
		try {
			Thread.sleep(90000);
		} catch (InterruptedException ie) {}
		ll.disconnect();
		System.exit(0);
	}
	
	public void acsLogConnEstablished() {
		System.out.println("Connection established");
	}
	
	/**
	 * @see com.cosylab.logging.engine.ACS.ACSLogConnectionListener
	 */
	public void acsLogConnDisconnected() {
		System.out.println("Connection disconnected");
	}
	
	/**
	 * @see com.cosylab.logging.engine.ACS.ACSLogConnectionListener
	 */
	public void acsLogConnLost() {
		System.out.println("Connection lost");
	}
	
	/**
	 * @see com.cosylab.logging.engine.ACS.ACSLogConnectionListener
	 */
	public void acsLogConnConnecting() {
		System.out.println("Connecting");
	}
	
	/**
	 * @see com.cosylab.logging.engine.ACS.ACSLogConnectionListener
	 */
	public void acsLogConnSuspended() {
		System.out.println("Connection suspended");
	}
	
	/**
	 * @see com.cosylab.logging.engine.ACS.ACSLogConnectionListener
	 */
	public void acsLogsDelay() {
		System.out.println("Delay detected");
	}
	
	/**
	 * @see com.cosylab.logging.engine.ACS.ACSLogConnectionListener
	 */
	public void reportStatus(String status) {
		System.out.println("Status msg received: "+status);
	}
	
	/**
	 * Print only the messages received from the client
	 * 
	 * @see com.cosylab.logging.engine.ACS.ACSRemoteLogListener
	 */
	public void xmlEntryReceived(String str) {
		if (str.indexOf("logClient.cpp")>=0) {
			System.out.println("RAW log: "+str);
		}
	}
	
	/**
	 * Print only the messages received from the client
	 * 
	 * @see com.cosylab.logging.engine.ACS.ACSRemoteRawLogListener
	 */
	public void logEntryReceived(ILogEntry log) {
		if (log==null) {
			throw new IllegalArgumentException("Invalid null log");
		}
		Object fileObj = log.getField(LogField.FILE);
		if (fileObj==null) {
			// The file field is not defined
			// For sure it is not generated by logClient so I can skip this message
			return;
		}
		String fileField=fileObj.toString();
		if (fileField==null) {
			System.out.println("Invalid conversion of field to String");
			System.out.println("Class of the object: "+fileObj.getClass().getName());
			System.out.println("XML = ["+log.toXMLString()+"]");
			return;
		}
		if (fileField.compareTo("logClient.cpp")==0) {
			System.out.println("Log received: "+log.toString());
		}
	}
	
	/**
	 * Disconnect from the logging channel
	 *
	 */
	public void disconnect() {
		engine.disconnect();
	}
}

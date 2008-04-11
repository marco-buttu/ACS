/*
 *    ALMA - Atacama Large Millimiter Array
 *    (c) European Southern Observatory, 2004
 *    Copyright by ESO (in the framework of the ALMA collaboration),
 *    All rights reserved
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *    MA 02111-1307  USA
 */
package alma.acs.container;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Thread factory that remembers all threads it creates.
 * Method <code>cleanUp</code> allows killing all threads.  
 * <p>
 * A <code>ThreadGroup</code> is used to deal with all threads at a time. Its name is that given in the constructor.
 * Even though Josh Bloch tells us in "Effective Java", Item 53, that thread groups are almost useless,
 * we like the UncaughtExceptionHandler which JDK 1.4 only offers through ThreadGroups.
 * TODO: revisit this in JDK 1.5 where Thread itself has this handler.
 * 
 * @author hsommer
 * created Mar 8, 2005 1:27:38 PM
 */
public class CleaningDaemonThreadFactory implements ThreadFactory
{
	// we keep track of our threads outside of the thread group
	private final List<Thread> threadList = new ArrayList<Thread>();

	private final AtomicInteger threadNumber = new AtomicInteger(1);

	private final Logger logger;
	private final String name;
	private LoggingThreadGroup group;

	private ClassLoader newThreadContextCL;

	
	/**
	 * Constructor.
	 * @param name  the name of the {@link ThreadGroup} to which all threads created by this factory will belong.
	 * @param logger  the logger to be used by this class
	 */
	public CleaningDaemonThreadFactory(String name, Logger logger) {
		this.logger = logger;
		this.name = name;
	}

	/**
	 * Creates a new daemon thread that is part of the same factory thread group 
	 * as all other threads created by this method.
	 * The thread's name will be that of the group, with an integer value appended.
	 * 
	 * @see java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
	 */
	public Thread newThread(Runnable command) {
		ensureGroupCreated();
		
		String threadName = group.getName() + "-" + threadNumber.getAndIncrement();
		Thread t = new Thread(group, command, threadName);
		t.setDaemon(true);
		if (newThreadContextCL != null) {
			t.setContextClassLoader(newThreadContextCL);
		}
		synchronized (threadList) {
			threadList.add(t);
		}
		return t;
	}

	private synchronized void ensureGroupCreated() {
		if (group == null) {
			group = new LoggingThreadGroup(name, logger);
			group.setMaxPriority(Thread.currentThread().getPriority());
		}
	}
	
	void setNewThreadContextClassLoader(ClassLoader cl) {
		if (cl != null) {
			newThreadContextCL = cl;
		}
	}

	/**
	 * Gets a copy of the list of all threads created by this factory up to this call.
	 * This method should only be used for testing, but not in operational code.
	 */
	public List<Thread> _getAllThreadsCreated() {
		synchronized (threadList) {
			return new ArrayList<Thread>(threadList);
		}
	}

	/**
	 * Kills running threads via {@link Thread#stop()}.
	 * Should be called by the container or similar classes when all threads 
	 * created by this factory supposedly have terminated anyway thanks to smart applications. 
	 * The safety concerns which led to the deprecation of the stop method thus don't seem to apply here.
	 */
	public synchronized void cleanUp() {
		if (group == null) {
			return;
		}
		group.setShuttingDown();
		synchronized (threadList) {
			for (Thread t : threadList) {
				try {
					if (t.isAlive()) {
						logger.warning("forcefully terminating surviving thread " + t.getName());
					}
					// @TODO HSO 2008-04: now that jbaci uses an external ThreadFactory, which is typically supplied by container services,
					//       we got jbaci test failures as long as stop() is called (see COMP-2362).
					//       We must check if we fix jbaci and go back to thread.stop, or stay with thread.interrupt.
					t.interrupt();
//					t.stop();
				} catch (RuntimeException e) {
					logger.finer("exception while stopping thread '" + t.getName() + "': " + e.toString());
				}
			}
			threadList.clear();
		}
		try {
			// if there are threads in the group which have never started, destroy() will fail
			if (!group.isDestroyed() && group.activeCount() == 0) {
				group.destroy();
			}
		} catch (Exception e) {
			logger.finer("unexpectedly failed to destroy thread group " + group.getName() + e.toString());
		}
		// in case this factory gets used again, we should build a new thread group
		group = null;
	}

	private static class LoggingThreadGroup extends ThreadGroup
	{
		private final Logger logger;
		private boolean shuttingDown = false;

		LoggingThreadGroup(String name, Logger logger) {
			super(name);
			this.logger = logger;
		}

		void setShuttingDown() {
			shuttingDown = true;
		}

		/**
		 * Called by the JVM if any of the threads in this thread group terminates with an error.
		 * Logs a warning to the logger provided in the ctor.
		 * <p>
		 * The error <code>ThreadDeath</code> is even logged during expected thread lifetime,
		 * because user threads are not supposed to be terminated through the deprecated <code>stop()</code> method
		 * during their normal operation. <br>
		 * During shutdown, exceptions are not logged, because the killing of surviving user threads
		 * is logged already in the <code>cleanUp</code> method. 
		 * <p>
		 * @TODO: since JDK 1.5 an {@link UncaughtExceptionHandler} can be attached to each thread,
		 * which may be an alternative to using a thread group for this purpose.
		 * 
		 * @see java.lang.ThreadGroup#uncaughtException(java.lang.Thread, java.lang.Throwable)
		 */
		public void uncaughtException(Thread t, Throwable e) {
			if (!shuttingDown) {
				logger.log(Level.WARNING, "User thread '" + t.getName() + "' terminated with error ", e);
			}
			// ThreadDeath must move on to really let the thread die
			if (e instanceof ThreadDeath) {
				super.uncaughtException(t, e);
			}
		}

	}
}

package edu.illinois.mitra.starl.harness;

import java.util.ArrayList;

/**
 * The core of the simulation. As simulated Guster would say, "the pseudo-king of it all, the virtual belle of the not-quite-real ball" (Yes, I'm making a joke about my other joke in GlobalVarHolder)
 * You really don't need to mess with this code. Please don't change anything in here.
 * @author Adam Zimmerman
 * @version One million
 */
public class SimulationEngine extends Thread {
	
	private static final int THREAD_DEADLOCK_TIMEOUT = 5000;
	
	// Matching lists of threads being tracked and their delays
	private ArrayList<Long> lastUpdateTime = new ArrayList<Long>(); 
	private ArrayList<Long> sleeps = new ArrayList<Long>(); 
	private ArrayList<Thread> regThreads = new ArrayList<Thread>();

	public SimGpsProvider gps;
	public DecoupledSimComChannel comms;
	public long startTime;
	public long time;
	private Object lock = new Object();
	private boolean done = false;
	private double ticRate = 0;
	
	private double lastTicAdvance = -1;
	private long lastTimeAdvance = -1;

	public SimulationEngine(int meanDelay, int delayStdDev, int dropRate, int seed, double ticRate) {
		super("SimulationEngine");
		comms = new DecoupledSimComChannel(meanDelay, delayStdDev, dropRate, seed);
		time = System.currentTimeMillis();
		startTime = time;
		done = false;
		this.ticRate = ticRate;
		this.start();
	}
	
	public void threadSleep(long time, Thread thread) {
		synchronized(lock) {
			int idx = regThreads.indexOf(thread);
			if(idx == -1) {
				throw new RuntimeException("Unregistered thread " + thread + " attempted to sleep for " + time);
			}
			sleeps.set(idx, time);
			lastUpdateTime.set(idx, System.currentTimeMillis());
			if(!this.isInterrupted()) {
				this.interrupt();
			}
		}
	}
		
	@Override
	public void run() {
		//TODO: please comment this
		while(!done) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
			}

			synchronized(lock) {
				if(sleeps.size() != regThreads.size()) throw new RuntimeException("DISJOINT!");
				if(clearToAdvance()) advance();
			}
		}
	}
	
	private void maintainRealTime() {
		if(ticRate <= 0) return;
		if(lastTicAdvance <= 0) return;

		// Determine the rate of advance in tics/millisecond
		double rate = lastTicAdvance/(System.currentTimeMillis()-lastTimeAdvance);
		
		// While the rate is too large, sleep the thread
		while(rate > ticRate) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
			}
			rate = lastTicAdvance/(System.currentTimeMillis()-lastTimeAdvance);
		}
	}

	private void deadlockCheck(int i) {
		long now = System.currentTimeMillis();
		if((sleeps.get(i) == null) && (now - lastUpdateTime.get(i)) > THREAD_DEADLOCK_TIMEOUT) {

			System.out.println("\n\nPossible deadlock encountered at " + now);
			Thread t = regThreads.get(i);
			
			StackTraceElement[] st = t.getStackTrace();
			System.out.println(t.getId() + " - " + t.getName() + " - " + sleeps.get(i));
			for(StackTraceElement ste : st) {
				System.out.println(ste.toString());
			}
		}
	}
	
	private void advance() {
		// Determine if a pause is needed to maintain ties to real-time
		maintainRealTime();
		
		long advance = comms.minDelay();
		
		for(Long l : sleeps) {
			advance = Math.min(l, advance);
		}
		
		// Advance time
		time += advance;			
		comms.advanceTime(advance);
		
		lastTicAdvance = advance;
		lastTimeAdvance = System.currentTimeMillis();

		// Detect threads to be woken
		for(int i = 0; i < regThreads.size(); i ++) {
			sleeps.set(i, (sleeps.get(i) - advance));
			
			if(sleeps.get(i) == 0) {				
				sleeps.set(i, null);
				regThreads.get(i).interrupt();
			}
		}
	}
	
	public void registerThread(Thread thread) {
		synchronized(lock) {
			lastUpdateTime.add(System.currentTimeMillis());
			sleeps.add(null);
			regThreads.add(thread);
		}
	}

	public void removeThread(Thread thread) {
		synchronized(lock) {
			int idx = regThreads.indexOf(thread);
			if(idx == -1) throw new RuntimeException("Thread " + thread + " tried to unregister itself without being registered! What a jerk.");
			regThreads.remove(idx);
			sleeps.remove(idx);
			lastUpdateTime.remove(idx);
		}
		if(!this.isInterrupted()) this.interrupt();
	}
	
	public void simulationDone() {
		done = true;
	}
	
	private boolean clearToAdvance() {
		for(int i = 0; i < sleeps.size(); i++) {
			if(sleeps.get(i) == null) {
				deadlockCheck(i);
				return false;
			}
		}	
		return true;
	}
	
	public Long getTime() {
		return time;
	}
}
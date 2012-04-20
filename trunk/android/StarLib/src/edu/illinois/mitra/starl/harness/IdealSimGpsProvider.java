package edu.illinois.mitra.starl.harness;

import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import edu.illinois.mitra.starl.objects.ItemPosition;
import edu.illinois.mitra.starl.objects.PositionList;

public class IdealSimGpsProvider extends Observable implements SimGpsProvider  {
	private static final int VELOCITY = 200;	// Millimeters per second
	
	private HashMap<String, SimGpsReceiver> receivers;
	private HashMap<String, TrackedRobot> robots;

	// Waypoint positions and robot positions that are shared among all robots
	private PositionList robot_positions;
	private PositionList waypoint_positions;
	
	private long period = 100;
	private int angleNoise = 0;
	private int posNoise = 0;

	private Random rand;
	
	private ScheduledThreadPoolExecutor exec;
		
	public IdealSimGpsProvider(long period, double angleNoise, double posNoise) {
		this.period = period;
		this.angleNoise = (int) angleNoise;
		this.posNoise = (int) posNoise;
		this.rand = new Random();
		
		receivers = new HashMap<String, SimGpsReceiver>();
		robots = new HashMap<String, TrackedRobot>();
		exec = new ScheduledThreadPoolExecutor(1);
		
		robot_positions = new PositionList();
		waypoint_positions = new PositionList();
	}
	
	@Override
	public synchronized void registerReceiver(String name, SimGpsReceiver simGpsReceiver) {
		receivers.put(name, simGpsReceiver);
	}
	
	@Override
	public synchronized void addRobot(ItemPosition bot) {
		robots.put(bot.name, new TrackedRobot(bot));
		robot_positions.update(bot);
	}

	@Override
	public synchronized void setDestination(String name, ItemPosition dest) {
		robots.get(name).setDest(dest);
	}

	@Override
	public synchronized void halt(String name) {
		robots.get(name).setDest(null);
	}
	
	@Override
	public PositionList getRobotPositions() {
		return robot_positions;
	}

	@Override
	public void setWaypoints(PositionList loadedWaypoints) {
		if(loadedWaypoints != null) waypoint_positions = loadedWaypoints;
	}

	@Override
	public PositionList getWaypointPositions() {
		return waypoint_positions;
	}

	@Override
	public void start() {
		// Create a periodic runnable which repeats every "period" ms to report positions
		exec.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				for(TrackedRobot r : robots.values()) {
					if(r.hasChanged()) {
						r.updatePos();
						receivers.get(r.getName()).receivePosition(r.inMotion());	
					}
				}	
				setChanged();
				notifyObservers(robot_positions);
			}
		}, period, period, TimeUnit.MILLISECONDS);
	}

	private class TrackedRobot {
		private ItemPosition start = null;
		private ItemPosition pos = null;
		private ItemPosition dest = null;
		private boolean newdest = false;
		private boolean reportpos = false;
		private long timeLastUpdate = 0;		
		private long totalMotionTime = 0;
		private long totalTimeInMotion = 0;
		private double motAngle = 0;
		private double vX = 0;
		private double vY = 0;
		private int aNoise = 0;
		private int xNoise = 0;
		private int yNoise = 0;
		
		public TrackedRobot(ItemPosition pos) {
			this.pos = pos;
			timeLastUpdate = System.currentTimeMillis();
		}
		public void updatePos() {
			long timeSinceUpdate = (System.currentTimeMillis() - timeLastUpdate);
			if(newdest) {
				// Snap to heading
				// Calculate angle and X/Y velocities
				int deltaX = dest.x-start.x;
				int deltaY = dest.y-start.y;
				motAngle = Math.atan2(deltaY, deltaX);
				vX = (Math.cos(motAngle) * VELOCITY);
				vY = (Math.sin(motAngle) * VELOCITY);
				
				// Set position to ideal angle +/- noise
				int angle = (int)Math.toDegrees(Math.atan2(deltaY, deltaX));
				if(angleNoise != 0) aNoise = rand.nextInt(angleNoise*2)-angleNoise;
				pos.setPos(start.x, start.y, angle+aNoise);
				newdest = false;
			} else if(dest != null) {
				// Calculate noise
				if(angleNoise != 0) aNoise = rand.nextInt(angleNoise*2)-angleNoise;
				if(posNoise != 0) {
					xNoise = rand.nextInt(posNoise*2) - posNoise;
					yNoise = rand.nextInt(posNoise*2) - posNoise;
				}
				// Determine how far we've traveled since the motion started
				// If we've been traveling for longer than it should take to reach the
				// destination, set position to destination and assume we're now at rest. 
				totalTimeInMotion += timeSinceUpdate;
				if(totalTimeInMotion < totalMotionTime) {
					int dX = (int)(vX * totalTimeInMotion)/1000;
					int dY = (int)(vY * totalTimeInMotion)/1000;
					pos.setPos(start.x+dX+xNoise, start.y+dY+yNoise, (int)Math.toDegrees(motAngle));
				} else {
					pos.setPos(dest.x+xNoise, dest.y+yNoise, pos.angle+aNoise);
					dest = null;
					reportpos = true;
				}
			} else {
				reportpos = false;
			}
			timeLastUpdate = System.currentTimeMillis();
		}
		public void setDest(ItemPosition dest) {
			if(hasChanged()) updatePos();
			this.dest = dest;
			this.start = new ItemPosition(pos);
			totalMotionTime = (int)(this.start.distanceTo(dest)*1000.0)/VELOCITY;
			totalTimeInMotion = 0;
			newdest = (dest != null);
		}
		public boolean hasChanged() {
			if(reportpos || inMotion()) {
				reportpos = false;
				return true;
			}
			return false;
		}
		public boolean inMotion() {
			return dest != null;
		}
		public String getName() {
			return pos.name;
		}
	}

	@Override
	public void setVelocity(String name, int fwd, int radial) {
		throw new RuntimeException("IdealSimGpsProvider does not use the setVelocity method, but the setDestination method. " +
				"Ideal motion does not use the motion automaton something went very wrong here.");
	}

	@Override
	public void addObserver(Observer o) {
		super.addObserver(o);
	}
}
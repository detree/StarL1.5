package edu.illinois.mitra.starlSim.main;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

import edu.illinois.mitra.starl.gvh.GlobalVarHolder;
import edu.illinois.mitra.starl.gvh.SimGlobalVarHolder;
import edu.illinois.mitra.starl.harness.SimGpsProvider;
import edu.illinois.mitra.starl.interfaces.LogicThread;
import edu.illinois.mitra.starl.interfaces.SimComChannel;
import edu.illinois.mitra.starl.objects.ItemPosition;

public class SimApp implements Callable<List<Object>> {
	protected String name;
	protected GlobalVarHolder gvh;
	
	protected LogicThread logic;
	
	public SimApp(String name, HashMap<String,String> participants, SimComChannel channel, SimGpsProvider gps, ItemPosition initpos, String traceDir, Class<?> app) {	
		this.name = name;
		gvh = new SimGlobalVarHolder(name, participants, channel, gps, initpos, traceDir);
		gvh.comms.startComms();
		gvh.gps.startGps();
		
		// Create the class to be simulated
		try {
			// Generically instantiate an instance of the requested LogicThread
			logic = (LogicThread) app.getConstructor(GlobalVarHolder.class).newInstance(gvh);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		if(logic == null) throw new RuntimeException("Failed to create LogicThread in SimApp class.");
	}

	public String getLog() {
		return gvh.log.getLog();
	}

	@Override
	public List<Object> call() throws Exception {
		return logic.call();
	}
}
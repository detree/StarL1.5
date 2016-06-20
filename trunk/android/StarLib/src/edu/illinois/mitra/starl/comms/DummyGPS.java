package edu.illinois.mitra.starl.comms;

import java.util.Vector;

import edu.illinois.mitra.starl.gvh.GlobalVarHolder;
import edu.illinois.mitra.starl.interfaces.GpsReceiver;
import edu.illinois.mitra.starl.objects.ItemPosition;
import edu.illinois.mitra.starl.objects.ObstacleList;
import edu.illinois.mitra.starl.objects.PositionList;

/**
 * Created by SC.
 * @Description: This class is to test some of the motion functions in the motoautomation. Therefore, all of the
 * positions in this class are static and preset manually.
 * Everything keep simple here
 */
public class DummyGPS extends Thread implements GpsReceiver {
    private static final String TAG = "DUMMY GPS";
    private static final String ERR = "Critical Error";

    private GlobalVarHolder gvh;
    private String name = null;
    public PositionList robotPos;
    public PositionList<ItemPosition> waypoints;
    public ObstacleList obs;
    public Vector<ObstacleList> view;

    private boolean running = false;

    private final int[] mypos = {0,0,0};//the pos of the robot
    private final int[][] goalpos = {{1,0,0},{-1,0,0},{0,1,0},{0,-1,0}};

    public DummyGPS(GlobalVarHolder gvh){
        super();
        this.gvh = gvh;
        this.name = gvh.id.getName();

        this.robotPos = new PositionList();
        this.waypoints = new PositionList<ItemPosition>();
        this.obs = new ObstacleList();
        view = null;//fixme: to see if there are errors

        WaypointHelper();

        gvh.trace.traceEvent(TAG, "Created", gvh.time());
    }

    @Override
    public PositionList<ItemPosition> get_robots(){
        return robotPos;
    }

    @Override
    public PositionList<ItemPosition> getWaypoints(){
        return waypoints;
    }

    private void WaypointHelper(){
        for(int i=0; i<goalpos.length; i++){
            ItemPosition temp = new ItemPosition("Goal"+Integer.toString(i),
                    goalpos[i][0], goalpos[i][1], goalpos[i][2]);
            waypoints.update(temp);
        }
    }

    @Override
    public PositionList<ItemPosition> getSensepoints(){
        return null;
    }

    @Override
    public ObstacleList getObspoints(){
        return obs;
    }

    @Override
    public Vector<ObstacleList> getViews(){
        return view;
    }

    @Override
    public void start(){
        gvh.log.i("DUMMY GPS", "Starting GPS receiver");
        running = true;
        super.start();
        robotPos.update((ItemPosition) gvh.plat.model);
    }

    @Override
    public void cancel() {
        running = false;
        gvh.trace.traceEvent(TAG, "cancelled", gvh.time());
    }
}

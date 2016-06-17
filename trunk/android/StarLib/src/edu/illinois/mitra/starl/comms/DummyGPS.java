package edu.illinois.mitra.starl.comms;

import java.util.Vector;

import edu.illinois.mitra.starl.gvh.GlobalVarHolder;
import edu.illinois.mitra.starl.interfaces.GpsReceiver;
import edu.illinois.mitra.starl.objects.ItemPosition;
import edu.illinois.mitra.starl.objects.ObstacleList;
import edu.illinois.mitra.starl.objects.PositionList;

/**
 * Created by SC on 6/17/16.
 */
public class DummyGPS extends Thread implements GpsReceiver {
    private static final String TAG = "DUMMY GPS";
    private static final String ERR = "Critical Error";

    private GlobalVarHolder gvh;
    private String name = null;
    public PositionList robotPos;
    public PositionList<ItemPosition> waypoint;
    public ObstacleList obs;
    public Vector<ObstacleList> view;

    private boolean running = false;

    private final int[][] goalpos = {{1,0,0},{-1,0,0},{0,1,0},{0,-1,0}};

    public DummyGPS(GlobalVarHolder gvh){
        super();
        this.gvh = gvh;
        this.name = gvh.id.getName();

        this.robotPos = new PositionList();
        this.waypoint = new PositionList<ItemPosition>();
        this.obs = new ObstacleList();
        view = null;//fixme: to see if there are errors

        WaypointHelper();

        gvh.trace.traceEvent(TAG, "Created", gvh.time());
    }

    @Override
    public PositionList<ItemPosition> get_robots(){
        return null;
    }

    @Override
    public PositionList<ItemPosition> getWaypoints(){
        return null;
    }

    private void WaypointHelper(){
        for(int i=0; i<goalpos.length; i++){
            ItemPosition temp = new ItemPosition("Goal"+Integer.toString(i),
                    goalpos[i][0], goalpos[i][1], goalpos[i][2]);
            waypoint.update(temp);
        }
    }

    @Override
    public PositionList<ItemPosition> getSensepoints(){
        return null;
    }

    @Override
    public ObstacleList getObspoints(){
        return null;
    }

    @Override
    public Vector<ObstacleList> getViews(){
        return null;
    }

    @Override
    public void start(){
        gvh.log.i("DUMMY GPS", "Starting GPS receiver");
        running = true;
        super.start();
    }

    @Override
    public void cancel() {
        running = false;

    }
}

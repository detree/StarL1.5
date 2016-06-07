package edu.illinois.mitra.starl.motion;

import de.yadrone.base.command.CommandManager;
import edu.illinois.mitra.starl.gvh.GlobalVarHolder;
import edu.illinois.mitra.starl.models.ModelARDrone2;
import edu.illinois.mitra.starl.objects.ItemPosition;
import edu.illinois.mitra.starl.objects.ObstacleList;

/**
 * Created by SC on 6/7/16.
 */
public class MotionAutomation_ARDrone2 extends RobotMotion {
    protected static final String TAG = "MotionAutomaton";
    protected static final String ERR = "Critical Error";
    final int safeHeight = 10;//TODO need to see the unit

    protected GlobalVarHolder gvh;
    protected CommandManager cmd;//need? TODO, see ModelARDrone2.java

    //Motion Tracking
    protected ItemPosition destination = null;
    private ModelARDrone2 mypos = null;

    protected enum STAGE{
        INIT, MOVE, HOVER, TAKEOFF, LAND, GOAL, STOP
    }
    private STAGE prev=null, next = null;
    protected STAGE stage = STAGE.INIT;
    protected boolean running = false;
    boolean colliding = false;

    //TODO need to pass some more parameteres into this param
    private volatile MotionParameters param = MotionParameters.defaultParameters();

    public MotionAutomation_ARDrone2(GlobalVarHolder gvhin){
        super(gvhin.id.getName());
        gvh = gvhin;
        cmd = ((ModelARDrone2)gvh.plat.getModel()).cmd;
    }

    public void goTo(ItemPosition dest, ObstacleList obsList) {
        goTo(dest);
    }

    public void goTo(ItemPosition dest) {
        if((inMotion && !this.destination.equals(dest)) || !inMotion) {
            done = false;
            this.destination = new ItemPosition(dest.name,dest.x,dest.y,dest.z);
            startMotion();
        }
    }

    @Override
    public synchronized void start() {
        super.start();
        gvh.log.d(TAG, "STARTED!");
    }



}

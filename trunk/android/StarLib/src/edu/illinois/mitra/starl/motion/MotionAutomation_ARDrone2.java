package edu.illinois.mitra.starl.motion;

import android.util.Log;

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
    protected ItemPosition dest = null;
    private ModelARDrone2 mypos = null;

    protected enum STAGE{
        INIT, MOVE, HOVER, TAKEOFF, LAND, GOAL, STOP
    }
    private STAGE prev=null, next = null;
    protected STAGE stage = STAGE.INIT;
    protected boolean running = false;
    boolean colliding = false;

    //TODO need to pass some more parameters into this param
    private volatile MotionParameters param = MotionParameters.defaultParameters();

    public MotionAutomation_ARDrone2(GlobalVarHolder gvhin){
        super(gvhin.id.getName());
        gvh = gvhin;
    }

    public void setParameters(MotionParameters param){
        this.param = param;
    }

    public void turnTo(ItemPosition dest){
        goTo(dest);
    }

    public void goTo(ItemPosition dest, ObstacleList obsList) {
        goTo(dest);
    }

    public void goTo(ItemPosition dest) {
        Log.i("Automation ARDrone2", "GoTo called!!!");
        if((inMotion && !this.dest.equals(dest)) || !inMotion) {
            done = false;
            this.dest = new ItemPosition(dest.name,dest.x,dest.y,dest.z);
            motionStart();
        }
    }

    //====================================================
    //fixme: this three func seems like need some commands toward the HW, but not sure yet.
    private void motionStart(){
        running = true;
        stage = STAGE.INIT;
        inMotion = true;
    }

    @Override
    public void motion_stop() {
        //land();
        //stage = STAGE.LAND;
        this.dest = null;
        running = false;
        inMotion = false;
    }

    @Override
    public void motion_resume() {
        running = true;
    }
    //====================================================

    public void cancel(){
        running = false;
        //TODO maybe? some command to disconnect the hardware.
    }

    @Override
    public void run(){
        super.run();
        gvh.threadCreated(this);
        while(true){
            if(!running) continue;
            Log.i("Automation ARDrone2", "Thread running, with state "+ StageToString(stage));
            int distance = 0;
            if(stage!=STAGE.INIT && stage != STAGE.TAKEOFF)
                distance = (int) Math.sqrt(Math.pow((mypos.x - dest.x),2) + Math.pow((mypos.y - dest.y), 2));
            if(colliding) continue;
            switch (stage){
                case INIT:
                    if(distance <= param.GOAL_RADIUS){
                        next = STAGE.GOAL;
                    }
                    cmd.takeOff();
                    next = STAGE.TAKEOFF;
                    break;
                case TAKEOFF:
                    try {
                        sleep(300, 0);
                    } catch (Exception exc){
                        exc.printStackTrace();
                    }
                    next = STAGE.MOVE;
                    break;
                case MOVE:
                    inMotion = true;
                    if(mypos.z < safeHeight)
                        cmd.move(0, 0, Math.abs(safeHeight-mypos.z), 0);
                    if(distance <= param.GOAL_RADIUS) {
                        next = STAGE.GOAL;
                    }
                    else {
                        cmd.move(dest.x - mypos.x, dest.y - mypos.y, 0, 0);
                        next = STAGE.MOVE;
                    }
                    break;
                case HOVER:
                    cmd.hover();
                    next = STAGE.HOVER;
                    inMotion = false;
                    break;
                case GOAL:
                    done = true;
                    gvh.log.i(TAG, "At goal!");
                    gvh.log.i("DoneFlag", "write");
                    if(param.STOP_AT_DESTINATION)
                        next = STAGE.HOVER;
                    else
                        next = STAGE.LAND;
                    break;
                case LAND:
                    cmd.landing();
                    try {
                        sleep(300, 0);
                    } catch (Exception exc){
                        exc.printStackTrace();
                    }
                    running = false;
                    inMotion = false;
                    break;

            }
            prev = stage;
            stage = next;
        }
    }

    @Override
    public synchronized void start() {
        mypos = (ModelARDrone2) gvh.plat.getModel();
        mypos.initialize();
        cmd = ((ModelARDrone2)gvh.plat.getModel()).cmd;
        running = true;
        super.start();
        gvh.log.d(TAG, "STARTED!");
    }

    private String StageToString(STAGE curr)
    {
        switch (curr){
            case INIT:
                return "INIT";
            case MOVE:
                return "MOVE";
            case HOVER:
                return "HOVER";
            case TAKEOFF:
                return "TAKEOFF";
            case LAND:
                return "LAND";
            case GOAL:
                return "GOAL";
            case STOP:
                return "STOP";
        }
        return "UNKNOWN";
    }

}

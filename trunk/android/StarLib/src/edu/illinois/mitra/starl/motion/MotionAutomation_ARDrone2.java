package edu.illinois.mitra.starl.motion;

import android.util.Log;

import de.yadrone.base.ARDrone;
import de.yadrone.base.IARDrone;
import de.yadrone.base.command.CommandManager;
import de.yadrone.base.command.LEDAnimation;
import de.yadrone.base.navdata.NavDataManager;
import edu.illinois.mitra.starl.gvh.GlobalVarHolder;
import edu.illinois.mitra.starl.models.ModelARDrone2;
import edu.illinois.mitra.starl.objects.ItemPosition;
import edu.illinois.mitra.starl.objects.ObstacleList;

/**
 * Created by SC on 6/7/16.
 */
public class MotionAutomation_ARDrone2 extends RobotMotion {
    protected static final String TAG = "MotionAuto ARDrone2";
    protected static final String ERR = "Critical Error";
    final int safeHeight = 400;//TODO need to see the unit

    protected GlobalVarHolder gvh;

    //Motion Tracking
    protected ItemPosition dest = null;
    private ModelARDrone2 mypos = null;

    protected enum STAGE{
        INIT, MOVE, HOVER, TAKEOFF, LAND, GOAL, STOP
    }
    private STAGE prev=null, next = null;
    protected STAGE stage = STAGE.INIT;
    volatile protected boolean running = false;
    boolean colliding = false;
    private static int timecount;//notice: only for the test of dummyGPS


    //=========================hardware related==========================
    public IARDrone droneInstance = null;
    protected CommandManager cmd;//need? TODO, see ModelARDrone2.java
    private NavDataManager nav;
    private int maxSpeed = 2;
    private boolean outdoor = false;
    class ARDrone2_AttitudeListn implements de.yadrone.base.navdata.AttitudeListener{
        private String tag = "AttitudeInfo";
        ARDrone2_AttitudeListn(String name){
            tag = name;
        }
        public void attitudeUpdated(float pitch, float roll, float yaw)
        {
            //Log.i(tag + " rcvData", "Pitch: " + pitch + " Roll: " + roll + " Yaw: " + yaw);
        }

        public void attitudeUpdated(float pitch, float roll) { ;}
        public void windCompensation(float pitch, float roll) { ;}
    }
    class ARDrone2_BatteryListn implements de.yadrone.base.navdata.BatteryListener{
        private String TAG = "Battery Info";
        public void batteryLevelChanged(int var1){
            if(var1<20)
                Log.e(TAG, "Low battery:"+var1+"%");
        }
        public void voltageChanged(int var1){;}
    }
    private void HardwareInit(){
        if(droneInstance == null)
            Log.e(TAG, "wrong order in init hardware. droneInstance=null.");
        try{
            droneInstance.reset();
            droneInstance.start();
            cmd = droneInstance.getCommandManager();
            nav = droneInstance.getNavDataManager();
            nav.addAttitudeListener(new ARDrone2_AttitudeListn(mypos.name));
            nav.addBatteryListener(new ARDrone2_BatteryListn());
            droneInstance.setSpeed(maxSpeed);
            cmd.setOutdoor(outdoor, outdoor);
            cmd.setLedsAnimation(LEDAnimation.BLINK_ORANGE, 3, 10);//some sig for us
        }catch (Exception exc)
        {
            exc.printStackTrace();
        }
    }
    //====================================================
    @Override
    public synchronized void start() {
        mypos = (ModelARDrone2) gvh.plat.getModel();
        droneInstance = new ARDrone(mypos.ipAddr, null);
        Log.i(TAG, "drone instance created with IP "+mypos.ipAddr);
        HardwareInit();
        running = true;
        inMotion = true;
        super.start();
        gvh.log.d(TAG, "STARTED!");
    }


    public void cancel(){
        running = false;
        //TODO maybe? some command to disconnect the hardware.
    }


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
        if( this.dest == null || (!inMotion && !this.dest.equals(dest))) {
            done = false;
            this.dest = new ItemPosition(dest.name,dest.x,dest.y,dest.z);
//            Log.i("Automation ARDrone2", "GoTo passed!!!");
//            this.dest = new ItemPosition("temp", 1, 0, 0);
            motionStart();
            Log.i("Automation ARDrone2", "GoTo Executed!!!");
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
//        land();
        stage = STAGE.LAND;
        this.dest = null;
        running = false;
        inMotion = false;
    }

    @Override
    public void motion_resume() {
        running = true;
    }

    //====================================================
    private double getDistance(){
//        if(timecount>=100000) {
//            timecount=0;
//            return 0.0;
//        }
//        else
        if(mypos==null) {
            Log.e(TAG, "mypos is null");
            return Math.sqrt(Math.pow((0 - dest.x), 2) + Math.pow((0 - dest.y), 2));
        }
        if(dest==null) {
            Log.e(TAG, "dest is null");
            return Math.sqrt(Math.pow((mypos.x - 0), 2) + Math.pow((mypos.y - 0), 2));
        }
        Log.d(TAG, "from ("+mypos.x+","+mypos.y+","+mypos.z+") to ("+dest.x+","+dest.y+","+dest.z+")" + "STATE:"+StageToString(stage));
        return Math.sqrt(Math.pow((mypos.x - dest.x), 2) + Math.pow((mypos.y - dest.y), 2));
    }

    @Override
    public void run(){
        super.run();
        gvh.threadCreated(this);
        while(true){
            if(!running){
                //Log.i("Automation ARDrone2", "Thread running, but skipping");
                continue;
            }
//            if(inMotion) timecount++;
            //Log.i("Automation ARDrone2", "Thread running, with state "+ StageToString(stage));
            double distance = 0;
            if(colliding) continue;
            mypos = (ModelARDrone2) gvh.gps.getMyPosition();
            if(stage!=STAGE.INIT && stage != STAGE.TAKEOFF) {
                distance = getDistance();
            }
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
                        cmd.move(0, 0, 1, 0).doFor(1);
                    if(distance <= param.GOAL_RADIUS){ //notice: only for curretn demos
                        next = STAGE.GOAL;
                    }
                    else {
                        int vx=0, vy=0;
                        if(dest.x - mypos.x>0) vx=1;
                        if(dest.y - mypos.y>0) vy=1;
                        if(dest.x - mypos.x<0) vx=-1;
                        if(dest.y - mypos.y<0) vy=-1;
                        cmd.move(vx,vy, 0, 0).doFor(1);
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
                    inMotion = false;
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

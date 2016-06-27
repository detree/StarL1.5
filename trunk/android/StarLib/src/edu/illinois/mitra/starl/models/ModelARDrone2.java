package edu.illinois.mitra.starl.models;

import android.util.Log;

import de.yadrone.base.ARDrone;
import de.yadrone.base.IARDrone;
import de.yadrone.base.command.CommandManager;
import de.yadrone.base.command.LEDAnimation;
import de.yadrone.base.navdata.NavDataManager;
import edu.illinois.mitra.starl.exceptions.ItemFormattingException;
import edu.illinois.mitra.starl.interfaces.TrackedRobot;
import edu.illinois.mitra.starl.objects.ItemPosition;
import edu.illinois.mitra.starl.objects.ObstacleList;
import edu.illinois.mitra.starl.objects.Point3d;
import edu.illinois.mitra.starl.objects.PositionList;

/**
 * Created by SC on 6/6/16.
 */
public class ModelARDrone2 extends ItemPosition implements TrackedRobot{
    class ARDrone2_AttitudeListn implements de.yadrone.base.navdata.AttitudeListener{
        private String tag;

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

    public String ipAddr;
    public IARDrone droneInstance;
    public CommandManager cmd;//TODO: re-encapsulate this thing
    private NavDataManager nav;
    //the position/velocity/status information
    //angular velocity in radius/second, regular velocity in meter/second
    //position x,y,z; name defined in ItemPosition
    public double currYaw, currPitch, currRoll;
    public double vYaw, vPitch, vRoll;
    public double vX, vY, vZ;
    public int maxSpeed;//in percentage

    //configurations supported by the drone
    //TODO: public double maxAltitude, minAltitude;
    public boolean outdoor;

    public ModelARDrone2(String wholedesc) throws ItemFormattingException {
        String[] parts = wholedesc.replace(",", "").split("\\|");
        if(parts.length==10){
            this.name = parts[1];
            this.ipAddr = parts[2];
            this.x = Integer.parseInt(parts[2]);
            this.y = Integer.parseInt(parts[3]);
            this.z = Integer.parseInt(parts[4]);
            this.currYaw = Integer.parseInt(parts[5]);
            this.currPitch = Integer.parseInt(parts[6]);
            this.currRoll = Integer.parseInt(parts[7]);
        } else {
            throw new ItemFormattingException("Should be length 10, is length " + parts.length);
        }
        initHelper();
    }

    public ModelARDrone2(String wholedesc, ModelARDrone2 old) throws ItemFormattingException {
        String[] parts = wholedesc.replace(",", "").split("\\|");
        if(parts.length==10){
            this.name = parts[1];
            this.ipAddr = parts[2];
            this.x = Integer.parseInt(parts[2]);
            this.y = Integer.parseInt(parts[3]);
            this.z = Integer.parseInt(parts[4]);
            this.currYaw = Integer.parseInt(parts[5]);
            this.currPitch = Integer.parseInt(parts[6]);
            this.currRoll = Integer.parseInt(parts[7]);
        } else {
            throw new ItemFormattingException("Should be length 10, is length " + parts.length);
        }
        CopySetupDrone(old);
    }

    private void CopyFull(ModelARDrone2 old){
        this.ipAddr = old.ipAddr;
        this.droneInstance = old.droneInstance;
        this.cmd = old.cmd;
        this.nav = old.nav;
        this.currPitch = old.currPitch;
        this.currRoll = old.currRoll;
        this.currYaw = old.currYaw;
        this.vYaw = old.vYaw;
        this.vPitch = old.vPitch;
        this.vRoll = old.vRoll;
        this.vX = old.vX;
        this.vY = old.vY;
        this.vZ = old.vZ;
        this.maxSpeed = old.maxSpeed;
        this.outdoor = old.outdoor;
        this.x = old.x;
        this.y = old.y;
        this.z = old.z;
    }

    private void CopySetupDrone(ModelARDrone2 old) throws ItemFormattingException{
        if(old.ipAddr == null || old.droneInstance == null || old.cmd == null)
            throw new ItemFormattingException("The OLD drone does not seems to be properly set up");
        this.ipAddr = old.ipAddr;
        this.droneInstance = old.droneInstance;
        this.cmd = old.cmd;
        this.nav = old.nav;
        this.vX = old.vX;
        this.vY = old.vY;
        this.vZ = old.vZ;
        this.maxSpeed = old.maxSpeed;
        this.outdoor = old.outdoor;
    }

    public ModelARDrone2(String name, int x, int y){
        super(name, x, y, 0);
        this.currYaw = 0;
        this.currPitch = 0;
        this.currRoll = 0;
        initHelper();
    }

    public ModelARDrone2(String name, int x, int y, int z){
        super(name, x, y, z);
        this.currYaw = 0;
        this.currPitch = 0;
        this.currRoll = 0;
        initHelper();
    }

    private void initHelper(){
        if(this.ipAddr==null)
            this.ipAddr = "192.168.1.1";
        droneInstance = new ARDrone(ipAddr, null);
        vX=0;
        vY=0;
        vZ=0;
        vYaw=0;
        vPitch=0;
        vRoll=0;
        outdoor = false;
        maxSpeed = 5;
    }

    @Override
    public void initialize(){
        try{
            Log.i("Model ARDrone2", "initialization called");
            droneInstance.reset();
            droneInstance.start();
            cmd = droneInstance.getCommandManager();
            nav = droneInstance.getNavDataManager();
            nav.addAttitudeListener(new ARDrone2_AttitudeListn(this.name));
            droneInstance.setSpeed(maxSpeed);
            cmd.setOutdoor(outdoor, outdoor);
            cmd.setLedsAnimation(LEDAnimation.BLINK_ORANGE, 3, 10);//some sig for us
        }catch (Exception exc)
        {
            exc.printStackTrace();
        }
    }

    //TODO, temp stub here
    @Override
    public Point3d predict(double[] noises, double timeSinceUpdate) {
        return new Point3d(x,y,z);
    }

    //TODO, temp stub here
    @Override
    public void collision(Point3d collision_point) {
    }

    //TODO, temp stub here
    @Override
    public void updatePos(boolean followPredict) {
    }

    //TODO: extend angular velocity
    @Override
    public boolean inMotion() {
        return (vX != 0 || vY != 0 || vZ != 0);
    }

    //TODO, temp stub here
    @Override
    public void updateSensor(ObstacleList obspoint_positions,
                             PositionList<ItemPosition> sensepoint_positions) {
    }

}

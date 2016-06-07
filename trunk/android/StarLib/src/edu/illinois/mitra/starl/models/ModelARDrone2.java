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
            Log.i(tag + " rcvData", "Pitch: " + pitch + " Roll: " + roll + " Yaw: " + yaw);
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

    public ModelARDrone2(String received) throws ItemFormattingException {
        String[] parts = received.replace(",", "").split("\\|");
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
        droneInstance = new ARDrone(ipAddr, null);
        cmd = droneInstance.getCommandManager();
        nav = droneInstance.getNavDataManager();
    }

    private void initHelper(){
        vX=0;
        vY=0;
        vZ=0;
        vYaw=0;
        vPitch=0;
        vRoll=0;
        outdoor = false;
        maxSpeed = 10;
    }

    @Override
    public void initialize(){
        try{
            droneInstance.reset();
            droneInstance.start();
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

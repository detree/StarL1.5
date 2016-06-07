package edu.illinois.mitra.starl.models;

import de.yadrone.base.ARDrone;
import de.yadrone.base.IARDrone;
import edu.illinois.mitra.starl.exceptions.ItemFormattingException;
import edu.illinois.mitra.starl.interfaces.TrackedRobot;
import edu.illinois.mitra.starl.objects.ItemPosition;

/**
 * Created by SC on 6/6/16.
 */
public class ModelARDrone2 extends ItemPosition implements TrackedRobot{
    public String ipAddr;
    public IARDrone DroneInstance;
    //the position/velocity/status information
    //angular velocity in radius/second, regular velocity in meter/second
    public double currYaw, currPitch, currRoll;
    public double vYaw, vPitch, vRoll;
    public double vX, vY, vZ;
    public double posX, posY, posZ;

    //configurations supported by the drone
    public double maxAltitude, minAltitude;
    public boolean outdoor;

    public ModelARDrone2(String received) throws ItemFormattingException {
        String[] parts = received.replace(",", "").split("\\|");
        if(parts.length==10){
            this.name = parts[1];
            this.ipAddr = parts[2];
            this.posX = Integer.parseInt(parts[2]);
            this.posY = Integer.parseInt(parts[3]);
            this.posZ = Integer.parseInt(parts[4]);
            this.currYaw = Integer.parseInt(parts[5]);
            this.currPitch = Integer.parseInt(parts[6]);
            this.currRoll = Integer.parseInt(parts[7]);
        } else {
            throw new ItemFormattingException("Should be length 10, is length " + parts.length);
        }
        initHelper();
        DroneInstance = new ARDrone(ipAddr, null);
    }

    private void initHelper(){
        vX=0;
        vY=0;
        vZ=0;
        vYaw=0;
        vPitch=0;
        vRoll=0;
        maxAltitude = 2;
        minAltitude = 0.5;
        outdoor = false;
    }

}

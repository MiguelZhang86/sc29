/**
 * @author Jaime Sousa
 */
package domain;

public class Device{
    private String name;
    private int upTime;
    private boolean turnOn;

    /**
     * Representa um device numa section
     *
     * @param name the device name
     */
    Device(String name) {
        this.name = name;
        this.upTime = 0;
        this.turnOn = false;
    }
    /**
    * Turns on the device for a certain amount of time.
    *
    * @param time the time to turn on the device
    */
    void turnOn(int time) {
        if (time < 0 || time > 600) {
            throw new IllegalArgumentException("Device value/time can not be negative or bigger tan 600.");
        }
        if(time == 0){
            turnOn = false;
        }
        if(time == 1){
            turnOn = true;
        }
        this.upTime += time;
        turnOn = true;
    }
    /**
     * Gets the name of the device.
     * @return the name of the device
     */
    String getName() {
        return name;
    }

    int getUpTime() {
        return upTime;
    }
    /**
     * Gets the status of the device.
     * @return true if turn on false if turn false
     */
    boolean getStatus(){
        return turnOn;
    }
}
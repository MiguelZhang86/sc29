/**
 * @author Jaime Sousa
 */
package domain;

public class Device{
    private String name;
    private int upTime;

    /**
     * Representa um device numa section
     *
     * @param name the device name
     */
    Device(String name) {
        this.name = name;
        this.upTime = 0;
    }
    /**
    * Turns on the device for a certain amount of time.
    *
    * @param time the time to turn on the device
    */
    void turnOn(int time) {
        if (time < 0 || time > 600) {
            throw new IllegalArgumentException("Device value/time must be between than 0 and 600.");
        }
        if(time == 0){
            this.upTime = 0;
        } else{
            this.upTime += time;
        }
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
}
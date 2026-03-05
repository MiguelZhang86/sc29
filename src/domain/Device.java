/**
 * @author Jaime Sousa
 */
public class Device{
    private String name;
    private int upTime;

    /**
     * Representa um device numa section
     *
     * @param name the device name
     */
    public Device(String name) {
        this.name = name;
        this.upTime = 0;
    }
    /**
    * Turns on the device for a certain amount of time.
    *
    * @param time the time to turn on the device
    */
    public void turnOn(int time) {
        this.upTime += time;
    }
    /**
     * Gets the name of the device.
     * @return the name of the device
     */
    public String getName() {
        return name;
    }
}
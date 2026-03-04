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

    public void turnOn(int time) {
        this.upTime += time;
    }
}
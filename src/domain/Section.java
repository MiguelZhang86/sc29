/**
 * @author Jaime Sousa
 */
package domain;

import java.util.ArrayList;
import java.util.List;

public class Section {
    private String name;
    private List<User> allowedUsers; //permissão de mudar
    private List<Device> devices;

    /**
     * Represents a section (room) in a house with a name and a list of allowed users.
     *
     * @param name the section name
     * @param allowedUsers the list of allowed users
     */
    
    Section(String name, List<User> allowedUsers, List<Device> devices) {
        this.name = name;
        this.allowedUsers = new ArrayList<>(allowedUsers);
        this.devices = devices;

    }

    Section(String name, List<User> allowedUsers) {
        this.name = name;
        this.allowedUsers = new ArrayList<>(allowedUsers);
        this.devices = new ArrayList<>();
    }

    Section(String name) {
        this.name = name;
        this.allowedUsers = new ArrayList<>();
        this.devices = new ArrayList<>();
    }

    /**
     * Gets the section name.
     *
     * @return the section name
     */
    String getName() {
        return name;
    }

    int devicesCount() {
        return devices.size();
    }
    /**
     * Checks whether a user is allowed to access this section.
     *
     * @param user the user to check
     * @return true if the user is allowed, false otherwise
     */

    void addAllowedUser(User user) {
        if (!allowedUsers.contains(user)) {
            allowedUsers.add(user);
        }
    }

    void removeAllowedUser(User user) {
        if (allowedUsers.contains(user)) {
            allowedUsers.remove(user);
        }
    }

    boolean isUserAllowed(User user) {
        return this.allowedUsers.contains(user);

    }
    /*
    * Turns on a device in this section for a specified time if the user is allowed to access this section.
    * @param deviceName the name of the device to turn on
    * @param time the time to turn on the device
    * @param user the user attempting to turn on the device
    * 
    * @returns true if the device was there
    * false if 
    */

    boolean turnOnDevice(String deviceName, int time, User user) {
        if (!isUserAllowed(user)) {
            throw new IllegalArgumentException("User is not allowed to access this section");
        }

        for (Device d : devices) {
            if (d.getName().equals(deviceName)) {
                d.turnOn(time);
                return true;
               
            }
        }
        return false;
    }

    int getDeviceUpTime(String deviceName) {
        for (Device d : devices) {
            if (d.getName().equals(deviceName)) {
                return d.getUpTime();
            }
        }
        throw new IllegalArgumentException("Device not found in this section");
    }

    void registerDevice(String deviceName) {
        for (Device d : devices) {
            if (d.getName().equals(deviceName)) {
                throw new IllegalArgumentException("Device already exists in this section");
            }
        }
        devices.add(new Device(generateDeviceName()));
    }
    //gera o d1 ou o m1 dependendo do nome da secção
    private String generateDeviceName() {
        return this.name.charAt(0) + String.valueOf(devicesCount()+1);
    }

    boolean hasDevice(String deviceName) {
        for (Device d : devices) {
            if (d.getName().equals(deviceName)) {
                return true;
            }
        }
        return false;
    }

}
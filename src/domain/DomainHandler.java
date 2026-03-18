/**
 * @author Jaime Sousa
 */

package domain;

import java.util.LinkedList;
import java.util.List;

public class DomainHandler implements IDomainHandler {

    private static final DomainHandler INSTANCE = new DomainHandler();

    private User authenticatedUser;
    private List<House> houses;
    private List<User> users;

    private DomainHandler() {
        this.houses = new LinkedList<House>();
        this.users = new LinkedList<User>();
        this.authenticatedUser = null;
    }

    public static DomainHandler getInstance() {
        return INSTANCE;
    }

    public void addHouse(House house) {
        if (!houses.contains(house)) {
            houses.add(house);
        }
    }

    public void addUser(User user) {
        if (!users.contains(user)) {
            users.add(user);
        }
    }

    public boolean authenticateUser(String username, String password) {
        for (User user : users) {
            if (user.getName().equals(username) && user.isPassword(password)) {
                authenticatedUser = user;
                return true;
            }
        }
        return false;
    }

    public boolean isUserAllowed(String houseName, String sectionName) {
        User user = this.authenticatedUser;
        if (user == null) {
            return false;
        }

        House house = getHouse(houseName);
        if (house == null) {
            return false;
        }

        return house.isOwner(user) || house.isUserAllowed(user, sectionName);
    }

    private User getUser(String userName) {
        for (User user : users) {
            if (user.getName().equals(userName)) {
                return user;
            }
        }
        throw new IllegalArgumentException("User not found: " + userName);
    }

    private House getHouse(String houseName) {
        for (House house : houses) {
            if (house.getName().equals(houseName)) {
                return house;
            }
        }
        throw new IllegalArgumentException("House not found: " + houseName);
    }

    //Log out the current user - exit
    public void logout() {
        authenticatedUser = null;
    }

    @Override
    public void createHouse(String name) {
        isAnyoneAuthenticated(); //throws if no user is authenticated

        House newHouse = new House(name, new LinkedList<Section>(), this.authenticatedUser);
        addHouse(newHouse);
    }

    @Override
    public void registerDevice(String houseName, String sectionName) {
        for (House house : houses) {
            if (house.getName().equals(houseName)) {
                for (Section section : house.getSections()) {
                    if (section.getName().equals(sectionName)) {
                        section.registerDevice("Device" + (section.devicesCount() + 1));
                        return;
                    }
                }
                throw new IllegalArgumentException("Section not found: " + sectionName);
            }
        }
        throw new IllegalArgumentException("House not found: " + houseName);
    }

    @Override
    public void addDeviceTime(String houseName, String deviceName, int value) {
        for (House house : houses) {
            if (house.getName().equals(houseName)) {
                //percorre as sections da casa e tenta ligar o dispositivo - se conseguir, retorna
                if(house.turnOnDevice(this.authenticatedUser, deviceName, value)) {
                    return;
                }
                
            }
        }
        throw new IllegalArgumentException("House or Device not found: " + houseName);
    }

    public boolean isUserAllowedInAllSections(String houseName){
        for (House house : houses) {
            if (house.getName().equals(houseName)) {
                return house.isUserAllowedInAllSections(authenticatedUser);
            }
        }
        throw new IllegalArgumentException("House not found: " + houseName);
    }

    public boolean isAnyoneAuthenticated() {
        if(authenticatedUser == null) {
            throw new IllegalStateException("No user is currently authenticated");
        }
        return authenticatedUser != null;
    }
    

    





}
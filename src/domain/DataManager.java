package domain;

import java.util.LinkedList;
import java.util.List;

public class DataManager {

    public static final DataManager INSTANCE = new DataManager();
    private final String DATA_FILE = "data.txt";
    
    private List<House> houses;
    private List<User> users;

    private DataManager() {
        this.houses = new LinkedList<House>();
        this.users = new LinkedList<User>();
        load(DATA_FILE);
        // Private constructor to prevent instantiation
    }

    private void load(String dataFile) {
        
        // Load data from file (not implemented)
    }
    
    public static DataManager getInstance() {
        return INSTANCE;
    }

    public User authenticateUser(String username, String password) {
        for (User user : users) {
            if (user.getName().equals(username) && user.isPassword(password)) {
                return user;
            }
        }
        return null;
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


    public boolean isUserAllowed(String houseName, String sectionName, User user) {
        House house = getHouse(houseName);
        if (house == null) {
            return false;
        }

        return house.isOwner(user) || house.isUserAllowed(user, sectionName);
    }

    private House getHouse(String houseName) {
        for (House house : houses) {
            if (house.getName().equals(houseName)) {
                return house;
            }
        }
        throw new IllegalArgumentException("House not found: " + houseName);
    }

    

    public void createHouse(String name, User owner) {
        House newHouse = new House(name, owner);
        addHouse(newHouse);
    }


    public void registerDevice(String houseName, String sectionName, User user) {
        if(!isUserAllowed(houseName, sectionName, user)){
            throw new IllegalArgumentException("User is not allowed to access this section");
        }
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

    
    public void addDeviceTime(String houseName, String deviceName, int value, User user) {
        for (House house : houses) {
            if (house.getName().equals(houseName)) {
                //percorre as sections da casa e tenta ligar o dispositivo - se conseguir, retorna
                if(house.turnOnDevice(user, deviceName, value)) {
                    return;
                }
                
            }
        }
        throw new IllegalArgumentException("House or Device not found: " + houseName);
    }

    public boolean isUserAllowedInAllSections(String houseName, User authenticatedUser) {
        for (House house : houses) {
            if (house.getName().equals(houseName)) {
                return house.isUserAllowedInAllSections(authenticatedUser);
            }
        }
        throw new IllegalArgumentException("House not found: " + houseName);
    }

    
}

package domain;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class DataManager {

    public static final DataManager INSTANCE = new DataManager();
    private final String DATA_FILE = "usersData.txt";

    private List<House> houses;
    private List<User> users;

    private DataManager() {
        this.houses = new LinkedList<House>();
        this.users = new LinkedList<User>();

        // Private constructor to prevent instantiation
    }

    public void load() {
        File f = new File(DATA_FILE);
        if (!f.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            String mode = null;
            House currentHouse = null;
            Section currentSection = null;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                if (line.equals("USERS")) {
                    mode = "USERS";
                    continue;
                }
                if (line.equals("HOUSES")) {
                    mode = "HOUSES";
                    continue;
                }

                if ("USERS".equals(mode)) {
                    String[] parts = line.split(":");
                    this.users.add(new User(parts[0], parts[1]));

                } else if ("HOUSES".equals(mode)) {
                    if (line.startsWith("HOUSE:")) {
                        String[] parts = line.split(":");
                        User owner = null;
                        for (User u : this.users) {
                            if (u.getName().equals(parts[2])) {
                                owner = u;
                                break;
                            }
                        }
                        currentHouse = new House(parts[1], owner);
                        houses.add(currentHouse);

                    } else if (line.startsWith("SECTION:")) {
                        String[] parts = line.split(":", -1);
                        for (Section s : currentHouse.getSections()) {
                            if (s.getName().equals(parts[1])) {
                                currentSection = s;
                                break;
                            }
                        }
                        if (!parts[2].isEmpty()) {
                            for (String username : parts[2].split(",")) {
                                for (User u : this.users) {
                                    if (u.getName().equals(username.trim())) {
                                        currentSection.addAllowedUser(u);
                                        break;
                                    }
                                }
                            }
                        }

                    } else if (line.equals("DEVICE")) {
                        currentSection.registerDevice("");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static DataManager getInstance() {
        return INSTANCE;
    }

    public AuthResult authenticateUser(String username, String password) {
        for (User user : users) {
            if (user.getName().equals(username)) {
                if (user.isPassword(password)) {
                    return new AuthResult(user, AuthEnum.OK_USER);
                } else {
                    return new AuthResult(null, AuthEnum.WRONG_PWD);
                }
            }
        }

        // If user not found, create a new one
        User newUser = new User(username, password);
        users.add(newUser);
        return new AuthResult(newUser, AuthEnum.OK_NEW_USER);
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
        if (!isUserAllowed(houseName, sectionName, user)) {
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
                if (house.turnOnDevice(user, deviceName, value)) {
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

    public void save() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(DATA_FILE))) {
            bw.write("USERS");
            bw.newLine();
            for (User u : this.users) {
                bw.write(u.toText());
                bw.newLine();
            }
            bw.newLine();
            bw.write("HOUSES");
            bw.newLine();
            for (House h : this.houses) {
                bw.write(h.toText());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean allowUser(User authenticatedUser, String userName, String houseName, String sectionName) {
        House house = getHouse(houseName);
        if (house == null) {
            throw new IllegalArgumentException("House not found: " + houseName);
        }
        if (!house.isOwner(authenticatedUser)) {
            throw new IllegalArgumentException("Only the owner can allow users");
        }
        User userToAllow = null;
        for (User u : this.users) {
            if (u.getName().equals(userName)) {
                userToAllow = u;
                break;
            }
        }
        if (userToAllow == null) {
            throw new IllegalArgumentException("User to allow not found: " + userName);
        }
        house.allowUser(authenticatedUser, userToAllow, sectionName);
        return true;
    }
    
}

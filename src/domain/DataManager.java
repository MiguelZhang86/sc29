package domain;

import java.util.LinkedList;
import java.util.List;
import java.io.*;

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
        File f = new File(dataFile);
        if(!f.exists()) return;

        try(BufferedReader br = new BufferedReader(new FileReader(f))){
            String line;
            String mode = null;
            House currentHouse = null;
            Section currentSection = null;

            while((line = br.readLine()) != null){
                line = line.trim();
                if(line.isEmpty()) continue;

                if(line.equals("USERS")){mode = "USERS"; continue;}
                if(line.equals("HOUSES")){mode ="HOUSES"; continue;}

                if("USERS".equals(mode)){
                    String[] parts = line.split(":");
                    this.users.add(new User(parts[0], parts[1]));

                } else if("HOUSES".equals(mode)){
                    if(line.startsWith("HOUSE:")){
                        String[] parts = line.split(":");
                        User owner = null;
                        for(User u: this.users){
                            if(u.getName().equals(parts[2])){owner=u; break;}
                        }
                        currentHouse = new House(parts[1], owner);
                        houses.add(currentHouse);

                    } else if(line.startsWith("SECTION:")){
                        String[] parts = line.split(":", -1);
                        for(Section s: currentHouse.getSections()){
                            if(s.getName().equals(parts[1])){currentSection = s; break;}
                        }
                        if(!parts[2].isEmpty()){
                            for(String username : parts[2].split(",")){
                                for(User u : this.users){
                                    if(u.getName().equals(username.trim())){
                                        currentSection.addAllowedUser(u);
                                        break;
                                    }
                                }
                            }
                        }

                    } else if(line.equals("DEVICE")){
                        currentSection.registerDevice("");
                    }
                }
            }
        } catch(IOException e){
            e.printStackTrace();
        }
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

    public void save(){
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(DATA_FILE))){
            bw.write("USERS");
            bw.newLine();
            for(User u: this.users){
                bw.write(u.toText());
                bw.newLine();
            }
            bw.newLine();
            bw.write("HOUSES");
            bw.newLine();
            for(House h : this.houses){
                bw.write(h.toText());
            }
        } catch(IOException e){
            e.printStackTrace();
        }
    }
    
}

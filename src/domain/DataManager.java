package domain;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

/**
 * Singleton que gere todos os dados persistentes do sistema Sperta,
 * incluindo utilizadores, casas, seções e dispositivos.
 * Carrega e guarda o estado no ficheiro usersData.txt.
 */
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

    /**
     * Carrega utilizadores, casas, seções e dispositivos a partir do ficheiro usersData.txt.
     * Deve ser chamado uma vez no arranque do servidor.
     */
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

                    } else if (line.startsWith("DEVICE:")) {
                        currentSection.registerDevice("");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retorna a instância singleton do DataManager.
     * @return a instância do DataManager
     */
    public static DataManager getInstance() {
        return INSTANCE;
    }

    /**
     * Autentica um utilizador pelo nome e password.
     * Se o utilizador não existir, regista-o como novo utilizador.
     * @param username o nome do utilizador
     * @param password a password
     * @return um AuthResult com o resultado e o objeto utilizador, se bem-sucedido
     */
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
        this.save();
        return new AuthResult(newUser, AuthEnum.OK_NEW_USER);
    }
    /**
     * Adiciona uma casa ao sistema, caso ainda não exista.
     * @param house a casa a adicionar
     */
    public void addHouse(House house) {
        if (!houses.contains(house)) {
            houses.add(house);
        }
    }

    /**
     * Verifica se uma casa com o nome indicado existe no sistema.
     * @param name o nome da casa
     * @return true se a casa existir, false caso contrário
     */
    public boolean isHouseCreated(String name){
        boolean exist = false;
        for (House h : houses) {
            if (h.getName().equals(name)) {
                exist = true;
            }
        }
        return exist;
    }

    /**
     * Adiciona um utilizador ao sistema, caso ainda não exista.
     * @param user o utilizador a adicionar
     */
    public void addUser(User user) {
        if (!users.contains(user)) {
            users.add(user);
        }
    }

    /**
     * Verifica se um utilizador tem permissão para aceder a uma seção ou dispositivo de uma casa.
     * Se sectionName for nulo ou vazio, verifica todas as seções.
     * @param houseName o nome da casa
     * @param sectionName a primeira letra da seção, o nome do dispositivo, ou nulo/vazio para todas
     * @param user o utilizador a verificar
     * @return true se o utilizador tiver permissão, false caso contrário
     */
    public boolean isUserAllowed(String houseName, String sectionName, User user) {
        House house = getHouse(houseName);
        if (sectionName == null || sectionName.isEmpty()) {
            return house.isUserAllowedInAllSections(user);
        }

        // If the target matches a section, validate section permission.
        if (house.hasSection(sectionName)) {
            return house.isUserAllowedInSection(user, sectionName);
        }

        // Otherwise treat target as a device and validate against that device's section.
        return house.isUserAllowed(user, sectionName);
    }

    /**
     * Retorna a casa com o nome indicado.
     * @param houseName o nome da casa
     * @return a casa correspondente
     * @throws IllegalArgumentException se a casa não existir
     */
    private House getHouse(String houseName) {
        for (House house : houses) {
            if (house.getName().equals(houseName)) {
                return house;
            }
        }
        throw new IllegalArgumentException("House not found: " + houseName);
    }
    /**
     * Cria uma nova casa com o nome e dono indicados e guarda o estado.
     * @param name o nome da casa
     * @param owner o utilizador dono
     * @throws IllegalArgumentException se a casa já existir
     */
    public void createHouse(String name, User owner) {
        for (House h : houses) {
            if (h.getName().equals(name)) {
                throw new IllegalArgumentException("House already exists: " + name);
            }
        }
        House newHouse = new House(name, owner);
        addHouse(newHouse);
        this.save();
    }


    /**
     * Regista um novo dispositivo na seção indicada da casa indicada.
     * Apenas o dono da casa pode registar dispositivos.
     * @param houseName o nome da casa
     * @param sectionName o identificador da seção (primeira letra)
     * @param user o utilizador que faz o pedido
     * @return OK, NOHM ou NOPERM
     */
    public String registerDevice(String houseName, String sectionName, User user) {
        if (!isHouseCreated(houseName)) return "NOHM";
        if (!getHouse(houseName).isOwner(user)) {
            return "NOPERM";
        }
        for (House house : houses) {
            if (house.getName().equals(houseName)) {
                for (Section section : house.getSections()) {
                    if (section.getName().charAt(0) == sectionName.charAt(0)) {
                        section.registerDevice("Device" + (section.devicesCount() + 1));
                        this.save();
                        return "OK";
                    }
                }
                throw new IllegalArgumentException("Section not found: " + sectionName);
            }
        }
        return "NOHM";
    }

    /**
     * Envia um valor para um dispositivo, atualizando o seu estado.
     * @param houseName o nome da casa
     * @param deviceName o nome do dispositivo
     * @param value 0 (desligar), 1 (ligar), ou 2-600 (ligar por x minutos)
     * @param user o utilizador que faz o pedido
     * @return OK, NOHM, NOD ou NOPERM
     */
    public String addDeviceTime(String houseName, String deviceName, int value, User user) {
        if (!isHouseCreated(houseName)) return "NOHM";                                                                                                             
        if (getHouse(houseName).hasSection(deviceName)) return "NOD";
        if (!isDeviceRegistered(houseName, deviceName)) return "NOD";                                                                                              
        if (!isUserAllowed(houseName, deviceName, user)) return "NOPERM";
        for (House house : houses) {
            if (house.getName().equals(houseName)) {
                //percorre as sections da casa e tenta ligar o dispositivo - se conseguir, retorna
                if (house.turnOnDevice(user, deviceName, value)) {
                    return "OK";
                }
            }
        }
        return "NOHM";
    }

    /**
     * Verifica se um utilizador tem permissão em todas as seções de uma casa.
     * @param houseName o nome da casa
     * @param authenticatedUser o utilizador a verificar
     * @return true se tiver permissão em todas as seções ou for o dono
     */
    public boolean isUserAllowedInAllSections(String houseName, User authenticatedUser) {
        for (House house : houses) {
            if (house.getName().equals(houseName)) {
                return house.isUserAllowedInAllSections(authenticatedUser);
            }
        }
        throw new IllegalArgumentException("House not found: " + houseName);
    }

    /**
     * Persiste todos os utilizadores, casas, seções e dispositivos no ficheiro usersData.txt.
     */
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

    /**
     * Concede permissão a um utilizador para gerir uma seção (ou todas) de uma casa.
     * Apenas o dono da casa pode conceder permissões.
     * @param authenticatedUser o utilizador que faz o pedido (deve ser o dono)
     * @param userName o utilizador a quem conceder acesso
     * @param houseName o nome da casa
     * @param sectionName o identificador da seção ou "all"
     * @return OK, NOHM, NOPERM ou NOUSER
     */
    public String allowUser(User authenticatedUser, String userName, String houseName, String sectionName) {
        if (authenticatedUser == null) {
            return "NOPERM";
        }
        if (userName == null || userName.trim().isEmpty()) {
            return "NOUSER";
        }
        if (houseName == null || houseName.trim().isEmpty()) {
            return "NOHM";
        }
        if (sectionName == null || sectionName.trim().isEmpty()) {
            throw new IllegalArgumentException("Section name is required");
        }
        if (!isHouseCreated(houseName)) return "NOHM";
        House house = getHouse(houseName);

        if (!house.isOwner(authenticatedUser)) {
            return "NOPERM";
        }

        User userToAllow = null;
        for (User u : this.users) {
            if (u.getName().equals(userName)) {
                userToAllow = u;
                break;
            }
        }
        if (userToAllow == null) {
            return "NOUSER";
        }
        if (!house.allowUser(authenticatedUser, userToAllow, sectionName)) {
            throw new IllegalArgumentException("Section not found: " + sectionName);
        }
        this.save();
        return "OK";
    }

    /**
     * Verifica se um dispositivo está registado numa casa.
     * @param houseName o nome da casa
     * @param deviceName o nome do dispositivo
     * @return true se o dispositivo existir, false caso contrário
     */
    public boolean isDeviceRegistered(String houseName, String deviceName) {                                                                                   
      for (House house : houses) {                                        
          if (house.getName().equals(houseName)) {                                                                                                           
              for (Section section : house.getSections()) {
                  if (section.hasDevice(deviceName)) return true;
              }
          }
      }
      return false;
    }
}

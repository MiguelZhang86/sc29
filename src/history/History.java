package history;
import java.io.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import domain.DomainHandler;

public class History {

    private static final String BASE_DIR = "logs/";
    private static final Map<String, Log> logs = new HashMap<>();

    public static void registerCommand(String entry) {

        try{
            String[] parts =entry.split(",");
            String house = parts[0];
            String device = parts[1];
            String value = parts[2];

            String key = house + "/" + device;
            if(!logs.containsKey(key)){
                logs.put(key, new Log(BASE_DIR + house + "/" + device + ".csv"));
            }
            logs.get(key).write(value);

        } catch (IOException e){
            e.printStackTrace();
        }
        
    }
    // RT<hm>Receber a informação sobre o último comando
    // (estados/temporizações) enviado a cada dispositivo da casa <hm>, desde
    // que o utilizador tenha permissões.

    public static String getLastCommand(DomainHandler domainHandler, String houseName) {


        if(!domainHandler.isUserAllowed(houseName, "")) {
            throw new IllegalArgumentException("User is not allowed to access all sections in the house");
        }
        
        File houseDir = new File(BASE_DIR + houseName);
        if(!houseDir.exists())return null;

        StringBuilder res = new StringBuilder();

        File[] files = houseDir.listFiles();                                                                                     
        if (files == null) return null;

        for (File f : files) {
        String deviceName = f.getName().replace(".csv", "");
        if (!domainHandler.isUserAllowed(houseName, deviceName)) continue;
                                                                                                                            
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;                                                                                                     
            String last = null;
            while ((line = br.readLine()) != null) last = line;
            if (last != null) res.append(f.getName()).append(": ").append(last).append("\n");                                
        } catch (IOException e) {
            e.printStackTrace();                                                                                             
        }       
    }                                                                                                                        
                
    return res.length() > 0 ? res.toString() : null;

    }


    public static String getHistory(DomainHandler domainHandler, String houseName, String deviceName) {
        if (!domainHandler.isUserAllowed(houseName, deviceName)) {
            return "NOPERM";
        }
        File file = new File(BASE_DIR + houseName + "/" + deviceName + ".csv");
        if(!file.exists()) return null;

        StringBuilder res = new StringBuilder();
        try( BufferedReader br = new BufferedReader(new FileReader(file))){
            String line;
            while((line = br.readLine())!= null){
                res.append(line).append("\n");
            }
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }
        return res.length() > 0 ? res.toString() : null;
    }
    
}

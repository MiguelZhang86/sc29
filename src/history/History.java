package history;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

import domain.DomainHandler;

/**
 * Gere o histórico de comandos enviados aos dispositivos e os últimos estados registados.
 * Escreve ficheiros CSV por dispositivo e mantém um ficheiro global last_states.csv.
 */
public class History {

    private static final String BASE_DIR = "logs/";
    private static final Map<String, Log> logs = new HashMap<>();

    /**
     * Regista um comando de dispositivo no seu ficheiro CSV e atualiza o last_states.csv.
     * @param entry string separada por vírgulas no formato "casa,dispositivo,valor"
     */
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
            updateLastStates(house, device, value);

        } catch (IOException e){
            e.printStackTrace();
        }
        
    }

    /**
     * Retorna o último estado registado de cada dispositivo de uma casa
     * para os quais o utilizador autenticado tem permissão de acesso.
     * @param domainHandler o handler da sessão do utilizador autenticado
     * @param houseName o nome da casa
     * @return string formatada com os estados dos dispositivos, ou NOHM / NODATA / NOPERM
     */
    public static String getLastCommand(DomainHandler domainHandler, String houseName) {
        
        if (!domainHandler.isHouseCreated(houseName)) return "NOHM";                                                                                                                                            
        File lastStates = new File(BASE_DIR + "last_states.csv");                                                                                                                                                         
        if (!lastStates.exists()) return "NODATA";

        StringBuilder res = new StringBuilder();
        boolean anyDeviceForHouse = false;

        try (BufferedReader br = new BufferedReader(new FileReader(lastStates))) {
            String line;
            while ((line = br.readLine()) != null){
                String[] parts = line.split(",");
                if (!parts[0].equals(houseName)) continue;
                anyDeviceForHouse = true;
                String deviceName = parts[1];
                String value = parts[2];
                if (!domainHandler.isUserAllowed(houseName, deviceName)) continue;
                res.append(deviceName).append(": ").append(value).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res.length() > 0 ? res.toString() : (anyDeviceForHouse ? "NOPERM" : "NODATA");
    }


    /**
     * Retorna o histórico completo de comandos (conteúdo CSV) de um dispositivo específico.
     * @param domainHandler o handler da sessão do utilizador autenticado
     * @param houseName o nome da casa
     * @param deviceName o nome do dispositivo
     * @return conteúdo do ficheiro CSV, ou NOHM / NOD / NOPERM / NODATA
     */
    public static String getHistory(DomainHandler domainHandler, String houseName, String deviceName) {
        if (!domainHandler.isHouseCreated(houseName)) return "NOHM";
        if (!domainHandler.isDeviceRegistered(houseName, deviceName)) return "NOD";
        if (!domainHandler.isUserAllowed(houseName, deviceName)) return "NOPERM";
        File file = new File(BASE_DIR + houseName + "/" + deviceName + ".csv");
        if (!file.exists()) return "NODATA"; 

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
    
    /**
     * Atualiza o último estado conhecido de um dispositivo no ficheiro last_states.csv.
     * @param house o nome da casa
     * @param device o nome do dispositivo
     * @param value o último valor enviado ao dispositivo
     * @throws IOException se a leitura ou escrita do ficheiro falhar
     */
    private static void updateLastStates(String house, String device, String value) throws IOException {
        File file = new File(BASE_DIR + "last_states.csv");
        file.getParentFile().mkdirs();

        java.util.List<String> lines = new java.util.ArrayList<>();
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) lines.add(line);
            }
        }

        String prefix = house + "," + device + ",";
        boolean found = false;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).startsWith(prefix)) {
                lines.set(i, prefix + value);
                found = true;
                break;
            }
        }
        if (!found) lines.add(prefix + value);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            for (String line : lines) {
                bw.write(line);
                bw.newLine();
            }
        }
    }
}

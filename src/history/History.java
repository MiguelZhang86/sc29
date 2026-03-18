package history;
import java.io.*;
import java.time.LocalDateTime;

import domain.DomainHandler;

public class History implements IHistory {

    private static final String BASE_DIR = "logs/";

    @Override
    public void registerCommand(String entry) {

        try{
            String[] parts =entry.split(",");
            String house = parts[0];
            String device = parts[1];
            String value = parts[2];

            File dir = new File(BASE_DIR + house);
            dir.mkdirs();

            File file = new File(dir, device + ".csv");

            BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
            bw.write(LocalDateTime.now() + "," + value);
            bw.newLine();
            bw.close();

        } catch (IOException e){
            e.printStackTrace();
        }
        
    }
    // RT<hm>Receber a informação sobre o último comando
    // (estados/temporizações) enviado a cada dispositivo da casa <hm>, desde
    // que o utilizador tenha permissões.
    @Override
    public String getLastCommand(String houseName) {
        if(!DomainHandler.getInstance().isUserAllowed(houseName, "")) {
            throw new IllegalArgumentException("User is not allowed to access all sections in the house");
        }
        
        
        File houseDir = new File(BASE_DIR + houseName);
        if(!houseDir.exists())return null;

        StringBuilder res = new StringBuilder();

        File[] files = houseDir.listFiles();
        if(files==null) return null;
        for(File f:files){

            try(BufferedReader br = new BufferedReader(new FileReader(f))){
                String line;
                String last=null;

                while((line = br.readLine()) != null) last = line;
                if(last!= null) res.append(f.getName()).append(": ").append(last).append("\n");

            } catch (IOException e){
                e.printStackTrace();
            }
        }
        return res.toString();
    }

    @Override
    public String getHistory(String houseName, String deviceName) {
        if (!DomainHandler.getInstance().isUserAllowed(houseName, deviceName)) {
            return "User is not allowed to access this section";
        }
        File file = new File(BASE_DIR + houseName + "/" + deviceName + ".csv");

        if(file.exists()) return file.getPath();
        return null;
    }
    
}

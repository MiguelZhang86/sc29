package serialization;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author: Jaime Sousa
 * A ideia desta classe é representar as mensagens enviadas entre cliente e servidor
 * A minha ideia é criar um construtor privado para prevenir estados incorretos de
 * utilização e metodos estáticos para criar mensagens específicas, correspondentes
 * aos comandos que o cliente pode enviar ao servidor + autenticação.
 * 
 * Nota: o servidor deve manter na sua conneccao com o utilizador que utilizador é
 */
public class Message{
    private String command;
    private String[] data;
    
    private Message(String command, String[] data) {
        this.command = command;
        this.data = data;
    }

    //MENSAGENS CLIENTE
    
    //Pedido de authenticação - AUTH <username> <password>
    public static Message auth(String username, String password) {
        return new Message("AUTH", new String[]{username, password});
    }

    //CREATE <hm> # Criar casa <hm> - utilizador é Owner
    public static Message createHouse(String name){
        return new Message("CREATE", new String[]{name});
    }

    //RD <hm> <s> # Registar um Dispositivo na casa <hm>, na seção <s>
    public static Message registerDevice(String houseName, String sectionName){
        return new Message("RD", new String[]{houseName, sectionName});
    }

    // EC <hm> <d> <int> # Enviar valor <int> de estado/temporização, do
    // dispositivo <d> da casa <hm>, para o servidor.
    public static Message sendStatus(String houseName, String deviceName, int value){
        return new Message("EC", new String[]{houseName, deviceName, Integer.toString(value)});
    }
    // RT <hm>#
    // Receber a informação sobre o último comando
    // (estados/temporizações) enviado a cada dispositivo da casa <hm>, desde
    // que o utilizador tenha permissões.
    public static Message receiveStatus(String houseName){
        
        return new Message("RT", new String[]{houseName});
    }
    // RH <hm> <d># Receber o Histórico (ficheiro de log .csv) de comandos
    // enviados ao dispositivo <d> da casa <hm>, desde que o utilizador tenha
    // permissões.
    public static Message receiveHistory(String houseName, String deviceName){
        return new Message("RH", new String[]{houseName, deviceName});
    }

    public static Message logout(){
        return new Message("EXIT", new String[]{});
    }



    //RESPOSTAS DO SERVIDOR

    public static Message authSuccess(){
        return new Message("AUTH_SUCCESS", new String[]{});
    }

    public static Message authFailure(){
        return new Message("AUTH_FAILURE", new String[]{});
    }

    //resposta aos commandos Create, RD e EC
    public static Message commandSuccess(){
        return new Message("COMMAND_SUCCESS", new String[]{});
    }

    public static Message commandFailure(String error){
        return new Message("COMMAND_FAILURE", new String[]{error});
    }

    //respostas de enviar data
    public static Message sendStatus(String[] command){
        String[] copy = Arrays.copyOf(command, command.length);
        return new Message("RT_SUCCESS", copy);
    }

    public static Message sendHistory(String csvFileName){
        return new Message("RH_SUCCESS", new String[]{csvFileName});
    }

    // AUX FUN FOR MIGUEL only to be used in RH_SUCCESS
    public File toCSV(String pathname) {
        File csvFile = new File(pathname);

        if(!this.command.equals("RH_SUCCESS")){
            throw new IllegalStateException("Only RH_SUCCESS messages can be converted to CSV.");
        }

        try (FileWriter writer = new FileWriter(csvFile, true)) { // true = append
            
            writer.write("Command,Arg1,Arg2,Arg3\n"); // Cabeçalho do CSV, so tem no maximo 3 args
            for (String entry : data) {
                //if entry = RH or RT or other command
                //writer.write(entry + "\n");
                //else:
                writer.write(entry + "\n"); // Escreve cada entrada em uma nova linha
            }
            
        } catch (IOException e) {
            e.printStackTrace();
    }

        return csvFile;
    }

    public static void serialize(DataOutputStream out, Message message) {
        // TODO
        // Implementar a serialização da mensagem para um formato adequado (e.g., JSON, XML, etc.)
        // Este método deve converter a mensagem em um formato que possa ser enviado pela rede
        // e posteriormente desserializado pelo destinatário.
    }

    public static Message deserialize(DataInputStream dataStream) {
        return null;
        // TODO
        // Implementar a desserialização da mensagem a partir do formato utilizado na serialização
        // Este método deve converter os dados recebidos de volta para um objeto Message.

}
}
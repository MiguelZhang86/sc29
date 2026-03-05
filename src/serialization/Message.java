/**
 * @author: Jaime Sousa
 * A ideia desta classe é representar as mensagens enviadas entre cliente e servidor
 * A minha ideia é criar um construtor privado para prevenir estados incorretos de
 * utilização e metodos estáticos para criar mensagens específicas, correspondentes
 * aos comandos que o cliente pode enviar ao servidor + autenticação.
 */
public class Message{
    private String command;
    private String[] data;
    
    private Message(String command, String[] data) {
        this.command = command;
        this.data = data;
    }

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
    // RT
    // <hm>#
    // Receber
    // a
    // informação
    // sobre
    // o
    // último
    // comando
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
    public static Message sendStatus(String[] data){
        return new Message("RT_SUCCESS", data);
    }

    public static Message sendHistory(String[] data){
        return new Message("RH_SUCCESS", data);
    }

}
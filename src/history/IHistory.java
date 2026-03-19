package history;
import domain.DomainHandler;

public interface IHistory {


    void registerCommand(String entry);


    //// RT<hm>Receber a informação sobre o último comando
    // (estados/temporizações) enviado a cada dispositivo da casa <hm>, desde
    // que o utilizador tenha permissões.
    String getLastCommand(DomainHandler domainHandler, String houseName);

    //RH <hm> <d> - Receber o Histórico (PATHNAME do ficheiro de log .csv) de comandos
    // enviados ao dispositivo <d> da casa <hm>, desde que o utilizador tenha
    // permissões.
    String getHistory(DomainHandler domainHandler, String houseName, String deviceName);
}
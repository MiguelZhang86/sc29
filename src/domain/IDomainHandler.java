package domain;

public interface IDomainHandler {
    
    //AUTH <username> <password>
    boolean authenticateUser(String username, String password);
    
    //CREATE <hm> - O utilizador é owner
    void createHouse(String name);

    //RD <hm> <s> # Registar um Dispositivo na casa <hm>, na seção <s>
    void registerDevice(String houseName, String sectionName);

    // EC <hm> <d> <int> # Utilizador logado tenta mudar valor do dispositivo
    void addDeviceTime(String houseName, String deviceName, int value);

    
    //EXIT
    void logout();


    //Bonus para o log, diz se o user autenticado tem permissão para aceder a uma seção da casa
    boolean isUserAllowed(String houseName, String sectionName);



}
    
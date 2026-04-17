package domain;

public interface IDomainHandler {
    
    //AUTH <username> <password>
    AuthEnum authenticateUser(String username, String password);
    
    //CREATE <hm> - O utilizador é owner
    void createHouse(String name);

    //Verifica se a casa foi criada com sucesso ou não
    boolean isHouseCreated(String name);

    //RD <hm> <s> # Registar um Dispositivo na casa <hm>, na seção <s>
    String registerDevice(String houseName, String sectionName);

    // EC <hm> <d> <int> # Utilizador logado tenta mudar valor do dispositivo
    String addDeviceTime(String houseName, String deviceName, int value);

    // ADD <user1> <hm> <s> # Autorizar utilizador <user1> à casa <hm>, seção <s>.
    String allowUser(String userName, String houseName, String sectionName);

    //Bonus
    boolean isAnyoneAuthenticated();

    //EXIT
    void logout();


    //Bonus para o log, diz se o user autenticado tem permissão para aceder a uma seção da casa
    boolean isUserAllowed(String houseName, String sectionName);






}
    
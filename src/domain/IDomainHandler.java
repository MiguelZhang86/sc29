package domain;

/**
 * Interface que define as operações disponíveis numa sessão de cliente conectado.
 * As implementações são responsáveis por verificar a autenticação e delegar
 * ao DataManager.
 */
public interface IDomainHandler {

    /** Autentica o utilizador com as credenciais fornecidas. */
    AuthEnum authenticateUser(String username, String password);

    /** Cria uma nova casa cujo dono é o utilizador autenticado. */
    void createHouse(String name);

    /** Retorna true se uma casa com o nome indicado existir. */
    boolean isHouseCreated(String name);

    /** Regista um novo dispositivo na seção indicada da casa indicada. */
    String registerDevice(String houseName, String sectionName);

    /** Envia um valor para um dispositivo (0=desligar, 1=ligar, 2-600=ligar por x minutos). */
    String addDeviceTime(String houseName, String deviceName, int value);

    /** Concede permissão a um utilizador para gerir uma seção (ou todas) de uma casa. */
    String allowUser(String userName, String houseName, String sectionName);

    /** Retorna true se algum utilizador estiver autenticado nesta sessão. */
    boolean isAnyoneAuthenticated();

    /** Termina a sessão do utilizador autenticado. */
    void logout();

    /** Retorna true se o utilizador autenticado puder aceder à seção ou dispositivo indicados. */
    boolean isUserAllowed(String houseName, String sectionName);

    /** Retorna true se o dispositivo indicado estiver registado na casa indicada. */
    boolean isDeviceRegistered(String houseName, String deviceName);
}
    
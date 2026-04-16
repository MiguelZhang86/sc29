/**
 * @author Jaime Sousa
 * Esta classe é responsável por lidar com as operações do dominio, chamadas pelos ServerThreads
 * Cada ServerThread representa um clinete, e tem uma instância de DomainHandler para lidar com as operações desse cliente
 * O DomainHandler tem uma referência para o DataManager, que é onde estão guardados os dados do sistema
 */

package domain;

public class DomainHandler implements IDomainHandler {
    private User authenticatedUser;
    private DataManager dm;

    public DomainHandler() {
        this.authenticatedUser = null;
        this.dm = DataManager.getInstance();
    }

    /**
     * Autentica o utilizador e guarda a sessão se bem-sucedido.
     * @param username o nome do utilizador
     * @param password a password
     * @return o resultado da autenticação
     */
    public AuthEnum authenticateUser(String username, String password) {
        AuthResult authentication = dm.authenticateUser(username, password);
        if (authentication.getAuthEnum() == AuthEnum.WRONG_PWD) {
            return AuthEnum.WRONG_PWD;
        } else if (authentication.getAuthEnum() == AuthEnum.OK_NEW_USER) {
            this.authenticatedUser = authentication.getUser();
            return AuthEnum.OK_NEW_USER;
        } else {
            this.authenticatedUser = authentication.getUser();
            return AuthEnum.OK_USER;
        }

    }

    /**
     * Retorna true se o utilizador autenticado puder aceder à seção ou dispositivo indicados.
     * @param houseName o nome da casa
     * @param sectionName o identificador da seção ou nome do dispositivo
     * @return true se tiver permissão, false caso contrário
     */
    public boolean isUserAllowed(String houseName, String sectionName) {
        isAnyoneAuthenticated(); //throws if no user is authenticated
        return dm.isUserAllowed(houseName, sectionName, this.authenticatedUser);
    }

    /** Termina a sessão do utilizador atual. */
    public void logout() {
        authenticatedUser = null;
    }

    @Override
    /**
     * Cria uma casa cujo dono é o utilizador autenticado.
     * @param name o nome da casa
     */
    public void createHouse(String name) {
        isAnyoneAuthenticated(); //throws if no user is authenticated
        dm.createHouse(name, this.authenticatedUser);
    }

    @Override
    /**
     * Retorna true se uma casa com o nome indicado existir.
     * @param name o nome da casa
     * @return true se a casa existir, false caso contrário
     */
    public boolean isHouseCreated(String name){
        return dm.isHouseCreated(name);
    }

    @Override
    /**
     * Regista um dispositivo na seção indicada em nome do utilizador autenticado.
     * @param houseName o nome da casa
     * @param sectionName o identificador da seção (primeira letra)
     * @return OK, NOHM ou NOPERM
     */
    public String registerDevice(String houseName, String sectionName) {
        isAnyoneAuthenticated(); //throws if no user is authenticated
        return dm.registerDevice(houseName, sectionName, this.authenticatedUser);
    }

    @Override
    /**
     * Envia um valor para um dispositivo em nome do utilizador autenticado.
     * @param houseName o nome da casa
     * @param deviceName o nome do dispositivo
     * @param value 0 (desligar), 1 (ligar), ou 2-600 (ligar por x minutos)
     * @return OK, NOHM, NOD ou NOPERM
     */
    public String addDeviceTime(String houseName, String deviceName, int value) {
        isAnyoneAuthenticated(); //throws if no user is authenticated
        return dm.addDeviceTime(houseName, deviceName, value, this.authenticatedUser);
    }
    /**
     * Retorna true se o utilizador autenticado tiver permissão em todas as seções da casa.
     * @param houseName o nome da casa
     * @return true se tiver permissão em todas as seções ou for o dono
     */
    public boolean isUserAllowedInAllSections(String houseName) {
        isAnyoneAuthenticated(); //throws if no user is authenticated
        return dm.isUserAllowedInAllSections(houseName, this.authenticatedUser);
    }

    /**
     * Retorna true se algum utilizador estiver autenticado nesta sessão.
     * @return true se houver um utilizador autenticado, false caso contrário
     */
    public boolean isAnyoneAuthenticated() {
        return this.authenticatedUser != null;
    }

    @Override
    /**
     * Concede permissão a um utilizador para aceder a uma seção de uma casa.
     * @param userName o utilizador a quem conceder acesso
     * @param houseName o nome da casa
     * @param sectionName o identificador da seção ou "all"
     * @return OK, NOHM, NOPERM ou NOUSER
     */
    public String allowUser(String userName, String houseName, String sectionName) {
        isAnyoneAuthenticated(); //throws if no user is authenticated
        return dm.allowUser(this.authenticatedUser, userName, houseName, sectionName);
    }

    @Override
    /**
     * Retorna true se o dispositivo indicado estiver registado na casa indicada.
     * @param houseName o nome da casa
     * @param deviceName o nome do dispositivo
     * @return true se o dispositivo existir, false caso contrário
     */
    public boolean isDeviceRegistered(String houseName, String deviceName) {
      return dm.isDeviceRegistered(houseName, deviceName);
    }
}
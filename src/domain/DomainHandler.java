/**
 * @author Jaime Sousa
 */

package domain;

public class DomainHandler implements IDomainHandler {


    private User authenticatedUser;
    private DataManager dm;
 
    public DomainHandler() {
        this.authenticatedUser = null;
        this.dm = DataManager.getInstance();
    }

    public AuthEnum authenticateUser(String username, String password) {
        AuthResult authentication = dm.authenticateUser(username, password);
        if(authentication.getAuthEnum() == AuthEnum.WRONG_PWD){
            return AuthEnum.WRONG_PWD;
        }
        else if(authentication.getAuthEnum() == AuthEnum.OK_NEW_USER){
            this.authenticatedUser = authentication.getUser();
            return AuthEnum.OK_NEW_USER;
        }
        else{
            this.authenticatedUser = authentication.getUser();
            return AuthEnum.OK_USER;
        }
       
    }

    public boolean isUserAllowed(String houseName, String sectionName) {
        isAnyoneAuthenticated(); //throws if no user is authenticated
        return dm.isUserAllowed(houseName, sectionName, this.authenticatedUser);
    }

    //Log out the current user - exit
    public void logout() {
        authenticatedUser = null;
    }

    @Override
    public void createHouse(String name) {
        isAnyoneAuthenticated(); //throws if no user is authenticated
        dm.createHouse(name, this.authenticatedUser);
    }

    @Override
    public void registerDevice(String houseName, String sectionName) {
        isAnyoneAuthenticated(); //throws if no user is authenticated
        dm.registerDevice(houseName, sectionName, this.authenticatedUser); 
    }

    @Override
    public void addDeviceTime(String houseName, String deviceName, int value) {
        isAnyoneAuthenticated(); //throws if no user is authenticated
        dm.addDeviceTime(houseName, deviceName, value, this.authenticatedUser);
    }

    

    public boolean isUserAllowedInAllSections(String houseName){
        isAnyoneAuthenticated(); //throws if no user is authenticated
        return dm.isUserAllowedInAllSections(houseName, this.authenticatedUser);
    }

    public boolean isAnyoneAuthenticated() {
        if(authenticatedUser == null) {
            throw new IllegalStateException("No user is currently authenticated");
        }
        return authenticatedUser != null;
    }
    

    





}
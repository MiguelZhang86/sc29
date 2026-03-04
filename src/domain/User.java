/**
 * @author Jaime Sousa
 */
public class User {
    private String name;

    /**
     * Creates a user with a name.
     *
     * @param name the user name
     */
    public User(String name) {
        this.name = name;
    }

    /**
     * Gets the user name.
     *
     * @return the user name
     */
    public String getName() {
        return name;
    }
}
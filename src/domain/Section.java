/**
 * @author Jaime Sousa
 */
import java.util.ArrayList;
import java.util.List;

public class Section {
    private String name;
    private List<User> allowedUsers; //permissão de mudar

    /**
     * Represents a section (room) in a house with a name and a list of allowed users.
     *
     * @param name the section name
     * @param allowedUsers the list of allowed users
     */
    
    public Section(String name, List<User> allowedUsers) {
        this.name = name;
        this.allowedUsers = new ArrayList<>(allowedUsers);
    }

    public Section(String name) {
        this.name = name;
        this.allowedUsers = new ArrayList<>();
    }

    /**
     * Gets the section name.
     *
     * @return the section name
     */
    public String getName() {
        return name;
    }

    /**
     * Checks if a user is allowed in this section.
     *
     * @param user the user to validate
     * @return true if user is allowed, false otherwise
     */
    public boolean isUserAllowed(User user) {
        return allowedUsers.contains(user);
    }
}
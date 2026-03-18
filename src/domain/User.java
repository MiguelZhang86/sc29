/**
 * @author Jaime Sousa
 */
package domain;

public class User {
    private String name;
    private String pw;

    /**
     * Creates a user with a name.
     *
     * @param name the user name
     * @param pw the user password
     */
    User(String name, String pw) {
        this.name = name;
        this.pw = pw;
    }

    /**
     * Gets the user name.
     *
     * @return the user name
     */
    String getName() {
        return name;
    }
    /**
     * Checks whether the provided password matches the user's password.
     *
     * @param pw the password to check
     * @return true if the password matches, false otherwise
     */
    boolean isPassword(String pw) {
        return this.pw.equals(pw);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof User) {
            User u = (User) o;
            return this.name.equals(u.name);
        }
        return false;
    }

    boolean authenticate(String pw) {
        return this.pw.equals(pw);
    }


}
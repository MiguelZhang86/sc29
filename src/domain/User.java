/**
 * @author Jaime Sousa
 */
package domain;

import java.util.Objects;
import org.mindrot.jbcrypt.BCrypt;

public final class User {
    private final String name;
    private final String pwHash;

    User(String name, String pwHash) {
        this.name = Objects.requireNonNull(name, "name cannot be null")
                   .trim()
                   .toLowerCase();
        this.pwHash = Objects.requireNonNull(pwHash, "pw cannot be null");
    }

    String getName() {
        return name;
    }

    boolean authenticate(String password) {
        return BCrypt.checkpw(password, this.pwHash);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User u)) return false;
        return name.equals(u.name);
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    String toText(){
        return this.name + ":" + this.pwHash;
    }
}
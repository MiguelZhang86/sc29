package domain;

public final class AuthResult {
    private final User user;
    private final AuthEnum authEnum;

    public AuthResult(User user, AuthEnum authEnum) {
        if (authEnum == null) {
            throw new IllegalArgumentException("authEnum cannot be null");
        }
        if (authEnum == AuthEnum.WRONG_PWD && user != null) {
            throw new IllegalArgumentException("user must be null when auth result is WRONG_PWD");
        }
        if ((authEnum == AuthEnum.OK_NEW_USER || authEnum == AuthEnum.OK_USER) && user == null) {
            throw new IllegalArgumentException("user cannot be null when authentication succeeds");
        }
        this.user = user;
        this.authEnum = authEnum;
    }

    public User getUser() {
        return user;
    }

    public AuthEnum getAuthEnum() {
        return authEnum;
    }
}



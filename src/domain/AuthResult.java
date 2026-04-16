package domain;

/**
 * Resultado imutável de uma tentativa de autenticação, associando
 * um valor do enum ao utilizador autenticado (ou null em caso de falha).
 */
public final class AuthResult {
    private final User user;
    private final AuthEnum authEnum;

    /**
     * Cria um resultado de autenticação.
     * @param user o utilizador autenticado, ou null se a autenticação falhou
     * @param authEnum o resultado da autenticação
     * @throws IllegalArgumentException se a combinação utilizador/enum for inválida
     */
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

    /**
     * Retorna o utilizador autenticado.
     * @return o utilizador, ou null se a autenticação falhou
     */
    public User getUser() {
        return user;
    }

    /**
     * Retorna o resultado da autenticação.
     * @return o valor do AuthEnum
     */
    public AuthEnum getAuthEnum() {
        return authEnum;
    }
}



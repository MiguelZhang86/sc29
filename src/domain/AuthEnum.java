package domain;

/**
 * Representa os possíveis resultados de uma tentativa de autenticação.
 * WRONG_PWD: a password não corresponde a um utilizador existente.
 * OK_NEW_USER: o utilizador não existia e foi registado.
 * OK_USER: o utilizador existia e a password estava correta.
 */
public enum AuthEnum {
    WRONG_PWD,
    OK_NEW_USER,
    OK_USER
}
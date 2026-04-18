package server;

/***************************************************************************
*   Seguranca e Confiabilidade 2025/26
*   Cliente simples para comunicar com SpertaServer
 ***************************************************************************/

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URISyntaxException;
import java.util.Scanner;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class SpertaClient {

    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final int DEFAULT_PORT = 23456;

    public static void main(String[] args) {

        System.setProperty("javax.net.ssl.trustStore", "truststore.client");
        System.setProperty("javax.net.ssl.trustStorePassword", "Scream");
        System.setProperty("javax.net.ssl.trustStoreType", "PKCS12");

        String truststorePath = null;
        String truststorePwd = null;
        String keystorePath = null;
        String keystorePwd = null;
        String host = DEFAULT_HOST;
        int port = DEFAULT_PORT;
        String username = null;
        String password = null;

        if (args.length >= 1) {
                if(args[0].contains(":")){
                    String[] parts = args[0].split(":");
                    host = parts[0];
                    try{
                        port = Integer.parseInt(parts[1]);
                    } catch(NumberFormatException e){
                        System.err.println("Porto inserido inválido. Usa DEFAULT_PORT");
                    }
                    if (args.length == 7) { truststorePath = args[1]; truststorePwd = args[2]; keystorePath = args[3]; keystorePwd = args[4]; username = args[5]; password = args[6]; }
                } else{
                    try{
                        port = Integer.parseInt(args[0]);
                        if (args.length == 3) { username = args[1]; password = args[2]; }
                    } catch(NumberFormatException e){
                        host = args[0];
                            try {
                            port = Integer.parseInt(args[1]);
                            } catch (NumberFormatException e2) {
                                System.err.println("Porto inserido inválido. Usa DEFAULT_PORT");
                            }
                        }
                        if (args.length == 8) { truststorePath = args[2]; truststorePwd = args[3]; keystorePath = args[4]; keystorePwd = args[5]; username = args[6]; password = args[7]; }
                    }
        }
        if (args.length == 7) {
            truststorePath = args[1];
            truststorePwd = args[2];
            keystorePath = args[3];
            keystorePwd = args[4];
            username = args[5];
            password = args[6];
        }

        Scanner sc = new Scanner(System.in);
        new SpertaClient().chat(host, port, sc, username, password);
    }

    /**
     * Liga ao servidor, autentica o utilizador se as credenciais foram fornecidas,
     * e entra num ciclo de comandos até o utilizador pressionar Ctrl+C.
     * @param host o hostname ou IP do servidor
     * @param port a porta do servidor
     * @param sc o Scanner para leitura de input do utilizador
     * @param username o nome do utilizador, ou null se não fornecido na linha de comandos
     * @param password a password, ou null se não fornecida na linha de comandos
     */
    public void chat(String host, int port, Scanner sc, String username, String password) {
        SSLSocketFactory sf = (SSLSocketFactory) SSLSocketFactory.getDefault();
        try (SSLSocket socket = (SSLSocket) sf.createSocket(host, port);
            
             ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream())) {

            // --- ATESTAÇÃO ---
            long clientSize = getOwnSize();
            outStream.writeObject(Long.valueOf(clientSize));
            outStream.flush();

            Object attestResponse = inStream.readObject();
            if (attestResponse instanceof String attestMsg) {
                if (attestMsg.equals("ATTEST_OK")) {
                    System.out.println("ATTESTATION OK");
                } else {
                    System.out.println("ATTESTATION FAILED (modo permissivo): a continuar.");
                }
            } else {
                System.out.println("ATTESTATION FAILED (resposta invalida, modo permissivo): a continuar.");
            }
            // --- FIM ATESTAÇÃO ---

            // Caso especial: se o cliente fornecer username e password como argumentos, tenta autenticar logo no inicio
            if (username != null && password != null) {
                outStream.writeObject(username + " " + password);
                outStream.flush();
                Object authResponse = inStream.readObject();

                if (authResponse instanceof String textResponse) {
                    System.out.println("Resposta de autenticacao: " + textResponse);
                } else {
                    System.out.println("Resposta inesperada do servidor: " + authResponse);
                    return;
                }
            }

            System.out.println("Ligado ao servidor. Escreve mensagens e Enter para enviar. Ctrl+C para sair." + SpertaServer.menuDeOpcoes);

            while (true) {
                System.out.print("> ");
                String msg = sc.nextLine();

                outStream.writeObject(msg);
                outStream.flush();

                Object serverResponse = inStream.readObject();
                if (serverResponse instanceof String textResponse) {
                    System.out.println(textResponse);
                    if((msg.startsWith("RT") || msg.startsWith("RH")) && textResponse.startsWith("Resposta: OK,")){
                        String[] parts = textResponse.split(",", 3);
                        saveFile(msg, parts[2]);
                    }
                } else {
                    System.out.println("Resposta inesperada do servidor: " + serverResponse);
                    break;
                }
            }

        } catch (IOException e) {
            System.err.println("Erro de comunicacao: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("Tipo de resposta desconhecido: " + e.getMessage());
        }
    }

    /**
     * Guarda localmente o conteúdo do ficheiro recebido de um comando RT ou RH.
     * Ficheiros RT são guardados como {@code <hm>_last.csv}.
     * Ficheiros RH são guardados como {@code <hm>_<d>_history.csv}.
     * @param command o comando original (ex: "RT Casa1" ou "RH Casa1 M1")
     * @param content o conteúdo do ficheiro recebido do servidor
     */
    /**
     * Retorna o tamanho em bytes do JAR ou diretório de classes do cliente.
     * Retorna -1 se o tamanho não puder ser determinado.
     * Nota: apenas funciona corretamente quando executado a partir do JAR.
     */
    private long getOwnSize() {
        try {
            java.net.URL location = SpertaClient.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation();
            File self = new File(location.toURI());
            return self.length();
        } catch (URISyntaxException e) {
            System.err.println("Nao foi possivel determinar o tamanho do cliente: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Guarda localmente o conteúdo do ficheiro recebido de um comando RT ou RH.
     * Ficheiros RT são guardados como {@code <hm>_last.csv}.
     * Ficheiros RH são guardados como {@code <hm>_<d>_history.csv}.
     * @param command o comando original (ex: "RT Casa1" ou "RH Casa1 M1")
     * @param content o conteúdo do ficheiro recebido do servidor
     */
    private void saveFile(String command, String content){
        try {
            String[] args = command.split(" ");
            String filename;
            if(args[0].equals("RT")){
                filename = args[1] + "_last.csv";
            } else{
                filename = args[1] + "_" + args[2] + "_history.csv";
            }

            try(java.io.BufferedWriter bw = new java.io.BufferedWriter(new java.io.FileWriter(filename))){
                bw.write(content);
            }
            System.out.println("Ficheiro guardado: " + filename);
      } catch (IOException e) {
          System.err.println("Erro a guardar ficheiro: " + e.getMessage());
      }
    }
}

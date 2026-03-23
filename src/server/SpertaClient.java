package server;

/***************************************************************************
*   Seguranca e Confiabilidade 2025/26
*   Cliente simples para comunicar com SpertaServer
 ***************************************************************************/

 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.Socket;
 import java.util.Scanner;

 public class SpertaClient {

    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final int DEFAULT_PORT = 23456;

    public static void main(String[] args) {
        String host = DEFAULT_HOST;
        int port = DEFAULT_PORT;
        String username = null;
        String password = null;
        if (args.length >= 1) {
            host = args[0];
        }
        if (args.length >= 2) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Porta invalida, usando valor por defeito: " + DEFAULT_PORT);
                port = DEFAULT_PORT;
            }
        }
        if(args.length == 4) {
            username = args[2];
            password = args[3];
        }

        Scanner sc = new Scanner(System.in);
        new SpertaClient().chat(host, port, sc, username, password);
    }

    public void chat(String host, int port, Scanner sc, String username, String password) {
        try (Socket socket = new Socket(host, port);
             ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream())) {

            // Caso especial: se o cliente fornecer username e password como argumentos, tenta autenticar logo no inicio
            if(username != null && password != null) {
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

            System.out.println("Ligado ao servidor. Escreve mensagens e Enter para enviar. Ctrl+C para sair.");

            while (true) {
                System.out.print("> ");
                String msg = sc.nextLine();

                outStream.writeObject(msg);
                outStream.flush();

                Object serverResponse = inStream.readObject();
                if (serverResponse instanceof String textResponse) {
                    System.out.println(textResponse);
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
}

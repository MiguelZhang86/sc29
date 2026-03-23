package server;

/***************************************************************************
*   Seguranca e Confiabilidade 2025/26
*   Cliente da tp01 (ou da 00 nao me lembro)
*	Boa sorte Miguel :p
***************************************************************************/

import java.io.IOException;
import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import domain.IDomainHandler;
import domain.AuthEnum;
import domain.DomainHandler;

//Servidor SpertaServer

public class SpertaServer{

	public static String menuDeOpcoes = """
			Comandos disponiveis:
			- <username> <password>: autentica-se com o servidor (obrigatorio antes de usar outros comandos)
			- createHouse <houseName>: cria uma nova casa
			- registerDevice <houseName> <sectionName>: regista um novo dispositivo na secção indicada da casa indicada
			- addDeviceTime <houseName> <deviceName> <time>: adiciona tempo de uso ao dispositivo indicado da casa indicada
			""";

	public static void main(String[] args) {
		System.out.println("Servidor iniciado");
		SpertaServer server = new SpertaServer();
		server.startServer();
	}

	public void startServer (){
		try (ServerSocket sSoc = new ServerSocket(23456)) {
			while(true) {
				try {
					Socket inSoc = sSoc.accept();
					ServerThread newServerThread = new ServerThread(inSoc);
					newServerThread.start();
			    }
			    catch (IOException e) {
			        e.printStackTrace();
			    }
			}
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
	}


	//Threads utilizadas para comunicacao com os clientes
	class ServerThread extends Thread {

		private Socket socket = null;
		private IDomainHandler domainHandler;

		ServerThread(Socket inSoc) {
			this.socket = inSoc;
			this.domainHandler = new DomainHandler();
			System.out.println("Novo cliente");
		}
 
		public void run(){
			try (ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
				 ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream())) {
				while (true) {
					Object request;
					try {
						request = inStream.readObject();
					} catch (EOFException | SocketException e) {
						// Client closed the terminal (Ctrl+C) or disconnected.
						break;
					} catch (ClassNotFoundException e) {
						outStream.writeObject("ERRO: formato de mensagem invalido");
						outStream.flush();
						continue;
					}

					if (!(request instanceof String msg)) {
						outStream.writeObject("ERRO: apenas mensagens de texto sao suportadas");
						outStream.flush();
						continue;
					}

					String reply = "Resposta: " + processRequest(msg);
					outStream.writeObject(reply);
					outStream.flush();
				}

			} catch (SocketException e) {
				System.out.println("Cliente desligou-se.");
			} catch (IOException e) {
				System.err.println("Ligacao ao cliente terminada: " + e.getMessage());
			} finally {
				try {
					socket.close();
				} catch (IOException e) {
					System.err.println("Erro ao fechar socket do cliente: " + e.getMessage());
				}
			}
		}
	

			private String processRequest(String request) {
				String[] arguments = request.split(" ");

				if (arguments.length == 0) {
					return "ERRO: comando vazio";
				}

				//autenticação
				if(!this.domainHandler.isAnyoneAuthenticated()) {
					if(arguments.length != 2) {
						return "ERRO: deve autenticar-se primeiro com o comando '<username> <password>'";
					}else{
						return authenticate(arguments[0], arguments[1]);
					}
				}




				return request.toUpperCase()+ "eheheheh ronaldinho soccer";
				}

			private String authenticate(String username, String password) {

				AuthEnum authResult = this.domainHandler.authenticateUser(username, password);

				if (authResult == AuthEnum.WRONG_PWD) {
					return "ERRO: WRONG_ PWD: Credenciais invalidas";
				}
				if (authResult == AuthEnum.OK_NEW_USER) {
					return "OK_NEW_USER: Novo utilizador criado e autenticado com sucesso";
				}
				if(authResult == AuthEnum.OK_USER) {
					return "OK_USER: Utilizador autenticado com sucesso";
				}
				return "ERRO: resultado de autenticação inesperado";
			}
		}
}
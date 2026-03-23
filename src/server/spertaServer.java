package server;

/***************************************************************************
*   Seguranca e Confiabilidade 2025/26
*   Cliente da tp01 (ou da 00 nao me lembro)
*	Boa sorte Miguel :p
***************************************************************************/

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import domain.IDomainHandler;
import domain.AuthEnum;
import domain.DomainHandler;

//Servidor SpertaServer

public class SpertaServer{

	public static void main(String[] args) {
		System.out.println("servidor: main");
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
			System.out.println("thread do server para cada cliente");
		}
 
		public void run(){
			try (ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
				 ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream())) {
				while (true) {
					Object request;
					try {
						request = inStream.readObject();
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

					if ("quit".equalsIgnoreCase(msg.trim())) {
						outStream.writeObject("BYE");
						outStream.flush();
						break;
					}
					String reply = "Resposta: " + processRequest(msg);
					outStream.writeObject(reply);
					outStream.flush();
				}

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
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

				return authResult == AuthEnum.OK_USER || authResult == AuthEnum.OK_NEW_USER ? "Login bem-sucedido" : "ERRO: credenciais invalidas";
			}
		}
}
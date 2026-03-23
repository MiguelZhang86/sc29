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
import history.History;

//Servidor SpertaServer

public class SpertaServer{

	public static String menuDeOpcoes = """
			Comandos disponiveis:
			CREATE <hm> # Criar casa <hm> - utilizador é Owner 
			• ADD <user1> <hm> <s> # Adicionar utilizador <user1> à casa <hm>, seção 
			<s>.  
			• RD <hm> <s> # Registar um Dispositivo na casa <hm>, na seção <s> 
			• EC  <hm>  <d>  <int>  #  Enviar  valor  <int>  de  estado/temporização,  do 
			dispositivo <d> da casa <hm>, para o servidor.  
			• RT <hm># Receber a informação sobre o último comando 
			(estados/temporizações) enviado a cada dispositivo da casa <hm>, desde 
			que o utilizador tenha permissões. 
			• RH <hm> <d># Receber o Histórico (ficheiro de log .csv) de comandos 
			enviados ao dispositivo <d> da casa <hm>, desde que o utilizador tenha 
			permissões.
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
				//a partir daqui, o cliente já está autenticado

				//CREATE <hm> - O utilizador é owner
				if(arguments[0].equals("CREATE") && arguments.length == 2) {
					try {
						this.domainHandler.createHouse(arguments[1]);
						return "Casa '" + arguments[1] + "' criada com sucesso";
					} catch (IllegalStateException e) {
						return "ERRO: " + e.getMessage();
					}
				}

				//RD <hm> <s> # Registar um Dispositivo na casa <hm>, na seção <s>
				if(arguments[0].equals("RD") && arguments.length == 3) {
					try {
						this.domainHandler.registerDevice(arguments[1], arguments[2]);
						return "Dispositivo registado com sucesso na casa '" + arguments[1] + "', seção '" + arguments[2] + "'";
					} catch (IllegalStateException e) {
						return "ERRO: " + e.getMessage();
					}
				}

				// EC <hm> <d> <int> # Utilizador logado tenta mudar valor do dispositivo
				if(arguments[0].equals("EC") && arguments.length == 4) {
					try {
						int value = Integer.parseInt(arguments[3]);
						this.domainHandler.addDeviceTime(arguments[1], arguments[2], value);
						return "Valor " + value + " adicionado ao dispositivo '" + arguments[2] + "' da casa '" + arguments[1] + "'";
					} catch (NumberFormatException e) {
						return "ERRO: valor para dispositivo deve ser um inteiro";
					} catch (IllegalStateException e) {
						return "ERRO: " + e.getMessage();
					}
				}
				// RT<hm>Receber a informação sobre o último comando
				// (estados/temporizações) enviado a cada dispositivo da casa <hm>, desde
				// que o utilizador tenha permissões.
				if(arguments[0].equals("RT") && arguments.length == 2) {
					try {
						String lastCommands = History.getLastCommand((DomainHandler) this.domainHandler, arguments[1]);
						if(lastCommands == null) {
							return "Nenhum comando registado para a casa '" + arguments[1] + "'";
						}
						return lastCommands;
					} catch (IllegalStateException e) {
						return "ERRO: " + e.getMessage();
					}
				}

				// RH <hm> <d># Receber o Histórico (ficheiro de log .csv) de comandos
				// enviados ao dispositivo <d> da casa <hm>, desde que o utilizador tenha permissões.
				if(arguments[0].equals("RH") && arguments.length == 3) {
					try {
						String history = History.getHistory((DomainHandler) this.domainHandler, arguments[1], arguments[2]);
						if(history.startsWith("ERRO")) {
							return history;
						}
						if(history == null) {
							return "Nenhum comando registado para o dispositivo '" + arguments[2] + "' da casa '" + arguments[1] + "'";
						}
						return "Historico do dispositivo '" + arguments[2] + "' da casa '" + arguments[1] + "':\n" + history;
					} catch (IllegalStateException e) {
						return "ERRO: " + e.getMessage();
					}
				}	






				return SpertaServer.menuDeOpcoes; //não haja match com nenhum comando, retorna o menu de opções
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
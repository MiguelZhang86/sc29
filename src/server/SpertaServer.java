package server;

/***************************************************************************
*   Seguranca e Confiabilidade 2025/26
*   Cliente da tp01 (ou da 00 nao me lembro)
*	Boa sorte Miguel :p
***************************************************************************/

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
import history.Log;

//Servidor SpertaServer

public class SpertaServer {
    public static final int defaultPort = 22345;
    public static String menuDeOpcoes = """
			Comandos disponiveis:
			CREATE <hm> 
			# Criar casa <hm> - utilizador é Owner 
			ADD <user1> <hm> <s> 
			# Autorizar utilizador <user1> à casa <hm>, seção <s>.  
			RD <hm> <s> 
			# Registar um Dispositivo na casa <hm>, na seção <s> 
			EC  <hm>  <d>  <int>  
			#  Enviar  valor  <int>  de  estado/temporização,  do 
			dispositivo <d> da casa <hm>, para o servidor.  
			RT <hm>
			# Receber a informação sobre o último comando 
			(estados/temporizações) enviado a cada dispositivo da casa <hm>, desde 
			que o utilizador tenha permissões. 
			RH <hm> <d>
			# Receber o Histórico (ficheiro de log .csv) de comandos 
			enviados ao dispositivo <d> da casa <hm>, desde que o utilizador tenha 
			permissões.
			""";
	
    //Inicia o servidor no porto especificado por args[0], ou no porto defaultPort se args estiver vazio ou tiver um valor inválido
    public static void main(String[] args) {

        System.setProperty("javax.net.ssl.keyStore", "keystore.server");
        System.setProperty("javax.net.ssl.keyStorePassword", "Scream");
        System.setProperty("javax.net.ssl.keyStoreType", "PKCS12");

        int port = defaultPort;
        if (args.length >= 1) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Porta invalida, usando valor por defeito: " + defaultPort);
            }
        }
        System.out.println("Servidor iniciado na porta " + port);
        SpertaServer server = new SpertaServer();
        server.startServer(port);
    }

    /**
     * Escreve uma mensagem no log do servidor, caso este tenha sido criado com sucesso.
     * @param serverLog o log do servidor, pode ser null
     * @param message a mensagem a escrever
     */
    private void writeServerLog(Log serverLog, String message) {
        if (serverLog == null) {
            return;
        }
        try {
            serverLog.write(message);
        } catch (IOException e) {
            System.err.println("Erro a escrever no log principal: " + e.getMessage());
        }
    }

    /**
     * Inicia o servidor, aceitando clientes e criando uma thread para cada um
     * Cada thread tem uma instância de DomainHandler para lidar com as operações do cliente
     * O servidor também mantém um log principal onde regista eventos como conexões de clientes e erros
     * O DataManager é atestado no início para carregar os dados do sistema antes de aceitar clientes
     * @param port
     */
    public void startServer(int port) {
        // Iniciar o log do servidor
        Log serverLog = null;
        try {
            serverLog = new Log("logs/log_server.csv");
            writeServerLog(serverLog, "Servidor iniciado na porta " + port);
        } catch (IOException e) {
            System.err.println("Nao foi possivel criar logs/log_server.csv: " + e.getMessage());
        }

        // Atestar o DataManager para carregar os dados do sistema antes de aceitar clientes
        // Não implementado, espero pela teresa
        domain.DataManager.getInstance().load();

        //loop principal do servidor, aceita clientes e cria uma thread para cada um
        try (ServerSocket sSoc = new ServerSocket(port)) {
            while (true) {
                try {
                    Socket inSoc = sSoc.accept();
                    writeServerLog(serverLog, "Novo cliente: " + inSoc.getInetAddress().getHostAddress() + ":" + inSoc.getPort());
                    ServerThread newServerThread = new ServerThread(inSoc);
                    newServerThread.start();
                } catch (IOException e) {
                    writeServerLog(serverLog, "Erro no accept(): " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            writeServerLog(serverLog, "Falha ao abrir socket do servidor: " + e.getMessage());
            System.err.println(e.getMessage());
            System.exit(-1);
        } finally {
            if (serverLog != null) {
                try {
                    serverLog.close();
                } catch (IOException e) {
                    System.err.println("Erro ao fechar log principal: " + e.getMessage());
                }
            }
        }
    }


	/**
	 * Cada Thread representa um cliente, e tem uma instância de DomainHandler para lidar com as operações desse cliente
	 * O DomainHandler tem uma referência para o DataManager, que é onde estão guardados os dados do sistema
	 * O log de cada cliente é guardado num ficheiro separado, para facilitar a leitura e evitar conflitos de escrita
	 * O log do cliente regista as mensagens enviadas pelo cliente e as respostas do servidor, para facilitar a análise de cada cliente individualmente
	 */
    class ServerThread extends Thread {

        private Socket socket = null;
        private IDomainHandler domainHandler;

        ServerThread(Socket inSoc) {
            this.socket = inSoc;
            this.domainHandler = new DomainHandler();
            System.out.println("Novo cliente");
        }

        public void run() {
            try (ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream())) {
                Log clientLog = new Log("logs/log_client_" + socket.getPort() + ".csv");

                // --- ATESTAÇÃO ---
                long expectedSize = readExpectedClientSize();
                Object sizeObj = null;
                try {
                    sizeObj = inStream.readObject();
                } catch (ClassNotFoundException e) {
                    outStream.writeObject("ATTEST_FAILED");
                    outStream.flush();
                    System.err.println("ATESTACAO: formato de atestacao invalido; a continuar em modo permissivo.");
                }

                boolean attestOk = false;
                if (sizeObj instanceof Long receivedSize) {
                    if (expectedSize >= 0 && receivedSize.equals(expectedSize)) {
                        attestOk = true;
                    }
                }

                if (attestOk) {
                    outStream.writeObject("ATTEST_OK");
                    outStream.flush();
                } else {
                    outStream.writeObject("ATTEST_FAILED");
                    outStream.flush();
                    System.err.println("ATESTACAO: falhou (esperado=" + expectedSize + ", recebido=" + sizeObj + "); a continuar em modo permissivo.");
                }
                // --- FIM ATESTAÇÃO ---

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

                    String reply;
                    try {
                        reply = "Resposta: " + processRequest(msg);
                    } catch (RuntimeException e) {
                        String errorMessage = e.getMessage() == null ? "erro interno" : e.getMessage();
                        reply = "Resposta: ERRO: " + errorMessage;
                    }
                    clientLog.write("\nMessagem enviada: " + msg + " \n" + reply + "\n");
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
	
        /**
         * Interpreta e executa um comando recebido do cliente, verificando a autenticação
         * e retornando o código de resposta adequado.
         * @param request a string de comando recebida
         * @return a string de resposta a enviar ao cliente
         */
        private String processRequest(String request) {
            String[] arguments = request.split(" ");

            if (arguments.length == 0) {
                return "ERRO: comando vazio";
            }

            //autenticação
            if (!this.domainHandler.isAnyoneAuthenticated()) {
                if (arguments.length != 2) {
                    return "ERRO: deve autenticar-se primeiro com o comando '<username> <password>'";
                } else {
                    return authenticate(arguments[0], arguments[1]);
                }
            }
				//a partir daqui, o cliente já está autenticado

				//CREATE <hm> - O utilizador é owner
            if (arguments[0].equals("CREATE") && arguments.length == 2) {
                try {
                    this.domainHandler.createHouse(arguments[1]);
                    return this.domainHandler.isHouseCreated(arguments[1])? "OK" : "NOK";
                } catch (IllegalArgumentException e) {
                    return "NOK";
                }
            }

				//RD <hm> <s> # Registar um Dispositivo na casa <hm>, na seção <s>
            if (arguments[0].equals("RD") && arguments.length == 3) {
                try {
                    return this.domainHandler.registerDevice(arguments[1], arguments[2]);
                } catch (IllegalArgumentException e) {
                    return "ERRO: " + e.getMessage();
                }
            }

				// EC <hm> <d> <int> # Utilizador logado tenta mudar valor do dispositivo
            if (arguments[0].equals("EC") && arguments.length == 4) {
                try {
                    int value = Integer.parseInt(arguments[3]);
                    String m = this.domainHandler.addDeviceTime(arguments[1], arguments[2], value);
                    if(m.equals("OK")){
                        History.registerCommand(arguments[1] + "," + arguments[2] + "," + arguments[3]);
                    }
                    return m;
                } catch (NumberFormatException e) {
                    return "NOK";
                } catch (IllegalArgumentException e) {
                    return "NOK";
                } 
            }

				//ADD <user1> <hm> <s> # Autorizar utilizador <user1> à casa <hm>, seção <s>.
            if (arguments[0].equals("ADD") && arguments.length == 4) {
                try {
                    return this.domainHandler.allowUser(arguments[1], arguments[2], arguments[3]);
                } catch (IllegalArgumentException e) {
                    return "ERRO: " + e.getMessage();
                }
            }


				// RT<hm>Receber a informação sobre o último comando
				// (estados/temporizações) enviado a cada dispositivo da casa <hm>, desde
				// que o utilizador tenha permissões.
            if (arguments[0].equals("RT") && arguments.length == 2) {
                try {
                    String last = History.getLastCommand((DomainHandler) this.domainHandler, arguments[1]);                                                            
                    if (last.equals("NOHM") || last.equals("NODATA") || last.equals("NOPERM")) return last;                                                            
                    return "OK," + last.getBytes().length + "," + last;
                    
                } catch (IllegalArgumentException e) {
                    return "ERRO: " + e.getMessage();
                }
            }

				// RH <hm> <d># Receber o Histórico (ficheiro de log .csv) de comandos
				// enviados ao dispositivo <d> da casa <hm>, desde que o utilizador tenha permissões.
            if (arguments[0].equals("RH") && arguments.length == 3) {
                try {
                    String history = History.getHistory((DomainHandler) this.domainHandler, arguments[1], arguments[2]);
                    if (history==null) {
                        return "NODATA";
                    }
                    if(history.equals("NOPERM")){
                        return history;
                    }
                    if (history.equals("NOHM")){ 
                        return history; 
                    }
                    if (history.equals("NOD")) {
                        return history;
                    }
                    if (history.equals("NODATA")) return history;
                    //Falta determinar o tamanho do ficheiro
                    return "OK," + history.getBytes().length + "," + history;
                } catch (IllegalArgumentException e) {
                    return "ERRO: " + e.getMessage();
                }
            }

            return SpertaServer.menuDeOpcoes; //não haja match com nenhum comando, retorna o menu de opções
        }
		/**
		 * Metodo auxiliar para autenticar o cliente, usando o DomainHandler para verificar as credenciais e retornar uma resposta adequada
		 * @param username
		 * @param password
		 * @return String indicando o resultado da autenticação, que pode ser uma mensagem de sucesso ou um erro
		 */
        /**
         * Autentica o cliente com as credenciais fornecidas.
         * @param username o nome do utilizador
         * @param password a password
         * @return WRONG-PWD, OK-NEW-USER ou OK-USER
         */
        private String authenticate(String username, String password) {

            AuthEnum authResult = this.domainHandler.authenticateUser(username, password);

            if (authResult == AuthEnum.WRONG_PWD) {
                return "WRONG-PWD";
            }
            if (authResult == AuthEnum.OK_NEW_USER) {
                return "OK-NEW-USER";
            }
            if (authResult == AuthEnum.OK_USER) {
                return "OK-USER";
            }
            return "ERRO: resultado de autenticação inesperado";
        }

        /**
         * Lê o tamanho esperado do cliente a partir do ficheiro de referência SpertaClient.txt.
         * O ficheiro deve conter uma linha no formato: SpertaClient:{tamanho}
         * Retorna -1 se o ficheiro não existir, estiver mal formatado ou não puder ser lido.
         */
        private long readExpectedClientSize() {
            File ref = new File("SpertaClient.txt");
            if (!ref.exists()) {
                System.err.println("ATESTACAO: ficheiro SpertaClient.txt nao encontrado.");
                return -1;
            }
            try (BufferedReader br = new BufferedReader(new FileReader(ref))) {
                String line = br.readLine();
                if (line == null || !line.startsWith("SpertaClient:")) {
                    System.err.println("ATESTACAO: formato invalido em SpertaClient.txt");
                    return -1;
                }
                return Long.parseLong(line.substring("SpertaClient:".length()).trim());
            } catch (IOException | NumberFormatException e) {
                System.err.println("ATESTACAO: erro a ler SpertaClient.txt: " + e.getMessage());
                return -1;
            }
        }
    }
}
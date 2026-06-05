package fortune;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.ServerException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class Servidor {

	private int port = 7000;
	private ServerSocket serverSocket;
	private final List<String> frases;
	private Random rand;
	
	public Servidor() throws ServerException, IOException {
		// Cria o ServerSocket na porta especificada se estiver dispon�vel
		serverSocket = new ServerSocket(port);
		
		System.out.println("Servidor iniciado na porta " + port);
		
		frases = new ArrayList<>();
		rand = new Random();

		while (true) {
			// Aguarda uma conex�o na porta especificada e cria retorna o socket que ir� comunicar com o cliente
			Socket s = serverSocket.accept();
			String ip = s.getInetAddress().getHostAddress();
			System.out.println("Conectado com " + ip);

			// Cria um DataInputStream para o canal de entrada de dados do socket
			DataInputStream  in  = new DataInputStream(s.getInputStream());
			
			// Cria um DataOutputStream para o canal de sa�da de dados do socket
			DataOutputStream out = new DataOutputStream(s.getOutputStream());

			// Aguarda por algum dado e imprime a linha recebida quando recebe
			String str = "";
			String frase = "";
			out.writeUTF("Bem vindo ao Fortune Cookie");
			
			do {
				
				str = in.readUTF();
				
				switch (str) {
					case "GET-FORTUNE":
						if (frases.isEmpty()) {
                            out.writeUTF("Nenhum biscoito da sorte disponível ainda!");
                            break;
                        }
						int chance = rand.nextInt(frases.size());
						out.writeUTF(frases.get(chance));
						break;
					case "ADD-FORTUNE":
						out.writeUTF("Qual a frase?");
						frase = in.readUTF();
						frases.add(frase);
						out.writeUTF("Anotado!");
						break;
					case "UPD-FORTUNE":
						out.writeUTF("Qual a posição?");
					    frase = in.readUTF(); 
					    int index = Integer.parseInt(frase); // Converte p/ número

					    out.writeUTF("O que pôr no lugar?");
					    frase = in.readUTF();
					    
					    if(index >= 0 && index < frases.size()) {
					        frases.set(index, frase);
					        out.writeUTF("Anotado!");
					    } else {
					        out.writeUTF("Sem chance!");
					    }
						
						break;
					case "LST-FORTUNE":
						if(frases.isEmpty()) {
					        out.writeUTF("Nenhuma frase cadastrada.");
					        out.writeUTF("fim da lista");
					        break;
					    }
					    
					    out.writeUTF("Exibindo " + frases.size() + " frases:");
					    
					    for(int i = 0; i < frases.size(); i++) {
					        out.writeUTF("[" + i + "] " + frases.get(i));
					    }
					    
					    out.writeUTF("fim da lista"); 
					    break;
					case "tchau":
						out.writeUTF("Valeu amig�o, at� a pr�xima!");
						break;
					default:
						out.writeUTF("Sua mensagem foi recebida, mas eu n�o sei o que fazer");
						break;
				}
				System.out.println("O cliente "+ip+" solicitou: " + str);
			} while( !str.equals("tchau") );

			// Encerro o socket de comunica��o
			s.close();
			System.out.println("Desconectado de " + ip);
		}

		// Encerro o ServerSocket
		// serv.close();
	}

	public static void main(String[] args) {
		
		try {
			new Servidor();
		} catch (ServerException e) {
			System.out.println("A conex�o com o cliente foi resetada!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

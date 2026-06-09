package forca;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.ServerException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Servidor {

	private int port = 7020;
	private ServerSocket serverSocket;
	private final List<String> frases;
	private Random rand;
	
	public Servidor() throws ServerException, IOException {
		// Cria o ServerSocket na porta especificada se estiver dispon�vel
		serverSocket = new ServerSocket(port);
		
		System.out.println("Servidor iniciado na porta " + port);
		
		frases = new ArrayList<>();
		rand = new Random();
		
		frases.add("abacate");
		frases.add("ovo");
		frases.add("bolacha");
		frases.add("bife");

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
		
			int chance = rand.nextInt(frases.size());
			String palavra = frases.get(chance);
			ArrayList<Character> letrasUsadas = new ArrayList<>();
			String linhas = palavraSecreta(palavra, letrasUsadas);
			
			int tentativas = 3;
			
			out.writeUTF("Jogo da forca\nPressione qualquer tecla para comecar");
			str = in.readUTF();
			
			do {
				out.writeUTF(linhas + "\nvidas: " + tentativas);
				str = in.readUTF();
				char caractere = str.charAt(0);
				
				if (letrasUsadas.contains(caractere)) {
	                continue;
	            }
				
				letrasUsadas.add(caractere);
				
				linhas = palavraSecreta(palavra, letrasUsadas);
				
				if (palavra.indexOf(caractere) < 0) {
					tentativas--;
	            }
				
				if(tentativas <= 0) {
					out.writeUTF("voce perdeu, a resposta era: " + palavra);
					break;
				}
				
				if (jogadorGanhou(palavra, letrasUsadas)) {
					out.writeUTF("Parabens! Voce ganhou");
					break;
	            }
				
				//System.out.println("O cliente "+ip+" solicitou: " + str);
			} while(true);

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
	
	private static String palavraSecreta(String palavraSecreta, ArrayList<Character> letrasTentadas) {
        String mascara = "";
        
        for (int i = 0; i < palavraSecreta.length(); i++) {
            char letraAtual = palavraSecreta.charAt(i);
            
            if (letrasTentadas.contains(letraAtual)) {
                mascara = mascara.concat(letraAtual + " ");
            } else {
                mascara = mascara.concat("_ ");
            }
        }
        return mascara;
    }
	
	private static boolean jogadorGanhou(String palavraSecreta, ArrayList<Character> letrasTentadas) {
        for (int i = 0; i < palavraSecreta.length(); i++) {
            if (!letrasTentadas.contains(palavraSecreta.charAt(i))) {
                return false;
            }
        }
        return true;
    }

}
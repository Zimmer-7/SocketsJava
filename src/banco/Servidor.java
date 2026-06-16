package banco;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.ServerException;

public class Servidor {

	private int port = 7000;
    private ServerSocket serverSocket;
    
    // O saldo precisa ser compartilhado ou gerenciado por conta (aqui simplificado como estático)
    // NOTA: Em um cenário real, precisaríamos de sincronização para evitar problemas de concorrência.
    private static int saldo = 0; 

    public Servidor() throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Servidor iniciado na porta " + port);

        while (true) {
            // Aguarda uma nova conexão
            Socket s = serverSocket.accept();
            
            // Cria uma nova Thread para cuidar DESTE cliente específico
            // Isso libera o loop principal para aceitar o próximo cliente imediatamente
            Thread clienteThread = new Thread(new ClientHandler(s));
            clienteThread.start();
        }
    }

    // Classe interna que implementa Runnable para rodar em paralelo
    private static class ClientHandler implements Runnable {
        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            String ip = socket.getInetAddress().getHostAddress();
            System.out.println("Conectado com " + ip);

            try (
                DataInputStream in = new DataInputStream(socket.getInputStream());
                DataOutputStream out = new DataOutputStream(socket.getOutputStream())
            ) {
                String str = "";
                String numero;
                int valor;

                out.writeUTF("O que deseja? \n1 - deposito \n2 - saque \n3 - saldo \n4 - sair");

                do {
                    str = in.readUTF();

                    switch (str) {
                        case "1":
                            out.writeUTF("Qual o valor?");
                            numero = in.readUTF();
                            valor = Integer.parseInt(numero);
                            
                            // Bloco sincronizado para evitar que dois clientes alterem o saldo ao mesmo tempo
                            synchronized (Servidor.class) {
                                saldo += valor;
                            }
                            out.writeUTF("Depositado");
                            break;
                            
                        case "2":
                            out.writeUTF("Qual o valor?");
                            numero = in.readUTF();
                            valor = Integer.parseInt(numero);
                            
                            synchronized (Servidor.class) {
                                if (saldo - valor < 0) {
                                    out.writeUTF("Saldo insuficiente");
                                    break;
                                }
                                saldo -= valor;
                            }
                            out.writeUTF("Retirado!");
                            break;
                            
                        case "3":
                            out.writeUTF("Saldo: " + saldo);
                            break;
                            
                        case "4":
                            out.writeUTF("Valeu amigão, até a próxima!");
                            str = "tchau"; // Força a saída do loop
                            break;
                            
                        default:
                            out.writeUTF("Sua mensagem foi recebida, mas eu não sei o que fazer");
                            break;
                    }
                    System.out.println("O cliente " + ip + " solicitou: " + str);
                } while (!str.equals("tchau"));

            } catch (IOException e) {
                System.out.println("Erro na comunicação com o cliente " + ip + ": " + e.getMessage());
            } finally {
                try {
                    socket.close();
                    System.out.println("Desconectado de " + ip);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            new Servidor();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
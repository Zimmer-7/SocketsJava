package loja;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor {
    private static final int PORTA = 6000;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORTA)) {
            System.out.println("=== SISTEMA CENTRAL DE LOJAS INICIADO ===");
            System.out.println("Aguardando conexões das filiais na porta " + PORTA + "...");

            while (true) {
                Socket filialSocket = serverSocket.accept();
                System.out.println("Nova filial conectada: " + filialSocket.getRemoteSocketAddress());
                new Thread(new ThreadFilial(filialSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Erro no Servidor Central: " + e.getMessage());
        }
    }
}

class ThreadFilial implements Runnable {
    private Socket socket;

    public ThreadFilial(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String linha;
            while ((linha = entrada.readLine()) != null) {
                System.out.println("[Central] " + linha);
            }
        } catch (IOException e) {
        } finally {
            try {
                socket.close();
                System.out.println("Conexão com uma filial encerrada.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
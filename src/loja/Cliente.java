package loja;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;

public class Cliente implements Runnable {
    private int idFilial;
    private static final String IP = "localhost";
    private static final int PORTA = 6000;

    public Cliente(int idFilial) {
        this.idFilial = idFilial;
    }

    @Override
    public void run() {
        try (Socket socket = new Socket(IP, PORTA);
             PrintWriter saida = new PrintWriter(socket.getOutputStream(), true)) {
            
            Random random = new Random();
            
            for (int i = 1; i <= 1500; i++) {
                String operacao = random.nextBoolean() ? "COMPRA" : "VENDA";
                double valor = 5.0 + (random.nextDouble() * 1000.0);
                
                String log = String.format("Filial %d -> Movimentação %d: %s no valor de R$ %.2f", 
                                            idFilial, i, operacao, valor);
                saida.println(log);
                
                // Delay de 5ms a 20ms 
                Thread.sleep(random.nextInt(15) + 5);
            }
            System.out.println(">>> Filial " + idFilial + " concluiu as 1500 movimentações!");
            
        } catch (Exception e) {
            System.err.println("Erro na Filial " + idFilial + ": " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        System.out.println("Iniciando simulação de rede com 5 filiais simultâneas...");
        for (int i = 1; i <= 5; i++) {
            new Thread(new Cliente(i)).start();
        }
    }
}
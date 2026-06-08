package operacoes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Cliente {

    public static void main(String[] args) {
       
        try (Socket socket = new Socket("127.0.0.1", 5000);
             PrintWriter saida = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Conectado com sucesso!");
            System.out.println("Digite os números e o operador na mesma linha (Ex: 10 20 30 +):");
            System.out.print("> ");
            
            String dados = scanner.nextLine();

            saida.println(dados);
            System.out.println("Dados enviados. Fechando fluxo de saída (Enviando EOF)...");

            socket.shutdownOutput(); 

            String resposta = entrada.readLine();
            
            System.out.println("\n[Resposta do Servidor]: " + resposta);

        } catch (IOException e) {
            System.err.println("Erro na comunicação com o servidor: " + e.getMessage());
        }
        
        System.out.println("Conexão encerrada pelo cliente.");
    }
}

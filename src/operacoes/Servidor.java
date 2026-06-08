package operacoes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Servidor {
    private static final int PORTA = 5000;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORTA)) {
            System.out.println("Servidor Java rodando e aguardando conexões na porta " + PORTA + "...");

            while (true) {
                System.out.println("\nAguardando próximo cliente...");
                
                try (Socket clientSocket = serverSocket.accept()) {
                    System.out.println("Cliente conectado: " + clientSocket.getRemoteSocketAddress());
                    
                    processarCliente(clientSocket);
                    
                    System.out.println("Cliente desconectado e conexão fechada.");
                } catch (IOException e) {
                    System.err.println("Erro ao lidar com o cliente: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao iniciar o servidor: " + e.getMessage());
        }
    }

    private static void processarCliente(Socket socket) throws IOException {
        BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter saida = new PrintWriter(socket.getOutputStream(), true);

        StringBuilder dadosAcumulados = new StringBuilder();
        String linha;

        while ((linha = entrada.readLine()) != null) {
            dadosAcumulados.append(linha).append(" ");
        }

        String dadosFinais = dadosAcumulados.toString().trim();
        if (dadosFinais.isEmpty()) {
            saida.println("Erro: Nenhum dado foi enviado.");
            return;
        }

        String[] tokens = dadosFinais.split("\\s+");
        List<Integer> numeros = new ArrayList<>();
        String operador = null;

        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            
            if (i == tokens.length - 1 && (token.equals("+") || token.equals("-") || token.equals("*") || token.equals("/"))) {
                operador = token;
            } else {
                try {
                    numeros.add(Integer.parseInt(token));
                } catch (NumberFormatException e) {
                    saida.println("Erro: Token inválido encontrado ('" + token + "'). Envie apenas números e termine com um operador.");
                    return;
                }
            }
        }

        if (operador == null || numeros.isEmpty()) {
            saida.println("Erro: Protocolo inválido. Envie a sequência de números terminando com um operador (+, -, *, /).");
            return;
        }

        long resultado = numeros.get(0);
        boolean erroDivisao = false;

        for (int i = 1; i < numeros.size(); i++) {
            int proximoNumero = numeros.get(i);
            switch (operador) {
                case "+": resultado += proximoNumero; break;
                case "-": resultado -= proximoNumero; break;
                case "*": resultado *= proximoNumero; break;
                case "/":
                    if (proximoNumero == 0) {
                        erroDivisao = true;
                    } else {
                        resultado /= proximoNumero;
                    }
                    break;
            }
        }

        if (erroDivisao) {
            saida.println("Erro: Divisão por zero.");
        } else {
            saida.println("Resultado da operacao (" + operador + "): " + resultado);
        }
    }
}
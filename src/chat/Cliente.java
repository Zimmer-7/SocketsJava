package chat;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Scanner;

public class Cliente {
    private static final String GRUPO_IP = "228.5.6.7";
    private static final int PORTA = 8000;
    private static boolean rodando = true;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== CHAT MULTICAST ===");
        System.out.print("Digite seu apelido para o Chat: ");
        String apelido = scanner.nextLine();

        if (apelido.trim().isEmpty()) {
            apelido = "Anônimo";
        }

        try {
            InetAddress group = InetAddress.getByName(GRUPO_IP);
            MulticastSocket socket = new MulticastSocket(PORTA);
            
            socket.joinGroup(group);
            
            System.out.println("=== Você entrou no chat multicast, " + apelido + "! ===");
            System.out.println("(Digite sua mensagem e aperte Enter. Para sair, digite 'sair')");

            // THREAD DE ESCUTA (Filtra para mostrar APENAS as mensagens dos outros)
            String apelidoFinal = apelido;
            Thread escutador = new Thread(() -> {
                byte[] buffer = new byte[1024];
                while (rodando) {
                    try {
                        DatagramPacket pacoteRecebido = new DatagramPacket(buffer, buffer.length);
                        socket.receive(pacoteRecebido); 
                        
                        String mensagemCompleta = new String(pacoteRecebido.getData(), 0, pacoteRecebido.getLength());
                        
                        // Se a mensagem NÃO começar com o seu apelido, ela é exibida
                        if (!mensagemCompleta.startsWith("[" + apelidoFinal + "]")) {
                            System.out.println(mensagemCompleta); 
                        }
                    } catch (IOException e) {
                        if (!rodando) break;
                    }
                }
            });
            escutador.start();

            // LOOP PRINCIPAL (Apenas envia para a rede, sem imprimir nada localmente)
            while (rodando) {
                String msg = scanner.nextLine();

                if (msg.equalsIgnoreCase("sair")) {
                    rodando = false;
                    String mensagemSaida = "👋 [" + apelido + "] saiu do chat.";
                    byte[] dadosSaida = mensagemSaida.getBytes(); // CORRIGIDO AQUI!
                    DatagramPacket pacoteSaida = new DatagramPacket(dadosSaida, dadosSaida.length, group, PORTA);
                    socket.send(pacoteSaida);
                    
                    socket.leaveGroup(group);
                    socket.close();
                    System.out.println("Você saiu do sistema de chat.");
                    break;
                }

                if (!msg.trim().isEmpty()) {
                    String mensagemFormatada = "[" + apelido + "]: " + msg;
                    
                    // Envia silenciosamente para o grupo
                    byte[] dados = mensagemFormatada.getBytes();
                    DatagramPacket pacoteEnvio = new DatagramPacket(dados, dados.length, group, PORTA);
                    socket.send(pacoteEnvio);
                }
            }

        } catch (IOException e) {
            System.err.println("Erro no chat: " + e.getMessage());
        }

        scanner.close();
        System.exit(0);
    }
}
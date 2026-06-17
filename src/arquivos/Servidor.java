package arquivos;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor {
    private static final int PORTA = 7000;
    private static final String PASTA_SERVIDOR = "SocketsJava/src/arquivos/arquivos_servidor/";

    // Usuário e senha padrão para simular o banco de dados de login
    private static final String USUARIO_CORRETO = "admin";
    private static final String SENHA_CORRETA = "1234";

    public static void main(String[] args) {
        File diretorio = new File(PASTA_SERVIDOR);
        if (!diretorio.exists()) {
            diretorio.mkdirs(); 
        }

        try (ServerSocket serverSocket = new ServerSocket(PORTA)) {
            System.out.println("=== SERVIDOR DE ARQUIVOS COM LOGIN INICIADO (Porta " + PORTA + ") ===");

            while (true) {
                Socket socket = serverSocket.accept();
                // Cada cliente que conecta ganha uma Thread para fazer login e usar o sistema
                new Thread(() -> processarCliente(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processarCliente(Socket socket) {
        try (DataInputStream entrada = new DataInputStream(socket.getInputStream());
             DataOutputStream saida = new DataOutputStream(socket.getOutputStream())) {

            // --- ETAPA DE LOGIN ---
            System.out.println("Aguardando autenticação do cliente: " + socket.getRemoteSocketAddress());
            String usuarioEnviado = entrada.readUTF();
            String senhaEnviada = entrada.readUTF();

            if (usuarioEnviado.equals(USUARIO_CORRETO) && senhaEnviada.equals(SENHA_CORRETA)) {
                saida.writeBoolean(true); // Informa ao cliente que o login deu certo
                System.out.println("Usuário '" + usuarioEnviado + "' logado com sucesso!");
            } else {
                saida.writeBoolean(false); // Informa que o login falhou
                System.out.println("Tentativa de login inválida. Conexão rejeitada.");
                socket.close();
                return; // Encerra o atendimento aqui
            }

            // --- ETAPA DE UPLOAD / DOWNLOAD (Só chega aqui se logar) ---
            String acao = entrada.readUTF(); 
            String nomeArquivo = entrada.readUTF();
            File arquivo = new File(PASTA_SERVIDOR + nomeArquivo);

            if (acao.equalsIgnoreCase("UPLOAD")) {
                long tamanho = entrada.readLong();
                try (FileOutputStream fos = new FileOutputStream(arquivo)) {
                    byte[] buffer = new byte[4096];
                    int bytesLidos;
                    long totalLido = 0;
                    while (totalLido < tamanho && (bytesLidos = entrada.read(buffer, 0, (int)Math.min(buffer.length, tamanho - totalLido))) != -1) {
                        fos.write(buffer, 0, bytesLidos);
                        totalLido += bytesLidos;
                    }
                    fos.flush();
                }
                System.out.println("Upload concluído para o usuário: " + usuarioEnviado);
            } else if (acao.equalsIgnoreCase("DOWNLOAD")) {
                if (arquivo.exists()) {
                    saida.writeBoolean(true); 
                    saida.writeLong(arquivo.length());
                    try (FileInputStream fis = new FileInputStream(arquivo)) {
                        byte[] buffer = new byte[4096];
                        int bytesLidos;
                        while ((bytesLidos = fis.read(buffer)) != -1) {
                            saida.write(buffer, 0, bytesLidos);
                        }
                        saida.flush();
                    }
                    System.out.println("Download concluído para o usuário: " + usuarioEnviado);
                } else {
                    saida.writeBoolean(false); 
                }
            }
        } catch (IOException e) {
            System.err.println("Erro na sessão do cliente: " + e.getMessage());
        }
    }
}
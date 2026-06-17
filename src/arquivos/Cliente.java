package arquivos;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Cliente {
    private static final String IP = "localhost";
    private static final int PORTA = 7000;
    private static final String PASTA_EXERCICIO = "SocketsJava/src/arquivos/";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== SISTEMA DE ARQUIVOS (LOGIN) ===");
        System.out.print("Digite o Usuário: ");
        String usuario = scanner.nextLine();
        System.out.print("Digite a Senha: ");
        String senha = scanner.nextLine();

        try (Socket socket = new Socket(IP, PORTA);
             DataOutputStream saida = new DataOutputStream(socket.getOutputStream());
             DataInputStream entrada = new DataInputStream(socket.getInputStream())) {

            // Envia os dados de login para o servidor conferir
            saida.writeUTF(usuario);
            saida.writeUTF(senha);

            // Recebe a resposta do servidor (true se logou, false se errou)
            boolean loginSucesso = entrada.readBoolean();

            if (!loginSucesso) {
                System.out.println("❌ Erro: Usuário ou senha incorretos! Acesso negado.");
                scanner.close();
                return; // Para o programa aqui
            }

            System.out.println("✅ Login efetuado com sucesso!\n");
            
            // MENU PRINCIPAL (Só aparece se o login der certo)
            System.out.println("Escolha uma opção:\n1 - Fazer UPLOAD\n2 - Fazer DOWNLOAD");
            System.out.print("Opção: ");
            int opcao = scanner.nextInt();
            scanner.nextLine(); 

            System.out.print("Digite apenas o nome do arquivo (ex: teste.txt): ");
            String nomeArquivo = scanner.nextLine();
            
            File arquivo = new File(PASTA_EXERCICIO + nomeArquivo);

            if (opcao == 1) {
                if (!arquivo.exists()) {
                    System.out.println("Arquivo não encontrado em: " + arquivo.getAbsolutePath());
                    scanner.close();
                    return;
                }
                realizarUpload(socket, arquivo); // Passa o socket logado atual
            } else if (opcao == 2) {
                realizarDownload(socket, nomeArquivo); // Passa o socket logado atual
            }

        } catch (IOException e) {
            System.err.println("Erro de conexão: " + e.getMessage());
        }
        
        scanner.close();
    }

    private static void realizarUpload(Socket socket, File arquivo) {
        try (DataOutputStream saida = new DataOutputStream(socket.getOutputStream());
             FileInputStream fis = new FileInputStream(arquivo)) {

            saida.writeUTF("UPLOAD");
            saida.writeUTF(arquivo.getName());
            saida.writeLong(arquivo.length());

            byte[] buffer = new byte[4096];
            int bytesLidos;
            while ((bytesLidos = fis.read(buffer)) != -1) {
                saida.write(buffer, 0, bytesLidos);
            }
            saida.flush();
            System.out.println("Arquivo enviado com sucesso!");

        } catch (IOException e) {
            System.err.println("Erro no upload: " + e.getMessage());
        }
    }

    private static void realizarDownload(Socket socket, String nomeArquivo) {
        try (DataOutputStream saida = new DataOutputStream(socket.getOutputStream());
             DataInputStream entrada = new DataInputStream(socket.getInputStream())) {

            saida.writeUTF("DOWNLOAD");
            saida.writeUTF(nomeArquivo);

            boolean existe = entrada.readBoolean();
            if (existe) {
                long tamanho = entrada.readLong();
                File arquivoDestino = new File(PASTA_EXERCICIO + "baixado_" + nomeArquivo);
                
                try (FileOutputStream fos = new FileOutputStream(arquivoDestino)) {
                    byte[] buffer = new byte[4096];
                    int bytesLidos;
                    long totalLido = 0;
                    while (totalLido < tamanho && (bytesLidos = entrada.read(buffer, 0, (int)Math.min(buffer.length, tamanho - totalLido))) != -1) {
                        fos.write(buffer, 0, bytesLidos);
                        totalLido += bytesLidos;
                    }
                    fos.flush();
                }
                System.out.println("Arquivo baixado com sucesso como: " + arquivoDestino.getName());
            } else {
                System.out.println("O arquivo solicitado não existe no servidor.");
            }
        } catch (IOException e) {
            System.err.println("Erro no download: " + e.getMessage());
        }
    }
}
package chat;

import java.io.*; // Importa as classes necessárias para entrada e saída
import java.net.*; // Importa as classes necessárias para rede
import java.util.Scanner; // Importa a classe Scanner para leitura do input do usuário

public class Cliente {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java Cliente <client-id>"); // Verifica se o ID do cliente foi passado como argumento
            System.exit(1); // Sai se não houver ID
        }
        String clientId = args[0]; // Obtém o ID do cliente dos argumentos
        String serverAddress = System.getenv("SERVER_ADDRESS"); // Obtém o endereço do servidor das variáveis de ambiente
        String serverPortStr = System.getenv("SERVER_PORT"); // Obtém a porta do servidor das variáveis de ambiente

        if (serverAddress == null || serverPortStr == null) {
            System.err.println("Variáveis de ambiente SERVER_ADDRESS ou SERVER_PORT não definidas."); // Verifica se as variáveis de ambiente estão definidas
            System.exit(1); // Sai se as variáveis não estiverem definidas
        }

        int serverPort;
        try {
            serverPort = Integer.parseInt(serverPortStr); // Converte a porta do servidor para inteiro
        } catch (NumberFormatException e) {
            System.err.println("Porta do servidor inválida: " + serverPortStr); // Lida com porta inválida
            System.exit(1);
            return;
        }

        try (Socket socket = new Socket(serverAddress, serverPort); // Conecta ao servidor
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true); // Inicializa o PrintWriter para enviar dados
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Inicializa o BufferedReader para receber dados
             Scanner scanner = new Scanner(System.in)) { // Inicializa o scanner para ler a entrada do usuário

            out.println(clientId); // Envia o ID do cliente para o servidor

            // Thread para ouvir as mensagens do servidor
            Thread listenerThread = new Thread(() -> {
                try {
                    String input;
                    while ((input = in.readLine()) != null) { // Lê mensagens do servidor
                        System.out.println(input); // Imprime as mensagens recebidas
                    }
                } catch (IOException e) {
                    e.printStackTrace(); // Lida com exceções de I/O
                }
            });

            listenerThread.start(); // Inicia a thread que ouve as mensagens do servidor

            while (true) {
                System.out.print("Digite a mensagem (formato: destinatario:mensagem): ");
                String message = scanner.nextLine(); // Lê a mensagem do usuário
                out.println(message); // Envia a mensagem para o servidor
            }

        } catch (IOException e) {
            e.printStackTrace(); // Lida com exceções de I/O
        }
    }
}
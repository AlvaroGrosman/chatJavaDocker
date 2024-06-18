package chat;

import java.io.*; // Importa as classes necessárias para entrada e saída
import java.net.*; // Importa as classes necessárias para rede
import java.util.Map; // Importa a classe Map
import java.util.concurrent.ConcurrentHashMap; // Importa a classe ConcurrentHashMap

public class Servidor {
    private static final int PORT = 8080; // Define a porta em que o servidor vai ouvir

    private static Map<String, PrintWriter> clients = new ConcurrentHashMap<>(); // Mapa para armazenar os clientes conectados

    public static void main(String[] args) {
        System.out.println("Servidor iniciado...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) { // Cria o ServerSocket na porta especificada
            while (true) {
                new ClientHandler(serverSocket.accept()).start(); // Aceita novas conexões e cria uma nova thread para cada cliente
            }
        } catch (IOException e) {
            e.printStackTrace(); // Lida com exceções de I/O
        }
    }

    // Classe interna para tratar clientes
    private static class ClientHandler extends Thread {
        private Socket socket; // Socket para o cliente
        private String clientId; // ID do cliente
        private PrintWriter out; // Para enviar dados ao cliente
        private BufferedReader in; // Para receber dados do cliente

        public ClientHandler(Socket socket) {
            this.socket = socket; // Inicializa o socket do cliente
        }

        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true); // Inicializa o PrintWriter para enviar dados
                in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Inicializa o BufferedReader para receber dados

                clientId = in.readLine(); // Lê o ID do cliente
                clients.put(clientId, out); // Adiciona o cliente ao mapa
                System.out.println(clientId + " conectado.");

                String input;
                while ((input = in.readLine()) != null) { // Lê mensagens do cliente
                    // Divide a mensagem no formato "destinatário:mensagem"
                    String[] parts = input.split(":", 2); 
                    if (parts.length == 2) {
                        String recipientId = parts[0];
                        String message = parts[1];
                        PrintWriter recipientOut = clients.get(recipientId); // Obtém o PrintWriter do destinatário

                        if (recipientOut != null) {
                            recipientOut.println("Mensagem de " + clientId + ": " + message); // Envia a mensagem para o destinatário
                            System.out.println("Mensagem de " + clientId + " para " + recipientId + ": " + message); // Imprime a mensagem no servidor
                        } else {
                            out.println("Destinatário não encontrado: " + recipientId); // Informa ao remetente que o destinatário não foi encontrado
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace(); // Lida com exceções de I/O
            } finally {
                if (clientId != null) {
                    clients.remove(clientId); // Remove o cliente do mapa ao desconectar
                    System.out.println(clientId + " desconectado.");
                }
                try {
                    socket.close(); // Fecha o socket do cliente
                } catch (IOException e) {
                    e.printStackTrace(); // Lida com exceções ao fechar o socket
                }
            }
        }
    }
}
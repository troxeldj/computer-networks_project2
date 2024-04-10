package chat.chatapp;

import java.net.*;

public class Server {
  private ServerSocket serverSock;
  private int port;

  // Constructor
  // Arguments: ServerSocket serverSock, int port
  public Server(ServerSocket serverSock, int port) {
    this.serverSock = serverSock;
    this.port = port;
  }

  public void start() {
    try {
      while (!serverSock.isClosed()) {
        // Blocking call to accept clients connection
        Socket clientAccSock = serverSock.accept();
        System.out.println("New Client connected");

        // Create a clienthandler thread for each client
        ClientHandler clientHandler = new ClientHandler(clientAccSock);

        // Start the client handler thread
        Thread clientHandlerThread = new Thread(clientHandler);
        clientHandlerThread.start();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // Method to close the server socket
  public void closeServerSock() {
    try {
      if (serverSock != null)
        serverSock.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    try {
      int PORT = 6060;
      ServerSocket serverSock = new ServerSocket(PORT);
      Server server = new Server(serverSock, PORT);
      server.start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

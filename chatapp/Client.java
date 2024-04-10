package chat.chatapp;

import java.net.Socket;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Scanner;

public class Client {
  private Socket sock;
  private BufferedReader buffRead;
  private BufferedWriter buffWrite;
  private String username;

  // Constructor
  // Arguments: Socket sock
  public Client(Socket sock, String username) {
    try {
      this.sock = sock;
      this.buffWrite = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
      this.buffRead = new BufferedReader(new InputStreamReader(sock.getInputStream()));
      this.username = username;
    } catch (Exception e) {
      closeEverything(sock, buffRead, buffWrite);
      e.printStackTrace();
    }
  }

  public void sendMessage() {
    try {
      buffWrite.write(username);
      buffWrite.newLine();
      buffWrite.flush();

      Scanner sysInScanner = new Scanner(System.in);
      while (sock.isConnected()) {
        String messageToSend = sysInScanner.nextLine();
        // Handle 'message <message_id>' to retrieve message from server.
        if (isRetrieveMessage(messageToSend) && isValidRetrieveMessage(messageToSend)) {
          handleretrieveMessage(messageToSend);
          continue;
        } else if (isRetrieveMessage(messageToSend) && !isValidRetrieveMessage(messageToSend)) {
          System.out.println("Invalid command format. Please use 'message <message_id>'");
          continue;
        }

        // Send the message to the server.
        buffWrite.write(messageToSend);
        buffWrite.newLine();
        buffWrite.flush();

        // Handle exit message
        if (isExitMessage(messageToSend)) {
          handleExitMessage(messageToSend);
          continue;
        }

        // Handle message to retrieve message from server
        // Handle exit message
      }
    } catch (Exception e) {
      closeEverything(sock, buffRead, buffWrite);
    }
  }

  public void listenForMessages() {
    new Thread(new Runnable() {
      @Override
      public void run() {
        String messageFromChat;

        while (sock.isConnected()) {
          try {
            messageFromChat = buffRead.readLine();
            System.out.println(messageFromChat);
          } catch (Exception e) {
            closeEverything(sock, buffRead, buffWrite);
          }
        }
      }
    }).start();
  }

  private void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
    try {
      if (bufferedReader != null)
        bufferedReader.close();
      if (bufferedWriter != null)
        bufferedWriter.close();
      if (socket != null)
        socket.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // Function to check if the message is an exit message
  // Arguments: String message
  // Returns: boolean
  private boolean isExitMessage(String message) {
    return message.equals("exit");
  }

  // Function to handle exit message. If the message is "exit", close the socket
  // and exit the program.
  // Arguments: String message
  // Returns: void
  private void handleExitMessage(String message) {
    if (message.equals("exit")) {
      // Close getmessage thread
      closeEverything(sock, buffRead, buffWrite);
      System.exit(0);
    } else {
      return;
    }
  }

  // Function to handle retrieve message. If the message is "message
  // <message_id>",
  private boolean isRetrieveMessage(String message) {
    return message.split(" ")[0].equals("message");
  }

  // Function to check if the retrieve message is valid
  // Arguments: String message
  // Returns: boolean

  private boolean isValidRetrieveMessage(String message) {
    String[] splitCommand = message.split(" ");
    if (splitCommand.length != 2) {
      return false;
    }

    if (!isInteger(splitCommand[1])) {
      return false;
    }

    return true;
  }

  private void handleretrieveMessage(String message) {
    try {
      buffWrite.write(message);
      buffWrite.newLine();
      buffWrite.flush();
    } catch (Exception e) {
      closeEverything(sock, buffRead, buffWrite);
      e.printStackTrace();
    }
  }

  private boolean isInteger(String s) {
    try {
      Integer.parseInt(s);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  // Main function
  public static void main(String[] args) {

    int SERVER_PORT = 6060;
    // Get the username from the user
    Scanner sysInScanner = new Scanner(System.in);
    System.out.println("Enter your username: ");
    String username = sysInScanner.nextLine();

    try {
      Socket socket = new Socket("localhost", SERVER_PORT);
      Client newClient = new Client(socket, username);
      newClient.listenForMessages();
      newClient.sendMessage();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

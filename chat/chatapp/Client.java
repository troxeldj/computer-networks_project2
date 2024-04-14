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

  // Function to send message to the server. Runs while connection is not closed.
  public void sendMessage() {
    try {
      buffWrite.write(username);
      buffWrite.newLine();
      buffWrite.flush();

      Scanner sysInScanner = new Scanner(System.in);
      while (!sock.isClosed()) {
        String messageToSend = sysInScanner.nextLine();

        handleClientCommand(messageToSend);

        // Handle exit message
        if (isExitMessage(messageToSend)) {
          handleExitMessage(messageToSend);
          continue;
        }
      }
    } catch (Exception e) {
      closeEverything(sock, buffRead, buffWrite);
    }
  }

  // Function to handle client command. If the command is not a special command,
  // send the message to the server.
  // Arguments: String fullMessageString
  // Returns: void
  public void handleClientCommand(String fullMessageString) {
    String[] splitCommand = fullMessageString.split(" ");
    String command = splitCommand[0];
    int commandLength = splitCommand.length;

    switch (command) {

      // Handle '%groupusers <group_name>' to get list of users in group.
      case "%groupusers":
        if (commandLength != 2) {
          System.out.println("Invalid command format. Please use '%groupusers <group_name>'");
          break;
        }
        sendMessageToServer(fullMessageString);
        break;

      // Handle '%groupjoin <group_name>' to join a group.
      case "%groupjoin":
        if (commandLength != 2) {
          System.out.println("Invalid command format. Please use '%groupjoin <group_name>'");
          break;
        }
        sendMessageToServer(fullMessageString);
        break;

      // Handle '%groupleave' to leave a group.
      case "%groupleave":
        if (commandLength != 2) {
          System.out.println("Invalid command format. Please use '%groupleave'");
          break;
        }
        sendMessageToServer(fullMessageString);
        break;

      // Handle '%grouppost <message>' to post a message to a group.
      case "%grouppost":
        if (commandLength < 2) {
          System.out.println("Invalid command format. Please use '%grouppost <group_name> <message>'");
          break;
        }
        sendMessageToServer(fullMessageString);
        break;

      case "%groupmessage":
        // Handle '%message <group_name> <message_id>' to retrieve message from server.
        if (commandLength == 3 && isInteger(splitCommand[2])) {
          sendMessageToServer(fullMessageString);
        } else {
          System.out.println("Invalid command format. Please use '%groupmessage <group_name> <message_id>'");
        }
        break;

      // Handle '%groups' to get list of groups.
      case "%groups":
        if (commandLength != 1) {
          System.out.println("Invalid command format. Please use '%groups'");
          break;
        }
        sendMessageToServer(fullMessageString);
        break;

      // Handle '%mygroups' to get list of groups user is in.
      case "%mygroups":
        if (commandLength != 1) {
          System.out.println("Invalid command format. Please use '%mygroups'");
          break;
        }
        sendMessageToServer(fullMessageString);
        break;

      case "ping":
        if (commandLength != 1) {
          System.out.println("Invalid command format. Please use 'ping'");
          break;
        }
        sendMessageToServer(fullMessageString);
        break;

      // Handle '%help' to print help message.
      case "%help":
        if (commandLength != 1) {
          System.out.println("Invalid command format. Please use '%help'");
          break;
        }
        sendMessageToServer(fullMessageString);
        break;

      // Postpone handling of exit command. Server side needs to be completed first.
      case "%exit":
        if (commandLength != 1) {
          System.out.println("Invalid command format. Please use '%exit'");
          break;
        }
        sendMessageToServer(fullMessageString);
        break;

      case "%clear":
        if (commandLength != 1) {
          System.out.println("Invalid command format. Please use '%clear'");
          break;
        }
        System.out.print("\033[H\033[2J");
        System.out.flush();
        break;

      default:
        System.out.println("Invalid Command Provided. Use '%help' to get list of commands.");
    }
  }

  // Thread to listen for messages from the server. Runs while connection is not
  // closed.
  public void listenForMessages() {
    new Thread(new Runnable() {
      @Override
      public void run() {
        String messageFromChat;

        while (!sock.isClosed()) {
          try {
            messageFromChat = buffRead.readLine();
            if (messageFromChat == null) {
              throw new Exception("Connection closed.");
            }
            System.out.println(messageFromChat);
          } catch (Exception e) {
            closeEverything(sock, buffRead, buffWrite);
            Thread.currentThread().interrupt();
          }
        }
      }
    }).start();
  }

  // Function to close the socket and buffer reader/writer
  // Arguments: Socket socket, BufferedReader bufferedReader, BufferedWriter
  // Returns: void
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
    return message.equals("%exit");
  }

  // Function to handle exit message. If the message is "exit", close the socket.
  // and exit the program.
  // Arguments: String message
  // Returns: void
  private void handleExitMessage(String message) {
    // Close getmessage thread
    closeEverything(sock, buffRead, buffWrite);
    System.exit(0);
  }

  // Function to send message to server over socket.
  // Arguments: String message
  // Returns: void
  private void sendMessageToServer(String message) {
    try {
      buffWrite.write(message);
      buffWrite.newLine();
      buffWrite.flush();
    } catch (Exception e) {
      closeEverything(sock, buffRead, buffWrite);
    }
  }

  // Function to check if a string can be parsed as an integer
  // Arguments: String s
  // Returns: boolean
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
      System.out.println("Server connection aborted.");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

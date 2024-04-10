package chat.chatapp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import chat.chatapp.Message;

public class ClientHandler implements Runnable {

  // Static variables
  public static ArrayList<ClientHandler> clients = new ArrayList<>(); // List of clients
  public static ArrayList<chat.chatapp.Message> messageList = new ArrayList<>(); // List of messages (username, message)

  private Socket sock;
  private BufferedReader buffRead;
  private BufferedWriter buffWrite;
  private String clientUsername;

  // Constructor
  // Arguments: Socket sock
  public ClientHandler(Socket sock) {
    try {
      // Setup the socket and buffer reader/writer
      this.sock = sock;
      this.buffWrite = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
      this.buffRead = new BufferedReader(new InputStreamReader(sock.getInputStream()));

      // Get the client username from buffered reader
      this.clientUsername = buffRead.readLine();

      // Add the client to the clients list
      clients.add(this);

      // Send a message to every client that a new client has joined
      sendBroadcastMessage(clientUsername + " has joined the chat");
    } catch (Exception e) {
      closeEverything(sock, buffRead, buffWrite);
    }

  }

  // Function to send broadcast message to all clients.
  // Arguments: String message
  // Returns: void
  private void sendBroadcastMessage(String message) {
    for (ClientHandler client : clients) {
      try {
        if (!client.clientUsername.equals(clientUsername)) {
          client.buffWrite.write(message);
          client.buffWrite.newLine();
          client.buffWrite.flush();
        }
      } catch (Exception e) {
        closeEverything(sock, buffRead, buffWrite);
      }
    }
  }

  // Function to send message to client
  // Arguments: String message
  // Returns: void
  private void sendMessageToClient(String message) {
    try {
      buffWrite.write(message);
      buffWrite.newLine();
      buffWrite.flush();
    } catch (Exception e) {
      closeEverything(sock, buffRead, buffWrite);
    }
  }

  // Function to close the socket and buffer reader/writer
  // Arguments: Socket sock, BufferedReader buffRead, BufferedWriter buffWrite
  // Returns: void
  private void closeEverything(Socket sock, BufferedReader buffRead, BufferedWriter buffWrite) {
    try {
      if (buffRead != null)
        buffRead.close();
      if (buffWrite != null)
        buffWrite.close();
      if (sock != null)
        sock.close();
    } catch (Exception e) {
    }
  }

  // Function to handle the client message.
  // Arguments: String message
  // Returns: void
  private void handleClientmessage(String message) {
    String commandString = message.split(" ")[0];
    switch (commandString) {
      case "exit":
        removeClient();
        closeEverything(sock, buffRead, buffWrite);
        break;

      case "users":
        sendMessageToClient("List of clients:");
        sendClientList();
        break;

      case "help":
        sendMessageToClient("List of commands:");
        sendMessageToClient("users: List all clients");
        sendMessageToClient("help: List all commands");
        sendMessageToClient("ping: Check connection to server.");
        sendMessageToClient("message <id>: Retrieve message by id");
        sendMessageToClient("exit: Exit the chat");
        break;

      case "ping":
        sendMessageToClient("pong");
        break;

      case "message":
        // Find message per message id
        int messageId = Integer.parseInt(message.split(" ")[1]);
        sendMessageGivenId(messageId);
        break;

      default:
        Message newMessage = addMessageToMessageList(clientUsername, message);
        sendBroadcastMessage(newMessage.toString());
        break;
    }
  }

  // Function to add message to message list
  // Arguments: String message
  // Returns: void
  private Message addMessageToMessageList(String username, String message) {
    Message newMessage = new Message(username, message);
    messageList.add(newMessage);
    return newMessage;
  }

  // Function to send client list to current client
  // Arguments: None
  // Returns: void
  private void sendClientList() {
    for (ClientHandler client : clients) {
      sendMessageToClient(client.clientUsername);
    }
  }

  // Function to remove client from clients list
  // Arguments: None
  // Returns: void
  private void removeClient() {
    sendBroadcastMessage(clientUsername + " has left the chat");
    clients.remove(this);
  }

  private void sendMessageGivenId(int messageId) {
    for (Message message : messageList) {
      if (message.getMessageId() == messageId) {
        sendMessageToClient(message.getMessage());
        return;
      }
    }
    sendMessageToClient("Message not found");
  }

  // Function to get the last two messages to send on client connection
  private ArrayList<Message> getLastTwoMessages() {
    int messageListSize = messageList.size();
    if (messageList.size() == 0) {
      return new ArrayList<Message>();
    } else if (messageList.size() == 1) {
      return new ArrayList<Message>(messageList.subList(0, 1));
    } else {
      return new ArrayList<Message>(messageList.subList(messageListSize - 2, messageListSize));
    }
  }

  // Function to send the last two messages to the client.
  // Arguments: None
  // Returns: void
  private void sendLastTwoMessages() {
    ArrayList<Message> lastTwoMessages = getLastTwoMessages();
    for (Message message : lastTwoMessages) {
      sendMessageToClient(message.toString());
    }
  }

  // Function to run the client handler thread to listen for messages
  // Arguments: None
  // Returns: void
  @Override
  public void run() {
    String clientMessage;

    // Send the last two messages to the client
    sendLastTwoMessages();

    while (!sock.isClosed()) {
      try {
        clientMessage = buffRead.readLine();
        handleClientmessage(clientMessage); // dispatch the message to the appropriate handler
      } catch (Exception e) {
        removeClient();
      }
    }
  }
}

package chat.chatapp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import chat.chatapp.Message;
import chat.chatapp.User;
import chat.chatapp.Group;

public class ClientHandler implements Runnable {

  // Static variables
  public static ArrayList<ClientHandler> clients = new ArrayList<>(); // List of clients
  public static Group[] possibleGroups = { new Group("general"), new Group("gaming"), new Group("programming") }; // List
                                                                                                                  // of
  // groups
  private Socket sock;
  private BufferedReader buffRead;
  private BufferedWriter buffWrite;
  public User userObj;

  // Constructor
  // Arguments: Socket sock
  public ClientHandler(Socket sock) {
    try {
      // Setup the socket and buffer reader/writer
      this.sock = sock;
      this.buffWrite = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
      this.buffRead = new BufferedReader(new InputStreamReader(sock.getInputStream()));

      // Get the username and create a new user object
      String username = buffRead.readLine();
      this.userObj = new User(username);
      // Join the general group by default
      this.userObj.joinGroup("general");
      // Add the client to the clients list
      clients.add(this);

      // Send a message to every client in group that a new client has joined
      sendGroupJoinMessage();
    } catch (Exception e) {
      closeEverything(sock, buffRead, buffWrite);
    }
  }

  // Function to send broadcast message to all clients in the same group.
  // Arguments: String message
  // Returns: void
  private void sendBroadcastMessage(String message) {
    for (ClientHandler client : clients) {
      if (client.userObj.getGroupName().equals(this.userObj.getGroupName())) {
        client.sendMessageToThisClient(message);
      }
    }
  }

  private void sendGroupJoinMessage() {
    sendBroadcastMessage(this.userObj.getUsername() + " has joined group " + this.userObj.getGroupName());
  }

  // Function to send message to this client
  // Arguments: String message
  // Returns: void
  private void sendMessageToThisClient(String message) {
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
      case "%exit":
        removeClient();
        closeEverything(sock, buffRead, buffWrite);
        break;

      case "%users":
        if (!clientInGroup()) { // Client is not in group
          sendMessageToThisClient("You are not in a group. Please join a group to see users.");
          break;
        } else { // Client is in group
          sendMessageToThisClient(String.format("List of clients in group (%s):", getClientGroup()));
          sendClientList();
          break;
        }

      case "%groups":
        sendGroupNames();
        break;

      case "%groupjoin":
        String groupName = message.split(" ")[1];
        if (!isValidGroup(groupName)) {
          sendMessageToThisClient("Group does not exist");
          break;
        }
        this.userObj.joinGroup(groupName);
        sendGroupJoinMessage();
        break;

      case "%groupleave":
        this.userObj.leaveGroup();
        sendBroadcastMessage(this.userObj.getUsername() + " has left the group");
        break;

      case "%help":
        sendHelpMessage();
        break;

      case "%ping":
        sendMessageToThisClient("pong");
        break;

      case "%message":
        // Find message per message id
        int messageId = Integer.parseInt(message.split(" ")[1]);
        sendMessageGivenId(messageId);
        break;

      default:
        if (!clientInGroup()) {
          sendMessageToThisClient("You are not in a group. Please join a group using %groupjoin <groupname>");
          break;
        }
        Message newMessage = addMessageToGroupMessageList(message);
        sendBroadcastMessage(newMessage.toString());
        break;
    }
  }

  // Function to add message to message list
  // Arguments: String message
  // Returns: Message
  private Message addMessageToGroupMessageList(String message) {
    Message newMessage = new Message(this.userObj.getUsername(), message);
    for (Group group : possibleGroups) {
      if (group.getGroupName().equals(this.userObj.getGroupName())) {
        group.addMessage(newMessage);
      }
    }
    return newMessage;
  }

  // Returns the client group
  // Arguments: None
  // Returns: String
  private String getClientGroup() {
    return this.userObj.getGroupName();
  }

  // Function to check if client is in a group
  // Arguments: None
  // Returns: boolean
  private boolean clientInGroup() {
    return this.userObj.getGroupName() != "";
  }

  // Function to check if group is valid (if it exists)
  // Arguments: None
  // Returns: boolean
  private boolean isValidGroup(String groupName) {
    for (Group group : possibleGroups) {
      if (group.getGroupName().equals(groupName)) {
        return true;
      }
    }
    return false;
  }

  // Function to send client list to current client
  // Arguments: None
  // Returns: void
  private void sendClientList() {
    for (ClientHandler client : clients) {
      if (client.userObj.getGroupName().equals(this.userObj.getGroupName())) {
        sendMessageToThisClient(client.userObj.getUsername());
      } else {
        continue;
      }
    }
  }

  // Function to remove client from clients list
  // Arguments: None
  // Returns: void
  private void removeClient() {
    sendBroadcastMessage(this.userObj.getUsername() + " has left the chat");
    clients.remove(this);
  }

  private void sendMessageGivenId(int messageId) {
    for (Group group : possibleGroups) {
      if (group.getGroupName().equals(this.userObj.getGroupName())) {
        ArrayList<Message> groupMsgList = group.getGroupMsgList();
        for (Message message : groupMsgList) {
          if (message.getMessageId() == messageId) {
            sendMessageToThisClient(message.toString());
            return;
          }
        }
      }
    }
    sendMessageToThisClient("Message not found");
  }

  // Function to get the last two messages to send on client connection
  private ArrayList<Message> getLastTwoMessages() {
    ArrayList<Message> lastTwoMessages = new ArrayList<>();
    for (Group group : possibleGroups) {
      if (group.getGroupName().equals(this.userObj.getGroupName())) {
        ArrayList<Message> groupMsgList = group.getGroupMsgList();
        int groupMsgListSize = group.getGroupMsgListSize();
        if (groupMsgListSize == 0) {
          return lastTwoMessages;
        }
        if (groupMsgListSize == 1) {
          lastTwoMessages.add(groupMsgList.get(0));
          return lastTwoMessages;
        }
        lastTwoMessages.add(groupMsgList.get(groupMsgListSize - 2));
        lastTwoMessages.add(groupMsgList.get(groupMsgListSize - 1));
        return lastTwoMessages;
      }
    }
    return lastTwoMessages;
  }

  // Function to send the last two messages to the client.
  // Arguments: None
  // Returns: void
  private void sendLastTwoMessages() {
    ArrayList<Message> lastTwoMessages = getLastTwoMessages();
    for (Message message : lastTwoMessages) {
      sendMessageToThisClient(message.toString());
    }
  }

  // Function to get group names
  // Arguments: None
  // Returns: String[]
  private String[] getGroupNames() {
    String[] groupNames = new String[possibleGroups.length];
    for (int i = 0; i < possibleGroups.length; i++) {
      groupNames[i] = possibleGroups[i].getGroupName();
    }
    return groupNames;
  }

  // Function to send group names to the client
  // Arguments: None
  // Returns: void
  private void sendGroupNames() {
    sendMessageToThisClient("List of groups:");
    for (String groupName : getGroupNames()) {
      sendMessageToThisClient(groupName);
    }
  }

  // Function to send help message to the client
  // Arguments: None
  // Returns: void
  private void sendHelpMessage() {
    sendMessageToThisClient("List of commands:");
    sendMessageToThisClient("%users: List all clients in group.");
    sendMessageToThisClient("%message <id>: Retrieve message by id");
    sendMessageToThisClient("%ping: Check connection to server.");
    sendMessageToThisClient("%help: List all commands");
    sendMessageToThisClient("%exit: Exit the chat");
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
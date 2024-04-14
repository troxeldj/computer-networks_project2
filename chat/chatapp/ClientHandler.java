
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
      for (String group : this.userObj.getGroupNames()) {
        sendGroupJoinMessage(group);
      }
    } catch (Exception e) {
      closeEverything(sock, buffRead, buffWrite);
    }
  }

  // Function to send broadcast message to all clients in the same group.
  // Arguments: String groupName, String message
  // Returns: void
  private void sendBroadcastMessage(String groupName, String message) {
    if (!clientInGroup()) {
      return;
    }
    if (!isPossibleGroupName(groupName)) {
      return;
    }
    for (ClientHandler client : clients) {
      ArrayList<String> clientGroupNames = client.getClientGroupNames();
      if (clientGroupNames.contains(groupName)) {
        client.sendMessageToThisClient(message);
      }
    }
  }

  // Function to send message on user joining a group
  // Arguments: String groupName
  // Returns: void
  private void sendGroupJoinMessage(String groupName) {
    sendBroadcastMessage(groupName, this.userObj.getUsername() + " has joined group " + groupName);
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
    String groupName;
    switch (commandString) {
      case "%groupusers":
        groupName = message.split(" ")[1];

        if (!clientInGroup()) {
          sendMessageToThisClient("You are not in a group. Please join a group using %groupjoin <groupname>");
          break;
        }
        if (!isPossibleGroupName(groupName)) {
          sendMessageToThisClient("Group does not exist");
          break;
        }
        if (!clientInThisGroup(groupName)) {
          sendMessageToThisClient("You are not currently in this group");
          break;
        }
        sendClientList(groupName);
        break;

      case "%groupjoin":
        groupName = message.split(" ")[1];
        if (!isPossibleGroupName(groupName)) {
          sendMessageToThisClient("Group does not exist");
          break;
        }
        if (clientInThisGroup(groupName)) {
          sendMessageToThisClient("You are already in this group");
          break;
        }
        this.userObj.joinGroup(groupName);
        sendGroupJoinMessage(groupName);

        // Send the last two messages to the client
        sendLastTwoMessages(groupName);
        break;

      case "%groupleave":
        groupName = message.split(" ")[1];
        if (!clientInGroup()) {
          sendMessageToThisClient("You are not in a group. Please join a group using %groupjoin <groupname>");
          break;
        }
        if (!isPossibleGroupName(groupName)) {
          sendMessageToThisClient("Group does not exist");
          break;
        }
        this.userObj.leaveGroup(groupName);
        sendBroadcastMessage(groupName, this.userObj.getUsername() + " has left the group");
        break;

      case "%grouppost":
        groupName = message.split(" ")[1];
        if (!clientInGroup()) {
          sendMessageToThisClient("You are not in a group. Please join a group using %groupjoin <groupname>");
          break;
        }
        if (!isPossibleGroupName(groupName)) {
          sendMessageToThisClient("Group does not exist");
          break;
        }
        if (!clientInThisGroup(groupName)) {
          sendMessageToThisClient("You are not currently in this group");
          break;
        }
        message = message.split(" ", 3)[2];
        Message newMessage = addMessageToGroupMessageList(groupName, message);
        sendBroadcastMessage(groupName, groupName + " " + newMessage.toStringNoContent());
        break;

      case "%groupmessage":
        // Find message per message id
        groupName = message.split(" ")[1];
        int messageId = Integer.parseInt(message.split(" ")[2]);

        sendMessageGivenId(groupName, messageId);
        break;

      case "%groups":
        sendGroupNames();
        break;

      case "%mygroups":
        sendMessageToThisClient("List of groups you are in:");
        ArrayList<String> groupNames = this.userObj.getGroupNames();
        for (String group : groupNames) {
          sendMessageToThisClient(group);
        }
        break;

      case "%ping":
        sendMessageToThisClient("pong");
        break;

      case "%help":
        sendHelpMessage();
        break;

      case "%exit":
        removeClient();
        closeEverything(sock, buffRead, buffWrite);
        break;

      default:
        break;
    }
  }

  // Function to add message to message list
  // Arguments: String message
  // Returns: Message
  private Message addMessageToGroupMessageList(String groupName, String message) {
    Message newMessage = new Message(this.userObj.getUsername(), message);
    for (Group group : possibleGroups) {
      if (group.getGroupName().equals(groupName)) {
        group.addMessage(newMessage);
        return newMessage;
      }
    }
    return newMessage;
  }

  // Function to check if client is in a group
  // Arguments: None
  // Returns: boolean
  private boolean clientInGroup() {
    return this.userObj.getGroupNames().size() > 0;
  }

  // Function to check if client is in a specific group
  // Arguments: String groupName
  // Returns: boolean
  private boolean clientInThisGroup(String groupName) {
    return this.userObj.getGroupNames().contains(groupName);
  }

  // Function to send client list to current client
  // Arguments: None
  // Returns: void
  private void sendClientList(String groupName) {
    sendMessageToThisClient("List of clients in group:");
    for (ClientHandler client : clients) {
      ArrayList<String> clientGroupNames = client.getClientGroupNames();
      if (clientGroupNames.contains(groupName)) {
        sendMessageToThisClient(client.userObj.getUsername());
      }
    }
  }

  // Function to remove client from clients list
  // Arguments: None
  // Returns: void
  private void removeClient() {
    for (String group : this.userObj.getGroupNames()) {
      sendBroadcastMessage(group, this.userObj.getUsername() + " has left the group");
    }
    clients.remove(this);
  }

  private void sendMessageGivenId(String groupName, int messageId) {
    if (!clientInGroup()) {
      sendMessageToThisClient("You are not in a group. Please join a group using %groupjoin <groupname>");
      return;
    }
    if (!isPossibleGroupName(groupName)) {
      sendMessageToThisClient("Group does not exist");
      return;
    }
    if (!clientInThisGroup(groupName)) {
      sendMessageToThisClient("You are not currently in this group");
      return;
    }
    for (Group group : possibleGroups) {
      if (group.getGroupName().equals(groupName)) {
        ArrayList<Message> groupMsgList = group.getGroupMsgList();
        if (messageId < 0) {
          sendMessageToThisClient("Message does not exist");
          return;
        }
        try {
          String message = groupMsgList.get(messageId).toString();
          sendMessageToThisClient(groupName + " " + message.toString());
        } catch (Exception e) {
          sendMessageToThisClient("Message does not exist");
        }
        return;
      }
    }
  }

  // Function to get the last two messages to send on client connection
  private ArrayList<Message> getLastTwoMessages(String groupName) {
    for (Group group : possibleGroups) {
      if (group.getGroupName().equals(groupName)) {
        ArrayList<Message> groupMsgList = group.getGroupMsgList();
        int groupMsgListSize = groupMsgList.size();
        if (groupMsgListSize == 0) {
          return new ArrayList<Message>();
        }
        if (groupMsgListSize == 1) {
          return new ArrayList<Message>(groupMsgList.subList(0, 1));
        }
        return new ArrayList<Message>(groupMsgList.subList(groupMsgListSize - 2, groupMsgListSize));
      }
    }
    return new ArrayList<Message>();
  }

  // Function to send the last two messages to the client.
  // Arguments: None
  // Returns: void
  private void sendLastTwoMessages(String groupName) {
    ArrayList<Message> lastTwoMessages = getLastTwoMessages(groupName);
    // Print messages in reverse order
    for (int i = lastTwoMessages.size() - 1; i >= 0; i--) {
      sendMessageToThisClient(groupName + " " + lastTwoMessages.get(i).toStringNoContent());
    }
  }

  // Function to get group names
  // Arguments: None
  // Returns: String[]
  private String[] getPossibleGroupNames() {
    String[] groupNames = new String[possibleGroups.length];
    for (int i = 0; i < possibleGroups.length; i++) {
      groupNames[i] = possibleGroups[i].getGroupName();
    }
    return groupNames;
  }

  private ArrayList<String> getClientGroupNames() {
    return this.userObj.getGroupNames();
  }

  private boolean isPossibleGroupName(String groupName) {
    for (String possibleGroupName : getPossibleGroupNames()) {
      if (possibleGroupName.equals(groupName)) {
        return true;
      }
    }
    return false;
  }

  // Function to send group names to the client
  // Arguments: None
  // Returns: void
  private void sendGroupNames() {
    sendMessageToThisClient("List of groups:");
    for (String groupName : getPossibleGroupNames()) {
      sendMessageToThisClient(groupName);
    }
  }

  // Function to send help message to the client
  // Arguments: None
  // Returns: void
  private void sendHelpMessage() {
    sendMessageToThisClient("List of commands:");
    sendMessageToThisClient("%groupusers <group_name>: List all users in group.");
    sendMessageToThisClient("%groupjoin <group_name>: Join a group.");
    sendMessageToThisClient("%groupleave <group_name>: Leave current group.");
    sendMessageToThisClient("%grouppost <group_name> <message>: Post a message to group.");
    sendMessageToThisClient("%groupmessage <group_name> <id>: Retrieve message content given id");
    sendMessageToThisClient("%groups: List all groups.");
    sendMessageToThisClient("%mygroups: List all groups you are in.");
    sendMessageToThisClient("%ping: Check connection to server.");
    sendMessageToThisClient("%help: List all commands");
    sendMessageToThisClient("%exit: Exit the chat application");
  }

  // Function to run the client handler thread to listen for messages
  // Arguments: None
  // Returns: void
  @Override
  public void run() {
    String clientMessage;

    // Send the last two messages to the client
    for (String group : this.userObj.getGroupNames()) {
      sendLastTwoMessages(group);
    }

    while (!sock.isClosed()) {
      try {
        clientMessage = buffRead.readLine();
        handleClientmessage(clientMessage); // dispatch the message to the appropriate handler
      } catch (Exception e) {
        removeClient();
        closeEverything(sock, buffRead, buffWrite);
      }
    }
  }
}
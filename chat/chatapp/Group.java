package chat.chatapp;

import java.util.ArrayList;

import chat.chatapp.Message;

public class Group {
  private String groupName;
  private ArrayList<Message> groupMsgList;

  // Constructor
  // Arguments: String groupName
  public Group(String groupName) {
    this.groupName = groupName;
    this.groupMsgList = new ArrayList<>();
  }

  // Function to add message to the group message list
  // Arguments: Message message
  // Returns: void
  public void addMessage(Message message) {
    groupMsgList.add(message);
  }

  // Getters

  // Function to get group name
  // Returns: String
  public String getGroupName() {
    return groupName;
  }

  // Function to get group message list
  // Returns: ArrayList<Message>
  public ArrayList<Message> getGroupMsgList() {
    return this.groupMsgList;
  }

  // Function to get group message list size
  // Returns: int
  public int getGroupMsgListSize() {
    return this.groupMsgList.size();
  }
}
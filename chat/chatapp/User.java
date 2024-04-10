package chat.chatapp;

import java.util.ArrayList;

public class User {
  private String username;
  private String groupName;

  // Constructor
  // Arguments: String username
  public User(String username) {
    this.username = username;
    this.groupName = "";
  }

  // Function to add group to the user group list
  // Arguments: String groupName
  // Returns: void
  public void joinGroup(String groupNameString) {
    this.groupName = groupNameString;
  }

  // Function to remove group from the user group list
  // Arguments: String groupName
  // Returns: void
  public void leaveGroup() {
    groupName = "";
  }

  public String getGroupName() {
    return groupName;
  }

  // Function to get username
  // Returns: String
  public String getUsername() {
    return username;
  }
}

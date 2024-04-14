package chat.chatapp;

import java.util.ArrayList;

public class User {
  private String username;
 private ArrayList<String> groupNames;

  // Constructor
  // Arguments: String username
  public User(String username) {
    this.username = username;
    this.groupNames = new ArrayList<String>();
  }

  // Getters
  public ArrayList<String> getGroupNames() {
    return groupNames;
  }

  public String getUsername() {
    return username;
  }

  // Function to add group to the user group list
  // Arguments: String groupName
  // Returns: void
  public void joinGroup(String groupNameString) {
    this.groupNames.add(groupNameString);
  }

  // Function to remove group from the user group list
  // Arguments: String groupName
  // Returns: void
  public void leaveGroup(String groupNameString) {
    this.groupNames.remove(groupNameString);
  }
}

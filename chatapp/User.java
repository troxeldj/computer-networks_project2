package chat.chatapp;

import java.util.ArrayList;

public class User {
  private String username;
  private ArrayList<String> groupNames;

  // Constructor
  // Arguments: String username
  public User(String username) {
    this.username = username;
    this.groupNames = new ArrayList<>();
  }

  // Function to add group to the user group list
  // Arguments: String groupName
  // Returns: void
  public void addGroup(String groupName) {
    groupNames.add(groupName);
  }

  // Function to remove group from the user group list
  // Arguments: String groupName
  // Returns: void
  public void removeGroup(String groupName) {
    groupNames.remove(groupName);
  }

  // Function to get username
  // Returns: String
  public String getUsername() {
    return username;
  }

  // Function to get number of groups
  // Returns: int
  public int getNumberOfGroups() {
    return groupNames.size();
  }
}

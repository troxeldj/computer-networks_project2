package chat.chatapp;

import java.sql.Timestamp;

public class Message {
  // Static variables
  private static int MESSAGE_ID = 0;

  // Instance variables
  private String author;
  private String message;
  private Timestamp timestamp;
  private int messageId;

  // Constructor
  // Arguments: String username, String message
  public Message(String username, String message) {
    this.author = username;
    this.message = message;
    this.timestamp = new Timestamp(System.currentTimeMillis());
    this.messageId = MESSAGE_ID++;
  }

  // Getters
  public String getAuthor() {
    return this.author;
  }

  public String getMessage() {
    return this.message;
  }

  public Timestamp getTimestamp() {
    return this.timestamp;
  }

  public int getMessageId() {
    return this.messageId;
  }

  // ToString methods

  // Returns the message in the format: messageId author timestamp message
  public String toString() {
    return this.messageId + " " + this.author + " " + this.timestamp + " " + this.message;
  }

  // Returns the message in the format: messageId author timestamp (no message content)
  public String toStringNoContent() {
    return this.messageId + " " + this.author + " " + this.timestamp;
  }
}

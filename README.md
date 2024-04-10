# Computer Networks (CS4065) Project 2

## Members:
- Dillon Troxell
- Ben Sabo 
- Justin Wade


## Components
### Server Components
- Server: Class with static main function. Run to start server.
- ClientHandler: Class used to represent client thread after server accepts connections. Performs almost all server side logic.
- Group: Class to represent a group. Each group has a name and a message list.
- Message: Class to represent a message. Each message has a message ID, author, timestamp, and messsage content.
- User: Class to represent a user on the server side. Each user has a username and a group name they are apart of.
### Client Components
- Client: Class with static main function. Run to start a client.

## Run Instructions
Note: package is setup as chat.chatapp, please keep chat and it's subfolder in tact.
- Drag chat to location of choice
- Run one instance of Server.java
- Run one or more instances of Client.java
# Decentralized-Messenger

Super quick start guide

# Requirements
 - MySql server
 - Node.js

## Server

 1. Clone the repo using `git clone https://github.com/jkk111/Decentralized-Messenger.git`
 2. Run `npm install` in server/ to get all dependencies for node.
 3. Run `node setup.js` from server/ and fill in all the required information
 4. Run `node app.js` to start the server.
  
## Android

TODO

## Web

TODO

## API

 All methods, except for login and register must provide a token, and in some cases id. This is to ensure that a given user is who they claim to be. A token is valid for 6 hours, but can be refreshed, there is also an encryption layer currently in development.

 `/register` Used to register a new user account returns an auth token.
 Usage: 
 ```
 {
   user: "The username to register",
   password: "The users password"
 }
 ```
 
 `/login` Used to log an existing user in.
 ```
 {
   user: "The username to login",
   password: "The users password"
 }
 ```
 
 `/refreshToken` Used to refresh an existing token.
 ```
 {
   token: "The token to refresh"
 }
 ```
 
 `/message` Used to refresh an existing token.
 ```
 {
   sender: "The senders id",
   token: "The token of the sender",
   dest: "The client to send to",
   message: "The message to send"
 }
 ```
 
 `/messages` Used to get all pending messages
 ```
 {
   sender: "the user to get messages for",
   token: "The token of the user"
 }
 ```
 
 `/received` Used to mark and/or delete received messages.
 ```
 {
   sender: "The user to mark messages received for",
   token: "The token to refresh"
   highest: "The highest received message"
 }
 ```




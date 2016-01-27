module.exports = function(config) {
  var user = config.DBUser;
  var pass = config.DBPass;
  var loc = config.Location;
  console.log("Using a flat-file, this configuration is slow, use a database if possible");
  function verifyToken(user, token, cb) {
    console.log("Checking if user %s is authorized to use token %s", user, token);
    // TODO (john): Check database/flat-file here
    cb(true);
  }

  function addMessage(sender, token, dest, message, cb) {
    console.log("Adding message %s for client %s from client %s", message, dest, sender);
    // TODO (john): Add a check for message successfully added to database, and return sucess
    cb(true);
  }

  function receivedMessages(sender, token, highest, cb) {
    // TODO (john): Clear messages up to the id of the highest recieved message.
  }

  function getMessages(sender, token, cb) {
    // TODO (john): Implement a method to get the messages
  }
}

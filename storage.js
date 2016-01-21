module.exports = function(config) {
  var serverMode = config.Message_Store;
  var user = config.DBUser;
  var pass = config.DBPass;
  var loc = config.Location;
  console.log("Connecting to %s server on %s using: user %s, pass %s", serverMode, loc, user, pass);
  function verifyToken(user, token, cb) {
    console.log("Checking if user %s is authorized to use token %s", user, token);
    // TODO (john): Check database/flat-file here
    cb(true);
  }

  function addMessage(sender, dest, message, cb) {
    console.log("Adding message %s for client %s from client %s", message, dest, sender);
    // TODO (john): Add a check for message successfully added to database, and return sucess
    cb(true);
  }
}

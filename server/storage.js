var storageEngines = {
  "mysql": "./mysql.js",
  "flat": "./flat.js"
};
module.exports = function(config) {
  var storageMode = config.Message_Store.toLowerCase();
  var storageScript = "";
  var keys = Object.keys(storageEngines);
  for(var i = 0 ; i < keys.length; i++) {
    console.log(storageMode +":"+keys[i]);
    if(storageMode == keys[i]) {
      storageScript = storageEngines[keys[i]];
      break;
    }
  }
  if(storageScript == "") {
    console.log("D: ***REMOVED***");
    process.exit(1);
  }

  var storageEngine = require(storageScript)(config);
  function verifyToken(user, token, cb) {
    console.log("Checking if user %s is authorized to use token %s", user, token);
    storageEngine.verifyToken(user, token, function(isValid) {
      cb(isValid);
    })
  }

  function addMessage(sender, token, dest, message, cb) {
    console.log("Adding message %s for client %s from client %s", message, dest, sender);
    storageEngine.addMessage(sender, token, dest, message, function(success) {
      cb(success);
    });
  }

  function receivedMessages(sender, token, highest, cb) {
    storageEngine.receivedMessages(sender, token, highest, function(success) {
      cb(success);
    });
  }

  function getMessages(sender, token, cb) {
    storageEngine.getMessages(sender, token, function(messages) {
      cb(messages);
    });
  }
}

var storageEngines = {
  "mysql": "./mysql.js"
};

module.exports = function(config) {

  var storageMode = config.databaseType.toLowerCase();
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
    console.log("You didn't specify a storage method! exiting");
    process.exit(1);
  }

  var storageEngine = require(storageScript)(config);

  this.verifyToken = function(user, token, cb) {
    storageEngine.verifyToken(user, token, cb);
  }

  this.login = function(user, pass, cb) {
    storageEngine.login(user, pass, cb);
  }

  this.register = function(user, pass, cb) {
    storageEngine.register(user, pass, cb);
  }

  this.addMessage = function(sender, dest, message, cb) {
    storageEngine.addMessage(sender, token, dest, message, cb);
  }

  this.receivedMessages = function(sender, token, highest, cb) {
    storageEngine.receivedMessages(sender, token, highest, cb);
  }

  this.getMessages = function(sender, token, cb) {
    storageEngine.getMessages(sender, token, cb);
  }
  return this;
}

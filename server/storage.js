var storageEngines = {
  "mysql": "./mysql.js"
};

module.exports = function(config) {

  var storageMode = config.databaseType.toLowerCase();
  var storageScript = "";
  var keys = Object.keys(storageEngines);

  for(var i = 0 ; i < keys.length; i++) {
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

  this.idFromName = storageEngine.idFromName;

  this.verifyToken = storageEngine.verifyToken;

  this.login = storageEngine.login;

  this.register = storageEngine.register;

  this.addMessage = storageEngine.addMessage;

  this.receivedMessages = storageEngine.receivedMessages;

  this.getMessages = storageEngine.getMessages;

  this.userExists = storageEngine.userExists;

  this.updateFriendship = storageEngine.updateFriendship;

  this.refreshToken = storageEngine.refreshToken;

  this.addFriend = storageEngine.addFriend;

  this.getFriends = storageEngine.getFriends;

  return this;
}

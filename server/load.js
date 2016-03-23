process.env.NODE_TLS_REJECT_UNAUTHORIZED = "0";
var cluster = require("cluster");
if(cluster.isMaster) {
  for(var i = 0 ; i < require('os').cpus().length; i++) {
    cluster.fork();
  }
}
var request = require("request");
var crypto = require("crypto");
var NodeRSA = require("node-rsa");
setInterval(genUser, 2500);

function genUser() {
  var id = crypto.randomBytes(32).toString("base64");
  var key = new NodeRSA({b: 2048});
  var priv = key.exportKey("pkcs1-private-pem");
  var pub = key.exportKey("pkcs8-public-pem");
  var opt = {
    url: "https://localhost/register",
    timeout: 1000,
    form: {
      user: id,
      password: id,
      pubKey: pub,
      privKey: priv
    }
  }
  request.post(opt, function(err, data, body) {
    if(err) return console.log(err);
    try {
      body = JSON.parse(body);
    } catch(e) {
      return;
    }
    findUser(body, id);
  });
}

function findUser(body, id) {
  var opt = {
    url: "https://localhost/search",
    timeout: 1000,
    form: {
      sender: body.id,
      token: body.token,
      query: "jkk1111"
    }
  }
  var user = {};
  user.id = body.id;
  user.token = body.token;
  user.public = body.public;
  user.private = body.private;
  request.post(opt, function(err, data, body) {
    if(err) return console.log(err);
    try {
      body = JSON.parse(body);
    } catch(e) {
      return;
    }
    if(!body || !body.users || body.users.length != 1)
      return console.log("error not 1 user", body);
    user.friend = body.users[0].id;
    user.username = body.users[0].username;
    addUser(user)
  })
}

function addUser(user) {
  var opt = {
    url: "https://localhost/addFriend",
    timeout: 1000,
    form: {
      client: user.friend,
      secret: "lol",
      sender: user.id,
      token: user.token
    }
  }
  request.post(opt, function(err, data, body) {
    if(err) return console.log("Error adding friend", err);
    try {
      body = JSON.parse(body);
    } catch(e) {
      return;
    }
    user.friendshipId = body.friendshipId;
    console.log(body);
    acceptFriend(user);
  });
}

function acceptFriend(user) {
  var opt = {
    url: "https://localhost/login",
    timeout: 1000,
    form: {
      user: user.username,
      password: user.username
    }
  }
  request.post(opt, function(err, data, body) {
    if(err) return console.log("error logging in jkk111", err);
    try {
      body = JSON.parse(body);
    } catch(e) {
      return;
    }
    opt = {
      url: "https://localhost/confirmFriend",
      timeout: 1000,
      form: {
        friendshipId: user.friendshipId,
        sender: body.id,
        response: true,
        token: body.token
      }
    }
    request.post(opt, function(err, data, body2) {
      if(err) return console.log("Error confirming friend");
      try {
        body2 = JSON.parse(body2);
      } catch(e) {
        return;
      }
      user.friendPub = body.public;
      spamMessages(user);
    });
  });
}
function spamMessages(user) {
  var message = "loooool";
  var enc = new NodeRSA(user.friendPub);
  var messageFriend = enc.encrypt(message, "base64");
  enc = new NodeRSA(user.public);
  var messageSelf = enc.encrypt(message, "base64");
  for(var i = 0; i < 1000; i++) {
    sendMessage(user, messageSelf, messageFriend, i);
  }
  console.log("Attempted to send messages");
}

function sendMessage(user, self, friend, i) {
  var opt = {
    url: "https://localhost/message",
    timeout: 1000,
    form: {
      messageSender: self,
      messageRecipient: friend,
      sender: user.id,
      dest: user.friend,
      token: user.token
    }
  }
  request.post(opt, function(err, data, body) {
    if(err) return console.log("Error sending message", err, i);
    try {
      body = JSON.parse(body);
    } catch(e) {
      return;
    }
    if(body && body.success != true)
      console.log(body);
  })
}
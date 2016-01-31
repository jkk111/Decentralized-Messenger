var request = require("request");
var nodeRSA = require("node-rsa");
// var key = new nodeRSA({ b: 2048 });
// var pub = key.exportKey('pkcs1-public');

var host = "http://" + (process.argv[2] || "localhost");

// https://github.com/jkk111/Decentralized-Messenger.git

startTest();

function startTest() {
  console.log("starting i guess")
  var opt = {
    url: host + "/register",
    form: {
      user: "test",
      password: "password"
    }
  }
  request.post(opt, function(err, data, body) {
    if(err)
      console.log(err);
    login();
  })
}

function login() {
  console.log("starting login")
  var opt = {
    url:  host + "/login",
    form: {
      user: "test",
      password: "password"
    }
  }
  request.post(opt, function(err, data, body) {
    if(err)
      console.log(err);
    body = JSON.parse(body);
    if(body.success) {
        sendMessage(body);
    } else {
      console.log(body)
    }
  })
}

function sendMessage(self) {
  var opt = {
    url:  host + "/message",
    form: {
      sender: self.id,
      dest: self.id,
      message: "hello world",
      token: self.token
    }
  }
  request.post(opt, function(err, data, body) {
    if(err)
      console.log(err);
    console.log(body);
    getMessages(self);
  })
}

function getMessages(self) {
  var opt = {
    url:  host + "/messages",
    form: {
      sender: self.id,
      token: self.token
    }
  }
  request.post(opt, function(err, data, body) {
    if(err)
      console.log(err);
    console.log(body);
    body = JSON.parse(body);
    clearMessages(self, body);
  });
}

function clearMessages(self, messages) {
  var highest = 0;
  var keys = Object.keys(messages);
  for(var i = 0 ; i < keys.length; i++) {
    for(var j = 0 ; j < messages[keys[i]].length; j++) {
      highest = Math.max(highest, messages[keys[i]][j].id);
    }
  }
  var opt = {
    url:  host + "/received",
    form: {
      sender: self.id,
      token: self.token,
      highest: highest
    }
  }
  request.post(opt, function(err, data, body) {
    console.log(body);
    console.log("Testing complete");
  })
}

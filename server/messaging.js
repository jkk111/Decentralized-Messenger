var rateLimits = {

}
var RATE_LIMITS = 200;

var USAGE_VALUES = {
  "/search": 1,
  "/addFriend": 1
}

var REQUIREMENTS = {
  message: ["sender", "dest", "message", "token"],
  basic: ["sender", "token"],
  auth: ["user", "password"],
  addFriend: ["sender", "token", "client", "secret"],
  search: ["token", "sender", "query"],
  confirmFriend: ["sender", "token", "response", "friendshipId"],
  received: ["sender", "token", "highest"],
  cancelFriend: ["sender", "token", "friendshipId"]
}



var rateLimiting = function(req, res, next) {
  if(!hasRequirements(req, res, REQUIREMENTS.basic, true)) {
    return;
  }
  var sender = req.body.sender;
  var method = req.url;
  if(!USAGE_VALUES[method]) {
    res.sendStatus(404);
    return;
  }
  if(!rateLimits[sender]) {
    rateLimits[sender] = USAGE_VALUES[method];
  } else {
    rateLimits[sender] += USAGE_VALUES[method];
  }
  if(rateLimits[sender] > RATE_LIMITS) {
    res.sendStatus(429);
  } else {
    next();
  }
}

setInterval(function() {
  RATE_LIMITS = {};
}, 1000 * 60 * 60);

module.exports = function(app, storage) {
  app.post("/refreshToken", function(req, res) {
    if(!hasRequirements(req, res, REQUIREMENTS.basic)) {
      return;
    }
    var token = req.body.token;
    storage.refreshToken(token, function(success) {
      handleResult(success, res);
    })
  });

  app.post("/login", function(req, res) {
    if(!hasRequirements(req, res, REQUIREMENTS.auth)) {
      return;
    }
    var user = req.body.user;
    var password = req.body.password;
    storage.login(user, password, function(success) {
      handleResult(success, res);
    });
  })

  app.post("/register", function(req, res) {
    if(!hasRequirements(req, res, REQUIREMENTS.auth)) {
      return;
    }
    var user = req.body.user;
    var password = req.body.password;
    var privKey = req.body.privKey || null;
    var pubKey = req.body.pubKey || null;
    storage.userExists(user, function(exists) {
      handleResult((exists === false) ? true : { error: "ERROR_USER_EXISTS" }, res, function() {
        storage.register(user, password, function(success) {
          handleResult(success, res, function() {
            console.log("registered")
            if(privKey && pubKey) {
              storage.setKeys(success, privKey, pubKey, function(success) {
                console.log(success);
                handleResult(success, res, function() {
                  storage.login(user, password, function(success) {
                    handleResult(success, res);
                  });
                });
              });
            } else {
              storage.login(user, password, function(success) {
                console.log(success);
                handleResult(success, res);
              });
            }
          });
        });
      });
    });
  });

  app.post("/messages", function(req, res) {
    if(!hasRequirements(req, res, REQUIREMENTS.basic)) {
      return;
    }
    var sender = req.body.sender;
    var token = req.body.token;
    var highest = req.body.highestReceived || 0;
    storage.verifyToken(sender, token, function(success) {
      handleResult(success, res, function() {
        storage.getMessages(sender, highest, function(messages) {
          res.send(messages);
        });
      });
    });
  })

  app.post("/confirmFriend", function(req, res) {
    if(!hasRequirements(req, res, REQUIREMENTS.confirmFriend)) {
      return;
    }
    var response = req.body.response;
    var token = req.body.token;
    var sender = req.body.sender;
    var friendshipId = req.body.friendshipId; //
    storage.verifyToken(sender, token, function(success) {
      handleResult(success, res, function() {
        storage.updateFriendship(friendshipId, response, sender, function(success) {
          handleResult(success, res)
        })
      });
    });
  });

  app.post("/received", function(req, res) {
    if(!hasRequirements(req, res, REQUIREMENTS.received)) {
      return;
    }
    var sender = req.body.sender;
    var token = req.body.token;
    var highest = req.body.highest;
    storage.verifyToken(sender, token, function(success) {
      handleResult(success, res, function() {
        storage.receivedMessages(sender, highest, function(success) {
          handleResult(success, res);
        });
      });
    });
  });

  app.post("/getFriends", function(req, res) {
    if(!hasRequirements(req, res, REQUIREMENTS.basic)) {
      return
    }
    var sender = req.body.sender;
    var token = req.body.token;
    storage.verifyToken(sender, token, function(success) {
      handleResult(success, res, function() {
        storage.getFriends(sender, function(success) {
          handleResult(success, res);
        });
      });
    });
  });

  app.post("/addFriend", rateLimiting, function(req, res) {
    if(!hasRequirements(req, res, REQUIREMENTS.addFriend)) {
      return;
    }
    var sender = req.body.sender;
    var token = req.body.token;
    var client = req.body.client;
    var secret = req.body.secret || "";
    storage.verifyToken(sender, token, function(success) {
      handleResult(success, res, function() {
        storage.userExists(client, function(exists) {
          handleResult(exists, res, function() {
            storage.addFriend(sender, client, secret, function(success) {
              handleResult(success, res);
            });
          });
        });
      });
    });
  });

  app.post("/search", rateLimiting, function(req, res) {
    if(!hasRequirements(req, res, REQUIREMENTS.search)) {
      return;
    }
    var sender = req.body.sender;
    var token = req.body.token;
    var query = req.body.query;
    storage.verifyToken(sender, token, function(success) {
      handleResult(success, res, function() {
        storage.search(query, function(result) {
          res.send(result);
        });
      });
    });
  });
  // TODO (johnkevink): Find alternative to this, perhaps search and add.
  app.post("/addFriendName", function(req, res) {
    if(!hasRequirements(req, res, REQUIREMENTS.addFriend)) {
      return;
    }
    var sender = req.body.sender;
    var token = req.body.token;
    var client = req.body.client;
    var secret = req.body.secret || "";
    storage.verifyToken(sender, token, function(success) {
      handleResult(success, res, function() {
        storage.idFromName(client, function(id) {
          handleResult(id.error === undefined, res, function() {
            storage.addFriend(sender, id, secret, function(success) {
              handleResult(success, res);
            })
          });
        });
      });
    });
  });

  app.post("/message", function(req, res) {
    if(!hasRequirements(req, res, REQUIREMENTS.message)) {
      return;
    }
    var sender = req.body.sender;
    var dest = req.body.dest;
    var messageSender = req.body.messageSender || req.body.message;
    var messageRecipient = req.body.messageRecipient || req.body.message;
    var token = req.body.token;
    storage.verifyToken(sender, token, function(success) {
      console.log("verified: %s", success)
      handleResult(success, res, function() {
        storage.checkFriendship(sender, dest, function(success) {
          console.log("friends: %s", success)
          handleResult(success, res, function() {
            storage.addMessage(sender, dest, messageSender, messageRecipient, function(success) {
              console.log("message added: %s", success)
              handleResult(success, res);
            });
          });
        });
      });
    });
  });

  app.post("/cancelFriend", function(req, res) {
    if(!hasRequirements(req, res, REQUIREMENTS.cancelFriend)) {
      return;
    }
    var sender = req.body.sender;
    var token = req.body.token;
    var fId = req.body.friendshipId;
    storage.verifyToken(sender,token, function(success){
      handleResult(success, res, function() {
        storage.cancelFriendRequest(fId, sender, function(success) {
          handleResult(success, res);
        });
      });
    });
  });
}

function badKeys(res, keys, req, missing) {
  var error = {}
  error.error = "ERROR_BAD_KEYS";
  error.detail = `Parameter missing: ${missing}`
  error.message = `Parameters for method "${req.url}": ${keys}`;
  res.json(error);
}

function handleResult(result, res, cb) {
  console.log(typeof result)
  if(typeof result == "boolean") {
    if(result !== false) {
      if(cb)
        cb();
      else
        res.send({success: true});
    }
    else
      res.send({success: false});
  } else if(typeof result == "object") {
    res.send(result)
  } else {
    if(cb)
      cb();
    else
      res.send({error: "UNKNOWN_ERROR"});
  }
}

function hasRequirements(req, res, keys, silent) {
  for(var i = 0 ; i < keys.length; i++ ) {
    if(!req.body[keys[i]]) {
      badKeys(res, keys, req, keys[i]);
      return false;
    } else if(!silent) {
      console.log(keys[i] + ") " + req.body[keys[i]]);
    }
  }
  return true;
}
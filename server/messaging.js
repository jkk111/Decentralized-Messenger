var rateLimits = {

}
var RATE_LIMITS = 200;

var USAGE_VALUES = {
  "/search": 1,
  "/addFriend": 1
}

var rateLimiting = function(req, res, next) {
  console.log(req.url);
  if(!req.body.token) {
    res.send(400);
    return;
  }
  var token = req.body.token;
  var method = req.url;
  if(!USAGE_VALUES[method]) {
    res.sendStatus(404);
    return;
  }
  if(!rateLimits[token]) {
    rateLimits[token] = USAGE_VALUES[method];
  } else {
    rateLimits[token] += USAGE_VALUES[method];
  }
  console.log(rateLimits[token])
  if(rateLimits[token] > RATE_LIMITS) {
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
    if(!req.body.token) {
      res.sendStatus(400);
      return;
    }
    var token = req.body.token;
    storage.refreshToken(token, function(success) {
      handleResult(success, res);
    })
  });

  app.post("/login", function(req, res) {
    if(!req.body.user || ! req.body.password) {
      res.sendStatus(400);
      return;
    }
    var user = req.body.user;
    var password = req.body.password;
    storage.login(user, password, function(success) {
      handleResult(success, res);
    });
  })

  app.post("/register", function(req, res) {
    if(!req.body.user || ! req.body.password) {
      res.sendStatus(400);
      return;
    }
    var user = req.body.user;
    var password = req.body.password;
    storage.userExists(user, function(exists) {
      handleResult(!exists == false ? true : { error: "ERROR_USER_EXISTS" }, res, function() {
        storage.register(user, password, function(success) {
          handleResult(success, res, function() {
            storage.login(user, password, function(success) {
              handleResult(success, res);
            });
          });
        });
      });
    });
  });

  app.post("/messages", function(req, res) {
    if(!req.body.sender || ! req.body.token) {
      res.sendStatus(400);
      return;
    }
    var sender = req.body.sender;
    var token = req.body.token;
    var highest = req.body.highestReceived || 0;
    console.log(token);
    storage.verifyToken(sender, token, function(success) {
      handleResult(success, res, function() {
        storage.getMessages(sender, highest, function(messages) {
          res.send(messages);
        });
      });
    });
  })

  app.post("/confirmFriend", function(req, res) {
    // Todo allow confirm/deny
    var response = req.body.confirm;
    var token = req.body.token;
    var sender = req.body.sender;
    var friendshipId = req.body.friendshipId; //
    storage.verifyToken(sender, token, function(success) {
      handleResult(success, res, function() {
        storage.updateFriendship(friendshipId, response, function(success) {
          handleResult(success, res)
        })
      });
    });
  });

  app.post("/received", function(req, res) {
    if(!req.body.sender || !req.body.token || req.body.highest) {
      res.sendStatus(400);
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
    var sender = req.body.sender;
    var token = req.body.token;
    if(!sender || ! token) {
      res.sendStatus(400);
      return
    }
    storage.verifyToken(sender, token, function(success) {
      handleResult(success, res, function() {
        storage.getFriends(sender, function(success) {
          handleResult(success, res);
        });
      });
    });
  });

  app.post("/addFriend", rateLimiting, function(req, res) {
    var sender = req.body.sender;
    var token = req.body.token;
    var client = req.body.client;
    var secret = req.body.secret || "";
    if(!(sender && token && client)) {
      res.sendStatus(400);
      return;
    }
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
    var sender = req.body.sender;
    var token = req.body.token;
    var query = req.body.query;
    storage.verifyToken(sender, token, function(success) {
      handleResult(success, res, function() {
        storage.search(query, function(result) {
          res.send(result);
        })
      })
    });
  })
  // TODO (johnkevink): Find alternative to this, perhaps search and add.
  app.post("/addFriendName", function(req, res) {
    var sender = req.body.sender;
    var token = req.body.token;
    var client = req.body.client;
    var secret = req.body.secret || "";
    if(!(sender && token && client)) {
      res.sendStatus(400);
      return;
    }
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
    if(!req.body.sender || !req.body.dest || !req.body.message || !req.body.token) {
      res.sendStatus(400);
      return;
    }
    var sender = req.body.sender;
    var dest = req.body.dest;
    var message = req.body.message;
    var token = req.body.token;
    storage.verifyToken(sender, token, function(success) {
      handleResult(success, res, function() {
        storage.addMessage(sender, dest, message, function(success) {
          handleResult(success, res);
        });
      });
    });
  });

  function handleResult(result, res, cb) {
    if(typeof result == "boolean") {
      if(result) {
        if(cb)
          cb();
        else
          res.send({success: true});
      }
      else
        res.send({success: false});
    } else {
      res.send(result)
    }
  }
}
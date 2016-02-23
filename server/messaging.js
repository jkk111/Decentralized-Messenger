  module.exports = function(app, storage) {

  app.post("/refreshToken", function(req, res) {
    if(!req.body.token) {
      res.sendStatus(400);
      return;
    }
    var token = req.body.token;
    storage.refreshToken(token, function(success) {
      if(typeof success == "boolean") {
        res.send({ success: success });
      } else {
        res.send(token);
      }
    })
  });

  app.post("/login", function(req, res) {
    if(!req.body.user || ! req.body.password) {
      res.sendStatus(400);
      return;
    }
    var user = req.body.user;
    var password = req.body.password;
    storage.userExists(user, function(exists) {
      if(!exists) {
        res.send({ error: "ERROR_BAD_LOGIN" });
      } else {
        storage.login(user, password, function(success) {
          if(typeof success == "boolean") {
            res.send({ success: success });
          } else {
            res.send(success);
          }
        });
      }
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
      if(exists) {
        res.send({ error: "ERROR_USER_EXISTS" });
      } else {
        storage.register(user, password, function(success) {
          if(typeof success == "boolean" && success) {
            storage.login(user, password, function(success) {
              res.send(success);
            });
          } else {
            res.send(success);
          }
        });
      }
    });
  });

  app.post("/messages", function(req, res) {
    if(!req.body.sender || ! req.body.token) {
      res.sendStatus(400);
      return;
    }
    var sender = req.body.sender;
    var token = req.body.token;
    console.log(token);
    storage.verifyToken(sender, token, function(success) {
      if(success) {
        console.log("getting messages");
        storage.getMessages(sender, function(messages) {
          console.log(messages);
          res.send(messages);
        })
      } else {
        res.send({ error: "ERROR_BAD_TOKEN" })
      }
    });
  })

  app.post("/confirmFriend", function(req, res) {
    // Todo allow confirm/deny
    var response = req.body.confirm;
    var token = req.body.token;
    var sender = req.body.sender;
    var friendshipId = req.body.friendshipId; //
    storage.verifyToken(sender, token, function(success) {
      if(success) {
        storage.updateFriendship(friendshipId, response, function(success) {
          if(typeof success == "Boolean") {
            res.send({ success: success })
          } else {
            res.send(success);
          }
        });
      } else {
        res.send({ error: "ERROR_BAD_TOKEN" })
      }
    });
    res.send("not implemented")
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
      if(success) {
        storage.receivedMessages(sender, highest, function(success) {
          if(typeof success == "boolean") {
            res.send({ success: success });
          } else {
            res.send(success);
          }
        })
      } else {
        res.send({ error: "ERROR_BAD_TOKEN" })
      }
    });
  })

  app.post("/getFriends", function(req, res) {
    var sender = req.body.sender;
    var token = req.body.token;
    if(!sender || ! token) {
      res.sendStatus(400);
      return
    }
    storage.verifyToken(sender, token, function(success) {
      if(success) {
        storage.getFriends(sender, function(success) {
          if(typeof success == "boolean") {
            res.send({ success: success });
          } else {
            res.send(success);
          }
        });
      } else {
        res.send({ error: "ERROR_BAD_TOKEN" });
      }
    })
  });

  app.post("/addFriend", function(req, res) {
    var sender = req.body.sender;
    var token = req.body.token;
    var client = req.body.client;
    var secret = req.body.secret || "";
    if(!(sender && token && client)) {
      res.sendStatus(400);
      return;
    }
    storage.verifyToken(sender, token, function(success) {
      if(success) {
        storage.userExists(client, function(exists) {
          if(exists)
            storage.addFriend(sender, client, secret, function(success) {
              if(typeof success == "boolean") {
                res.send({ success: success });
              } else {
                res.send(success);
              }
            });
          else
            res.send({ error: "USER_NOT_EXISTS" });
        });
      } else {
        res.send({ error: "ERROR_BAD_TOKEN" });
      }
    })
  });

  app.post("/search", function(req, res) {
    var sender = req.body.sender;
    var token = req.body.token;
    var query = req.body.query;
    storage.verifyToken(sender, token, function(success) {
      if(success) {
        storage.search(query, function(result) {
          res.send(result);
        })
      } else {
        res.send({error: "ERROR_BAD_TOKEN"});
      }
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
      if(success) {
        storage.idFromName(client, function(id) {
          if(id) {
            storage.addFriend(sender, client, secret, function(success) {
              if(typeof success == "boolean") {
                res.send({ success: success });
              } else {
                res.send(success);
              }
            });
          } else {
            res.send({error: "USER_NOT_EXISTS"});
          }
        });
      } else {
        res.send({ error: "ERROR_BAD_TOKEN" });
      }
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
      if(success) {
        storage.addMessage(sender, dest, message, function(success) {
          if(typeof success == "boolean") {
            res.send({ success: success });
          } else {
            res.send(success);
          }
        });
      } else {
        res.send({ error: "ERROR_BAD_TOKEN" });
      }
    })
  })
}

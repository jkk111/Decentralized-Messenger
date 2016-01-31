module.exports = function(app, storage) {
  /*
    Message format
    {
      sender: "client that sent the message",
      token: "Token that proves the client 'is' the client",
      message: "The message to send to the recipient",
      recipient: "The id of the recipient for the server"
    }
  */

  app.get("/login", function(req, res) {
    var u = req.query.u;
    var p = req.query.p;
    storage.login(u, p, function(result) {
      res.send(result);
    })
  });

  app.get("/register", function(req, res) {
    var u = req.query.u;
    var p = req.query.p;
    storage.register(u, p, function(result) {
      res.send(result);
    })
  });

  app.get("/refreshToken", function(req, res) {
    var t = req.query.t;
    t = t.replace(/\s/g, "+");
    storage.refreshToken(t, function(success) {
      if(typeof success == "boolean") {
        res.send({ success: success });
      } else {
        res.send(token);
      }
    })
  })

  app.post("/refreshToken", function(req, res) {
    var token = req.body.token;
    storage.refreshToken(token, function(success) {
      if(typeof success == "boolean") {
        res.send({ success: success });
      } else {
        res.send(token);
      }
    })
  })

  app.post("/messages", function(req, res) {
    var id = req.body.id;
    var token = req.body.token;
    storage.verifyToken(id, token, function(success) {
      if(success) {
        storage.getMessages(id)
      } else {
        res.send({ error: "ERROR_BAD_TOKEN" })
      }
    })
    storage.getMessages(u, t, function(messages) {
      res.send(messages);
    })
  });


  app.post("/login", function(req, res) {
    var user = req.body.user;
    var password = req.body.password;
    storage.userExists(user, function(exists) {
      if(exists) {
        res.send({ error: "ERROR_USER_EXISTS" });
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
    var user = req.body.user;
    var password = req.body.password;
    storage.userExists(user, function(exists) {
      if(exists) {
        res.send({ error: "ERROR_USER_EXISTS" });
      } else {
        storage.register(user, password, function(success) {
          if(typeof success == "boolean") {
            res.send({ success: success });
          } else {
            res.send(success);
          }
        });
      }
    });
  });

  app.post("/messages", function(req, res) {
    var id = req.body.id;
    var token = req.body.token;
    storage.verifyToken(id, token, function(success) {
      if(success) {
        storage.getMessages(id, token, function(messages) {
          res.send(messages);
        })
      } else {
        res.send({ error: "ERROR_BAD_TOKEN" })
      }
    });
  })

  app.post("/message", function(req, res) {
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

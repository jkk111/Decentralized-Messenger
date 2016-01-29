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

  app.get("/messages", function(req, res) {
    var u = req.query.u;
    var t = req.query.t;
    storage.getMessages(u, t, function(messages) {
      res.send(messages);
    })
  });

  app.post("/message", function(req, res) {
    var sender = req.body.sender;
    var dest = req.body.dest;
    var message = req.body.message;
    var token = req.body.token;
    storage.verifyToken(sender, token, function(success) {
      if(success) {
        storage.addMessage(sender, dest, message, function(success) {
          // TODO (john): Inform client the message was successfully received.
        });
      } else {
        // TODO (john): Add error handling. Inform client of failure to verify.
      }
    })
  })
}

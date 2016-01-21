module.exports = function(app) {
  // TODO (jonn): Add fuctionality to send messages, receive messages, serve static web pages.
  app.get("/:user/messages", function(req, res) {
    res.send("your token: " + req.query.token + " your id" + req.params.id);
  });
}

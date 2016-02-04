/*
  Static web server for development of the web front end.
*/
var express = require("express");
var app = express();
app.use(express.static("www"));
app.listen(80);
var id = 0;
var messageId = 0;
var bp = require("body-parser");
app.use(bp.urlencoded({extended: true}));
var messages = [];
var users = {

}

app.get("/secret", function(req, res) {
  res.send({ key: "Hello world", secret: "sample secret" });
})

app.post("/register", function(req, res) {
  console.log("hit");
  users[req.body.user] = id;
  res.send({success: true, token: "sample token", id: id++ })
});

app.post("/login", function(req, res) {
  res.send({success: true, token: "sample token", id: users[req.body.user] });
});

app.post("/message", function(req, res) {
  messages.push({ message: req.body.message, id: messageId++ })
})

app.post("/messages", function(req, res) {

});

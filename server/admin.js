/*
 * Admin Panel for decentralized-messenger
 * Serves an admin panel and communicates with the client using websockets
 */
var conn;
var fs = require("fs");
var express = require("express");
var app = express();
var httpUpgrade = express();
var auth = require("./nodeAuth.js");
var cookieParser = require("cookie-parser");
app.use(cookieParser());

httpUpgrade.get("/*", function(req, res) {
  res.redirect("https://" + req.hostname + ":8443" + req.url);
});

var opts = {
  key: fs.readFileSync("ssl.key"),
  cert: fs.readFileSync("ssl.crt")
}

var http = require("http");
var https = require("https");
http.createServer(httpUpgrade).listen(8080);
https.createServer(opts, app).listen(8443, function() {
  console.log("Webserver running on port: %d Process: %d", 8443, process.pid);
});

module.exports = function(config) {
  var mysql = require("mysql2");
  databaseUser = config.databaseUser;
  databasePassword = config.databasePassword;
  databaseHost = config.databaseHost;
  databasePort = config.databasePort;
  databaseName = config.databaseName;

  conn = mysql.createConnection({
    user: databaseUser,
    password: databasePassword,
    host: databaseHost,
    port: databasePort,
    database: databaseName,
    multipleStatements: true,
    namedPlaceholders: true
  });
  app.use(auth({username: config.serverAdmin, password: config.serverPassword}))
  app.use(express.static("admin"));
  app.get("/", function(req, res) {
    res.send("It works");
  })
  var io = require("socket.io")(https);
  io.on("connection", function(socket) {

    setInterval(function() {
      if(socket.authenticated) {
        getUpdateData(function(data) {
          socket.emit("update", data);
        })
      }
    }, 1000)

    socket.on("authenticate", function(data) {
      if(data.user == user && data.pass == pass) {
        socket.authenticated = true;
        socket.emit("authenticated");
      }
    });
  });
}

function getUpdateData(cb) {
  cb = cb || function() {}
  var q = "SELECT COUNT(*) as numMessages from messages";
  conn.query(q, function(err, results) {
    if(err) {
      return cb({error: "DB"});
    }
    return cb(results);
  })
}
/*
 * Admin Panel for decentralized-messenger
 * Serves an admin panel and communicates with the client using websockets
 */
var conn;
var fs = require("fs");
var express = require("express");
var app = express();
var auth = require("./nodeAuth.js");
var cookieParser = require("cookie-parser");
app.use(cookieParser());
var connected = 0, errors = 0;

module.exports = function(config, standalone, server) {
  if(standalone || standalone === undefined) {
    var httpUpgrade = express();
    httpUpgrade.get("/*", function(req, res) {
      if(req.hostname && req.hostname.indexOf(":") != - 1)
        res.redirect("https://" + req.hostname.substring(0, req.hostname.indexOf(":")) + ":" + (config.securePort || 8443) + req.url);
      else
        res.redirect("https://" + req.hostname + ":" + (config.securePort || 8443) + req.url);
    });
    var opts = {
      key: fs.readFileSync("ssl.key"),
      cert: fs.readFileSync("ssl.crt")
    }
    var http = require("http");
    var https = require("https");
    http.createServer(httpUpgrade).listen(config.port || 8080);
    server = https.createServer(opts, app).listen(config.securePort || 8443, function() {
      console.log("Webserver running on port: %d Process: %d", config.securePort || 8443, process.pid);
    });
  } else {
    this.route = app;
  }
  var io = require("socket.io")(server);
  io.on("connection", function(socket) {
    socket.on("authenticate", function(data) {
      if(data.user == user && data.pass == pass) {
        socket.authenticated = true;
        socket.emit("authenticated");
      }
    });
  });

  app.use(auth({username: config.serverAdmin, password: config.serverPassword}))
  app.use(express.static(__dirname + "/admin"));
  app.get("/", function(req, res) {
    res.send("It works");
  })

  this.pushLog = function(log) {
    if(io) {
      var logData = {}
      var parts = log.split(" ");
      logData.worker = parts[0];
      logData.time = parts[1];
      // logData.caller = parts[2];
      logData.ip = parts[3];
      logData.method = parts[4].substring(1, parts[4].length - 1);
      logData.route = parts[5];
      logData.status = parts[6];
      logData.latency = parts[7].substring(0, parts[7].length);
      io.emit("log", logData);
    }
  }
  this.conn = function() {
    connected++;
    io.emit("connected", connected);
  }
  this.disco = function() {
    connected--;
    io.emit("connected", connected);
  }
  this.error = function() {
    errors++;
    io.emit("errors", errors);
  }
  return this;
}
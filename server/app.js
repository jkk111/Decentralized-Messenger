/*
 * Main file for Decentralized-Messenger
 * Imports necessary modules, starts the admin panel,
 * Then creates multiple forks of the process to run the application
 */
var fs = require("fs");
var conf;
try {
  conf = JSON.parse(fs.readFileSync("config.json", "utf-8"));
} catch(e) {
  console.log(e);
  console.error("Could not open configuration file!");
  process.exit();
}
var d = new Date();
var express = require("express");
var app = express();
var httpUpgrade = express();
var http = require("http");
var https = require("https");
var logger = require("./logger.js")(process.pid, process.env.StartTime || d, true);
var bodyParser = require("body-parser");
var cookieParser = require("cookie-parser");
app.use(logger);
app.use(bodyParser.urlencoded({ extended: true }));
app.use(function(req, res, next) {
  res.set({
    "X-Frame-Options": "DENY",
    "Strict-Transport-Security": "max-age=31536000",
    "Schrodingers-Cat-State": "Unknown"
  });
  next();
});

httpUpgrade.get("/*", function(req, res) {
  res.redirect("https://" + req.hostname + ":" + conf.securePort + req.url);
});
app.use(cookieParser());
app.use(express.static("../web/www"));

var opts = {
  key: fs.readFileSync("ssl.key"),
  cert: fs.readFileSync("ssl.crt"),
  ca: fs.readFileSync("root.crt")
}

var request = require("request");

var ct = require("./crosstalk.js")(conf);
const cluster = require('cluster');
const numCPUs = conf.threads || require('os').cpus().length;

// Start a thread for each cpu core
if(cluster.isMaster) {
  for(var i = 0 ; i < numCPUs; i++) {
    cluster.fork({StartTime: d});
  }
  cluster.on("exit", function(worker, code, signal) {
    console.log("a worker died, restarting....");
    cluster.fork();
  });
  require("./admin.js");
} else {
  http.createServer(httpUpgrade).listen(conf.serverPort);
  https.createServer(opts, app).listen(conf.securePort, function() {
    console.log("Webserver running on port: %d Process: %d", conf.securePort, process.pid);
  });
  var storage = require("./storage.js")(conf);
  var messaging = require("./messaging.js")(app, storage, ct, conf);
}
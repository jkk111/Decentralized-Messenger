var express = require("express");
var app = express();
var httpUpgrade = express();
var http = require("http");
var https = require("https");
var fs = require("fs");
var bodyParser = require("body-parser");
var cookieParser = require("cookie-parser");
app.use(bodyParser.urlencoded({ extended: true }));
app.use(function(req, res, next) {
  console.log(req.url);
  next();
});
app.use(function(req, res, next) {
  res.set({
    "X-Frame-Options": "DENY",
    "Strict-Transport-Security": "max-age=31536000",
    "Schrodingers-Cat-State": "Unknown"
  });
  next();
});

httpUpgrade.get("/*", function(req, res) {
  res.redirect("https://" + req.headers.host + req.url);
});
app.use(cookieParser());
app.use(express.static("../web/www"));

var opts = {
  key: fs.readFileSync("ssl.key"),
  cert: fs.readFileSync("ssl.crt"),
  ca: fs.readFileSync("root.crt")
}

var request = require("request");
var conf;

try {
  conf = JSON.parse(fs.readFileSync("config.json", "utf-8"));
} catch(e) {
  console.log(e);
  console.error("Could not open configuration file!");
  process.exit();
}

const cluster = require('cluster');
const numCPUs = conf.threads || require('os').cpus().length;

if(cluster.isMaster) {
  for(var i = 0 ; i < numCPUs; i++) {
    cluster.fork();
  }
  cluster.on("exit", function(worker, code, signal) {
    console.log("a worker died, restarting....");
    cluster.fork();
  });
} else {
  http.createServer(httpUpgrade).listen(conf.serverPort);
  https.createServer(opts, app).listen(443, function() {
    console.log("Webserver running on port: 443");
  });
  var storage = require("./storage.js")(conf);
  var routes  = require("./routes.js")(app, storage, conf);
}
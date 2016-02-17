var express = require("express");
var app = express();
var bodyParser = require("body-parser");
var cookieParser = require("cookie-parser");
app.use(bodyParser.urlencoded({ extended: true }));
app.use(cookieParser());
app.use(function(req, res, next) {
  console.log(req.protocol + ":" + req.secure + ":" + req.cookies.confirmDeprecation);
  if(req.secure) {
    next();
  } else {
    if(!req.cookies.confirmDeprecation) {
      res.cookie("confirmDeprecation", true);
      res.status(301)
      res.write("HTTP IS DEPRECATED! COOKIE STORED<br/>, RELOAD TO ACCESS INSECURE RESOURCE will be moved shortly, change to https!");
      res.end();
    } else {
      console.log("confirmedDeprecated")
      next();
    }
  }
});
app.use(express.static("../web/www"));
var http = require("http");
var https = require("https");
var fs = require("fs");
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
  http.createServer(app).listen(conf.serverPort);
  // Switching to https, soon there will be no http
  https.createServer(opts, app).listen(443, function(e) {
    console.log(e);
  });
  var storage = require("./storage.js")(conf);
  var routes  = require("./routes.js")(app, storage, conf);
}

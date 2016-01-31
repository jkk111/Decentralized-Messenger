var express = require("express");
var app = express();
var bodyParser = require("body-parser");
app.use(bodyParser.urlencoded({ extended: true }));
app.use(express.static("static"));
var fs = require("fs");
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
const http = require('http');
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
  app.listen(8080);
  var storage = require("./storage.js")(conf);
  var routes  = require("./routes.js")(app, storage, conf);
}

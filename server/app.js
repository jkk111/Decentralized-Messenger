var express = require("express");
var fs = require("fs");
var app = express();
app.use(express.static("static"));
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
  var storage = require("./storage.js")(conf);
  var routes  = require("./routes.js")(app, storage, conf);
  app.listen(80);
}

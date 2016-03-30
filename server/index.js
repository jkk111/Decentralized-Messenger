/*
 * modularized main file for Decentralized-Messenger
 * Imports necessary modules, starts the admin panel,
 * Then creates multiple forks of the process to run the application
 */
module.exports = function(cluster) {
  var fs = require("fs");
  var conf;
  try {
    conf = JSON.parse(fs.readFileSync(__dirname + "/config.json", "utf-8"));
  } catch(e) {
    console.log(e);
    console.error("Could not open configuration file!");
    process.exit();
  }
  var d = new Date();
  var express = require("express");
  var app = express();
  var logger = require("./logger.js")(process.pid, process.env.StartTime || d, true);
  var bodyParser = require("body-parser");
  var cookieParser = require("cookie-parser");
  app.use(logger.route);
  app.use(bodyParser.urlencoded({ extended: true }));
  app.use(function(req, res, next) {
    res.set({
      "X-Frame-Options": "DENY",
      "Strict-Transport-Security": "max-age=31536000",
      "Schrodingers-Cat-State": "Unknown"
    });
    next();
  });

  app.use(cookieParser());
  app.use(express.static("../web/www"));

  var ct = require("./crosstalk.js")(conf);
  const numCPUs = conf.threads || require('os').cpus().length;

  // Start a thread for each cpu core
  if(cluster.isMaster) {
    var admin = require("./admin.js")(conf, false);
    app.use("/admin", admin.route);
    for(var i = 0 ; i < numCPUs; i++) {
      var worker = cluster.fork({StartTime: d});
      worker.on("message", function(data) {
        logHandler(data, admin);
      });
    }
    cluster.on("exit", function(worker, code, signal) {
      console.log("a worker died, restarting....");
      var worker = cluster.fork({StartTime: d});
      worker.on("message", function(data) {
        logHandler(data, admin);
      })
    });

    logger.addListener(function(data) {
      logHandler(data, admin);
    })
  } else {
    logger.addListener(masterListener);
    var storage = require("./storage.js")(conf);
    var messaging = require("./messaging.js")(app, storage, ct, conf);
  }

  function masterListener(key, data) {
    var el = {}
    el[key] = data;
    process.send(el);
  }

  function logHandler(data, admin) {
    if(data.log)
      admin.pushLog(data.log);
    else if(data.conn)
      admin.conn();
    else if(data.disco)
      admin.disco();
  }
  return app;
}
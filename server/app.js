/*
 * Entry point into the program, starts up a server and registers all required listeners.
 */
var cluster = require("cluster");
var http = require("http");
var https = require("https");
var fs = require("fs");
var d = new Date();
var logger = require("./logger")(process.pid, process.env.StartTime || d, true);
var express = require("express"),
app = express(),
upgrade = express();
var conf;
try {
  conf = JSON.parse(fs.readFileSync(__dirname + "/config.json", "utf-8"));
} catch(e) {
  console.log(e);
  console.error("Could not open configuration file!");
  process.exit();
}
var httpPort = conf.serverPort || 80;
var httpsPort = conf.securePort || 443;
var adminPort = conf.adminPort || 8080;
var adminHttpsPort = conf.adminSecurePort || 8443;
if(cluster.isMaster) {
  var admin = require(__dirname + "/admin.js")({serverAdmin: "admin", serverPassword: "admin",
                                                port: adminPort, securePort: adminHttpsPort});
  for(var i = 0 ; i < require("os").cpus().length; i++) {
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
  function logHandler(data, admin) {
    if(data.log)
      admin.pushLog(data.log);
    else if(data.conn)
      admin.conn();
    else if(data.disco)
      admin.disco();
  }
} else {
  app.use(function(req, res, next) {
    res.set({
      "X-Frame-Options": "DENY",
      "Strict-Transport-Security": "max-age=31536000; includeSubDomains; preload",
      "Schrodingers-Cat-State": "Unknown"
    });
    next();
  });

  var opts = {
    key: fs.readFileSync(__dirname + "/ssl.key"),
    cert: fs.readFileSync(__dirname + "/ssl.crt"),
    ca: fs.readFileSync(__dirname + "/root.crt")
  }

  http.createServer(upgrade).listen(httpPort, function() {
    console.log("http listening on port: %d", httpPort)
  });

  var server = https.createServer(opts, app).listen(httpsPort, function() {
    console.log("https listening on port: %d", httpsPort);
  });

  upgrade.get("/*", function(req, res) {
    res.redirect("https://" + req.hostname + ":" + httpsPort + req.url);
  });

  app.use(require(__dirname + "/index.js")(logger))
}
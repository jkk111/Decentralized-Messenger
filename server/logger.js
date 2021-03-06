/*
 * Simplified and less generic version of my logger module
 * Format "stackTrace", "method", "url", "statusCode", "requestBody", "time to fulfil request"
 */
var fs = require("fs");
var logFile;
var threadId;
var print = true;
var d;
var listeners = [];
module.exports = function(id, StartTime, silent) {
  print = silent === undefined ? true: !silent;
  d = new Date(StartTime);
  threadId = id;
  logFile = fs.createWriteStream(__dirname + "/logs/" + getTimeString(d) +".log", { flags: "a", defaultEncoding: "utf8"});
  return {route: route, addListener: addListener};
}


function addListener (listener) {
  listeners.push(listener);
}

function getTimeString(d) {
  return fNum(d.getDate()) + "-"+ fNum(d.getMonth() + 1) + "-" + d.getUTCFullYear() + "-"
               + fNum(d.getHours()) + "-" + fNum(d.getMinutes()) + "-" + fNum(d.getSeconds()) + "-" + fNum(d.getMilliseconds(), 4);
}

function getTimeStringLine(d) {
  return fNum(d.getDate()) + "-"+ fNum(d.getMonth() + 1) + "-" + d.getUTCFullYear() + "-"
               + fNum(d.getHours()) + ":" + fNum(d.getMinutes()) + ":" + fNum(d.getSeconds()) + ":" + fNum(d.getMilliseconds(), 4);
}

function route(req, res, next) {
  if(listeners && listeners.length > 0) {
    for(var i = 0 ; i < listeners.length; i++) {
      listeners[i]("conn", true);
    }
  }
  req.StartTime = new Date();
  res.on("finish", function() {
    if(listeners && listeners.length > 0) {
      for(var i = 0 ; i < listeners.length; i++) {
        listeners[i]("disco", true);
      }
    }
    generateLog(req, res);
  })
  res.on("close", function() {
    if(listeners && listeners.length > 0) {
      for(var i = 0 ; i < listeners.length; i++) {
        listeners[i]("error", true);
      }
    }
    generateLog(req, res);
  })
  next();
}

function generateLog(req, res) {
  var logStr = "[" + req.method + "] " + req.path + " " + res.statusCode + " " + JSON.stringify(req.body || {}) + " " + ((new Date().getTime() - req.StartTime.getTime())  + "ms");
  var noBodyLog = req.ip + " [" + req.method + "] " + req.path + " " + res.statusCode + " " + ((new Date().getTime() - req.StartTime.getTime())  + "ms");
  var caller = getCaller();
  var line = formatString(logStr, caller);
  logFile.write(line);
  if(listeners && listeners.length > 0) {
    for(var i = 0 ; i < listeners.length; i++) {
      listeners[i]("log", formatString(noBodyLog, caller).reverseSubstring(1));
    }
  }
  if(print)
    console.log(logStr);
}

function formatString(str, o) {
  var res = "";
  var lines = str.split("\n");
  for(var i = 0 ; i < lines.length; i++) {
    res += threadId + " " + getTimeStringLine(new Date()) + " " + o.file + "." + o.method + "." + o.line + ") " + lines[i] + "\n"
  }
  return res;
}

function getCaller() {
  var e = new Error();
  var line = e.stack.split("\n")[3];
  var method = e.stack.split("\n")[3].substring(line.indexOf("at") + 3,
               line.indexOf("(")).trim();
  if(method == "Object.<anonymous>") {
    method = "<main>";
  }
  line = line.substring(line.lastIndexOf("(") + 1, line.lastIndexOf(")"));
  var isWin = /^win/.test(process.platform);
  var lineType = isWin ? "\\" : "/";
  line = line.substring(line.lastIndexOf(lineType) + 1)
  var parts = line.split(":");
  var res = {};
  res.file = parts[0];
  res.line = parts[1];
  res.char = parts[2];
  res.method = method;
  return res;
}


function fNum(num, digits) {
  digits = digits || 2;
  var pre = "";
  for(var i = 0; i < digits; i++)
    pre += "0";
  return String(pre + num).slice( -1 * digits);
}

String.prototype.reverseSubstring = function(num) {
  return this.substring(0, this.length - num);
};
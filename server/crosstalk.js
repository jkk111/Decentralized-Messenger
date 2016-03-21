var hosts = [];
var request = require("request");
module.exports = function(config) {
  config.crosstalk = config.crosstalk || {};
  hosts = config.crosstalk.hosts || [];
  return new initCrosstalk();
}

function initCrosstalk() {
  this.forward = function(url, form, cb) {
    var pending = hosts.length;
    if(pending == 0)
      return cb({success: true});
    for(var i = 0 ; i < hosts.length; i++) {
      form.crosstalk = true;
      var opt = {
        url: hosts[i] + url,
        form: form
      }
      sendRequest(opt, function(data) {
        pending--;
        if(data.error) {
          return cb(data);
        }
        if(pending == 0)
          return cb({success: true});
      });
    }
  }

  this.login = function(form, cb) {
    var pending = hosts.length;
    if(pending == 0)
      return cb({success: true});
    for(var i = 0 ; i < hosts.length; i++) {
      form.crosstalk = true;
      var opt = {
        url: hosts[i] + "/login",
        form: form
      }
      sendRequest(opt, function(data) {
        pending--;
        if(data.error) {
          return cb(data);
        }
        if(pending == 0) {
          return cb({success: true});
        }
      });
    }
  }
  return this;
}

function sendRequest(opt, cb) {
  request.post(opt, function(err, data, body) {
    if(err) {
      console.log(err);
      return cb({ error: "REPLICATION_ERROR" });
    }
    try {
      body = JSON.parse(body);
    } catch(e) {
      return cb({error: "INVALID_REPLICATION_RESPONSE"});
    }
    return cb({success: body.error === undefined});
  });
}
/*
 * Authentication module
 * Can be extended to use a database, external error handler, use custom cookie expiries and provide custom messages.
 */

var crypto = require("crypto");

function loginAuth(req, res, next) {
  if(!req.cookies) {
    throw new Error("cookie parser must be before auth");
    process.exit();
  }
  if(!req.cookies.auth || req.headers.authorization) {
    if(req.headers.authorization) {
      checkCredentials(req, res, next);
    } else {
      loginHandler(res);
    }
  } else {
    checkCookie(req, res, next);
  }
}

module.exports = init;

var cookies = [];
var message = "Secure Content"
var user, pass, unauthorizedHandler, authHandler, cookieStoreHandler, cookieCheckHandler, expiry;

function init(settings) {
  if(!settings) {
    throw new Error("Settings is required");
  }
  if(settings.authenticator) {
    authHandler = settings.authenticator;
  } else {
    if(!settings.username || !settings.password) {
      throw new Error("Username or password not specified, and no authenticator provided!")
      process.exit();
    }
    authHandler = simpleAuthHandler;
    user = settings.username;
    pass = settings.password;
  } if(settings.cookieStore) {
    cookieStoreHandler = settings.cookieStore;
  } else {
    cookieStoreHandler = simpleCookieStore;
  }
  if(settings.cookieCheck) {
    cookieCheckHandler = settings.cookieCheckHandler;
  } else {
    cookieCheckHandler = simpleCookieCheck;
  }
  if(settings.unauthorized) {
    unauthorizedHandler = settings.unauthorized;
  } else {
    unauthorizedHandler = unauthorized;
  }
  if(settings.message) {
    message = settings.message;
  }
  if(settings.cookieExpiry) {
    expiry = settings.cookieExpiry;
  }
  return loginAuth;
}

function checkCredentials(req, res, next) {
  var auth = req.headers.authorization;
  var type = auth.substring(0, 5);
  var code = auth.substring(6);
  code = new Buffer(code, 'base64').toString("utf8")
  var creds = code.split(":");
  authHandler(creds[0], creds[1], function(success) {
    if(success) {
      var cookie = crypto.randomBytes(16).toString("base64");
      var opts = {};
      if(expiry) {
        opts.expiry = new Date() + expiry;
      }
      res.cookie("auth", cookie, opts);
      cookieStoreHandler(cookie);
      next();
    } else {
      unauthorizedHandler(res);
    }
  });
}

function loginHandler(res) {
  res.status(401);
  res.setHeader('WWW-Authenticate', 'Basic realm="' + message + '"');
  res.end("denied");
}

function checkCookie(req, res, next) {
  var cookie = req.cookies.auth;
  cookieCheckHandler(cookie, function(success) {
    if(success) {
      next();
    } else {
      res.cookie("auth", "", { expires: new Date() });
      loginHandler(res);
    }
  })
}

function unauthorized(res) {
  loginHandler(res);
}

function simpleAuthHandler(username, password, callback) {
  var result = (username == user && password == pass);
  callback(result);
}

function simpleCookieStore(cookie) {
  cookies.push(cookie);
}

function simpleCookieCheck(cookie, callback) {
  for(var i = 0 ; i < cookies.length; i++) {
    if(cookies[i] == cookie) {
      callback(true);
      return;
    }
  }
  callback(false);
}

var mysql = require("mysql");
var crypto = require("crypto");
var salt, databaseUser, databasePassword, databaseHost
    ,databaseName, databasePort, connector, conn, tokenCache;
module.exports = function(config) {
  connector = {};
  databaseUser = config.databaseUser;
  databasePassword = config.databasePassword;
  databaseHost = config.databaseHost;
  databasePort = config.databasePort;
  databaseName = config.databaseName;
  salt = config.salt;

  conn = mysql.createConnection({
    user: databaseUser,
    password: databasePassword,
    host: databaseHost,
    port: databasePort,
    database: databaseName
  });

  tokenCache = {}

  // TODO (john): Stop logging password #DEBUG
  console.log("Connecting to MySql server on %s using: user %s, pass %s",
               databaseHost, databaseUser, databasePassword);

  /*
   * Queries are in need of some optimization, but gets the job done.
   */
  connector.verifyToken = function(user, token, cb) {
    console.log("Checking if user %s is authorized to use token %s", user, token);
    if(tokenCache[token]) {
      if(tokenCache[token].expiry <= new Date().getTime()) {
        tokenCache[token] = undefined;
      }
      cb(tokenCache[token] && tokenCache[token].user == user);
    }
    var q = "SELECT expiry FROM tokens WHERE user = ? AND token = ? \
             AND expiry > NOW() ORDER BY expiry DESC;";
    conn.query(q, [user, token], function(err, results) {
      if(err) {
        console.log(err);
      }
      else {
        if(results) {
          tokenCache[token] = { user: user, expiry: new Date(results[0].expiry) };
        }
      }
      cb(!err && results != undefined);
    });
  }

  connector.userExists = function(user, cb) {
    var q = "SELECT id FROM users WHERE username = ?";
    conn.query(q, user, function(err, results) {
      console.log(results)
      cb(!err && results != undefined && results.length > 0);
    })
  }

  connector.userIdExists = function(user, cb) {
    var q = "SELECT id FROM users WHERE id = ?";
    conn.query(q, user, function(err, results) {
      console.log(results)
      cb(!err && results != undefined && results.length > 0);
    })
  }

  connector.login = function(user, pass, cb) {
    var q = "SELECT id FROM users WHERE username = ? AND password = ?";
    pass = generateHash(pass);
    conn.query(q, [user, pass], function(err, results) {
      if(err) {
        console.log(err);
      }
      if(results && results.length > 0)
        generateToken(results[0].id, cb);
      else
        cb({ success: false });
    });
  }

  connector.register = function(user, pass, cb) {
    var hash = generateHash(pass);
    var q = "INSERT INTO users (username, password) VALUES(?, ?)";
    conn.query(q, [user, hash], function(err, results) {
      if(err)
        console.log(err);
      cb({ success: true, id: results.insertId });
    })
  }

  connector.addMessage = function(sender, dest, message, cb) {
    connector.userIdExists(dest, function(exists) {
      if(exists) {
        conn.query(q, [sender, dest, message], function(err, results) {
          if(err) {
            console.log(err);
          }
          cb(err == undefined);
        });
      } else {
        cb({ error: "ERROR_RECIPIENT_NOT_EXISTS" });
      }
    });



    console.log("Adding message %s for client %s from client %s", message, dest, sender);
    var q = "INSERT INTO messages (sender, recipient, message) VALUES(?, ?, ?)";
    conn.query(q, [sender, dest, message], function(err, results) {
      if(err) {
        console.log(err);
      }
      cb(err == undefined);
    });
  }

  connector.recievedMessages = function(sender, token, highest, cb) {
    var q = "DELETE FROM messages WHERE user = ? AND id <= ?";
    conn.query(q, [sender, highest], function(err, results) {
      if(err) {
        console.log(err);
      }
      cb(err == undefined);
    });
  }

  connector.refreshToken = function(token, cb) {
    var q = "UPDATE tokens SET expiry = NOW() + INTERVAL 6 HOUR WHERE token = ?";
    console.log(token);
    conn.query(q, [token], function(err, results) {
      if(err) {
        console.log(err);
        return cb(false);
      }
      if(results.changedRows == 0) {
        cb({ error: "ERROR_BAD_TOKEN" });
      } else {
        cb(true);
      }
    })
  }

  connector.getMessages = function(sender, cb) {
    var q = "SELECT id, sender, message FROM messages WHERE recipient = ?";
    conn.query(q, [sender], function(err, results) {
      if(err)
        console.log(err);
      var messages = {};
      if(results) {
        for(var i = 0 ; i < results.length; i++) {
          var item = results[i];
          if(!messages[item.sender])
            messages[item.sender] = [];
          messages[item.sender].push({id: item.id, message: item.message});
        }
      }
      cb(messages);
    })
  }
  return connector;
}

function pruneExpiredTokens() {
  console.log("pruning expired tokens");
  if(tokenCache) {
    var keys = Object.keys(tokenCache);
    for(var i = 0 ; i < keys.length; i++) {
      if(tokenCache[keys[i].expiry < new Date()])
        tokenCache[keys[i]] = undefined;
    }
  }
  var q = "DELETE FROM tokens WHERE expiry < NOW()";
  conn.query(q, function(err, res) {
    if(err)
      console.log(err);
  })
}

function generateHash(pass) {
  return crypto.pbkdf2Sync(pass, salt, 1000, 512, "sha512");
}

function addToken(user, token, cb) {
  var q = "INSERT INTO tokens VALUES(?, ?, NOW() + INTERVAL 2 HOUR);"
  conn.query(q, [user, token], function(err, rows, fields) {
    if(err) {
      console.log(err);
      return cb({ success: false, error: "Storing token failed" });
    }
    cb({ success: true, token: token });
  })
}

function generateExpiry() {
  var d = new Date();
  d.setHours(d.getHours() + 24);
  return d.toISOString().slice(0, 19).replace('T', ' ');
}

function generateToken(user, cb) {
  var token = crypto.randomBytes(64).toString("base64");
  addToken(user, token, cb);
}

setInterval(pruneExpiredTokens, 1000 * 60);

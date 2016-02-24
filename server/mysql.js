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
    database: databaseName,
    multipleStatements: true
  });

  tokenCache = {}

  /*
   * Queries are in need of some optimization, but gets the job done.
   */
  connector.verifyToken = function(user, token, cb) {
    if(tokenCache[token]) {
      var tokenObj = tokenCache[token]
      if(tokenObj.expiry <= new Date().getTime()) {
        tokenCache[token] = undefined;
        connector.verifyToken(user, token, cb);
        return;
      }
      console.log("is in cache")
      return cb((tokenCache[token] && tokenCache[token].user == user) ? true : {error: "ERROR_BAD_TOKEN"});
    }

    var q = "SELECT expiry FROM tokens WHERE user = ? AND token = ? \
             AND expiry > NOW() ORDER BY expiry DESC;";

    conn.query(q, [user, token], function(err, results) {
      if(err) {
        cb({ error: "DATABASE_ERROR" });
      }
      else {
        q = "UPDATE users set lastActive = NOW() WHERE id = ?;";
        conn.query(q, [user], function(err2, results2) {
          if(err2) {
            cb({error: "USER_STATUS_UPDATE_ERROR"});
          }
          if(results && results[0]) {
            tokenCache[token] = { user: user, expiry: new Date(results[0].expiry) };
            cb(!err && results != undefined)
          } else {
            cb({ error: "ERROR_BAD_TOKEN" });
          }
        });
      }
    });
  }

  connector.userExists = function(user, cb) {
    var q = "SELECT id FROM users WHERE username = ?";
    conn.query(q, user, function(err, results) {
      console.log(results)
      cb(!err && results != undefined && results.length > 0);
    })
  }

  connector.search = function(query, cb) {
    var q = "SELECT id, username FROM users WHERE INSTR(username, ?);"
    conn.query(q, [query], function(err, results) {
      if(err) {
        console.log(err);
        cb({error: "DATABASE_ERROR"});
        return;
      } else {
        if(results && results[0]) {
          cb({ users: results });
        } else {
          cb({error: "NO_RESULTS_FOUND"});
        }
      }
    })
  }

  connector.userIdExists = function(user, cb) {
    console.log(user);
    var q = "SELECT id FROM users WHERE id = ?";
    conn.query(q, [user], function(err, results) {
      console.log(results)
      cb(!err && results != undefined && results.length > 0);
    })
  }

  connector.idFromName = function(user, cb) {
    var q = "SELECT id FROM users WHERE id = ?"
    conn.query(q, [user], function(err, results) {
      if(err) {
        console.log(err);
        cb({ error: "DATABASE_ERROR" });
        return
      }
      console.log(results)
      cb((results != null && results[0] != null) ? results[0] : {error: "USER_NOT_EXISTS"});
    });
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
        cb({ error: "ERROR_BAD_LOGIN" });
    });
  }

  connector.updateFriendship = function(fId, confirm, cb) {
    if(confirm) {
      var q = "UPDATE friends WHERE id = ? SET pending = FALSE;";
    } else {
      var q = "DELETE FROM friends where id = ?";
    }
    conn.query(q, [fId], function(err, results) {
      cb(!err && results);
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
        var q = "INSERT INTO messages (sender, recipient, message) VALUES(?, ?, ?)";
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
  }

  connector.receivedMessages = function(sender, highest, cb) {
    var q = "DELETE FROM messages WHERE recipient = ? AND id <= ?";
    console.log(sender + highest);
    console.log("DELETE FROM messages WHERE recipient = %s AND id <= %s", sender, highest);
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
      if(err) {
        console.log(err);
        cb({error: "DATABASE_ERROR"});
        return;
      }
      var messages = {};
      if(results) {
        console.log(results)
        for(var i = 0 ; i < results.length; i++) {
          var item = results[i];
          if(!messages[item.sender])
            messages[item.sender] = [];
          messages[item.sender].push({id: item.id, message: item.message});
        }
      }
      else {
        cb({error: "NO_MESSAGES_FOUND"})
        return;
      }
      console.log("im here");
      console.log(messages)
      cb(messages);
    })
  }

  connector.getFriends = function (sender, cb) {
    var q = "SELECT * FROM friends where user1 = ? OR user2 = ?";
    conn.query(q, [sender, sender], function(err, results) {
      if(err) {
        console.log(err);
        cb({error: "DATABASE_ERROR"});
      }
      else {
        console.log(results);
        cb(results);
      }
    })
  }

  connector.addFriend = function (user1, user2, secret, cb) {
    console.log("first: ) ")
    var q = "SELECT * FROM friends WHERE user1 = ? AND user2 = ? OR user2 = ? AND user1 = ?"
    conn.query(q, [user1, user2, user2, user1], function(err, results) {
      if(err || ! results) {
        cb({ error: "DATABASE_ERROR"});
      } else if(results && results.length > 0) {
        cb({ error: "FRIENDSHIP_EXISTS" });
      } else if( results && results.length == 0) {
        q = "INSERT INTO friends(user1, user2, secret) VALUES(?, ?, ?)"
        conn.query(q, [user1, user2, secret], function(err, results) {
          if(err) {
            cb({ error: "DATABASE_ERROR" });
          } else {
            cb(results != undefined);
          }
        });
      }
    });
  }
  return connector;
}


function pruneExpiredTokens() {
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
  console.log("Adding token: %s", token);
  var q = "INSERT INTO tokens VALUES(?, ?, NOW() + INTERVAL 2 HOUR);"
  conn.query(q, [user, token], function(err, rows, fields) {
    if(err) {
      console.log(err);
      return cb({ success: false, error: "Storing token failed" });
    }
    cb({ success: true, id: user, token: token });
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

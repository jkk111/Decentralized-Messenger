/*
 * Module to handle all required queries and return them in necessary format with necessary data.
 */
var mysql = require("mysql2");
var crypto = require("crypto");
var salt, databaseUser, databasePassword, databaseHost, databaseName, databasePort, connector, conn, tokenCache;
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
    multipleStatements: true,
    namedPlaceholders: true
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
        return connector.verifyToken(user, token, cb);
      }
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
            cb({error: "DATABASE_ERROR", type: "USER_STATUS_UPDATE_ERROR"});
          }
          if(results && results[0]) {
            tokenCache[token] = { user: user, expiry: new Date(results[0].expiry), accessed: new Date().getTime() };
            if(Object.keys(tokenCache).length > 50) {
              pruneTokens();
            }
            cb(!err && results != undefined)
          } else {
            cb({ error: "ERROR_BAD_TOKEN" });
          }
        });
      }
    });
  }

  connector.userExists = function(user, cb) {
    var q = "SELECT username FROM users WHERE username = ?";
    conn.query(q, user, function(err, results) {
      if(err)
        return cb({error: "DATABASE_ERROR"});
      cb(results != undefined && results.length > 0);
    })
  }

  connector.search = function(query, cb) {
    var q = "SELECT id, username FROM users WHERE INSTR(username, ?);"
    conn.query(q, [query], function(err, results) {
      if(err) {
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
    var q = "SELECT id FROM users WHERE id = ?";
    conn.query(q, [user], function(err, results) {
      if(err) {
        return cb({error: "DATABASE_ERROR"});
      }
      cb(results != undefined && results.length > 0);
    })
  }

  connector.idFromName = function(user, cb) {
    var q = "SELECT id FROM users WHERE username = ?;";
    conn.query(q, [user], function(err, results) {
      if(err) {
        cb({ error: "DATABASE_ERROR" });
        return
      }
      cb((results != null && results[0] != null) ? results[0].id : {error: "USER_NOT_EXISTS"});
    });
  }

  connector.login = function(user, pass, cttoken, cb) {
    pass = generateHash(pass);
    var q = `SELECT CASE
              WHEN users.isManaged = 1 THEN (SELECT private FROM keypairs where keypairs.id = users.id)
              ELSE FALSE
            END AS "private",
            CASE
              WHEN users.isManaged = 1 THEN (SELECT public FROM keypairs where keypairs.id = users.id)
              ELSE FALSE
            END AS "public", id, username FROM users where username = ? AND password = ?`;
    conn.query(q, [user, pass], function(err, results) {
      if(err) {
        console.log(err);
        return cb({error: "DATABASE_ERROR"});
      }
      if(results && results.length > 0) {
        if(results[0].private === 0)
          results[0].private = false;
        if(cttoken)
          return addToken(user, cttoken, cb);
        else
          return generateToken(results[0], cb);
      }
      else
        return cb({ error: "ERROR_BAD_LOGIN" });
    });
  }

  connector.updateFriendship = function(fId, confirm, user, cb) {
    if(confirm) {
      var q = "UPDATE friends SET pending = FALSE WHERE id = :id AND user2 = :user;";
    } else {
      var q = "DELETE FROM friends where id = :id";
    }
    conn.query(q, { id: fId, user: user }, function(err, results) {
      if(err)
        return cb({error: "DATABASE_ERROR"})
      if(results != undefined && results.changedRows > 0 || results != undefined && results.affectedRows > 0)
        return cb(true);
      else
        return cb({error: "FRIENDSHIP_NOT_EXISTS"});
    });
  }

  connector.cancelFriendRequest = function(fId, user, cb) {
    var q = "DELETE FROM friends WHERE pending = 1 AND id = ? AND user1 = ?;";
    conn.query(q, [fId, user], function(err, results) {
      if(err)
        return cb({error: "DATABASE_ERROR"});
      if(results != undefined && results.affectedRows > 0)
        return cb(true);
      else
        return cb({error: "FRIENDSHIP_NOT_PENDING"})
    })
  }

  connector.setKeys = function(id, priv, pub, managed, cb) {
    var q;
    if(managed)
      q = "INSERT INTO keypairs (id, private, public) VALUES(:id, :private, :public) ON DUPLICATE KEY UPDATE private = :private, public = :public;";
    else
      q = "INSERT INTO keypairs (id, public) VALUES(:id, :public) ON DUPLICATE KEY UPDATE public = :public;";
    conn.query(q, {id: id, private: priv, public: pub}, function(err, results) {
      if(err) {
        console.log(err);
        return cb({error: "DATABASE_ERROR"})
      } else {
        return cb(results.changedRows > 0 || results.affectedRows > 0);
      }
    })
  }

  connector.register = function(user, pass, cb) {
    var hash = generateHash(pass);
    var q = "INSERT INTO users (username, password) VALUES(?, ?)";
    conn.query(q, [user, hash], function(err, results) {
      if(err) {
        console.log(err);
        return cb({error: "DATABASE_ERROR"});
      }
      return cb(results.insertId);
    })
  }

  connector.addMessage = function(sender, dest, messageSender, messageRecipient, cb) {
    connector.userIdExists(dest, function(exists) {
      if(exists) {
        var q = "INSERT INTO messages (sender, recipient, messageSender, messageRecipient) VALUES(?, ?, ?, ?)";
        conn.query(q, [sender, dest, messageSender, messageRecipient], function(err, results) {
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
    conn.query(q, [sender, highest], function(err, results) {
      if(err) {
        return cb({error: "DATABASE_ERROR"});
      }
      cb(err == undefined);
    });
  }

  connector.refreshToken = function(token, cb) {
    var q = "UPDATE tokens SET expiry = NOW() + INTERVAL 6 HOUR WHERE token = ?";
    conn.query(q, [token], function(err, results) {
      if(err) {
        return cb(false);
      }
      if(results.changedRows == 0) {
        cb({ error: "ERROR_BAD_TOKEN" });
      } else {
        cb(true);
      }
    })
  }

  connector.fetchMessages = function(sender, lowest, friend, cb) {
    var q = `SELECT id, sender, recipient, ts, CASE
             WHEN sender = :sender THEN messageSender
             WHEN recipient = :sender THEN messageRecipient
             ELSE NULL
             END AS "message"
             FROM messages
             WHERE recipient = :sender AND id < :lowest AND sender = :friend
             OR sender = :sender AND id < :lowest AND recipient = :friend
             ORDER BY id desc
             LIMIT 30`;
    conn.query(q, { sender: sender, lowest: lowest, friend: friend }, function(err, results) {
      if(err) {
        console.log(err);
        return cb({error: "DATABASE_ERROR"});
      }
      var messages = {};
      if(results) {
        for(var i = 0 ; i < results.length; i++) {
          var item = results[i];
          if(item.sender != sender) {
            if(!messages[item.sender])
              messages[item.sender] = [];
            messages[item.sender].push({id: item.id, message: item.message, sent: item.ts, fromSelf: false});
          }
          else {
            if(!messages[item.recipient])
              messages[item.recipient] = [];
            messages[item.recipient].push({id: item.id, message: item.message, sent: item.ts, fromSelf: true});
          }
        }
      }
      else {
        cb({error: "NO_MESSAGES_FOUND"})
        return;
      }
      cb(messages);
    });
  }

  connector.getMessages = function(sender, highest, cb) {
    var d = new Date().getTime();
    var q = `SELECT id, sender, recipient, ts, CASE
             WHEN sender = :sender THEN messageSender
             WHEN recipient = :sender THEN messageRecipient
             ELSE NULL
             END AS "message"
             FROM messages
             WHERE recipient = :sender AND id > :highest
             OR sender = :sender AND id > :highest`;
    conn.query(q, { sender: sender, highest: highest}, function(err, results) {
      if(err) {
        console.log(err);
        return cb({error: "DATABASE_ERROR"});
      }
      var messages = {};
      if(results) {
        for(var i = 0 ; i < results.length; i++) {
          var item = results[i];
          if(item.sender != sender) {
            if(!messages[item.sender])
              messages[item.sender] = [];
            messages[item.sender].push({id: item.id, message: item.message, sent: item.ts, fromSelf: false});
          }
          else {
            if(!messages[item.recipient])
              messages[item.recipient] = [];
            messages[item.recipient].push({id: item.id, message: item.message, sent: item.ts, fromSelf: true});
          }
        }
      }
      else {
        cb({error: "NO_MESSAGES_FOUND"})
        return;
      }
      cb(messages);
    })
  }
  connector.getFriends = function (sender, cb) {
    var d = new Date().getTime();
    var q = `SELECT friends.pending, users.username, keypairs.public, friends.id as friendshipId,
              CASE
                WHEN friends.pending = 1 AND friends.user1 = :user THEN true
                ELSE FALSE
              END AS "initiatedBySelf",
              CASE
                WHEN friends.user1 = :user THEN friends.user2
                ELSE friends.user1
              END AS "id"
              FROM friends
              LEFT JOIN users ON
                friends.user1=users.id AND users.id != :user OR friends.user2=users.id AND users.id != :user
              LEFT JOIN keypairs ON
                friends.user1=keypairs.id AND keypairs.id != :user OR friends.user2=keypairs.id AND keypairs.id != :user
              WHERE friends.user1=:user OR friends.user2=:user`;
    conn.query(q, {user: sender}, function(err, results) {
      if(err || !results) {
        console.log(err);
        cb({error: "DATABASE_ERROR"});
      }
      else {
        if(results.length === 0) {
          return cb({error: "ERROR_NO_FRIENDS"});
        }
        for(var i = 0 ; i < results.length; i++) {
          results[i].pending = results[i].pending == 1 ? true : false;
          results[i].initiatedBySelf = results[i].initiatedBySelf == 1 ? true : false;
        }
        return cb(results)
      }
    })
  }

  connector.checkFriendship = function(user1, user2, cb) {
    var q = "SELECT * FROM friends WHERE user1 = :first AND user2 = :second OR user1 = :second AND user2 = :first"
    conn.query(q, {first: user1, second: user2} , function(err, results) {
      if(err) {
        return cb({error: "DATABASE_ERROR"});
      } else {
        return cb(results != undefined && results.length > 0);
      }
    })
  }

  connector.addFriend = function (user1, user2, secret, cb) {
    if(user1 == user2) {
      cb({error: "ERROR_ADD_SELF"});
      return;
    }
    var q = "SELECT * FROM friends where user1 = :friend AND user2 = :self OR user1 = :self AND user2 = :friend";
    conn.query(q, {self: user1, friend: user2}, function(err, results) {
      if(err || ! results) {
        cb({ error: "DATABASE_ERROR"});
      } else if(results && results.length > 0) {
        cb({ error: "FRIENDSHIP_EXISTS" });
      } else if( results && results.length == 0) {
        q = "INSERT INTO friends(user1, user2, secret) VALUES(?, ?, ?);"
        conn.query(q, [user1, user2, secret], function(err, results2) {
          if(err) {
            cb({ error: "DATABASE_ERROR" });
          } else {
            cb(results2 != undefined ? {friendshipId: results2.insertId, success: true} : false);
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
  var q = "INSERT INTO tokens VALUES(?, ?, NOW() + INTERVAL 2 HOUR);"
  conn.query(q, [user.id, token], function(err, rows, fields) {
    if(err) {
      console.log(err);
      return cb({ success: false, error: "Storing token failed" });
    }
    user.success = true;
    user.token = token;
    return cb(user);
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

function pruneTokens() {
  var tokens = Object.keys(tokenCache)
  while(tokens.length > 50) {
    var oldest, oldestTime;
    for(var i = 0 ; i < tokens.length ;i++) {
      if(!oldest) {
        oldest = tokens[i];
        oldestTime = tokenCache[tokens[i]].accessed;
      }
      else if (tokenCache[tokens[i]].accessed < oldestTime) {
        oldest = tokens[i];
        oldestTime = tokenCache[tokens[i]].accessed;
      }
    }
    delete tokenCache[oldest];
    tokens = Object.keys(tokenCache);
  }
}

setInterval(pruneExpiredTokens, 1000 * 60);

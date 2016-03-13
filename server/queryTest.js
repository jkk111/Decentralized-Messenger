var mysql = require("mysql2");

var conn = mysql.createConnection({
  user: "root",
  password: "root",
  host: "localhost",
  port: 3306,
  database: "dm",
  multipleStatements: true,
  namedPlaceholders: true
});
// var q = `SELECT users.id, users.username, keypairs.public FROM users join keypairs on users.id=keypairs.id WHERE users.id IN
//          (SELECT id from users);`
var q = `SELECT CASE
            WHEN friends.pending = 1 AND friends.user1 = :user THEN true
            WHEN friends.pending = 1 THEN false
            ELSE FALSE
           END AS "initiatedBySelf", friends.pending, users.username, keypairs.public, friends.user1, friends.user2, friends.id as friendshipId
           FROM friends
            LEFT JOIN users ON
              friends.user1=users.id AND users.id != :user OR friends.user2=users.id AND users.id != :user
            LEFT JOIN keypairs ON
              friends.user1=keypairs.id AND keypairs.id!=:user OR friends.user2=keypairs.id AND keypairs.id!=:user
           WHERE friends.user1=:user OR friends.user2=:user`;
conn.query(q, {user: 1}, function(err, results) {
  console.log(err);
  console.log(results)
  conn.end();
})

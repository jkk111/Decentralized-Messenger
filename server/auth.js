module.exports = function(app) {
  // Verify the client is who they say they are
  app.get("/key", function(req, res) {
    res.send(/*public key */"hello world");
  });
}

/*
  This will be a tricky bit, because we want to believe the user is who they claim, but anyone could act as the user.
  I propose a system of multiple ids, one per server.
  1. Same server credentials, login and verify identity, trusted by default. (Strongly recommend 2 factor auth).
  2. Referral credentials. A server can refer a client, and the then the new server can request a password from the client (we should not share hashes, it leaves way too much room for trouble).
  3. Self credentials. Client can maintain their own credentials, "register" with a server to get a local id, and assign a password (again don't share hashes).
  4. No server side id, client maintains a public key for the server, and an assigned id, then the client can show that it is who it claims to be by solving a challenge. ***This may not be possible depending on implementation***
*/

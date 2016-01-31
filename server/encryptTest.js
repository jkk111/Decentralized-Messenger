var nodeRSA = require("node-rsa");
var fs = require("fs");
var keyText = fs.readFileSync("private.txt", "utf-8").trim();
var encrypted = fs.readFileSync("encrypted.txt");


// var key = new nodeRSA({b: 2048});
var key = new nodeRSA(keyText);
// console.log(key);
// console.log(encrypted)
// console.log(keyText);
// console.log(key.isPrivate());
// console.log(key.exportKey('components-public').n.toString("base64"))
console.log(key.decrypt(encrypted));
console.log(key.exportKey('pkcs1'));
console.log(key.exportKey('pkcs1-public'));


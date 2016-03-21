var messages = [];
var pkey;
var decrypting = false;
importScripts("JSEncrypt.js");
var dec = new JSEncrypt();
self.addEventListener("message", function(data, data2) {
  if(data.privateKey != null) {
    pkey = data.privateKey;
    dec.setPrivateKey(pkey);
  } else {
    if(decrypting)
      messages.push({message: data, friend: data2})
    else if(pkey != null) {
      messages.push({message: data, friend: data2})
      decrypt();
    } else {
      messages.push({message: data, friend: data2})
    }
  }
});

function decrypt() {
  decrypting = true;
  if(messages.length > 0) {
    var tmp = messages.shift();
    var message = tmp.message;
    var text = message.message;
    decrypting = true;
    var chunks = text.split(",");
    var decStr = "";
    for(var i = 0 ; i < chunks.length; i++) {
      decStr += dec.decrypt(chunks[i]);
    }
  }
  self.postMessage({message: { id: message.id, message: decStr || "COULDNT DECRYPT", fromSelf: message.fromSelf }, friend: tmp.friend})
  if(messages.length > 0) {
    decrypt();
  }
  decrypting = false;
}
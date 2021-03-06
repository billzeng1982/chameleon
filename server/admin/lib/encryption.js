/**
 * Created by Administrator on 2015/3/10.
 */
'use strict';

var fs = require('fs-extra'),
    path = require('path'),
    crypto = require('crypto');

function Encryption(keyDir) {
    var keyPath = path.join(keyDir, 'chameleon-server.key.pem');
    var diffHelm = crypto.createDiffieHellman(60);
    var newKey = diffHelm.generateKeys('base64');
    fs.ensureFileSync(keyPath, newKey);
    var pem = fs.readFileSync(keyPath);
    this.key = pem.toString('ascii');
}

Encryption.prototype.encrypt = function(input){
    var cipher = crypto.createCipher('aes-256-cbc', this.key);
    var encrypted = cipher.update(input, 'utf8', 'base64');
    encrypted += cipher.final('base64');
    return encrypted;
}

Encryption.prototype.decrypt = function(input){
    var decipher = crypto.createDecipher('aes-256-cbc', this.key);
    var decrypted = decipher.update(input, 'base64', 'utf8');
    decrypted += decipher.final('utf8');
    return decrypted;
}

module.exports = Encryption;
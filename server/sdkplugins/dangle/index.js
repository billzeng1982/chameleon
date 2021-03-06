"use strict";

var crypto = require('crypto');
var querystring = require('querystring');
var util = require('util');

var async = require('async');
var restify = require('restify');

var commonLib = require('../_common');
var ErrorCode = commonLib.ErrorCode;
var SDKPluginBase = commonLib.SDKPluginBase;

var cfgDesc = {
    appId: 'string',
    appKey: 'string',
    paymentKey: 'string'
};


var DangleChannel = function(logger, cfgChecker) {
    SDKPluginBase.call(this, logger, cfgChecker);
    this.requestUri = "http://ngsdk.d.cn/";
    this.client = restify.createJsonClient({
        url: this.requestUri,
        retry: false,
        log: logger,
        requestTimeout: 10000,
        connectTimeout: 20000
    });
};
util.inherits(DangleChannel, SDKPluginBase);

DangleChannel.prototype.calcSecret = function (s) {
    var md5sum = crypto.createHash('md5');
    md5sum.update(s);
    return md5sum.digest('hex').toLowerCase();
};


DangleChannel.prototype.verifyLogin = function(wrapper, token, others, callback) {
    var self = this;
    var cfgItem = wrapper.cfg;
    var sig = this.calcSecret(cfgItem.appId+'|'+cfgItem.appKey+'|'+token+'|'+others);
    var q = '/api/cp/checkToken?' +
        querystring.stringify({
            appid: cfgItem.appId,
            umid: others,
            token: token,
            sig: sig
        });
    this.client.get(q, function (err, req, res, obj) {
        req.log.debug({req: req, err: err, obj: obj, q: q}, 'on result ');
        if (err) {
            req.log.warn({err: err}, 'request error');
            return callback(err);
        }
        if (obj.msg_code !== 2000) {
            req.log.debug({body: res.body}, "Fail to verify login");
            callback(null, {
                code: self.mapError(obj.msg_code),
                msg: obj.msg_desc
            });
        } else {
            callback(null, {
                code: 0,
                loginInfo: {
                   uid: others,
                   token: token,
                   channel: wrapper.channelName
                }
            });
        }
    });
};


DangleChannel.prototype.getPayUrlInfo = function ()  {
    return [
        {
            method: 'get',
            path: '/pay'
        }
    ];
};


DangleChannel.prototype.send = function (res, body) {
    res.writeHead(200, {
        'Content-Length': Buffer.byteLength(body),
        'Content-Type': 'text/plain'
    });
    res.write(body);
};


DangleChannel.prototype.respondsToPay = function (req, res, next, wrapper) {
    var self = this;
    self._logger.debug({req: req}, 'recv callback from dangle');
    var params = req.params;
    try {
        var cfgItem = wrapper.cfg;
        var orderId = params.ext;
        var success = ErrorCode.ERR_OK;
        if (params.result !== '1') {
            success = ErrorCode.ERR_FAIL;
        }
        var money = Math.ceil(parseFloat(params.money) * 100);
        var channelOrderNo = params.order;
        var uid = params.mid;
        var timestamp = params.time;
        var sign = params.signature;
        var calcString = "order="+params.order+
            "&money="+params.money+
            "&mid="+params.mid+
            "&time="+params.time+
            "&result="+params.result+
            "&ext="+params.ext+
            "&key="+cfgItem.paymentKey;
        var expectSign = self.calcSecret(calcString);
        if (expectSign !== sign) {
            self.send(res, 'invalid sign');
            return next();
        }
        var other = {
            chOrderId: channelOrderNo,
            timestamp: timestamp
        };
        wrapper.userAction.pay(wrapper.channelName, uid, null, orderId,
            success, null, 0, money, other,
        function (err, result) {
            if (err) {
                self._logger.error({err: err}, "fail to pay");
                self.send(res, err.message);
                return next();
            }
            self._logger.debug({result: result}, "recv result");
            self.send(res, 'success');
            return next();
        });
    } catch (e) {
        req.log.warn({err: e}, 'Fail to parse pay notification');
        self.send(res, 'invalid content');
        return next();
    }

};


DangleChannel.prototype.mapError = function (errorCode) {
    switch (errorCode) {
        case 100:
        case 220:
        case 221:
        case 222:
            return ErrorCode.ERR_LOGIN_SESSION_INVALID;
        case 223:
            return ErrorCode.ERR_LOGIN_UID_INVALID;
        case 101:
            return ErrorCode.ERR_FAIL;
        case 103:
            return ErrorCode.ERR_PAY_CANCEL;
        case 104:
            return ErrorCode.ERR_PAY_FAIL;
        case 239:
            return ErrorCode.ERR_PAY_FAIL;
        default:
            return ErrorCode.ERR_FAIL;
    }
};


module.exports =
{
    name: 'dangle',
    cfgDesc: cfgDesc,
    createSDK: function (logger, cfgChecker) {
        return new DangleChannel(logger, cfgChecker);
    }
};



package prj.chameleon.lenovo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.lenovo.lsf.gamesdk.LenovoGameApi;

import org.json.JSONException;
import org.json.JSONObject;

import prj.chameleon.channelapi.ApiCommonCfg;
import prj.chameleon.channelapi.Constants;
import prj.chameleon.channelapi.IAccountActionListener;
import prj.chameleon.channelapi.IDispatcherCb;
import prj.chameleon.channelapi.JsonMaker;
import prj.chameleon.channelapi.SingleSDKChannelAPI;

public final class LenovoChannelAPI extends SingleSDKChannelAPI.SingleSDK {

    private static class UserInfo {
        public String mUserId;
        public String mUserToken;
    }

    private static class Config {
        public String mAppID;
        public String mAppKey;
        public String mPayUrl;
    }

    private Config mCfg;
    private UserInfo mUserInfo;
    private IAccountActionListener mAccountActionListener;

    public void initCfg(ApiCommonCfg commCfg, Bundle cfg) {
        mCfg = new Config();
        mCfg.mAppID = cfg.getString("appId");
        mCfg.mAppKey = cfg.getString("appKey");
        if (commCfg.mIsDebug) {
            mCfg.mPayUrl = cfg.getString("payUrl");
        }
        mChannel = commCfg.mChannel;
    }

    @Override
    public void init(Activity activity, IDispatcherCb cb) {
        LenovoGameApi.doInit(activity, mCfg.mAppID);
        cb.onFinished(Constants.ErrorCode.ERR_OK, null);
    }

    @Override
    public void login(Activity activity, final IDispatcherCb cb, final IAccountActionListener accountActionListener) {
        LenovoGameApi.doAutoLogin(activity,new LenovoGameApi.IAuthResult() {
            @Override
            public void onFinished(boolean ret, String data) {
                Log.d(Constants.TAG, "check which thread I am running at");
                if (ret) {
                    mUserInfo = new UserInfo();
                    mUserInfo.mUserToken = data;
                    cb.onFinished(Constants.ErrorCode.ERR_OK,
                            JsonMaker.makeLoginResponse(data, "", mChannel));
                    mAccountActionListener = accountActionListener;
                } else {
                    cb.onFinished(Constants.ErrorCode.ERR_FAIL, null);
                }
            }
        });
    }

    @Override
    public void logout(Activity activity) {
        if (mAccountActionListener != null) {
            mAccountActionListener.onAccountLogout();
        }
        mUserInfo = null;
    }

    @Override
    public boolean onLoginRsp(String loginRsp) {
        JSONObject obj = null;
        try {
            obj = new JSONObject(loginRsp);
            int code = obj.getInt("code");
            if (code != Constants.ErrorCode.ERR_OK) {
                return false;
            } else {
                mUserInfo.mUserId = obj.getJSONObject("loginInfo").getString("uid");
                return true;
            }
        } catch (JSONException e) {
            Log.e(Constants.TAG, "Fail to parse login rsp", e);
            return false;
        }
    }

    @Override
    public void charge(Activity activity,
                       String orderId,
                       String uidInGame,
                       String userNameInGame,
                       String serverId,
                       String currencyName,
                       String payInfo,
                       int rate,
                       int realPayMoney,
                       boolean allowUserChange,
                       final IDispatcherCb cb) {
        startPay(activity, 1, orderId, realPayMoney, payInfo, cb);
    }

    @Override
    public void buy(Activity activity,
                    String orderId,
                    String uidInGame,
                    String userNameInGame,
                    String serverId,
                    String productName,
                    String productID,
                    String payInfo,
                    int productCount,
                    int realPayMoney,
                    final IDispatcherCb cb) {
        int waresid = Integer.valueOf(productID);
        startPay(activity, waresid, orderId, realPayMoney, payInfo, cb);
    }

    @Override
    public String getUid() {
        if (mUserInfo == null) {
            return "";
        } else {
            return mUserInfo.mUserId;
        }
    }

    @Override
    public String getToken() {
        if (mUserInfo == null) {
            return "";
        } else {
            return mUserInfo.mUserToken;
        }
    }

    @Override
    public boolean isLogined() {
        return mUserInfo != null;
    }

    @Override
    public String getId() {
        return "lenovo";
    }

    @Override
    public void exit(Activity activity, final IDispatcherCb cb) {
        LenovoGameApi.doQuit(activity, new LenovoGameApi.IQuitCallback() {
            @Override
            public void onFinished(String data) {
                //退出游戏
                cb.onFinished(Constants.ErrorCode.ERR_OK, null);
            }
        });
    }


    /***********
     *  支付LenovoGameApi.doPay（） 接口 调用
     */
    private void startPay(Activity activity, int waresid, String orderId, int realPayMoney, String payInfo, final IDispatcherCb cb){
        LenovoGameApi.GamePayRequest payRequest = new LenovoGameApi.GamePayRequest();
        // 请填写商品自己的参数
        payRequest.addParam("appid", mCfg.mAppID);
        payRequest.addParam("waresid", waresid);//商户自建商品编码
        payRequest.addParam("exorderno", orderId);
        payRequest.addParam("price", realPayMoney);
        if (mCfg.mPayUrl != null) {
            payRequest.addParam("notifyurl", mCfg.mPayUrl);
        }

        LenovoGameApi.doPay(activity, mCfg.mAppKey, payRequest, new LenovoGameApi.IPayResult() {
            @Override
            public void onPayResult(int resultCode, String signValue,
                                    String resultInfo) {// resultInfo = 应用编号&商品编号&外部订单号
                if (LenovoGameApi.PAY_SUCCESS == resultCode) {
                    // 没有签名值，默认采用finish()，请根据需要修改
                    cb.onFinished(Constants.ErrorCode.ERR_PAY_FAIL, null);
                } else if (LenovoGameApi.PAY_CANCEL == resultCode) {
                    // 取消支付处理，默认采用finish()，请根据需要修改
                    cb.onFinished(Constants.ErrorCode.ERR_PAY_CANCEL, null);
                } else {
                    // 计费失败处理，默认采用finish()，请根据需要修改
                    cb.onFinished(Constants.ErrorCode.ERR_PAY_FAIL, null);
                }
            }
        });
    }
}
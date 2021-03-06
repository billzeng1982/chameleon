package prj.chameleon.channelapi.cbinding;

import android.app.Activity;
import android.util.Log;

import org.json.JSONObject;

import java.lang.ref.WeakReference;

import prj.chameleon.channelapi.ChannelInterface;
import prj.chameleon.channelapi.Constants;
import prj.chameleon.channelapi.IAccountActionListener;

/**
 * Created by wushauk on 4/21/14.
 */
public class AccountActionListener implements IAccountActionListener {

    @Override
    public void preAccountSwitch() {
        NativeChannelInterface.runInRunEnv(new Runnable() {
            @Override
            public void run() {
                ChannelAPINative.preAccountSwitch();
            }
        });
    }

    @Override
    public void afterAccountSwitch(final int code, final JSONObject newUserInfo) {
        NativeChannelInterface.runInRunEnv(new Runnable() {
            @Override
            public void run() {
                try {
                    ChannelAPINative.afterAccountSwitch(code, newUserInfo.toString().getBytes("UTF-8"));
                } catch (Exception e) {
                    Log.e(Constants.TAG, "internal error", e);
                }
            }
        });
    }

    @Override
    public void onAccountLogout() {
        NativeChannelInterface.runInRunEnv(new Runnable() {
            @Override
            public void run() {
                ChannelAPINative.onAccountLogout();
            }
        });
    }

    @Override
    public void onGuestBind(final JSONObject newUserInfo) {
        NativeChannelInterface.runInRunEnv(new Runnable() {
            @Override
            public void run() {
                try {
                    ChannelAPINative.onGuestBind(newUserInfo.toString().getBytes("UTF-8"));
                } catch (Exception e) {
                    Log.e(Constants.TAG, "internal error", e);
                }
            }
        });
    }

}

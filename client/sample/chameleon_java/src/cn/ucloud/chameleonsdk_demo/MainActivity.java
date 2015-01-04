package cn.ucloud.chameleonsdk_demo;

import org.json.JSONException;
import org.json.JSONObject;

import cn.ucloud.chameleonsdk_demo.callback.AccountListener;
import cn.ucloud.chameleonsdk_demo.callback.BuyCallBack;
import cn.ucloud.chameleonsdk_demo.callback.LoginCallBack;
import cn.ucloud.chameleonsdk_demo.callback.LoginGuestCallback;
import cn.ucloud.chameleonsdk_demo.tools.PlatformAPIRestClient;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import prj.chameleon.channelapi.ChannelInterface;
import prj.chameleon.channelapi.Constants;
import prj.chameleon.channelapi.IAccountActionListener;
import prj.chameleon.channelapi.IDispatcherCb;
import prj.chameleon.update.DefaultUI;
import prj.chameleon.update.DefaultUILisener;
import prj.chameleon.update.UpdateManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private static String TAG = MainActivity.class.getSimpleName();
	
	// �˺���Ϊ����
	private IAccountActionListener mIAccountActionListener = new AccountListener(
			this);
	// ��¼�ص�
	private IDispatcherCb mLoginCallBack = new LoginCallBack(this);
	// ֧���ص�
	private IDispatcherCb mBuyCallBack = new BuyCallBack(this);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// init ChannelAPI
		ChannelInterface.init(this, true, new IDispatcherCb() {
			@Override
			public void onFinished(int arg0, JSONObject arg1) {
				// TODO Auto-generated method stub
				Log.i(TAG, "init() finished.");
			}
		});
	}

	/*
	 * �����ǰ�ť����¼���Ӧ����
	 */

	/****************** �û��˻����� *********************/
	// ��¼
	public void onClickLogin(View v) {
		if (ChannelInterface.isLogined()) {
			Toast.makeText(this, "���Ѿ���¼�������˳���", Toast.LENGTH_SHORT).show();
			return;
		}
		ChannelInterface.login(this, mLoginCallBack, mIAccountActionListener);
	}

	// �ǳ�
	public void onClickLogout(View v) {
		if (ChannelInterface.isLogined()) {
			ChannelInterface.logout(this);
			setUserInfo("�û����˳���");
		}else {
			Toast.makeText(this, "����û�е�¼�����ȵ�¼��", Toast.LENGTH_SHORT).show();
		}
	}
	
	// �л��˻�
	public void onClickSwitchAccount(View v) {
		if (ChannelInterface.isLogined()) {
			ChannelInterface.switchAccount(this, mLoginCallBack);
		}else {
			Toast.makeText(this, "����û�е�¼�����ȵ�¼��", Toast.LENGTH_SHORT).show();
		}
	}

	// �ο͵�¼
	public void onClickGuestLogin(View v) {
		if ("test".equals(ChannelInterface.getChannelName())) {
			Toast.makeText(this, "��API�ݲ�֧���ο͵�¼��", Toast.LENGTH_SHORT).show();
			return;
		}
		ChannelInterface.loginGuest(this, new LoginGuestCallback(this),
				mIAccountActionListener);
	}

	// �ο�ע��
	public void onClickGuestRegist(View v) {
		if ("test".equals(ChannelInterface.getChannelName())) {
			Toast.makeText(this, "��API�ݲ�֧���ο�ע�ᣡ", Toast.LENGTH_SHORT).show();
			return;
		}
		boolean t = ChannelInterface.registGuest(this, "please register", new LoginCallBack(this));
        if (!t) {
            setUserInfo("current is not guest. " + "uid: " + ChannelInterface.getUin() + "session: " + ChannelInterface.getToken());
        }
	}	

	/****************** ���� *********************/
	// �˳�Ӧ��
	public void onClickExit(View v) {
		ChannelInterface.exit(this, new IDispatcherCb() {
            @Override
            public void onFinished(int retCode, JSONObject data) {
                if (retCode == Constants.ErrorCode.ERR_OK) {
                    MainActivity.this.finish();
                }
            }
        });
	}

	// ������
	public void onClickAddiction(View v) {
		getAddiction();
	}
	
	// ToggleToolbar
	private boolean mHasCreateToolbar = false;
    private boolean mShowToolbar = false;
	public void onClickToggleToolbar(View v) {
		if (!mHasCreateToolbar) {
            final Activity activity = this;
            ChannelInterface.createToolBar(this, Constants.TOOLBAR_BOTTOM_LEFT);
            setUserInfo("success to create toolbar");
            mHasCreateToolbar = true;
            ChannelInterface.showFloatBar(activity, !mShowToolbar);
            mShowToolbar = !mShowToolbar;
        } else {
            ChannelInterface.showFloatBar(this, !mShowToolbar);
            mShowToolbar = !mShowToolbar;
        }
	}
	
	
	/****************** �û�֧������ *********************/
	// ��ֵ�������
	public void onClickBuy(View v) {
		if(!ChannelInterface.isLogined()){
			setUserInfo("���ȵ�¼��");
			return;
		}
		RequestParams params = new RequestParams();
		params.put("token", ChannelInterface.getPayToken());
		params.put("productId", "xxxx");
		params.put("uid", ChannelInterface.getUin());
		params.put("count", "10000");
		params.put("productName", "testproduct");
		params.put("productDesc", "the test product ");
		final Activity activity = this;
		PlatformAPIRestClient.post("/sdkapi/buy", params,
				new JsonHttpResponseHandler() {
					@Override
					public void onSuccess(JSONObject ret) {
						try {
							if (ret.getInt("code") == 0) {
								String payInfo = null;
								if (ret.has("payInfo")) {
									payInfo = ret.getString("payInfo");
								}
								ChannelInterface.buy(activity,
										ret.getString("orderId"),
										ret.getString("appUid"),
										"usernameinapp",
										ret.getString("serverId"), "xxxx",
										"xxxx", payInfo,
										ret.getInt("productCount"),
										ret.getInt("realPayMoney"),
										mBuyCallBack);
							} else {
								Log.e(TAG,
										"wrong code "
												+ String.valueOf(ret
														.getInt("code")));
							}
						} catch (JSONException e) {
							Log.e(TAG, "wrong json", e);
						}
					}
				});
	}

	// ������Ϸ����
	public void onClickCharge(View v) {
		if(!ChannelInterface.isLogined()){
			setUserInfo("���ȵ�¼��");
			return;
		}
		RequestParams params = new RequestParams();
		params.put("uid", ChannelInterface.getUin());
		params.put("count", "10");
		params.put("token", ChannelInterface.getPayToken());

		final Activity activity = this;
		PlatformAPIRestClient.post("/sdkapi/charge", params,
				new JsonHttpResponseHandler() {
					@Override
					public void onSuccess(JSONObject ret) {
						try {
							if (ret.getInt("code") == 0) {
								String payInfo = null;
								if (ret.has("payInfo")) {
									payInfo = ret.getString("payInfo");
								}
								ChannelInterface.charge(activity,
										ret.getString("orderId"),
										ret.getString("appUid"),
										"usernameinapp",
										ret.getString("serverId"), "xxxxx",
										payInfo, ret.getInt("ratio"),
										ret.getInt("realPayMoney"), true,
										mBuyCallBack);
							} else {
								Log.e(TAG,
										"wrong code "
												+ String.valueOf(ret
														.getInt("code")));
							}
						} catch (JSONException e) {
							Log.e(TAG, "wrong json", e);
						}
					}
				});
	}

	/*
	 * �����Ǹ�������
	 */

	public void setUserInfo(String str) {
		TextView user_info = (TextView) findViewById(R.id.user_info);
		user_info.setText(str);
	}

	public void onGotAuthroizationCode(final JSONObject authorization) {
		String t = authorization.toString();
		Log.i(TAG, "authorization jsonobject:" + t);
		try {
			ChannelInterface.onLoginRsp(authorization.toString());
			setUserInfo("user login as uid: "+ ChannelInterface.getUin()+ ", token: " + ChannelInterface.getToken());
		} catch (Exception e) {
			Log.e(TAG, "fail to get param", e);
		}
	}

	private void getAddiction() {
        ChannelInterface.antiAddiction(this, new IDispatcherCb() {
            @Override
            public void onFinished(int retCode, JSONObject data) {
                if (retCode != 0) {
                    Log.e(TAG, "fail to get anti addiction info");
                    return;
                }
                try {
                    int flag = data.getInt("flag");
                    String text = null;
                    switch (flag) {
                        case Constants.ANTI_ADDICTION_ADULT:
                            text = "adult";
                            break;
                        case Constants.ANTI_ADDICTION_CHILD:
                            text = "child";
                            break;
                        case Constants.ANTI_ADDICTION_UNKNOWN:
                            text = "unknown";
                            break;
                    }
                    setUserInfo(text);
                } catch (JSONException e) {
                    Log.e(TAG, "fail to get anti addiction info", e);
                }
            }
        });
    }
	
	
	/*
	 * ������activity�������ڸ������Ļص�
	 */

	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		ChannelInterface.onNewIntent(this, intent);
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		ChannelInterface.onStart(this);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		// ����ʱ�������쳣 null point
		ChannelInterface.onResume(this, new IDispatcherCb() {
			@Override
			public void onFinished(int arg0, JSONObject arg1) {
				// TODO Auto-generated method stub
			}
		});
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		ChannelInterface.onPause(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		ChannelInterface.onActivityResult(this, requestCode, resultCode, data);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		ChannelInterface.onStop(this);
	}

}

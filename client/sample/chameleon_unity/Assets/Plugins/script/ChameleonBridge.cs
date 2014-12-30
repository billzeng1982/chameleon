// ------------------------------------------------------------------------------
//  <autogenerated>
//      This code was generated by a tool.
//      Mono Runtime Version: 4.0.30319.1
// 
//      Changes to this file may cause incorrect behavior and will be lost if 
//      the code is regenerated.
//  </autogenerated>
// ------------------------------------------------------------------------------
using System;
using UnityEngine;
using SimpleJSON;

namespace chameleon
{
#if UNITY_ANDROID
	public class ChameleonBridge : MonoBehaviour
	{
		private AndroidJavaClass mJObj;
		private ChameleonSDK.EventListener mEvtListener;
		#region Used by ChamleonSDK
		public void init() {
			Debug.Log ("inited");
			mJObj = new AndroidJavaClass( "prj.chameleon.channelapi.unity.UnityChannelInterface" );
		}

		public void initChameleon(ChameleonSDK.EventListener listener) {
			mEvtListener = listener;
			mJObj.CallStatic( "init");
		}

		public T callFunc<T>(string func, params object[] list) {
			Debug.Log (string.Format("callfunc {0}, {1}", func, list.Length));
			return mJObj.CallStatic<T> (func, list);	
		}

		public void callFunc(string func, params object[] list) {
			Debug.Log (string.Format("callfunc {0}, {1}", func, list.Length));
			mJObj.CallStatic (func, list);	
		}

		public void unregisterListener() {
			mEvtListener = null;
		}
		#endregion

		#region Used by java side
		public void onInited(string message) {
			if (mEvtListener != null) {
				var obj = JSON.Parse(message);
				int code = obj["code"].AsInt;
				mEvtListener.onInit(code);
			}
		}

		public void onLogin(string message) {
			Debug.Log ("on login in bridge: " + message);
			if (mEvtListener != null) {
				var obj = JSON.Parse(message);
				int code = obj["code"].AsInt;
				if (code == (int)ChamConstant.ErrorCode.ERR_OK) {
					JSONNode dataObj = obj["data"];
					mEvtListener.onLogin(dataObj.ToString());
				}
			}
		}

		public void onLoginGuest(string message) {
			if (mEvtListener == null) {
				mEvtListener.onLoginGuest();
			}
		}

		public void onLoginFail(string message) {
			Debug.Log ("on login fail in bridge");
			if (mEvtListener != null) {
				var obj = JSON.Parse(message);
				int code = obj["code"].AsInt;
				mEvtListener.onLoginFail(code);
			}
		}

		public void onPay(string message) {
			if (mEvtListener != null) {
				var obj = JSON.Parse(message);
				int code = obj["code"].AsInt;
				mEvtListener.onPay(code);
			}
		}

		public void onRunProtocol(string message) {
			if (mEvtListener != null) {
				var obj = JSON.Parse(message);
				int code = obj["code"].AsInt;
                                var data = obj["data"];
                                var method = data["method"];
                                var res = data["res"];
                                JSONNode dataObj = obj["data"];
				mEvtListener.onRunProtocol(code, method, res);
			}
		}


		public void onSwitchAccount(string message) {
			if (mEvtListener != null) {
				var obj = JSON.Parse(message);
				int code = obj["code"].AsInt;
				if (code == (int)ChamConstant.ErrorCode.ERR_OK) {
					JSONNode dataObj = obj["data"];
					mEvtListener.onSwitchAccount(code, dataObj.ToString());
				} else {
					mEvtListener.onSwitchAccount(code, null);
				}
			}
		}

		public void onPause(string message) {
			if (mEvtListener != null) {
				mEvtListener.onPause();
			}
		}

		public void onAntiAddiction(string message) {
			if (mEvtListener != null) {
				var obj = JSON.Parse(message);
				int code = obj["code"].AsInt;
				if (code == (int)ChamConstant.ErrorCode.ERR_OK) {
					JSONNode dataObj = obj["data"];
					int flag = dataObj["flag"].AsInt;
					mEvtListener.onAntiAddiction(flag);
				} else {
					mEvtListener.onAntiAddiction(ChamConstant.ANTI_ADDICTION_ADULT);
				}
			}
		}

		public void onDestroyed(string message) {
			if (mEvtListener != null) {
				var obj = JSON.Parse(message);
				int code = obj["code"].AsInt;
				mEvtListener.onDestroyed(code);
			}
		}

		public void preAccountSwitch(string message) {
			if (mEvtListener != null) {
				mEvtListener.preAccountSwitch();
			}
		}

		public void onLogout(string message) {
			if (mEvtListener != null) {
				mEvtListener.onLogout();
			}
		}

		public void onGuestBind(string message) {
			if (mEvtListener != null) {
				mEvtListener.onGuestBind(message);
			}
		}

		#endregion

	}
#else 
	public class ChameleonBridge : MonoBehaviour
	{
		private ChameleonSDK.EventListener mEvtListener;
		#region Used by ChamleonSDK
		public void init() {
		}
		
		public T callFunc<T>(string func, params object[] list) {
		}
		
		public void callFunc(string func, params object[] list) {
		}
		
		public void registerListener(ChameleonSDK.EventListener listener) {
			mEvtListener = listener;
		}
		
		public void unregisterListener() {
			mEvtListener = null;
		}
		#endregion
	}
	#endif
	
}


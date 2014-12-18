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
namespace chameleon
{
		public class ChamConstant
		{
			public static int ANTI_ADDICTION_UNKNOWN = 0;
			public static int ANTI_ADDICTION_CHILD = 1;
			public static int ANTI_ADDICTION_ADULT = 2;

			public enum ErrorCode {
				ERR_INTERNAL = -1, // some internal error happened...
				ERR_OK = 0, // OK
				ERR_CANCEL = 1,
				ERR_UNKNOWN = 2,
				ERR_ILL_PARAMS = 3,
				ERR_FAIL = 4,
				ERR_NO_LOGIN = 5,
				ERR_SERVER_BUSY = 6,

                                // pay action erros 11~ 20
                                ERR_PAY_FAIL = 11, // fail to pay
                                ERR_PAY_CANCEL = 12, // user cancel the payment
                                ERR_PAY_IN_PROGRESS = 13, // payment is in progress..
                                ERR_PAY_SESSION_INVALID = 14, // the pay token is expired
                                ERR_PAY_UNKNOWN = 15, // 支付结果未定，有些支付渠道无法实时到账
                                // 如果返回这个错误码，可能是托管的二级货币余额不足，引导了用户充值之后
                                // 可以提示用户重新进行购买
                                ERR_PAY_RETRY = 16,

                                // login action errors 21~30
                                ERR_LOGIN_IN_PROGRESS = 21,
                                ERR_LOGIN_IN_QQ_NON_INSTALLED = 22,
                                ERR_LOGIN_IN_WX_NON_INSTALLED = 23,
                                ERR_LOGIN_GAME_EXIT_NOCARE = 24, // 渠道不关心退出游戏的事件
                                ERR_LOGIN_MSDK_PLAT_NO_SPEC = 25 // 渠道不关心退出游戏的事件
			}
		}
}


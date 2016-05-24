package cn.dacas.emmclient.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import cn.dacas.emmclient.db.EmmClientDb;
import cn.dacas.emmclient.event.MessageEvent;
import cn.dacas.emmclient.security.AESUtil;
import cn.dacas.emmclient.util.NetworkDef;
import cn.dacas.emmclient.util.PrefUtils;
import cn.dacas.emmclient.util.QDLog;
import de.greenrobot.event.EventBus;

public class CheckAccount {
	private static final String TAG = "CheckAccount";
	private static CheckAccount mCheckAccount = null;

	private SharedPreferences settings = null;
	private String currentAccount = null;
	private String currentName = null;
	private String currentPassword=null;
	private String accessToken = null;
	private String refreshToken = null;


	private Context context;

	public static CheckAccount getCheckAccountInstance(Context context) {
		if (mCheckAccount == null) {
			mCheckAccount = new CheckAccount(context);
		}
		return mCheckAccount;
	}

	private CheckAccount(Context context) {
		this.context = context;

		// 如果用户已经登录，启动安全接入转发
		if (currentAccount != null) {
			Bundle params=new Bundle();
			params.putString("email", currentAccount);
			EventBus.getDefault().post(new MessageEvent(MessageEvent.Event_StartForwarding,params));
		} else {
			EventBus.getDefault().post(new MessageEvent(MessageEvent.Event_StopForwarding));
		}
		
					
		settings = context.getSharedPreferences(PrefUtils.PREF_NAME, 0);

		String accountChaos = settings.getString(PrefUtils.CURRENT_ACCOUNT,
                null);
        String accessTokenChaos = settings.getString(PrefUtils.ACCESS_TOKEN,
                null);
		String refreshTokenChaos = settings.getString(PrefUtils.REFRESH_TOKEN,
				null);
		this.currentAccount = (accountChaos == null) ? null : AESUtil.decrypt(
                PrefUtils.CURRENT_ACCOUNT, accountChaos);
		this.accessToken = (accessTokenChaos == null) ? null : AESUtil.decrypt(
				PrefUtils.ACCESS_TOKEN, accessTokenChaos);
		this.refreshToken = (refreshTokenChaos == null) ? null : AESUtil
				.decrypt(PrefUtils.REFRESH_TOKEN, refreshTokenChaos);

		String nameChaos = settings.getString(PrefUtils.CURRENT_NAME, null);
		this.currentName = (nameChaos == null) ? null : AESUtil.decrypt(PrefUtils.CURRENT_NAME, nameChaos);
        String passwordChaos=settings.getString(PrefUtils.PASS_WORD,null);
        this.currentPassword=(passwordChaos==null)?null:AESUtil.decrypt(PrefUtils.PASS_WORD,passwordChaos);
	}


	public String getCurrentName() {
		return currentName;
	}

	private void setCurrentName(String currentName) {
		this.currentName = currentName;

		SharedPreferences.Editor editor = settings.edit();
		editor.putString(PrefUtils.CURRENT_NAME, currentName == null ? null
                : AESUtil.encrypt(PrefUtils.CURRENT_NAME, currentName));
		editor.commit();
	}




	public String getCurrentAccount() {
		return currentAccount;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void  setCurrentAccount(String currentAccount) {
		String accountChaos = settings.getString(PrefUtils.CURRENT_ACCOUNT,
				null);
		String oldAccount = (accountChaos == null) ? null : AESUtil.decrypt(
				PrefUtils.CURRENT_ACCOUNT, accountChaos);
		if(oldAccount == null || !oldAccount.equals(currentAccount)){
			EmmClientApplication.mDb.deleteAllDbItem(EmmClientDb.CONTACT_DATABASE_TABLE);
			SharedPreferences.Editor editor = settings.edit();
			editor.putString(
					PrefUtils.CURRENT_ACCOUNT,
					currentAccount == null ? null : AESUtil.encrypt(
							PrefUtils.CURRENT_ACCOUNT, currentAccount));
			editor.commit();
		}
		this.currentAccount = currentAccount;
	}

	public void setAccesstoken(String accessToken) {
		this.accessToken = accessToken;
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(PrefUtils.ACCESS_TOKEN, accessToken == null ? null
				: AESUtil.encrypt(PrefUtils.ACCESS_TOKEN, accessToken));
		editor.commit();
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(PrefUtils.REFRESH_TOKEN, refreshToken == null ? null
				: AESUtil.encrypt(PrefUtils.REFRESH_TOKEN, refreshToken));
		editor.commit();
	}

    public void setCurrentPassword(String password) {
        this.currentPassword = password;
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PrefUtils.PASS_WORD, password == null ? null
				: AESUtil.encrypt(PrefUtils.PASS_WORD, password));
        editor.commit();
    }

    public String getCurrentPassword() {
        return currentPassword;
    }


	public void userLogin(final String email, final String password,final Response.Listener<JSONObject> listener, final Response.ErrorListener errorListener) {
		final String ip = NetworkDef.getAddrWebservice();
		StringRequest requestApplyToken = new StringRequest(Request.Method.POST, "https://" + ip + "/api/v1/oauth/token",
				new Response.Listener<String>() {
					@Override
					public void onResponse(final String response) {
						QDLog.i(TAG,"userLogin response===========:" +response );
						try {
							JSONObject resultApplyToken = new JSONObject(response);
							if (resultApplyToken.has("access_token")) {
								String token = resultApplyToken.getString("access_token");
								setAccesstoken(resultApplyToken.getString("access_token"));
								setRefreshToken(resultApplyToken.getString("refresh_token"));
								String urlStr = "https://" + ip + "/api/v1/user/devices/" + EmmClientApplication.mPhoneInfo.getIMEI() + "/login?access_token=" + token;
								QDLog.i(TAG,"userLogin urlStr===========:" +urlStr );
								JsonObjectRequest requestLogin = new JsonObjectRequest(Request.Method.POST,
										urlStr,
										new Response.Listener<JSONObject>() {
											@Override
                                            public void onResponse(JSONObject obj) {
												QDLog.i(TAG,"userLogin obj===========:" +obj.toString() );
                                                try {
                                                    boolean status = obj.getBoolean("status");
                                                    if (!status) {
                                                        if (errorListener!=null)
                                                            errorListener.onErrorResponse(new AuthFailureError());
                                                        return;
                                                    }
                                                    String owner=obj.getString("owner_username");
                                                    EmmClientApplication.mActivateDevice.setDeviceBinder(owner);
                                                    EmmClientApplication.mActivateDevice.setBinderName(owner);
                                                    EmmClientApplication.mActivateDevice.setDeviceType(obj.getString("type"));
                                                    EmmClientApplication.mActivateDevice.setDeviceReported(true);
                                                    setCurrentAccount(email);
                                                    setCurrentName(email);
                                                    setCurrentPassword(password);
                                                    EmmClientApplication.mDb.setWordsPassword(email, password, email);
                                                    Bundle params=new Bundle();
                                                    params.putString("email", email);
                                                    EventBus.getDefault().post(new MessageEvent(MessageEvent.Event_StartForwarding, params));
                                                    Intent mIntent = new Intent("get_message");
                                                    context.sendBroadcast(mIntent);
                                                    listener.onResponse(obj);
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                    if (errorListener!=null)
                                                        errorListener.onErrorResponse(new ParseError());
                                                }
                                            }
                                        }, new Response.ErrorListener() {
									@Override
									public void onErrorResponse(VolleyError error) {
                                        if (errorListener!=null)
											errorListener.onErrorResponse(error);
									}
								}) ;
								EmmClientApplication.mVolleyQueue.add(requestLogin);
							}
						} catch (JSONException e) {
                            if (errorListener!=null)
							    errorListener.onErrorResponse(new ParseError());
						}
					}
				}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				if (errorListener!=null) {
					errorListener.onErrorResponse(error);
                }
			}
		}) {
			@Override
			protected Map<String, String> getParams() {
				Map<String, String> params = new HashMap<String, String>();
				params.put("username", email);
				params.put("password", password);
				params.put("grant_type", "password");
				params.put("client_id", "302a7d556175264c7e5b326827497349");
				params.put("client_secret", "4770414c283a20347c7b553650425773");
				return params;
			}
		};
        requestApplyToken.setRetryPolicy(new DefaultRetryPolicy(5000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
		EmmClientApplication.mVolleyQueue.add(requestApplyToken);
    }


    public void clearCurrentAccount() {
		SharedPreferences.Editor editor = settings.edit();
		editor.remove(PrefUtils.CURRENT_ACCOUNT);
		editor.remove(PrefUtils.CURRENT_NAME);
        editor.remove(PrefUtils.PASS_WORD);
		editor.commit();

		this.setCurrentAccount(null);
		this.setCurrentName(null);
        this.setCurrentPassword(null);
		EventBus.getDefault().post(new MessageEvent(MessageEvent.Event_StopForwarding));
	}


//	@Override
//	public void finalize() {
//
//		try {
//			super.finalize();
//		} catch (Throwable e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
}

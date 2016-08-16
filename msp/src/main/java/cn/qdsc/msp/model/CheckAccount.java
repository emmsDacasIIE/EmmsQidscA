package cn.qdsc.msp.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import cn.qdsc.msp.core.EmmClientApplication;
import cn.qdsc.msp.event.MessageEvent;
import cn.qdsc.msp.security.AESUtil;
import cn.qdsc.msp.util.PrefUtils;
import de.greenrobot.event.EventBus;

/**
 * 检查账户是否登陆
 */
public class CheckAccount {
	private static final String TAG = "CheckAccount";
	private static CheckAccount mCheckAccount = null;

	private SharedPreferences settings = null;
	private String currentAccount = null;
	private String currentName = null;
	private String currentPassword=null;


	private Context context;

	public static CheckAccount getCheckAccountInstance(Context context) {
		if (mCheckAccount == null) {
			mCheckAccount = new CheckAccount(context);
		}
		return mCheckAccount;
	}

	private CheckAccount(Context context) {
		this.context = context.getApplicationContext();

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
		this.currentAccount = (accountChaos == null) ? null : AESUtil.decrypt(
				PrefUtils.CURRENT_ACCOUNT, accountChaos);

		String nameChaos = settings.getString(PrefUtils.CURRENT_NAME, null);
		this.currentName = (nameChaos == null) ? null : AESUtil.decrypt(PrefUtils.CURRENT_NAME, nameChaos);
        String passwordChaos=settings.getString(PrefUtils.PASS_WORD,null);
        this.currentPassword=(passwordChaos==null)?null:AESUtil.decrypt(PrefUtils.PASS_WORD,passwordChaos);
	}


	public String getCurrentName() {
		return currentName;
	}

	public void setCurrentName(String currentName) {
		this.currentName = currentName;

		SharedPreferences.Editor editor = settings.edit();
		editor.putString(PrefUtils.CURRENT_NAME, currentName == null ? null
                : AESUtil.encrypt(PrefUtils.CURRENT_NAME, currentName));
		editor.commit();
	}




	public String getCurrentAccount() {
		return currentAccount;
	}

	public void  setCurrentAccount(String currentAccount) {
		String accountChaos = settings.getString(PrefUtils.CURRENT_ACCOUNT,
				null);
		String oldAccount = (accountChaos == null) ? null : AESUtil.decrypt(
				PrefUtils.CURRENT_ACCOUNT, accountChaos);
		if(oldAccount == null || !oldAccount.equals(currentAccount)){
			EmmClientApplication.mDatabaseEngine.deleteContactAllData();
			SharedPreferences.Editor editor = settings.edit();
			editor.putString(
					PrefUtils.CURRENT_ACCOUNT,
					currentAccount == null ? null : AESUtil.encrypt(
							PrefUtils.CURRENT_ACCOUNT, currentAccount));
			editor.commit();
		}
		this.currentAccount = currentAccount;
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
}

package cn.dacas.emmclient.mdm;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.apache.http.client.methods.HttpPost;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.dacas.emmclient.R;
import cn.dacas.emmclient.main.ActivateDevice;
import cn.dacas.emmclient.main.EmmClientApplication;
import cn.dacas.emmclient.event.MessageEvent;
import cn.dacas.emmclient.util.MyJsonObjectRequest;
import cn.dacas.emmclient.util.UpdateTokenRequest;
import cn.dacas.emmclient.worker.PhoneInfoExtractor;
import de.greenrobot.event.EventBus;

public class AppCapabilityDlg {
	private Context context;
	private View dlgView;

	private ListView appCapabilityList;

	private AlertDialog alertDialog;

	PhoneInfoExtractor phoneInfoExtractor;

	private SharedPreferences settings = null;

	private boolean isNetworkAvaible = true;

	private JSONObject jsonObject;
	private boolean originalPrivacy[];

	private String device_type;

	public static class capabilityItem {
		int imageId;
		public String desc;

		public capabilityItem(int imageId, String desc) {
			this.imageId = imageId;
			this.desc = desc;
		}
	}

	private final String privacyInfo[] = { "hardware_show", "system_show",
			"location_show", "network_show", "app_service_show" };

	private static final List<capabilityItem> capabilityMap = new ArrayList<capabilityItem>();

	static {
		capabilityMap.add(new capabilityItem(R.drawable.perm_hardware,
				"硬件信息（生产商、设备型号、屏幕、处理器等）"));
		capabilityMap.add(new capabilityItem(R.drawable.perm_os,
				"系统信息（操作系统、内核、基带、接口版本等）"));
		capabilityMap.add(new capabilityItem(R.drawable.perm_location,
				"位置信息（网络、GPS定位等）"));
		capabilityMap.add(new capabilityItem(R.drawable.perm_network,
				"网络信息（电话号码、数据连接状态、网络运营商、wifi等）"));
		capabilityMap.add(new capabilityItem(R.drawable.perm_app,
				"应用和服务列表（应用名称、版本号、安装类型，运行内存等）"));
	}

	private static final Map<String, String> desc2Key = new HashMap<String, String>();
	static {
		desc2Key.put("硬件信息（生产商、设备型号、屏幕、处理器等）", "allowHardInfo");
		desc2Key.put("系统信息（操作系统、内核、基带、接口版本等）", "allowSysInfo");
		desc2Key.put("位置信息（网络、GPS定位等）", "allowLocationInfo");
		desc2Key.put("网络信息（电话号码、数据连接状态、网络运营商、wifi等）", "allowNetInfo");
		desc2Key.put("应用和服务列表（应用名称、版本号、安装类型，运行内存等）", "allAppInfo");
	}

	public static final String PREF_NAME = "APP_CAPA";
	public static final String DEVICE = "deviceType";
	public static final String HARDKEY = "allowHardInfo";
	public static final String SYSKEY = "allowSysInfo";
	public static final String LOCKEY = "allowLocationInfo";
	public static final String NETKEY = "allowNetInfo";
	public static final String APPKEY = "allAppInfo";

	public AppCapabilityDlg(Context context) {
		this.context = context;
		LayoutInflater inflater = ((Activity) context).getLayoutInflater();
		dlgView = inflater.inflate(R.layout.app_capability_dlg_view, null);
		appCapabilityList = (ListView) dlgView
				.findViewById(R.id.appCapabilityList);

		settings = context.getSharedPreferences(PREF_NAME, 0);
		originalPrivacy = new boolean[5];
		originalPrivacy[0] = settings.getBoolean(HARDKEY, true);
		originalPrivacy[1] = settings.getBoolean(SYSKEY, true);
		originalPrivacy[2] = settings.getBoolean(LOCKEY, false);
		originalPrivacy[3] = settings.getBoolean(NETKEY, false);
		originalPrivacy[4] = settings.getBoolean(APPKEY, true);
		device_type = EmmClientApplication.mActivateDevice.getDeviceType();
		phoneInfoExtractor = PhoneInfoExtractor.getPhoneInfoExtractor(context);
	}

	public void showLogInDlg() {
		// 注意事项：注册成功后跳转到dlg_goto指定的界面，dlg_goto置0，设置DeviceRegister的userAccount等内容
		AlertDialog.Builder logInBuilder = new AlertDialog.Builder(context);

		alertDialog = logInBuilder.setTitle("隐私策略设置").setView(dlgView)
				.setIcon(R.drawable.app_capability_setting).create();

		alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						jsonObject = new JSONObject();
						int count = appCapabilityList.getChildCount();
						for (int idx = 0; idx < count; idx++) {
							View childView = appCapabilityList.getChildAt(idx);
							TextView descView = (TextView) childView
									.findViewById(R.id.title);
							CheckBox checkBox = (CheckBox) childView
									.findViewById(R.id.checkBox);

							SharedPreferences.Editor editor = settings.edit();
							editor.putBoolean(desc2Key.get(descView.getText()),
									checkBox.isChecked());
							editor.commit();
							try {
								jsonObject.put(privacyInfo[idx],
										checkBox.isChecked());
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}
						//为了修改上传位置信息选项
						ActivateDevice activate = ((EmmClientApplication) context.getApplicationContext()).getActivateDevice();
						String deviceBinder = activate.getDeviceBinder();
						if (deviceBinder != null && deviceBinder.length() > 0) {
							EventBus.getDefault().post(new MessageEvent(MessageEvent.Event_UploadLocation));
						}
//						updatePrivacy();
					}
				});

		alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.dismiss();
					}
				});

		appCapabilityList.setAdapter(new CapabilityAdapter(context));

		alertDialog.show();
	}

	/**
	 * 在新线程中传递privacy信息
	 */
	private void updatePrivacy() {
        MyJsonObjectRequest request=new MyJsonObjectRequest(Request.Method.PUT, "/client/devices/" + EmmClientApplication.mPhoneInfo.getIMEI() + "/privacy",
				UpdateTokenRequest.TokenType.DEVICE, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                SharedPreferences.Editor editor = settings.edit();
                for (int i = 0; i < 5; ++i) {
                    View childView = appCapabilityList.getChildAt(i);
                    TextView descView = (TextView) childView
                            .findViewById(R.id.title);
                    editor.putBoolean(desc2Key.get(descView.getText()),
                            originalPrivacy[i]);
                }
                editor.commit();
                Toast.makeText(context.getApplicationContext(),
                        "隐私策略设置未成功，请稍后再试！", Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; charset=UTF-8");
                return headers;
            }
        };
		EmmClientApplication.mVolleyQueue.add(request);
	}


	private class CapabilityAdapter extends BaseAdapter {
		private LayoutInflater mInflater;// 得到一个LayoutInfalter对象用来导入布局

		/** 构造函数 */
		public CapabilityAdapter(Context context) {
			this.mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return capabilityMap.size();// 返回数组的长度
		}

		@Override
		public Object getItem(int position) {
			return capabilityMap.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			AppCapHolder holder;
			if (convertView == null) {
				convertView = mInflater.inflate(
						R.layout.list_item_app_capability, null);
				holder = new AppCapHolder();
				holder.image = (ImageView) convertView.findViewById(R.id.image);
				holder.title = (TextView) convertView.findViewById(R.id.title);
				holder.checkBox = (CheckBox) convertView
						.findViewById(R.id.checkBox);
				capabilityItem item = capabilityMap.get(position);
				holder.image.setImageResource(item.imageId);
				holder.title.setText(item.desc);
				holder.checkBox.setChecked(originalPrivacy[position]);
				// holder.checkBox.setChecked(settings.getBoolean(
				// desc2Key.get(item.desc), true));

				if ((device_type.equals("BYOD") || (device_type
						.equals("UNKNOWN")))
						&& (position == 2 || position == 3)) {
					holder.checkBox.setClickable(true);
					holder.checkBox.setEnabled(true);
				} else {
					holder.checkBox.setChecked(true);
					holder.checkBox.setEnabled(false);
					holder.checkBox
							.setBackgroundResource(R.drawable.check_true_disable);
				}
				convertView.setTag(holder);
			} else {
				holder = (AppCapHolder) convertView.getTag();
			}

			convertView.setOnClickListener(new OnClickListener() {
				Boolean flag = true;

				@Override
				public void onClick(View view) {
					TextView stv = (TextView) view.findViewById(R.id.title);
					if (flag) {
						flag = false;
						stv.setEllipsize(null); // 展开
						stv.setSingleLine(flag);
					} else {
						flag = true;
						stv.setEllipsize(TextUtils.TruncateAt.END); // 收缩
						stv.setSingleLine(flag);
					}
				}
			});
			return convertView;
		}
	}

	class HttpPatch extends HttpPost {

		public HttpPatch(String url) {
			// TODO Auto-generated constructor stub
			super(url);
		}

		@Override
		public String getMethod() {
			// TODO Auto-generated method stub
			return "PATCH";
		}
	}

	private class AppCapHolder {
		public ImageView image;
		public TextView title;
		public CheckBox checkBox;
	}
}

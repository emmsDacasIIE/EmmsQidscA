package cn.dacas.emmclient.ui;

import android.R.color;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import cn.dacas.emmclient.R;
import cn.dacas.emmclient.event.MessageEvent;
import cn.dacas.emmclient.gesturelock.SharedPreferencesHelper;
import cn.dacas.emmclient.main.EmmClientApplication;
import cn.dacas.emmclient.mam.AppStoreActivity;
import cn.dacas.emmclient.mcm.BackupActivity;
import cn.dacas.emmclient.mcm.ContactActivity;
import cn.dacas.emmclient.mcm.DocActivity;
import cn.dacas.emmclient.mdm.AppCapabilityDlg;
import cn.dacas.emmclient.update.UpdateManager;
import cn.dacas.emmclient.util.PrefUtils;
import cn.dacas.emmclient.widget.BadgeView;
import cn.dacas.emmclient.worker.ConnectionDetector;
import de.greenrobot.event.EventBus;

public class NewMainActivity extends QdscFormalActivity {

	private static EmmClientApplication app;

	private static boolean isShowingPolicyAlert = false;
	private MenuItem mMenuItem;
	private MenuItem certItem;
	SharedPreferencesHelper patternPwdSetting;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_main);
		app = (EmmClientApplication) this.getApplicationContext();
		patternPwdSetting = SharedPreferencesHelper
				.getInstance(getApplicationContext());
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
//		getMenuInflater().inflate(R.menu.default_setting, menu);
//		mMenuItem = menu.findItem(R.id.action_list);
//		certItem = menu.findItem(R.id.certificate_settings);
//		int uninstallCert = getUninstallCertNum();
//		if (uninstallCert != 0) {
//			mMenuItem.setIcon(buildCounterDrawable(uninstallCert,
//					R.drawable.ic_action_overflow));
//			certItem.setIcon(buildCounterDrawable(uninstallCert,
//					R.drawable.ic_cert_settings));
//		} else {
//			mMenuItem.setIcon(R.drawable.ic_action_overflow);
//			certItem.setIcon(R.drawable.ic_cert_settings);
//		}
//		return super.onCreateOptionsMenu(menu);
        return true;
	}

	private int getUninstallCertNum() {
		File certDirFile = this.getDir("cert", MODE_PRIVATE);
		if (!certDirFile.exists()) {
			certDirFile.mkdir();
			return 0;
		}
		certDirFile.setReadable(true);
		certDirFile.setWritable(true);
		File[] newCertList = certDirFile.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String filename) {
				// TODO Auto-generated method stub
				if (filename.lastIndexOf(".cer") != -1
						|| filename.lastIndexOf(".crt") != -1)
					return true;
				return false;
			}
		});

		if (newCertList == null)
			return 0;
		int count = newCertList.length;
		// 已安装的cert
		File installedCertDirFile = new File(System.getenv("ANDROID_DATA")
				+ "/misc/keychain/cacerts-added");
		File[] installedCertList = installedCertDirFile.listFiles();

		// 比对获取未安装的cert
		for (File nF : newCertList) {
			Map<String, Object> item = new HashMap<String, Object>();
			String name = nF.getName();

			item.put("title", name);
			item.put("installed", "未安装");

			if (installedCertList != null) {
				try {
					// nF.setReadable(true);
					// nF.setWritable(true);
					InputStream newCert = new FileInputStream(nF);
					byte newCertbytes[] = new byte[(int) nF.length()];
					newCert.read(newCertbytes);
					newCert.close();
					for (File iF : installedCertList) {
						InputStream installedCert = new FileInputStream(iF);
						byte installedCertbytes[] = new byte[(int) iF.length()];
						installedCert.read(installedCertbytes);
						installedCert.close();
						if (Arrays.equals(installedCertbytes, newCertbytes)) {
							item.put("installed", "已安装");
							count--;
							break;
						}
					}
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return count;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			startActivity(new Intent(this, SettingsActivity.class));
			break;
		case R.id.certificate_settings:
			startActivityForResult((new Intent(this,
					CertificateSettingsActivity.class)), 0);
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private static Drawable buildCounterDrawable(int count,
			int backgroundImageId) {
		LayoutInflater inflater = LayoutInflater.from(app);
		View view = inflater.inflate(R.layout.menu_badge, null);
		view.setBackgroundResource(backgroundImageId);

		if (count == 0) {
			View counterTextPanel = view.findViewById(R.id.counterValuePanel);
            counterTextPanel.setVisibility(View.GONE);
		} else {
			TextView textView = (TextView) view.findViewById(R.id.count);
			textView.setText("");
		}

		view.measure(View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
				.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
		view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
		view.buildDrawingCache();

		view.setDrawingCacheEnabled(true);
		view.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
		Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
		view.setDrawingCacheEnabled(false);

		return new BitmapDrawable(app.getResources(), bitmap);
	}

	public void checkPolicy() {
		if (!app.getPolicyManager().accordToPolicyRestrictions()) {
			AlertDialog.Builder policyBuilder = new AlertDialog.Builder(
					NewMainActivity.this);
			policyBuilder.setIcon(R.drawable.rootblock_icon_attention_color);
			policyBuilder.setTitle("危险");
			policyBuilder.setMessage("设备状态不满足管理员设置的安全策略，前往修复？");

			policyBuilder.setPositiveButton("确定",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent intent = new Intent(NewMainActivity.this,
									PolicyAttentionActivity.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							NewMainActivity.this.startActivity(intent);
							dialog.dismiss();
							isShowingPolicyAlert = false;
						}
					});
			AlertDialog alertDialog = policyBuilder.create();
			alertDialog.setCanceledOnTouchOutside(false);
			alertDialog.setCancelable(false);

			// alertDialog.getWindow().setType(
			// WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
			if (!isShowingPolicyAlert) {
				alertDialog.show();
				isShowingPolicyAlert = true;
			}
		}
	}

    public void onStart() {
        super.onStart();
    }

	public void onResume() {
		super.onResume();
		checkPolicy();
		MobclickAgent.onResume(this);
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}


	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 0) {
			int uninstallCert = getUninstallCertNum();
			if (uninstallCert != 0) {
				mMenuItem.setIcon(buildCounterDrawable(0,
						R.drawable.ic_action_overflow));
				certItem.setIcon(buildCounterDrawable(0,
						R.drawable.ic_cert_settings));
			} else {
				mMenuItem.setIcon(R.drawable.ic_action_overflow);
				certItem.setIcon(R.drawable.ic_cert_settings);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// this.moveTaskToBack(true);
			exitBy2Click(); // 调用双击退出函数
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private static Boolean isExit = false;

	private void exitBy2Click() {
		// TODO Auto-generated method stub
		Timer tExit = null;
		if (isExit == false) {
			isExit = true; // 准备退出
			Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
			tExit = new Timer();
			tExit.schedule(new TimerTask() {
				@Override
				public void run() {
					isExit = false; // 取消退出
				}
			}, 2000); // 如果2秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务

		} else {
			finish();
			//System.exit(0);
		}
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		private ViewPager vpMain;
		private View rootView;
		// private ImageView ivPage;
		private TextView tvPage;

		private ImageButton sbOnline;
		private TextView tvOnline;

		private ImageButton sbPrivacy;
		private ImageButton sbAdvice;
		private ImageButton sbUpdate;
		private ImageButton sbPolicy;
		private ImageButton sbAbout;

		private View page1;

		private LinearLayout page1section1;
		private LinearLayout page1section2;
		private LinearLayout page1section3;
		private LinearLayout page1section4;
		private LinearLayout page1section5;

		private FrameLayout flPage;

		private List<View> views;
		private EmmClientApplication app;
		private SharedPreferences appListSharedPreferences;
		private SharedPreferences unreadMsgCount;
//		ListAsynTask listAsynTask;
		MyAdapter adapter = new MyAdapter();
		MyListener listener = new MyListener();
		public final static String REFRESH_MAIN_ACTIVITY = "refresh_main_activity";
		public final static String NEW_MESSAGE = "new_message";
		private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				if (action.equals(REFRESH_MAIN_ACTIVITY)) {
					appListSharedPreferences = app.getSharedPreferences(app
							.getCheckAccount().getCurrentAccount() + "appList",
							0);
					String pageNo = "";
					pageNo += String.valueOf(vpMain.getCurrentItem() + 1);
					pageNo += "/";
					pageNo += String.valueOf(views.size());
					tvPage.setText(pageNo);
					if (getActivity() instanceof NewMainActivity) {
						((NewMainActivity) getActivity()).checkPolicy();
					}
				}
				else if(action.equals(NEW_MESSAGE)){
					refreshFragment();
					adapter.notifyDataSetChanged();
					String pageNo = "";
					pageNo += String.valueOf(vpMain.getCurrentItem() + 1);
					pageNo += "/";
					pageNo += String.valueOf(views.size());
					tvPage.setText(pageNo);
				}
			}

		};

		public void registerBoradcastReceiver() {
			IntentFilter myIntentFilter = new IntentFilter();
			myIntentFilter.addAction(REFRESH_MAIN_ACTIVITY);
			myIntentFilter.addAction(NEW_MESSAGE);
			// 注册广播
			getActivity().getApplicationContext().registerReceiver(
					mBroadcastReceiver, myIntentFilter);
		}

		public void unregisterBoradcastReceiver() {
			// 反注册广播
			getActivity().getApplicationContext().unregisterReceiver(
					mBroadcastReceiver);
		}

		private Interpolator accelerator = new AccelerateInterpolator();
		private Interpolator decelerator = new DecelerateInterpolator();

		@Override
		public void onCreate(Bundle savedInstanceState) {
			// TODO Auto-generated method stub
			super.onCreate(savedInstanceState);
			registerBoradcastReceiver();
			unreadMsgCount = getActivity().getApplicationContext()
					.getSharedPreferences(PrefUtils.MSG_COUNT, 0);
			app = (EmmClientApplication) getActivity().getApplicationContext();
			appListSharedPreferences = app.getSharedPreferences(app
					.getCheckAccount().getCurrentAccount() + "appList", 0);
			EventBus.getDefault().register(this);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			
			rootView = inflater.inflate(R.layout.activity_imitate_main,
					container, false);

			sbOnline = (ImageButton) rootView.findViewById(R.id.sb_online);

			tvOnline = (TextView) rootView.findViewById(R.id.tv_online);
            if (EmmClientApplication.mActivateDevice.online)
                sbOnline.setImageResource(R.drawable.rootblock_icon_online);
			sbOnline.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if (!((EmmClientApplication)PlaceholderFragment.this.getActivity().getApplicationContext()).getActivateDevice().online) {
						if (ConnectionDetector
								.isNetworkConnected(PlaceholderFragment.this
										.getActivity())) {
						} else {
							AlertDialog.Builder builder = new AlertDialog.Builder(
									PlaceholderFragment.this.getActivity());
							builder.setTitle("网络错误")
									.setMessage("网络异常，请重新配置网络！")
									.setPositiveButton(
											"确定",
											new DialogInterface.OnClickListener() {

												@Override
												public void onClick(
														DialogInterface dialog,
														int which) {
													// TODO Auto-generated
													// method stub
													Intent intent = new Intent(
															Settings.ACTION_SETTINGS);
													startActivity(intent);

													dialog.dismiss();
												}
											})
									.setNegativeButton(
											"取消",
											new DialogInterface.OnClickListener() {

												@Override
												public void onClick(
														DialogInterface dialog,
														int which) {
													// TODO Auto-generated
													// method stub
													dialog.dismiss();
												}
											});

							builder.create().show();
						}
					}
				}
			});

			// // activate = ((EmmClientApplication) this.getActivity()
			// .getApplicationContext()).getActivateDevice();

			vpMain = (ViewPager) rootView.findViewById(R.id.vp_main);
			views = new ArrayList<View>();
			page1 = View.inflate(this.getActivity(),
					R.layout.activity_imitate_rootblock_main_first, null);
			views.add(page1);

			flPage = (FrameLayout) rootView.findViewById(R.id.framelayout_page);
			// ivPage = (ImageView)rootView.findViewById(R.id.iv_page);
			tvPage = (TextView) rootView.findViewById(R.id.tv_page);

			// 企业消息		
			page1section1 = (LinearLayout) page1
					.findViewById(R.id.page1section1);
			//ImageView target = (ImageView) page1section1.findViewById(R.id.imageView1);
			BadgeView bv = new BadgeView(getActivity().getApplicationContext(), page1section1);
			if(unreadMsgCount.getInt("unread_count", 0) == 0){
				bv.hide();
			}
			else{
				bv.setText(""+unreadMsgCount.getInt("unread_count", 0));
				bv.setTextColor(Color.YELLOW);
				bv.setTextSize(12);
				bv.setBadgePosition(BadgeView.POSITION_TOP_RIGHT); //默认值
				bv.show();
			}
			page1section1.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					SharedPreferences.Editor editor = unreadMsgCount.edit();
					editor.putInt("unread_count", 0);
					editor.commit();
					rootView.getContext().startActivity(
							new Intent(rootView.getContext(),
									DeviceMessageActivity.class));
				}
			});

			// 应用商店
			page1section2 = (LinearLayout) page1
					.findViewById(R.id.page1section2);
			page1section2.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					rootView.getContext().startActivity(
							new Intent(rootView.getContext(),
									AppStoreActivity.class));
				}
			});

			// 个人备份恢复
			page1section3 = (LinearLayout) page1
					.findViewById(R.id.page1section3);
			page1section3.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					rootView.getContext().startActivity(
							new Intent(rootView.getContext(),
									BackupActivity.class));
				}
			});

			// 企业文档
			page1section4 = (LinearLayout) page1
					.findViewById(R.id.page1section4);
			page1section4.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(rootView.getContext(),
							DocActivity.class);
					rootView.getContext().startActivity(intent);
				}
			});

			// 企业通讯录
			page1section5 = (LinearLayout) page1
					.findViewById(R.id.page1section5);
			page1section5.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(rootView.getContext(),
							ContactActivity.class);
					rootView.getContext().startActivity(intent);
				}
			});

			sbAdvice = (ImageButton) rootView.findViewById(R.id.sb_advice);
			sbAdvice.setVisibility(View.INVISIBLE);
			sbAdvice.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					PlaceholderFragment.this.getActivity().startActivity(
							new Intent(PlaceholderFragment.this.getActivity(),
									AdviceActivity.class));
				}
			});

			sbUpdate = (ImageButton) rootView.findViewById(R.id.sb_update);
			sbUpdate.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					UpdateManager manager = new UpdateManager(
							PlaceholderFragment.this.getActivity());
					// 检查软件更新
					manager.checkUpdate();
				}
			});

			sbPolicy = (ImageButton) rootView.findViewById(R.id.sb_policy);
			sbPolicy.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					v.getContext().startActivity(
							new Intent(v.getContext(),
									PolicyAttentionActivity.class));

				}
			});

			sbAbout = (ImageButton) rootView.findViewById(R.id.sb_about);
			sbAbout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					v.getContext().startActivity(
							new Intent(v.getContext(), ExitFromSettings.class));
				}
			});

			sbPrivacy = (ImageButton) rootView.findViewById(R.id.sb_privacy);
			sbPrivacy.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					AppCapabilityDlg dlg = new AppCapabilityDlg(
							PlaceholderFragment.this.getActivity());
					dlg.showLogInDlg();

				}
			});

			List<LayoutItem> list = getLocalAppList();
			LinearLayout lLayout;
			ImageView iV = null;
			TextView tV = null;
			View vIV = null;
			View vTV = null;
			LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.MATCH_PARENT, 1);
			imageParams.setMargins(0, 20, 0, 0);
			LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.MATCH_PARENT, (float) 1.5);
			int resId = 0;

			// 处理附加模块
			int otherSectionNumber = list.size();
			int lastSection = otherSectionNumber;
			int count = 0;

			if (lastSection >= 1) {
				// 设置page1section6
				vIV = View
						.inflate(getActivity(), R.layout.imageview_item, null);
				vTV = View.inflate(getActivity(), R.layout.textview_item, null);
				final LayoutItem lItem = list.get(count++);
				lLayout = (LinearLayout) page1.findViewById(R.id.page1section6);
				lLayout.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						if (lItem.type.equals("WEB")) {
							Uri uri = Uri.parse(lItem.url);
							Intent webappIntent = new Intent(
									Intent.ACTION_VIEW, uri);
							startActivity(webappIntent);
							return;
						}
						if (!isInstall(lItem.pkgName)) {
							Toast.makeText(getActivity(), "程序未安装！",
									Toast.LENGTH_SHORT).show();
							return;
						}
						Intent intent = getActivity().getPackageManager()
								.getLaunchIntentForPackage(lItem.pkgName);
						startActivity(intent);
					}
				});
				iV = (ImageView) vIV.findViewById(R.id.imageViewItem);
				tV = (TextView) vTV.findViewById(R.id.textViewItem);

				if (lItem.icon != null)
					iV.setImageDrawable(lItem.icon);
				tV.setText(lItem.appName);
				iV.setLayoutParams(imageParams);
				tV.setLayoutParams(textParams);
				tV.setGravity(Gravity.CENTER);
				lLayout.addView(iV);
				lLayout.addView(tV);

				lastSection -= 1;

				while (lastSection != 0) {
					// 新建一页
					View newPage = View.inflate(getActivity(),
							R.layout.activity_imitate_main_second, null);
					views.add(newPage);

					if (lastSection >= 6) {
						// 放6个
						for (int i = 0; i < 6; i++) {
							vIV = View.inflate(getActivity(),
									R.layout.imageview_item, null);
							vTV = View.inflate(getActivity(),
									R.layout.textview_item, null);
							final LayoutItem lItem3 = list.get(count++);

							try {
								resId = getResId(i);
							} catch (NoSuchFieldException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IllegalAccessException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IllegalArgumentException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

							lLayout = (LinearLayout) newPage
									.findViewById(resId);
							lLayout.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									// TODO Auto-generated method stub
									if (lItem3.type.equals("WEB")) {
										Uri uri = Uri.parse(lItem3.url);
										Intent webappIntent = new Intent(
												Intent.ACTION_VIEW, uri);
										startActivity(webappIntent);
										return;
									}
									if (!isInstall(lItem3.pkgName)) {
										Toast.makeText(getActivity(), "程序未安装！",
												Toast.LENGTH_SHORT).show();
										return;
									}
									Intent intent = getActivity()
											.getPackageManager()
											.getLaunchIntentForPackage(
													lItem3.pkgName);
									startActivity(intent);

								}
							});
							iV = (ImageView) vIV
									.findViewById(R.id.imageViewItem);
							tV = (TextView) vTV.findViewById(R.id.textViewItem);
							tV.setText(lItem3.appName);
							if (lItem3.icon != null)
								iV.setImageDrawable(lItem3.icon);
							iV.setLayoutParams(imageParams);
							tV.setLayoutParams(textParams);
							tV.setGravity(Gravity.CENTER);
							lLayout.addView(iV);
							lLayout.addView(tV);
						}
						lastSection -= 6;
					} else {
						// 放完剩下的
						for (int i = 0; i < lastSection; i++) {
							vIV = View.inflate(getActivity(),
									R.layout.imageview_item, null);
							vTV = View.inflate(getActivity(),
									R.layout.textview_item, null);
							final LayoutItem lItem4 = list.get(count++);

							try {
								resId = getResId(i);
							} catch (NoSuchFieldException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IllegalAccessException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IllegalArgumentException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

							lLayout = (LinearLayout) newPage
									.findViewById(resId);
							lLayout.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									if (lItem4.type.equals("WEB")) {
										Uri uri = Uri.parse(lItem4.url);
										Intent webappIntent = new Intent(
												Intent.ACTION_VIEW, uri);
										startActivity(webappIntent);
										return;
									}
									if (!isInstall(lItem4.pkgName)) {
										Toast.makeText(getActivity(), "程序未安装！",
												Toast.LENGTH_SHORT).show();
										return;
									}
									Intent intent = getActivity()
											.getPackageManager()
											.getLaunchIntentForPackage(
													lItem4.pkgName);
									startActivity(intent);

								}
							});
							iV = (ImageView) vIV
									.findViewById(R.id.imageViewItem);
							tV = (TextView) vTV.findViewById(R.id.textViewItem);
							tV.setText(lItem4.appName);
							if (lItem4.icon != null)
								iV.setImageDrawable(lItem4.icon);
							iV.setLayoutParams(imageParams);
							tV.setLayoutParams(textParams);
							tV.setGravity(Gravity.CENTER);
							lLayout.addView(iV);
							lLayout.addView(tV);
						}
						// 不放的透明
						for (int i = lastSection; i < 6; i++) {
							try {
								resId = getResId(i);
							} catch (NoSuchFieldException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IllegalAccessException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IllegalArgumentException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							lLayout = (LinearLayout) newPage
									.findViewById(resId);
							lLayout.setBackgroundColor(getResources().getColor(color.transparent));
						}
						break;
					}
				}
			} else {
				if (lastSection == 0) {
					// 第一页最后两个透明
					lLayout = (LinearLayout) page1
							.findViewById(R.id.page1section6);
					lLayout.setBackgroundColor(getResources().getColor(color.transparent));
				} else {
					// 设置page1section6
					View v1 = View.inflate(getActivity(),
							R.layout.imageview_item, null);
					View v2 = View.inflate(getActivity(),
							R.layout.textview_item, null);

					final LayoutItem lItem5 = list.get(0);
					lLayout = (LinearLayout) page1
							.findViewById(R.id.page1section6);
					lLayout.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							if (lItem5.type.equals("WEB")) {
								Uri uri = Uri.parse(lItem5.url);
								Intent webappIntent = new Intent(
										Intent.ACTION_VIEW, uri);
								startActivity(webappIntent);
								return;
							}
							if (!isInstall(lItem5.pkgName)) {
								Toast.makeText(getActivity(), "程序未安装！",
										Toast.LENGTH_SHORT).show();
								return;
							}
							Intent intent = getActivity().getPackageManager()
									.getLaunchIntentForPackage(lItem5.pkgName);
							startActivity(intent);

						}
					});
					iV = (ImageView) v1.findViewById(R.id.imageViewItem);
					tV = (TextView) v2.findViewById(R.id.textViewItem);
					tV.setText(lItem5.appName);
					if (lItem5.icon != null)
						iV.setImageDrawable(lItem5.icon);
					iV.setLayoutParams(imageParams);
					tV.setLayoutParams(textParams);
					tV.setGravity(Gravity.CENTER);
					lLayout.addView(iV);
					lLayout.addView(tV);

				}
			}

			vpMain.setAdapter(adapter);
			vpMain.setOnPageChangeListener(listener);

			return rootView;
		}

		@Override
		public void onResume() {
			// TODO Auto-generated method stub
			super.onResume();
			String pageNo = "";
			pageNo += String.valueOf(vpMain.getCurrentItem() + 1);
			pageNo += "/";
			pageNo += String.valueOf(views.size());
			tvPage.setText(pageNo);
//			listAsynTask = new ListAsynTask();
//			listAsynTask.execute((Void) null);
		}

		private List<LayoutItem> getLocalAppList() {
			List<LayoutItem> list = new ArrayList<LayoutItem>();
			int size = appListSharedPreferences.getInt("count", 0);
			for(int i = 0;i<size;i++){
				String tmp = appListSharedPreferences.getString(String.valueOf(i), null);
				JSONObject json = null ;
				try {
					if(tmp != null){
						json = new JSONObject(tmp);
					}			
					else continue;
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				LayoutItem item = new LayoutItem();
				try {
					item.type = json.getString("type");
					item.appName = json.getString("appName");
					item.firstInstallTime = json.getLong("firstInstallTime");
					item.id = json.getString("id");
				} catch (JSONException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				

				FileInputStream fis = null;
				BufferedInputStream bis = null;
				Bitmap bmpRet = null;

				if (item.type.equals("APK")) {
					try {
						item.pkgName = json.getString("pkgName");
					} catch (JSONException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
					try {
						fis = app.openFileInput(item.id + ".png");
						bis = new BufferedInputStream(fis);
						bmpRet = BitmapFactory.decodeStream(bis);
						item.icon = new BitmapDrawable(app.getResources(),
								bmpRet);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						PackageManager pm = getActivity().getPackageManager();
						try {
							item.icon = pm.getApplicationIcon(item.pkgName);
						} catch (NameNotFoundException e1) {
							// TODO Auto-generated catch block
							item.icon = getActivity().getApplicationContext()
									.getResources()
									.getDrawable(R.drawable.android_logo);
							e.printStackTrace();
						}
					}

				} else if (item.type.equals("WEB")) {
					try {
						item.url = json.getString("url");
					} catch (JSONException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					try {
						fis = app.openFileInput(item.id + ".png");
						bis = new BufferedInputStream(fis);
						bmpRet = BitmapFactory.decodeStream(bis);
						item.icon = new BitmapDrawable(app.getResources(),
								bmpRet);
					} catch (FileNotFoundException e) {
						item.icon = getActivity().getApplicationContext()
								.getResources()
								.getDrawable(R.drawable.android_logo);
						e.printStackTrace();
					}
				}
				try {
					if (bis != null)
						bis.close();
					if (fis != null)
						fis.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				list.add(item);
			}
			return list;
		}

		private void refreshFragment() {
			adapter = new MyAdapter();
			listener = new MyListener();
			views = new ArrayList<View>();
			page1 = View.inflate(this.getActivity(),
					R.layout.activity_imitate_rootblock_main_first, null);
			views.add(page1);

			// 企业消息
			page1section1 = (LinearLayout) page1
					.findViewById(R.id.page1section1);
			BadgeView bv = new BadgeView(getActivity().getApplicationContext(), page1section1);
			if(unreadMsgCount.getInt("unread_count", 0) == 0){
				bv.hide();
			}
			else{
				bv.setText(""+unreadMsgCount.getInt("unread_count", 0));
				bv.setTextColor(Color.YELLOW);
				bv.setTextSize(12);
				bv.setBadgePosition(BadgeView.POSITION_TOP_RIGHT); //默认值
				bv.show();
			}
			page1section1.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					SharedPreferences.Editor editor = unreadMsgCount.edit();
					editor.putInt("unread_count", 0);
					editor.commit();
					rootView.getContext().startActivity(
							new Intent(rootView.getContext(),
									DeviceMessageActivity.class));
				}
			});

			// 应用商店
			page1section2 = (LinearLayout) page1
					.findViewById(R.id.page1section2);
			page1section2.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					rootView.getContext().startActivity(
							new Intent(rootView.getContext(),
									AppStoreActivity.class));
				}
			});

			// 个人备份恢复
			page1section3 = (LinearLayout) page1
					.findViewById(R.id.page1section3);
			page1section3.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					rootView.getContext().startActivity(
							new Intent(rootView.getContext(),
									BackupActivity.class));
				}
			});

			// 企业文档
			page1section4 = (LinearLayout) page1
					.findViewById(R.id.page1section4);
			page1section4.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(rootView.getContext(),
							DocActivity.class);
					rootView.getContext().startActivity(intent);
				}
			});

			// 企业通讯录
			page1section5 = (LinearLayout) page1
					.findViewById(R.id.page1section5);
			page1section5.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(rootView.getContext(),
							ContactActivity.class);
					rootView.getContext().startActivity(intent);
				}
			});

			sbAdvice = (ImageButton) rootView.findViewById(R.id.sb_advice);
			sbAdvice.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					PlaceholderFragment.this.getActivity().startActivity(
							new Intent(PlaceholderFragment.this.getActivity(),
									AdviceActivity.class));
				}
			});

			sbUpdate = (ImageButton) rootView.findViewById(R.id.sb_update);
			sbUpdate.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					UpdateManager manager = new UpdateManager(
							PlaceholderFragment.this.getActivity());
					// 检查软件更新
					manager.checkUpdate();
				}
			});

			sbPolicy = (ImageButton) rootView.findViewById(R.id.sb_policy);
			sbPolicy.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					v.getContext().startActivity(
							new Intent(v.getContext(),
									PolicyAttentionActivity.class));

				}
			});

			sbAbout = (ImageButton) rootView.findViewById(R.id.sb_about);
			sbAbout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					v.getContext().startActivity(
							new Intent(v.getContext(), ExitFromSettings.class));
				}
			});

			sbPrivacy = (ImageButton) rootView.findViewById(R.id.sb_privacy);
			sbPrivacy.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					AppCapabilityDlg dlg = new AppCapabilityDlg(
							PlaceholderFragment.this.getActivity());
					dlg.showLogInDlg();

				}
			});

			List<LayoutItem> list = getLocalAppList();
			LinearLayout lLayout;
			ImageView iV = null;
			TextView tV = null;
			View vIV = null;
			View vTV = null;
			LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.MATCH_PARENT, 1);
			imageParams.setMargins(0, 20, 0, 0);
			LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.MATCH_PARENT, (float) 1.5);
			int resId = 0;

			// 处理附加模块
			int otherSectionNumber = list.size();
			int lastSection = otherSectionNumber;
			int count = 0;

			if (lastSection >= 1) {
				// 设置page1section6
				vIV = View
						.inflate(getActivity(), R.layout.imageview_item, null);
				vTV = View.inflate(getActivity(), R.layout.textview_item, null);
				final LayoutItem lItem = list.get(count++);
				lLayout = (LinearLayout) page1.findViewById(R.id.page1section6);
				lLayout.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						if (lItem.type.equals("WEB")) {
							Uri uri = Uri.parse(lItem.url);
							Intent webappIntent = new Intent(
									Intent.ACTION_VIEW, uri);
							startActivity(webappIntent);
							return;
						}
						if (!isInstall(lItem.pkgName)) {
							Toast.makeText(getActivity(), "程序未安装！",
									Toast.LENGTH_SHORT).show();
							return;
						}
						Intent intent = getActivity().getPackageManager()
								.getLaunchIntentForPackage(lItem.pkgName);
						startActivity(intent);
					}
				});
				iV = (ImageView) vIV.findViewById(R.id.imageViewItem);
				tV = (TextView) vTV.findViewById(R.id.textViewItem);

				if (lItem.icon != null)
					iV.setImageDrawable(lItem.icon);
				tV.setText(lItem.appName);
				iV.setLayoutParams(imageParams);
				tV.setLayoutParams(textParams);
				tV.setGravity(Gravity.CENTER);
				lLayout.addView(iV);
				lLayout.addView(tV);

				lastSection -= 1;

				while (lastSection != 0) {
					// 新建一页
					View newPage = View.inflate(getActivity(),
							R.layout.activity_imitate_main_second, null);
					views.add(newPage);

					if (lastSection >= 6) {
						// 放6个
						for (int i = 0; i < 6; i++) {
							vIV = View.inflate(getActivity(),
									R.layout.imageview_item, null);
							vTV = View.inflate(getActivity(),
									R.layout.textview_item, null);
							final LayoutItem lItem3 = list.get(count++);

							try {
								resId = getResId(i);
							} catch (NoSuchFieldException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IllegalAccessException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IllegalArgumentException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

							lLayout = (LinearLayout) newPage
									.findViewById(resId);
							lLayout.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									// TODO Auto-generated method stub
									if (lItem3.type.equals("WEB")) {
										Uri uri = Uri.parse(lItem3.url);
										Intent webappIntent = new Intent(
												Intent.ACTION_VIEW, uri);
										startActivity(webappIntent);
										return;
									}
									if (!isInstall(lItem3.pkgName)) {
										Toast.makeText(getActivity(), "程序未安装！",
												Toast.LENGTH_SHORT).show();
										return;
									}
									Intent intent = getActivity()
											.getPackageManager()
											.getLaunchIntentForPackage(
													lItem3.pkgName);
									startActivity(intent);

								}
							});
							iV = (ImageView) vIV
									.findViewById(R.id.imageViewItem);
							tV = (TextView) vTV.findViewById(R.id.textViewItem);
							tV.setText(lItem3.appName);
							if (lItem3.icon != null)
								iV.setImageDrawable(lItem3.icon);
							iV.setLayoutParams(imageParams);
							tV.setLayoutParams(textParams);
							tV.setGravity(Gravity.CENTER);
							lLayout.addView(iV);
							lLayout.addView(tV);
						}
						lastSection -= 6;
					} else {
						// 放完剩下的
						for (int i = 0; i < lastSection; i++) {
							vIV = View.inflate(getActivity(),
									R.layout.imageview_item, null);
							vTV = View.inflate(getActivity(),
									R.layout.textview_item, null);
							final LayoutItem lItem4 = list.get(count++);

							try {
								resId = getResId(i);
							} catch (NoSuchFieldException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IllegalAccessException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IllegalArgumentException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

							lLayout = (LinearLayout) newPage
									.findViewById(resId);
							lLayout.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									// TODO Auto-generated method stub
									if (lItem4.type.equals("WEB")) {
										Uri uri = Uri.parse(lItem4.url);
										Intent webappIntent = new Intent(
												Intent.ACTION_VIEW, uri);
										startActivity(webappIntent);
										return;
									}
									if (!isInstall(lItem4.pkgName)) {
										Toast.makeText(getActivity(), "程序未安装！",
												Toast.LENGTH_SHORT).show();
										return;
									}
									Intent intent = getActivity()
											.getPackageManager()
											.getLaunchIntentForPackage(
													lItem4.pkgName);
									startActivity(intent);

								}
							});
							iV = (ImageView) vIV
									.findViewById(R.id.imageViewItem);
							tV = (TextView) vTV.findViewById(R.id.textViewItem);
							tV.setText(lItem4.appName);
							if (lItem4.icon != null)
								iV.setImageDrawable(lItem4.icon);
							iV.setLayoutParams(imageParams);
							tV.setLayoutParams(textParams);
							tV.setGravity(Gravity.CENTER);
							lLayout.addView(iV);
							lLayout.addView(tV);
						}
						// 不放的透明
						for (int i = lastSection; i < 6; i++) {
							try {
								resId = getResId(i);
							} catch (NoSuchFieldException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IllegalAccessException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IllegalArgumentException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							lLayout = (LinearLayout) newPage
									.findViewById(resId);
							lLayout.setBackgroundColor(getResources().getColor(color.transparent));
						}
						break;
					}
				}
			} else {
				if (lastSection == 0) {
					// 第一页最后两个透明
					lLayout = (LinearLayout) page1
							.findViewById(R.id.page1section6);
					lLayout.setBackgroundColor(getResources().getColor(color.transparent));
				} else {
					// 设置page1section6
					View v1 = View.inflate(getActivity(),
							R.layout.imageview_item, null);
					View v2 = View.inflate(getActivity(),
							R.layout.textview_item, null);

					final LayoutItem lItem5 = list.get(0);
					lLayout = (LinearLayout) page1
							.findViewById(R.id.page1section6);
					lLayout.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							if (lItem5.type.equals("WEB")) {
								Uri uri = Uri.parse(lItem5.url);
								Intent webappIntent = new Intent(
										Intent.ACTION_VIEW, uri);
								startActivity(webappIntent);
								return;
							}
							if (!isInstall(lItem5.pkgName)) {
								Toast.makeText(getActivity(), "程序未安装！",
										Toast.LENGTH_SHORT).show();
								return;
							}
							Intent intent = getActivity().getPackageManager()
									.getLaunchIntentForPackage(lItem5.pkgName);
							startActivity(intent);

						}
					});
					iV = (ImageView) v1.findViewById(R.id.imageViewItem);
					tV = (TextView) v2.findViewById(R.id.textViewItem);
					tV.setText(lItem5.appName);
					if (lItem5.icon != null)
						iV.setImageDrawable(lItem5.icon);
					iV.setLayoutParams(imageParams);
					tV.setLayoutParams(textParams);
					tV.setGravity(Gravity.CENTER);
					lLayout.addView(iV);
					lLayout.addView(tV);

				}
			}

			vpMain.setAdapter(adapter);
			vpMain.setOnPageChangeListener(listener);

		}

		/**
		 * @param index
		 *            模块在页中编号
		 * @return 模块的ResId
		 * @throws NoSuchFieldException
		 * @throws IllegalAccessException
		 * @throws IllegalArgumentException
		 */
		private int getResId(int index) throws NoSuchFieldException,
				IllegalAccessException, IllegalArgumentException {
			String name = "page2section";
			name += String.valueOf(index + 1);
			Field f = R.id.class.getField(name);
			return f.getInt(R.id.class);
		}

		@Override
		public void onDestroyView() {
			super.onDestroyView();
		}

		public void onDestroy() {
			// TODO Auto-generated method stub
			super.onDestroy();
//			if (listAsynTask!=null)
//				listAsynTask.cancel(true);
			unregisterBoradcastReceiver();
			EventBus.getDefault().unregister(this);
		}
		
		public void onEventMainThread(MessageEvent event)
		{
			switch (event.type)
			{
				case MessageEvent.Event_OnlineState:
					boolean online=event.params.getBoolean("online");
					if (!online) {
					sbOnline.setImageResource(R.drawable.rootblock_icon_online_selected);
					tvOnline.setText("离线");
					}
					else {
						sbOnline.setImageResource(R.drawable.rootblock_icon_online);
						tvOnline.setText("在线");
					}
				default:
					break;
			}
		}
			

		private void flipit(View one, View two) {
			final View visible;
			final View invisible;
			if (one.getVisibility() == View.GONE) {
				visible = two;
				invisible = one;

			} else {
				invisible = two;
				visible = one;

			}
			ObjectAnimator visToInvis = ObjectAnimator.ofFloat(visible,
					"rotationY", 0f, 90f);
			visToInvis.setDuration(500);
			visToInvis.setInterpolator(accelerator);
			final ObjectAnimator invisToVis = ObjectAnimator.ofFloat(invisible,
					"rotationY", -90f, 0f);
			invisToVis.setDuration(500);
			invisToVis.setInterpolator(decelerator);
			visToInvis.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator anim) {
					visible.setVisibility(View.GONE);
					invisToVis.start();
					invisible.setVisibility(View.VISIBLE);
				}
			});
			visToInvis.start();

		}

		/**
		 * 通过报名判断app是否已经安装
		 * 
		 * @param pkgName
		 *            包名
		 * @return true:已安装 false：未安装
		 */
		private boolean isInstall(String pkgName) {

			try {
				getActivity().getPackageManager().getPackageInfo(pkgName, 0);
			} catch (NameNotFoundException e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}

		class MyAdapter extends PagerAdapter {

			@Override
			public int getCount() {
				return views.size();
			}

			@Override
			public boolean isViewFromObject(View view, Object obj) {
				return view == obj;
			}

			@Override
			public void destroyItem(ViewGroup container, int position,
					Object object) {
				container.removeView((View) object);
			}

			@Override
			public Object instantiateItem(ViewGroup container, int position) {
				container.addView(views.get(position));
				return views.get(position);
			}
		}

		class MyListener implements OnPageChangeListener {

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageSelected(int arg0) {
				String pageNo = "";
				pageNo += String.valueOf(vpMain.getCurrentItem() + 1);
				pageNo += "/";
				pageNo += String.valueOf(views.size());
				flipit(flPage, flPage);
				tvPage.setText(pageNo);
			}
		}

		class LayoutItem implements Comparable<Object> {
			public String appName = null;
			public Drawable icon = null;
			public String pkgName = null;
			public long firstInstallTime = -1;
			public String type = null;
			public String url = null;
			public String id = null;

			@Override
			public int compareTo(Object another) {
				// TODO Auto-generated method stub
				LayoutItem tgt = (LayoutItem) another;
				long result = this.firstInstallTime - tgt.firstInstallTime;
				if (result < 0)
					return -1;
				if (result > 0)
					return 1;
				return 0;
			}

		}

		/**
		 * @author Wang
		 * 
		 */
//		class ListAsynTask extends
//				AsyncTask<Void, LayoutItem, List<LayoutItem>> {
//
//			@Override
//			protected List<LayoutItem> doInBackground(Void... params) {
//				// TODO Auto-generated method stub
//
//				String ip = NetworkDef.getAvailableWebServiceIp();
//				if (ip == null)
//					return null;
//				CheckAccount account = CheckAccount
//						.getCheckAccountInstance(app);
//				// 创建连接
//				// HttpClient httpClient = new DefaultHttpClient();
//				HttpClient httpClient = HttpsClient.newHttpsClient();
//				HttpGet get = new HttpGet(NetworkDef.PROTOCOL + ip
//						+ "/EMMS-WS/api/v1/users/"
//						+ app.getCheckAccount().getCurrentAccount()
//						+ "/apps?platform=ANDROID&access_token="
//						+ account.getAccessToken());
//				ip = null;
//				account = null;
//				HttpResponse httpResponse = null;
//				String result = null;
//				JSONArray array = null;
//				List<LayoutItem> list = new ArrayList<LayoutItem>();
//				HashMap<String,JSONObject> newMap = new HashMap<String, JSONObject>();
//				SharedPreferences settings = app.getSharedPreferences(
//						PrefUtils.PREF_NAME, 0);
//				String blackApps = settings
//						.getString(PrefUtils.BLACK_KEY, null);
//
//				try {
//					httpResponse = httpClient.execute(get);
//					result = EntityUtils.toString(httpResponse.getEntity());
//					httpResponse = null;
//					array = new JSONArray(result);
//					result = null;
//				} catch (ClientProtocolException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//					return (List<LayoutItem>) null;
//				} catch (IOException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//					return (List<LayoutItem>) null;
//				} catch (JSONException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//
//
//
//				try {
//					if (array != null) {
//						for (int i = 0; i < array.length(); i++) {
//							JSONObject json = (JSONObject) array.get(i);
//							String type = json.getString("type");
//
//							if (type.equals("APK")) {
//								int versionCode=0;
//								if (json.has("version_code")&&!json.getString("version_code").trim().equals(""))
//									versionCode=Integer.parseInt(json.getString("version_code"));
//								PushAppsListItem item = new PushAppsListItem(
//										json.getString("name"),
//										json.getString("description"),
//										versionCode,
//										json.getString("version_name"),
//										json.getString("package_name"),
//										json.getString("icon_path"),
//										AppListFragment.OPTIONAL_APP,json.getString("id"));
//								if (getVersionCode(item.pkgName) != -1) {
//									if (blackApps != null
//											&& blackApps.contains(item.pkgName))
//										continue;
//									Drawable icon = getIcon(item.iconUrl);
//									if (icon != null) {
//										Bitmap bmpToSave = ((BitmapDrawable) icon)
//												.getBitmap();
//										try {
//											FileOutputStream fOS = app.openFileOutput(
//													item.id + ".png",
//													Context.MODE_PRIVATE);
//											bmpToSave.compress(Bitmap.CompressFormat.PNG,
//													100, fOS);
//											fOS.flush();
//											fOS.close();
//										} catch (FileNotFoundException e) {
//											// TODO Auto-generated catch block
//											e.printStackTrace();
//										} catch (IOException e) {
//											// TODO Auto-generated catch block
//											e.printStackTrace();
//										}
//									}
//									JSONObject newJsonObject = new JSONObject();
//									newJsonObject.put("appName", item.appName);
//									newJsonObject.put("pkgName", item.pkgName);
//									newJsonObject.put("firstInstallTime", getActivity().getPackageManager().getPackageInfo(item.pkgName, 0).firstInstallTime);
//									newJsonObject.put("icon", getIcon(item.iconUrl));
//									newJsonObject.put("type", type);
//									newJsonObject.put("id", item.id);
//									newMap.put(item.id, newJsonObject);
//								}
//							} else if (type.equals("WEB")) {
//								PushAppsListItem item = new PushAppsListItem(
//										json.getString("name"),
//										json.getString("icon_path"),
//										json.getString("created_at"),AppListFragment.OPTIONAL_APP,json.getString("id"));
//
//
//								Date date = new Date();
//								DateFormat sdf = new SimpleDateFormat(
//										"yyyy-MM-dd HH:mm:ss", Locale.CHINA);
//								try {
//									date = sdf.parse(item.created_time);
//								} catch (java.text.ParseException e) {
//									// TODO Auto-generated catch block
//									e.printStackTrace();
//								}// date类型
//								Drawable icon = getIcon(item.iconUrl);
//								if (icon != null) {
//									Bitmap bmpToSave = ((BitmapDrawable) icon)
//											.getBitmap();
//									try {
//										FileOutputStream fOS = app.openFileOutput(
//												item.id + ".png",
//												Context.MODE_PRIVATE);
//										bmpToSave.compress(Bitmap.CompressFormat.PNG,
//												100, fOS);
//										fOS.flush();
//										fOS.close();
//									} catch (FileNotFoundException e) {
//										// TODO Auto-generated catch block
//										e.printStackTrace();
//									} catch (IOException e) {
//										// TODO Auto-generated catch block
//										e.printStackTrace();
//									}
//								}
//								JSONObject newJsonObject = new JSONObject();
//								newJsonObject.put("appName", item.appName);
//								newJsonObject.put("firstInstallTime", date.getTime());
//								newJsonObject.put("icon", getIcon(item.iconUrl));
////								newJsonObject.put("url", item.url);
//								newJsonObject.put("type", type);
//								newJsonObject.put("id", item.id);
//								newJsonObject.put("use", false);
//								newMap.put(item.id, newJsonObject);
//							}
//						}
//					}
//				} catch (JSONException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (NameNotFoundException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				array = null;
//
//				//Collections.sort(list);
//
//				// 再在本地存一份
//				SharedPreferences.Editor appListEditor = appListSharedPreferences
//						.edit();
//				HashMap<String,JSONObject> oldMap = new HashMap<String, JSONObject>();
//				int count = appListSharedPreferences.getInt("count", 0);
//				for(int i = 0;i<count;i++){
//					String tmp = appListSharedPreferences.getString(String.valueOf(i), null);
//					JSONObject json = null ;
//					if(tmp!=null){
//						try {
//							json = new JSONObject(tmp);
//						} catch (JSONException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//					}
//					else
//						continue;
//					String id = null;
//					try {
//						id = json.getString("id");
//					} catch (JSONException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					oldMap.put(id, json);
//				}
//
//				appListEditor.clear();
//				count = 0;
//				Set<String> set = newMap.keySet();
//				Iterator<String> iter = set.iterator();
//				while(iter.hasNext()){
//					String key = iter.next();
//					if(oldMap.containsKey(key)){
//						newMap.put(key, oldMap.get(key));
//					}
//					else{
//						JSONObject json = newMap.get(key);
//						String id;
//						String type;
//						try {
//							type = json.getString("type");
//							if(type.equals("WEB")){
//								id = json.getString("id");
//								String url = getUrl(id);
//								if(url == null || url == ""){
//									continue;
//								}
//								json.put("url", url);
//							}
//						} catch (JSONException e) {
//							e.printStackTrace();
//							continue;
//						}
//
//						newMap.put(key, json);
//					}
//					appListEditor.putString(String.valueOf(count++), newMap.get(key).toString());
//				}
//				appListEditor.putInt("count", count);
//				appListEditor.commit();
//				list = getLocalAppList();
//
//				return list;
//			}
//
//			private String getUrl(String id) {
//				String ip = NetworkDef.getAvailableWebServiceIp();
//				if (ip == null)
//					return null;
//				CheckAccount account = CheckAccount
//						.getCheckAccountInstance(app);
//				// 创建连接
//				// HttpClient httpClient = new DefaultHttpClient();
//				HttpClient httpClient = HttpsClient.newHttpsClient();
//				HttpGet get = new HttpGet(NetworkDef.PROTOCOL + ip
//						+ "/EMMS-WS/api/v1/user/apps/download/"+id+"?uuid="+ PhoneInfoExtractor.getPhoneInfoExtractor(app).getIMEI()+"&access_token="
//						+ account.getAccessToken());
//
//				HttpResponse httpResponse = null;
//				String result = null;
//
//				String url = null ;
//				try {
//					httpResponse = httpClient.execute(get);
//					System.gc();
//					result = EntityUtils.toString(httpResponse.getEntity());
//					JSONObject json = new JSONObject(result);
//					url = json.getString("url");
//				} catch (ClientProtocolException e1) {
//					e1.printStackTrace();;
//				} catch (IOException e1) {
//					e1.printStackTrace();
//				} catch (JSONException e) {
//					e.printStackTrace();
//				}
//				return url;
//			}
//
//			@Override
//			protected void onPreExecute() {
//				super.onPreExecute();
//			}
//
//			@Override
//			protected void onPostExecute(List<LayoutItem> list) {
//				super.onPostExecute(list);
//
//				Message message = Message.obtain();
//				if (list == null)
//					message.arg1 = 3;
//				else {
//					message.arg1 = 4;
//				}
//				OnlineHandler.sendMessage(message);
//			}
//
//			@Override
//			protected void onProgressUpdate(LayoutItem... values) {
//				super.onProgressUpdate(values);
//			}
//
//			private int getVersionCode(String pkgName) {
//				try {
//					PackageManager manager = getActivity().getPackageManager();
//					PackageInfo info = manager.getPackageInfo(pkgName, 0);
//					return info.versionCode;
//				} catch (Exception e) {
//					e.printStackTrace();
//					return -1;
//				}
//			}
//
//			private Drawable getIcon(String url) {
//				Drawable drawable;
//				try {
//
//					String str = getTrueUrl(url);
//					drawable = Drawable.createFromStream(
//							new URL(str).openStream(), null);
//				} catch (MalformedURLException e) {
//					e.printStackTrace();
//					return (Drawable) null;
//				} catch (IOException e) {
//					e.printStackTrace();
//					return (Drawable) null;
//				}
//				return drawable;
//
//			}
//
//			private String getTrueUrl(String str)
//					throws UnsupportedEncodingException {
//				String c;
//				String result = "";
//				for (int i = 0; i < str.length(); i++) {
//					c = str.substring(i, i + 1);
//					if (c.length() != c.getBytes().length) {
//						result += URLEncoder.encode(c, "utf-8");
//					} else
//						result += c;
//				}
//
//				return result;
//			}
//		}

	}

}

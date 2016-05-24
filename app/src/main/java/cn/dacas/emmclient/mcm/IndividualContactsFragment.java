package cn.dacas.emmclient.mcm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import cn.dacas.emmclient.R;
import cn.dacas.emmclient.mcm.ContactItem.MPair;
import cn.dacas.emmclient.util.IsRunningForeground;
import cn.dacas.emmclient.worker.PhoneInfoExtractor;

/**
 * A placeholder fragment containing a simple view.
 */
@SuppressLint("HandlerLeak")
public class IndividualContactsFragment extends Fragment {
	private View rootView = null;
	private RelativeLayout backupContactsRelativeLayout = null;
	private RelativeLayout restoreContactsRelativeLayout = null;
	private RelativeLayout backupSmsRelativeLayout = null;
	private RelativeLayout restoreSmsRelativeLayout = null;

	private static final int BACKUP_CONTACTS_HDL = 1;
	private static final int RESTORE_CONTACTS_HDL = 2;
	private static final int BACKUP_SMS_HDL = 3;
	private static final int RESTORE_SMS_HDL = 4;

	private ProgressDialog m_pDialog = null;

	private static final String SMS_ALL = "content://sms/";

	/**
	 * The fragment argument representing the section number for this fragment.
	 */
	private static final String ARG_SECTION_NUMBER = "section_number";

	public static Fragment newInstance(int sectionNumber) {
		IndividualContactsFragment curFragment = new IndividualContactsFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		curFragment.setArguments(args);
		return curFragment;
	}

	private Handler actionOverHandler = new Handler() {
		@SuppressWarnings("deprecation")
		@TargetApi(Build.VERSION_CODES.HONEYCOMB)
		public void handleMessage(Message msg) {
			StringBuilder notify_title = new StringBuilder("");
			StringBuilder notify_msg = new StringBuilder("");
			int smallIcon = 0;
			switch (msg.what) {
			case BACKUP_CONTACTS_HDL:
				notify_title.append("通讯录备份完成");
				notify_msg.append("共备份" + msg.arg2 + "个手机或sim卡联系人");
				smallIcon = R.drawable.backup_contact_local;
				break;
			case RESTORE_CONTACTS_HDL:
				// 联系人还原结束
				notify_title.append("通讯录恢复完成");
				notify_msg.append("新增" + msg.arg2 + "个手机或sim卡联系人");
				smallIcon = R.drawable.revert_contact_local;
				break;
			case BACKUP_SMS_HDL:
				// 短信备份结束
				notify_title.append("短信备份完成");
				notify_msg.append("共备份" + msg.arg2 + "条短信");
				smallIcon = R.drawable.backup_message_local;
				break;
			case RESTORE_SMS_HDL:
				// 短信还原结束
				notify_title.append("短信恢复完成");
				notify_msg.append("新增" + msg.arg2 + "条短信");
				smallIcon = R.drawable.revert_message_local;
				break;
			}

			// 联系人备份结束
			if (msg.arg1 != 0) {
				// 直接显示备份通讯录的条目数量
				AlertDialog.Builder builder = new AlertDialog.Builder(
						rootView.getContext());
				builder.setTitle(notify_title);
				builder.setMessage(notify_msg);
				builder.setIcon(smallIcon);
				builder.setNegativeButton("确定",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								dialog.cancel();
							}
						});
				builder.create().show();
			} else {
				// 如果在后台执行，在通知栏显示备份通讯录的条目数量
				NotificationManager notificationManager = (NotificationManager) rootView
						.getContext().getSystemService(
								android.content.Context.NOTIFICATION_SERVICE);
				Notification.Builder nBuilder = new Notification.Builder(
						rootView.getContext());
				nBuilder.setSmallIcon(smallIcon);

				Intent intent = new Intent(rootView.getContext(),
						BackupActivity.class);
				PendingIntent contentIntent = PendingIntent.getActivity(
						rootView.getContext(), 0, intent, 0);
				nBuilder.setContentIntent(contentIntent)
						.setWhen(System.currentTimeMillis())
						.setAutoCancel(true).setContentTitle(notify_title)
						.setContentText(notify_msg);

				Notification notification = nBuilder.getNotification();
				notification.defaults = Notification.DEFAULT_SOUND;
				notificationManager.notify(1, notification);
			}
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(
				R.layout.fragment_individual_contacts_management, container,
				false);
		backupContactsRelativeLayout = (RelativeLayout) rootView
				.findViewById(R.id.backupContactsRelativeLayout);
		backupContactsRelativeLayout.setOnClickListener(backupContactsListener);

		restoreContactsRelativeLayout = (RelativeLayout) rootView
				.findViewById(R.id.restoreContactsRelativeLayout);
		restoreContactsRelativeLayout
				.setOnClickListener(restoreContactsListener);

		backupSmsRelativeLayout = (RelativeLayout) rootView
				.findViewById(R.id.backupSmsRelativeLayout);
		backupSmsRelativeLayout.setOnClickListener(backupSmsListener);

		restoreSmsRelativeLayout = (RelativeLayout) rootView
				.findViewById(R.id.restoreSmsRelativeLayout);
		restoreSmsRelativeLayout.setOnClickListener(restoreSmsListener);

		PhoneInfoExtractor.getPhoneInfoExtractor(rootView.getContext()
				.getApplicationContext());

		return rootView;
	}

	private OnClickListener backupContactsListener = new OnClickListener() {

		@Override
		public void onClick(final View v) {
			// TODO Auto-generated method stub, back up individual contacts
			// 如果没有登录，跳转到登录界面
			final Cursor cursor = rootView
					.getContext()
					.getContentResolver()
					.query(ContactsContract.Contacts.CONTENT_URI,
							null,
							null,
							null,
							ContactsContract.Contacts.DISPLAY_NAME
									+ " COLLATE LOCALIZED ASC");
			cursor.moveToFirst();
			final int contactsCount = cursor.getCount();

			// 创建ProgressDialog对象
			m_pDialog = new ProgressDialog(rootView.getContext());
			// 设置进度条风格，风格为长形
			m_pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			// 设置ProgressDialog 标题
			m_pDialog.setTitle("备份个人通讯录");
			// 设置ProgressDialog 提示信息
			m_pDialog.setMessage("正在备份,请等待");
			// 设置ProgressDialog 标题图标
			m_pDialog.setIcon(R.drawable.backup_contact_local);
			// 设置ProgressDialog 的进度条是否不明确
			m_pDialog.setIndeterminate(false);
			// 设置ProgressDialog 是否可以按退回按键取消
			m_pDialog.setCancelable(false);
			// 设置ProgressDialog 的最大长度
			m_pDialog.setMax(contactsCount);
			// 让ProgressDialog显示
			m_pDialog.show();

			new Thread() {
				public void run() {
					synchronized (this) {
						ArrayList<ContactItem> contacts = new ArrayList<ContactItem>();
						int m_count = 0;
						int idColumn = cursor
								.getColumnIndex(ContactsContract.Contacts._ID);
						int displayNameColumn = cursor
								.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
						if ((cursor != null) && (cursor.getCount() > 0)) {
							do {
								m_count++;
								m_pDialog.setProgress(m_count);

								ContactItem contactItem = new ContactItem();
								// 获得联系人的ID号
								String contactId = cursor.getString(idColumn);
								// 获得联系人姓名
								String disPlayName = cursor
										.getString(displayNameColumn);

								contactItem.displayName = disPlayName;

								if (disPlayName == null) {
									continue;
								}

								// 查看该联系人有多少个电话号码。如果没有这返回值为0
								int phoneCount = cursor
										.getInt(cursor
												.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
								if (phoneCount > 0) {
									// 获得联系人的电话号码
									Cursor phones = rootView
											.getContext()
											.getContentResolver()
											.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
													null,
													ContactsContract.CommonDataKinds.Phone.CONTACT_ID
															+ " = " + contactId,
													null, null);
									if (phones == null) {
										continue;
									}
									if (phones.moveToFirst()) {
										do {
											// 遍历所有的电话号码
											String phoneNumber = phones
													.getString(phones
															.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
											String phoneType = phones
													.getString(phones
															.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
											if (phoneNumber == null) {
												continue;
											}
											contactItem.phones
													.add(new MPair(
															phoneType,
															phoneNumber));
										} while (phones.moveToNext());
									}
									if (contactItem.phones.size() <= 0) {
										continue;
									}
									if (!phones.isClosed()) {
										phones.close();
									}
								} else {
									// 不包含号码的联系人不进行备份
									continue;
								}

								// 获取该联系人邮箱
								Cursor emails = rootView
										.getContext()
										.getContentResolver()
										.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
												null,
												ContactsContract.CommonDataKinds.Phone.CONTACT_ID
														+ " = " + contactId,
												null, null);
								if (emails == null) {
									continue;
								}
								if (emails.moveToFirst()) {
									do {
										// 遍历所有的电话号码
										String emailType = emails
												.getString(emails
														.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));
										String emailValue = emails
												.getString(emails
														.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
										contactItem.emails
												.add(new MPair(
														emailType, emailValue));
									} while (emails.moveToNext());
								}
								if (!emails.isClosed()) {
									emails.close();
								}
								contacts.add(contactItem);
							} while (cursor.moveToNext());
							// TODO 上传通讯录到企业服务器
							uploadContact(contacts);
						}

						if ((cursor != null) && (!cursor.isClosed())) {
							cursor.close();
						}

						m_pDialog.cancel();

						// TODO：将contacts中的数据发送除去

						boolean foreground = IsRunningForeground
								.isRunningForeground(rootView.getContext());

						Message msg = new Message();
						msg.what = BACKUP_CONTACTS_HDL;
						msg.arg1 = foreground ? 1 : 0;
						msg.arg2 = contacts.size();
						actionOverHandler.sendMessage(msg);
					}
				}
			}.start();
		}
	};

	public void uploadContact(final ArrayList<ContactItem> contacts) {
		// 将contact写文件并加密
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				String workingDir = rootView.getContext()
						.getApplicationContext().getFilesDir()
						.getAbsolutePath();
				
				File originalFile = new File(workingDir
						+ "/contact");
				
				if(originalFile.exists()){
					originalFile.delete();
				}
				
				File contactFile = new File(workingDir
						+ "/contact");
				
				ObjectOutputStream oos;
				try {
					oos = new ObjectOutputStream(new FileOutputStream(contactFile));
					synchronized (this) {
						for (ContactItem item : contacts) {
							try {
								oos.writeObject(item);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						contacts.clear();
					}
					oos.close();
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}).start();
	}

	private OnClickListener restoreContactsListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub, restore individual contacts
			// 网络连接可用，开始恢复通讯录
			synchronized (this) {
				final ArrayList<ContactItem> contacts = getAllBackupContacts();
				if (contacts == null) {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							rootView.getContext());
					builder.setTitle("无法恢复");
					builder.setMessage("不存在已经备份的联系人");
					builder.setNegativeButton("确定",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									dialog.cancel();
								}
							});
					builder.create().show();
				} else {
					// 将contacts中的联系人重新添加到本地通讯录中
					m_pDialog = new ProgressDialog(rootView.getContext());
					// 设置进度条风格，风格为长形
					m_pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
					// 设置ProgressDialog 标题
					m_pDialog.setTitle("恢复个人通讯录");
					// 设置ProgressDialog 提示信息
					m_pDialog.setMessage("正在恢复,请等待");
					// 设置ProgressDialog 标题图标
					m_pDialog.setIcon(R.drawable.revert_contact_local);
					// 设置ProgressDialog 的进度条是否不明确
					m_pDialog.setIndeterminate(false);
					// 设置ProgressDialog 是否可以按退回按键取消
					m_pDialog.setCancelable(false);
					// 让ProgressDialog显示
					m_pDialog.show();

					new Thread() {
						public void run() {
							HashSet<Integer> existedContactsStr = getExistedContactsStr();

							// contacts
							int count = 0;
							for (int idx = 0; idx < contacts.size(); idx++) {
								ContactItem contactItem = contacts.get(idx);

								ContentValues values = new ContentValues();

								Uri rawContactUri = null;
								long rawContactId = -1;

								// 如果该姓名下的号码或者邮件在手机原来通讯录中不存在，则添加该名字
								boolean nameInserted = false;
								List<MPair> phones = contactItem.phones;

								for (MPair pair : phones) {
									if (!existedContactsStr
											.contains(pair.content.hashCode())) {
										if (!nameInserted) {
											// 插入姓名行
											// 往data表入姓名数据
											values.clear();
											// 首先向RawContacts.CONTENT_URI执行一个空值插入，目的是获取系统返回的rawContactId
											rawContactUri = rootView
													.getContext()
													.getContentResolver()
													.insert(RawContacts.CONTENT_URI,
															values);
											rawContactId = ContentUris
													.parseId(rawContactUri);
											values.put(Data.RAW_CONTACT_ID,
													rawContactId);
											values.put(
													Data.MIMETYPE,
													StructuredName.CONTENT_ITEM_TYPE);
											values.put(
													StructuredName.GIVEN_NAME,
													contactItem.displayName);
											rootView.getContext()
													.getContentResolver()
													.insert(android.provider.ContactsContract.Data.CONTENT_URI,
															values);

											count++;
											nameInserted = true;
										}

										// 插入号码行
										// 往data表入电话数据
										values.clear();
										values.put(
												android.provider.ContactsContract.Contacts.Data.RAW_CONTACT_ID,
												rawContactId);
										values.put(Data.MIMETYPE,
												Phone.CONTENT_ITEM_TYPE);
										values.put(Phone.NUMBER, pair.content);
										values.put(Phone.TYPE, pair.type);
										rootView.getContext()
												.getContentResolver()
												.insert(android.provider.ContactsContract.Data.CONTENT_URI,
														values);
									}
								}

								List<MPair> emails = contactItem.emails;
								for (MPair pair : emails) {
									if (!existedContactsStr
											.contains(pair.content.hashCode())) {
										if (!nameInserted) {
											// 插入姓名行
											values.clear();
											if ((rawContactUri == null)
													|| (rawContactId == -1)) {
												rawContactUri = rootView
														.getContext()
														.getContentResolver()
														.insert(RawContacts.CONTENT_URI,
																values);
												rawContactId = ContentUris
														.parseId(rawContactUri);
											}
											values.put(Data.RAW_CONTACT_ID,
													rawContactId);
											values.put(
													Data.MIMETYPE,
													StructuredName.CONTENT_ITEM_TYPE);
											values.put(
													StructuredName.GIVEN_NAME,
													contactItem.displayName);
											rootView.getContext()
													.getContentResolver()
													.insert(android.provider.ContactsContract.Data.CONTENT_URI,
															values);

											count++;
											nameInserted = true;
										}

										// 插入邮件行
										// 往data表入Email数据
										values.clear();
										values.put(
												android.provider.ContactsContract.Contacts.Data.RAW_CONTACT_ID,
												rawContactId);
										values.put(Data.MIMETYPE,
												Email.CONTENT_ITEM_TYPE);
										values.put(Email.DATA, pair.content);
										values.put(Email.TYPE, pair.type);
										rootView.getContext()
												.getContentResolver()
												.insert(android.provider.ContactsContract.Data.CONTENT_URI,
														values);
									}
								}
							}

							m_pDialog.cancel();

							boolean foreground = IsRunningForeground
									.isRunningForeground(rootView.getContext());

							Message msg = new Message();
							msg.what = RESTORE_CONTACTS_HDL;
							msg.arg1 = foreground ? 1 : 0;
							msg.arg2 = count;
							actionOverHandler.sendMessage(msg);
						}
					}.start();
				}
			}
		}
	};

	private ArrayList<ContactItem> getAllBackupContacts() {
		// TODO 获取全部已经备份的联系人
		ArrayList<ContactItem> contacts = new ArrayList<ContactItem>();
		String workingDir = rootView.getContext().getApplicationContext()
				.getFilesDir().getAbsolutePath();
		File contactFile = new File(workingDir + "/contact");
		
		if(contactFile.exists()){
			ObjectInputStream ois;
			try {
				ois = new ObjectInputStream(new FileInputStream(contactFile));
				try {
					ContactItem obj;
					do{
						obj = (ContactItem) ois.readObject();
						contacts.add(obj);
					}while(obj != null);
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ois.close();
			} catch (StreamCorruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		return contacts;
	}

	private OnClickListener backupSmsListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub, back up individual contacts
			// 网络连接可用，开始备份短信
			// 短信备份功能
			synchronized (this) {
				final ArrayList<SmsContent> smsArray = new ArrayList<SmsContent>();
				ContentResolver resolver = rootView.getContext()
						.getContentResolver();
				Uri uri = Uri.parse(SMS_ALL);
				final Cursor cursor = resolver.query(uri, null, null, null,
						null);
				cursor.moveToFirst();

				final int smsCount = cursor.getCount();

				// 创建ProgressDialog对象
				m_pDialog = new ProgressDialog(rootView.getContext());
				// 设置进度条风格，风格为长形
				m_pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				// 设置ProgressDialog 标题
				m_pDialog.setTitle("备份个人短信");
				// 设置ProgressDialog 提示信息
				m_pDialog.setMessage("正在备份,请等待");
				// 设置ProgressDialog 标题图标
				m_pDialog.setIcon(R.drawable.backup_message_local);
				// 设置ProgressDialog 的进度条是否不明确
				m_pDialog.setIndeterminate(false);
				// 设置ProgressDialog 是否可以按退回按键取消
				m_pDialog.setCancelable(false);
				// 设置ProgressDialog 的最大长度
				m_pDialog.setMax(smsCount);
				// 让ProgressDialog显示
				m_pDialog.show();

				new Thread() {
					public void run() {
						smsArray.clear();
						int m_count = 0;

						if ((cursor != null) && (cursor.getCount() > 0)) {
							do {
								m_count++;
								m_pDialog.setProgress(m_count);
								SmsContent smsItem = new SmsContent();
								smsItem.address = cursor.getString(cursor
										.getColumnIndex("address"));
								smsItem.body = cursor.getString(cursor
										.getColumnIndex("body"));
								smsItem.date = cursor.getString(cursor
										.getColumnIndex("date"));
								smsItem.type = cursor.getString(cursor
										.getColumnIndex("type"));
								if (smsItem.address != null) {
									smsArray.add(smsItem);
								}
							} while (cursor.moveToNext());
							uploadSms(smsArray);
						}
						if (!cursor.isClosed()) {
							cursor.close();
						}

						m_pDialog.cancel();

						// TODO: 将smsArray中的数据发送出去

						boolean foreground = IsRunningForeground
								.isRunningForeground(rootView.getContext());

						Message msg = new Message();
						msg.what = BACKUP_SMS_HDL;
						msg.arg1 = foreground ? 1 : 0;
						msg.arg2 = smsArray.size();
						actionOverHandler.sendMessage(msg);
					}
				}.start();
			}
		}
	};

	public void uploadSms(final ArrayList<SmsContent> smsArray) {
		// 将smsArray写文件并加密
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				String workingDir = rootView.getContext()
						.getApplicationContext().getFilesDir()
						.getAbsolutePath();
				
				File origFile = new File(workingDir
						+ "/sms");
				if(origFile.exists()){
					origFile.delete();
				}
				
				File sms = null;
				FileOutputStream fos = null;
				ObjectOutputStream oos = null;
				try {
					sms = new File(workingDir
							+ "/sms");
					fos = new FileOutputStream(sms);
					oos = new ObjectOutputStream(fos);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				for (SmsContent item : smsArray) {
					try {
						oos.writeObject(item);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				try {
					oos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	private OnClickListener restoreSmsListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub, back up individual contacts
			// 网络连接可用，开始恢复短信
			synchronized (this) {
				final ArrayList<SmsContent> smsArray = getAllBackupSms();
				if (smsArray == null) {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							rootView.getContext());
					builder.setTitle("无法恢复");
					builder.setMessage("不存在已经备份的信息");
					builder.setNegativeButton("确定",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									dialog.cancel();
								}
							});
					builder.create().show();
				} else {
					m_pDialog = new ProgressDialog(rootView.getContext());
					// 设置进度条风格，风格为长形
					m_pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
					// 设置ProgressDialog 标题
					m_pDialog.setTitle("恢复个人短信");
					// 设置ProgressDialog 提示信息
					m_pDialog.setMessage("正在恢复,请等待");
					// 设置ProgressDialog 标题图标
					m_pDialog.setIcon(R.drawable.revert_message_local);
					// 设置ProgressDialog 的进度条是否不明确
					m_pDialog.setIndeterminate(false);
					// 设置ProgressDialog 是否可以按退回按键取消
					m_pDialog.setCancelable(false);
					// 让ProgressDialog显示
					m_pDialog.show();

					new Thread() {
						public void run() {
							HashSet<Integer> existedSms = getExistedSms();
							int count = 0;
							for (int idx = 0; idx < smsArray.size(); idx++) {
								SmsContent smsItem = smsArray.get(idx);
								if (existedSms
										.contains((smsItem.address
												+ smsItem.body + smsItem.date + smsItem.type)
												.hashCode()) == false) {
									ContentValues values = new ContentValues();
									values.put("address", smsItem.address);
									values.put("body", smsItem.body);
									values.put("date", smsItem.date);
									values.put("type", smsItem.type);

									// TODO IllegalArgumentException, unable to
									// find or allocate a thread id
									rootView.getContext()
											.getContentResolver()
											.insert(Uri.parse("content://sms/"),
													values);
									count++;
								}
							}
							m_pDialog.cancel();
							boolean foreground = IsRunningForeground
									.isRunningForeground(rootView.getContext());

							Message msg = new Message();
							msg.what = RESTORE_SMS_HDL;
							msg.arg1 = foreground ? 1 : 0;
							msg.arg2 = count;
							actionOverHandler.sendMessage(msg);
						}
					}.start();
				}
			}
		}
	};

	private HashSet<Integer> getExistedSms() {
		HashSet<Integer> existedSms = new HashSet<Integer>();
		ContentResolver resolver = rootView.getContext().getContentResolver();
		Uri uri = Uri.parse(SMS_ALL);
		Cursor cursor = resolver.query(uri, null, null, null, null);
		cursor.moveToFirst();
		if ((cursor != null) && (cursor.getCount() > 0)) {
			do {
				SmsContent smsItem = new SmsContent();
				smsItem.address = cursor.getString(cursor
						.getColumnIndex("address"));
				smsItem.body = cursor.getString(cursor.getColumnIndex("body"));
				smsItem.date = cursor.getString(cursor.getColumnIndex("date"));
				smsItem.type = cursor.getString(cursor.getColumnIndex("type"));
				if (smsItem.address != null) {
					existedSms.add((smsItem.address + smsItem.body
							+ smsItem.date + smsItem.type).hashCode());
				}
			} while (cursor.moveToNext());
		}
		return existedSms;
	}

	private HashSet<Integer> getExistedContactsStr() {
		// 初始化
		// existedContactsStr
		HashSet<Integer> existedContactsStr = new HashSet<Integer>();

		final Cursor cursor = rootView
				.getContext()
				.getContentResolver()
				.query(ContactsContract.Contacts.CONTENT_URI,
						null,
						null,
						null,
						ContactsContract.Contacts.DISPLAY_NAME
								+ " COLLATE LOCALIZED ASC");

		cursor.moveToFirst();
		if ((cursor == null) || (cursor.getCount() <= 0)) {
			return existedContactsStr;
		}

		int idColumn = cursor.getColumnIndex(ContactsContract.Contacts._ID);
		int displayNameColumn = cursor
				.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
		do {
			ContactItem contactItem = new ContactItem();
			// 获得联系人的ID号
			String contactId = cursor.getString(idColumn);
			// 获得联系人姓名
			String disPlayName = cursor.getString(displayNameColumn);

			contactItem.displayName = disPlayName;

			if (disPlayName == null) {
				continue;
			}

			// 查看该联系人有多少个电话号码。如果没有这返回值为0
			int phoneCount = cursor
					.getInt(cursor
							.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
			if (phoneCount > 0) {
				// 获得联系人的电话号码
				Cursor phones = rootView
						.getContext()
						.getContentResolver()
						.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
								null,
								ContactsContract.CommonDataKinds.Phone.CONTACT_ID
										+ " = " + contactId, null, null);
				if (phones == null) {
					continue;
				}
				if (phones.moveToFirst()) {
					do {
						// 遍历所有的电话号码
						String phoneNumber = phones
								.getString(phones
										.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
						if (phoneNumber != null) {
							existedContactsStr.add(phoneNumber.hashCode());
						}
					} while (phones.moveToNext());
				}
				if (!phones.isClosed()) {
					phones.close();
				}
			} else {
				continue;
			}

			// 获取该联系人邮箱
			Cursor emails = rootView
					.getContext()
					.getContentResolver()
					.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
							null,
							ContactsContract.CommonDataKinds.Phone.CONTACT_ID
									+ " = " + contactId, null, null);
			if (emails == null) {
				continue;
			}
			if (emails.moveToFirst()) {
				do {
					// 遍历所有的电话号码
					String emailValue = emails
							.getString(emails
									.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
					if (emailValue != null) {
						existedContactsStr.add(emailValue.hashCode());
					}
				} while (emails.moveToNext());
			}
			if (!emails.isClosed()) {
				emails.close();
			}
		} while (cursor.moveToNext());

		if (!cursor.isClosed()) {
			cursor.close();
		}

		return existedContactsStr;
	}

	private ArrayList<SmsContent> getAllBackupSms() {
		// TODO 获取全部已经备份的联系人
		ArrayList<SmsContent> smsArray = new ArrayList<SmsContent>();
		String workingDir = rootView.getContext().getApplicationContext()
				.getFilesDir().getAbsolutePath();
		File smsFile = new File(workingDir + "/sms");

		if (smsFile.exists()) {
			try {
				ObjectInputStream ois = new ObjectInputStream(
						new FileInputStream(smsFile));
				try {
					SmsContent obj;
					do{
						obj = (SmsContent) ois.readObject();
						smsArray.add(obj);
					}while(obj != null);
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ois.close();
			} catch (StreamCorruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return smsArray;
	}
}

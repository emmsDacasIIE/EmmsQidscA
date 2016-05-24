package cn.qdsc.msp.ui.activity.mainframe;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
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
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

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

import cn.qdsc.msp.R;
import cn.qdsc.msp.core.mcm.ContactItem;
import cn.qdsc.msp.core.mcm.SmsContent;
import cn.qdsc.msp.ui.activity.base.BaseSlidingFragmentActivity;
import cn.qdsc.msp.ui.qdlayout.QdProgressDialog;

public class BackupAndRestoreActivity extends BaseSlidingFragmentActivity {

    LinearLayout layout_contact_backup, layout_contact_restore, layout_message_backup, layout_message_restore;
    private static final int BACKUP_CONTACTS_HDL = 1;
    private static final int RESTORE_CONTACTS_HDL = 2;
    private static final int BACKUP_SMS_HDL = 3;
    private static final int RESTORE_SMS_HDL = 4;
    private static final int REPORT_PROGRESS = 0;
    private static final String SMS_ALL = "content://sms/";
    private QdProgressDialog mDialog;

    @Override
    protected HearderView_Style setHeaderViewSyle() {
        return HearderView_Style.Image_Text_Null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup_and_restore, "");

        //初始化header
        initHeader();
        initView();

        //来自slidemenuActivity，必须调用，调用后，会出现左边的菜单。同时，调用setTouchModeAbove，这样，在滑动的时候，
        //就不会显示左侧的menu了。
//        setBehindContentView(R.layout.left_menu_frame);
//        getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
    }

    private void initView() {
        layout_contact_backup = (LinearLayout) findViewById(R.id.layout_backupandrestore_contact_backup);
        layout_contact_restore = (LinearLayout) findViewById(R.id.layout_backupandrestore_contact_restore);
        layout_message_backup = (LinearLayout) findViewById(R.id.layout_backupandrestore_message_backup);
        layout_message_restore = (LinearLayout) findViewById(R.id.layout_backupandrestore_message_restore);
        layout_contact_backup.setOnClickListener(backupContactsListener);
        layout_contact_restore.setOnClickListener(restoreContactsListener);
        layout_message_backup.setOnClickListener(backupSmsListener);
        layout_message_restore.setOnClickListener(restoreSmsListener);
    }

    private void initHeader() {

//        mLeftHeaderView.setTextVisibile(false);
//        mLeftHeaderView.setImageVisibile(true);
        mLeftHeaderView.setImageView(R.mipmap.msp_titlebar_leftarrow_icon);

        mMiddleHeaderView.setText(mContext.getString(R.string.title_backup_and_restore));
        mMiddleHeaderView.setTextVisibile(true);
//        mMiddleHeaderView.setImageVisibile(false);

//        mRightHeaderView.setTextVisibile(false);
//        mRightHeaderView.setImageVisibile(false);
    }



    private Handler actionOverHandler = new Handler() {
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public void handleMessage(Message msg) {
            StringBuilder notify_msg = new StringBuilder("");
            int smallIcon = 0;
            switch (msg.what) {
                case REPORT_PROGRESS:
                    if (mDialog!=null){
                        mDialog.setProgress(msg.arg1);
                    }
                    return;
                case BACKUP_CONTACTS_HDL:
                    notify_msg.append("备份成功,联系人增加" + msg.arg2 + "位");
                    smallIcon = R.mipmap.backup_contact_local;
                    break;
                case RESTORE_CONTACTS_HDL:
                    // 联系人还原结束
                    notify_msg.append("恢复成功,联系人增加" + msg.arg2 + "位");
                    smallIcon = R.mipmap.revert_contact_local;
                    break;
                case BACKUP_SMS_HDL:
                    // 短信备份结束
                    notify_msg.append("备份成功,短信增加" + msg.arg2 + "条");
                    smallIcon = R.mipmap.backup_message_local;
                    break;
                case RESTORE_SMS_HDL:
                    // 短信还原结束
                    notify_msg.append("恢复成功,短信增加" + msg.arg2 + "条");
                    smallIcon = R.mipmap.revert_message_local;
                    break;
            }
            if (mDialog!=null){
                mDialog.setMessage(notify_msg.toString());
                mDialog.setProgress(100);
            }
        }
    };

    private View.OnClickListener backupContactsListener = new View.OnClickListener() {

        @Override
        public void onClick(final View v) {
            mDialog=new QdProgressDialog(BackupAndRestoreActivity.this);
            mDialog.show();
            mDialog.setTitle("备份");
            mDialog.setMessage("正在备份,请稍候");

            final Cursor cursor = mContext
                    .getContentResolver()
                    .query(ContactsContract.Contacts.CONTENT_URI,
                            null,
                            null,
                            null,
                            ContactsContract.Contacts.DISPLAY_NAME
                                    + " COLLATE LOCALIZED ASC");
            cursor.moveToFirst();
            final int contactsCount = cursor.getCount();

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
                                Message msg = Message.obtain();
                                msg.what = REPORT_PROGRESS;
                                msg.arg1 = (int)(m_count*1.0/contactsCount*100);
                                actionOverHandler.sendMessage(msg);

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
                                    Cursor phones = mContext
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
                                                    .add(new ContactItem.MPair(
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
                                Cursor emails = mContext
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
                                                .add(new ContactItem.MPair(
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

                        // TODO：将contacts中的数据发送除去

                        Message msg = new Message();
                        msg.what = BACKUP_CONTACTS_HDL;
                        msg.arg1 = 0;
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
                String workingDir = mContext
                        .getApplicationContext().getFilesDir()
                        .getAbsolutePath();

                File originalFile = new File(workingDir
                        + "/contact");

                if (originalFile.exists()) {
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

    private View.OnClickListener restoreContactsListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
// TODO Auto-generated method stub, restore individual contacts
// 网络连接可用，开始恢复通讯录
            synchronized (this) {
                final ArrayList<ContactItem> contacts = getAllBackupContacts();
                if (contacts == null) {
                    Toast.makeText(mContext, "不存在已经备份的联系人", Toast.LENGTH_SHORT);
                } else {
                    // 将contacts中的联系人重新添加到本地通讯录中
                    mDialog=new QdProgressDialog(mContext);
                    mDialog.show();
                    mDialog.setTitle("恢复");
                    mDialog.setMessage("正在恢复,请稍候...");


                    new Thread() {
                        public void run() {
                            HashSet<Integer> existedContactsStr = getExistedContactsStr();

                            // contacts
                            int count = 0;
                            for (int idx = 0; idx < contacts.size(); idx++) {
                                Message msg = Message.obtain();
                                msg.what = REPORT_PROGRESS;
                                msg.arg1 = (int)((int)(idx*1.0/contacts.size()*100));
                                actionOverHandler.sendMessage(msg);
                                ContactItem contactItem = contacts.get(idx);

                                ContentValues values = new ContentValues();

                                Uri rawContactUri = null;
                                long rawContactId = -1;

                                // 如果该姓名下的号码或者邮件在手机原来通讯录中不存在，则添加该名字
                                boolean nameInserted = false;
                                List<ContactItem.MPair> phones = contactItem.phones;

                                for (ContactItem.MPair pair : phones) {
                                    if (!existedContactsStr
                                            .contains(pair.content.hashCode())) {
                                        if (!nameInserted) {
                                            // 插入姓名行
                                            // 往data表入姓名数据
                                            values.clear();
                                            // 首先向RawContacts.CONTENT_URI执行一个空值插入，目的是获取系统返回的rawContactId
                                            rawContactUri = mContext
                                                    .getContentResolver()
                                                    .insert(ContactsContract.RawContacts.CONTENT_URI,
                                                            values);
                                            rawContactId = ContentUris
                                                    .parseId(rawContactUri);
                                            values.put(ContactsContract.Data.RAW_CONTACT_ID,
                                                    rawContactId);
                                            values.put(
                                                    ContactsContract.RawContacts.Data.MIMETYPE,
                                                    ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
                                            values.put(
                                                    ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
                                                    contactItem.displayName);
                                            mContext
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
                                        mContext
                                                .getContentResolver()
                                                .insert(android.provider.ContactsContract.Data.CONTENT_URI,
                                                        values);
                                    }
                                }

                                List<ContactItem.MPair> emails = contactItem.emails;
                                for (ContactItem.MPair pair : emails) {
                                    if (!existedContactsStr
                                            .contains(pair.content.hashCode())) {
                                        if (!nameInserted) {
                                            // 插入姓名行
                                            values.clear();
                                            if ((rawContactUri == null)
                                                    || (rawContactId == -1)) {
                                                rawContactUri = mContext
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
                                            mContext
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
                                        mContext
                                                .getContentResolver()
                                                .insert(android.provider.ContactsContract.Data.CONTENT_URI,
                                                        values);
                                    }
                                }
                            }

                            Message msg = new Message();
                            msg.what = RESTORE_CONTACTS_HDL;
                            msg.arg1 = 0;
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
        String workingDir = mContext.getApplicationContext()
                .getFilesDir().getAbsolutePath();
        File contactFile = new File(workingDir + "/contact");

        if (contactFile.exists()) {
            ObjectInputStream ois;
            try {
                ois = new ObjectInputStream(new FileInputStream(contactFile));
                try {
                    ContactItem obj;
                    do {
                        obj = (ContactItem) ois.readObject();
                        contacts.add(obj);
                    } while (obj != null);
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

    private View.OnClickListener backupSmsListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
// TODO Auto-generated method stub, back up individual contacts
// 网络连接可用，开始备份短信
// 短信备份功能
            synchronized (this) {
                final ArrayList<SmsContent> smsArray = new ArrayList<SmsContent>();
                ContentResolver resolver = mContext
                        .getContentResolver();
                Uri uri = Uri.parse(SMS_ALL);
                final Cursor cursor = resolver.query(uri, null, null, null,
                        null);
                cursor.moveToFirst();

                final int smsCount = cursor.getCount();

                mDialog=new QdProgressDialog(mContext);
                mDialog.show();
                mDialog.setTitle("备份");
                mDialog.setMessage("正在备份,请稍候...");


                new Thread() {
                    public void run() {
                        smsArray.clear();
                        int m_count = 0;

                        if ((cursor != null) && (cursor.getCount() > 0)) {
                            do {
                                m_count++;

                                Message msg = Message.obtain();
                                msg.what = REPORT_PROGRESS;
                                msg.arg1 = (int)(m_count*1.0/smsCount*100);
                                actionOverHandler.sendMessage(msg);

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

                        // TODO: 将smsArray中的数据发送出去

                        Message msg = new Message();
                        msg.what = BACKUP_SMS_HDL;
                        msg.arg1 = 0;
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
                String workingDir = mContext
                        .getApplicationContext().getFilesDir()
                        .getAbsolutePath();

                File origFile = new File(workingDir
                        + "/sms");
                if (origFile.exists()) {
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

    private View.OnClickListener restoreSmsListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
// TODO Auto-generated method stub, back up individual contacts
// 网络连接可用，开始恢复短信
            synchronized (this) {
                final ArrayList<SmsContent> smsArray = getAllBackupSms();
                if (smsArray == null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            mContext);
                    builder.setTitle("无法恢复");
                    Toast.makeText(mContext,"不存在已经备份的信息",Toast.LENGTH_SHORT).show();
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
                    mDialog=new QdProgressDialog(mContext);
                    mDialog.show();
                    mDialog.setTitle("恢复");
                    mDialog.setMessage("正在恢复,请稍候...");


                    new Thread() {
                        public void run() {
                            HashSet<Integer> existedSms = getExistedSms();
                            int count = 0;
                            for (int idx = 0; idx < smsArray.size(); idx++) {

                                Message msg = Message.obtain();
                                msg.what = REPORT_PROGRESS;
                                msg.arg1 = (int)(idx*1.0/smsArray.size()*100);
                                actionOverHandler.sendMessage(msg);

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
                                    mContext
                                            .getContentResolver()
                                            .insert(Uri.parse("content://sms/"),
                                                    values);
                                    count++;
                                }
                            }

                            Message msg = new Message();
                            msg.what = RESTORE_SMS_HDL;
                            msg.arg1 = 0;
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
        ContentResolver resolver = mContext.getContentResolver();
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

        final Cursor cursor = mContext
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
                Cursor phones = mContext
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
            Cursor emails = mContext
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
        String workingDir = mContext.getApplicationContext()
                .getFilesDir().getAbsolutePath();
        File smsFile = new File(workingDir + "/sms");

        if (smsFile.exists()) {
            try {
                ObjectInputStream ois = new ObjectInputStream(
                        new FileInputStream(smsFile));
                try {
                    SmsContent obj;
                    do {
                        obj = (SmsContent) ois.readObject();
                        smsArray.add(obj);
                    } while (obj != null);
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

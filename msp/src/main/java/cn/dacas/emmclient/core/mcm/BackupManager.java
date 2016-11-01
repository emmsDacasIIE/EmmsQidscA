package cn.dacas.emmclient.core.mcm;

/**
 * Created by lenovo on 2016-1-12.
 */
public class BackupManager {
//    public static synchronized  void backupContacts(Context mContext) {
//        final Cursor cursor = mContext.getContentResolver()
//                .query(ContactsContract.Contacts.CONTENT_URI,
//                        null,
//                        null,
//                        null,
//                        ContactsContract.Contacts.DISPLAY_NAME
//                                + " COLLATE LOCALIZED ASC");
//        cursor.moveToFirst();
//        final int contactsCount = cursor.getCount();
//        ArrayList<ContactItem> contacts = new ArrayList<ContactItem>();
//        int m_count = 0;
//        int idColumn = cursor
//                .getColumnIndex(ContactsContract.Contacts._ID);
//        int displayNameColumn = cursor
//                .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
//        if ((cursor != null) && (cursor.getCount() > 0)) {
//            do {
//                m_count++;
//                ContactItem contactItem = new ContactItem();
//                // 获得联系人的ID号
//                String contactId = cursor.getString(idColumn);
//                // 获得联系人姓名
//                String disPlayName = cursor
//                        .getString(displayNameColumn);
//
//                contactItem.displayName = disPlayName;
//
//                if (disPlayName == null) {
//                    continue;
//                }
//
//                // 查看该联系人有多少个电话号码。如果没有这返回值为0
//                int phoneCount = cursor
//                        .getInt(cursor
//                                .getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
//                if (phoneCount > 0) {
//                    // 获得联系人的电话号码
//                    Cursor phones = mContext
//                            .getContentResolver()
//                            .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
//                                    null,
//                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID
//                                            + " = " + contactId,
//                                    null, null);
//                    if (phones == null) {
//                        continue;
//                    }
//                    if (phones.moveToFirst()) {
//                        do {
//                            // 遍历所有的电话号码
//                            String phoneNumber = phones
//                                    .getString(phones
//                                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
//                            String phoneType = phones
//                                    .getString(phones
//                                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
//                            if (phoneNumber == null) {
//                                continue;
//                            }
//                            contactItem.phones
//                                    .add(new ContactItem.MPair(
//                                            phoneType,
//                                            phoneNumber));
//                        } while (phones.moveToNext());
//                    }
//                    if (contactItem.phones.size() <= 0) {
//                        continue;
//                    }
//                    if (!phones.isClosed()) {
//                        phones.close();
//                    }
//                } else {
//                    // 不包含号码的联系人不进行备份
//                    continue;
//                }
//
//                // 获取该联系人邮箱
//                Cursor emails = mContext
//                        .getContentResolver()
//                        .query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
//                                null,
//                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID
//                                        + " = " + contactId,
//                                null, null);
//                if (emails == null) {
//                    continue;
//                }
//                if (emails.moveToFirst()) {
//                    do {
//                        // 遍历所有的电话号码
//                        String emailType = emails
//                                .getString(emails
//                                        .getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));
//                        String emailValue = emails
//                                .getString(emails
//                                        .getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
//                        contactItem.emails
//                                .add(new ContactItem.MPair(
//                                        emailType, emailValue));
//                    } while (emails.moveToNext());
//                }
//                if (!emails.isClosed()) {
//                    emails.close();
//                }
//                contacts.add(contactItem);
//            } while (cursor.moveToNext());
//            // TODO 上传通讯录到企业服务器
//            uploadContact(contacts);
//        }
//
//        if ((cursor != null) && (!cursor.isClosed())) {
//            cursor.close();
//        }
//    }
}

package cn.dacas.emmclient.ui.activity.mainframe;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.io.File;
import java.io.IOException;

import cn.dacas.emmclient.R;
import cn.dacas.emmclient.core.EmmClientApplication;
import cn.dacas.emmclient.model.UserModel;
import cn.dacas.emmclient.ui.activity.base.BaseSlidingFragmentActivity;
import cn.dacas.emmclient.ui.qdlayout.RoundImageView;
import cn.dacas.emmclient.util.BitMapUtil;
import cn.dacas.emmclient.util.FileEngine;
import cn.dacas.emmclient.util.GlobalConsts;
import cn.dacas.emmclient.util.QDLog;
import cn.dacas.emmclient.util.QdCamera;
import cn.dacas.emmclient.webservice.QdWebService;

/**
 * @author Wang
 */
public class MyInformationActivity extends BaseSlidingFragmentActivity {

    private static final String TAG = "MyInformationActivity";


    private EditText mUserNameView,mEmailView,mPhoneView;
    private TextView mUserAccountView;
    private TextView mUserGroupView;
    private TextView mRolesView;
    private TextView mDutyView;

    private RoundImageView mMyPhotoImageView;

    private Button mTakePicButton;
    private Button mChoicePicButton;

    private Button mCancelButton;

    private Dialog mPopupMenuDialog;

    int takeOrOpenPic = 0;

    private String headPicPath;

    @Override
    protected HearderView_Style setHeaderViewStyle() {
        return HearderView_Style.Image_Text_Text;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_information, "");

        takeOrOpenPic = 0;
        init();
    }

    public void onResume() {
        super.onResume();
        setDataForUi();

    }

    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    ////////////////自定义函数////////////
    private void initMyView() {
//        mLeftHeaderView.setTextVisibile(false);
//        mLeftHeaderView.setImageVisibile(true);
        mLeftHeaderView.setImageView(R.mipmap.msp_titlebar_leftarrow_icon);

        mMiddleHeaderView.setText(mContext.getString(R.string.account_information));
//        mMiddleHeaderView.setTextVisibile(true);
//        mMiddleHeaderView.setImageVisibile(false);

        mRightHeaderView.setText(mContext.getString(R.string.completed));
//        mRightHeaderView.setTextVisibile(true);
//        mRightHeaderView.setImageVisibile(false);


        mMyPhotoImageView = (RoundImageView)findViewById(R.id.imageview_myphoto);
        mMyPhotoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });



        mUserNameView = (EditText)findViewById(R.id.edit_myname_value);
        mUserAccountView = (TextView)findViewById(R.id.imageview_account_value);
        mUserGroupView = (TextView)findViewById(R.id.imageview_group_value);
        mEmailView = (EditText)findViewById(R.id.edit_myemail_value);
        mPhoneView = (EditText)findViewById(R.id.edit_phonenumber_value);
        mRolesView = (TextView)findViewById(R.id.imageview_role_value);
        mDutyView = (TextView)findViewById(R.id.imageview_duty_account_value);

        setOnClickRight("完成", new OnRightListener() {
            @Override
            public void onClick() {
                if (mUserNameView.getText().toString().equals(EmmClientApplication.mUserModel.getName()) &&
                        mEmailView.getText().toString().equals(EmmClientApplication.mUserModel.getEmail()) &&
                        mPhoneView.getText().toString().equals(EmmClientApplication.mUserModel.getTelephone_number())) {
                    MyInformationActivity.this.finish();
                    return;
                }
                QdWebService.submitUserInformation(mUserNameView.getText().toString(), mEmailView.getText().toString(), mPhoneView.getText().toString(),
                        new Response.Listener<UserModel>() {
                            public void onResponse(final UserModel userInfo) {
                                try {
                                    EmmClientApplication.mUserModel =userInfo;
                                    Toast.makeText(mContext,"同步成功",Toast.LENGTH_SHORT).show();
                                    MyInformationActivity.this.finish();
                                }
                                catch (Exception e) {
                                    e.printStackTrace();
                                    Toast.makeText(mContext,"同步失败",Toast.LENGTH_SHORT).show();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(mContext,"同步失败",Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

    }


    private void init() {
        initMyView();
        // initMyData();

        //来自slidemenuActivity，必须调用，调用后，会出现左边的菜单。同时，调用setTouchModeAbove，这样，在滑动的时候，
        //就不会显示左侧的menu了。
//        setBehindContentView(R.layout.left_menu_frame);
//        getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);

    }

    private void setDataForUi() {
        QDLog.i(TAG, "setDataForUi 0000begin===========" + takeOrOpenPic);
        if (takeOrOpenPic > 0) {
            String pic = checkCameraHeadPic();
            if (!TextUtils.isEmpty(pic)) {
                SaveHeadPicThread();
            } else {
                QDLog.i(TAG, "setDataForUi 00===========" + pic);
//                this.takeOrOpenPic2Zero();
            }
        }else {
            // set image
            String headphotoName = QdCamera.GetHeadFullPathName(mContext);
            if (!TextUtils.isEmpty(headphotoName)) {
                File f = new File(headphotoName);
                if (f != null && f.exists())
                    setImageView(headphotoName);
            }
            this.takeOrOpenPic2Zero();
        }

        if (EmmClientApplication.mUserModel!=null) {
            mUserNameView.setText(EmmClientApplication.mUserModel.getName());
            mUserAccountView.setText(EmmClientApplication.mUserModel.getUsername());
            mUserGroupView.setText(EmmClientApplication.mUserModel.getType());
            mEmailView.setText(EmmClientApplication.mUserModel.getEmail());
            mPhoneView.setText(EmmClientApplication.mUserModel.getTelephone_number());
            mRolesView.setText(EmmClientApplication.mUserModel.getRole_name());
            mDutyView.setText(EmmClientApplication.mCheckAccount.getCurrentName().equals(EmmClientApplication.mActivateDevice.getBinderName())?"是":"否");
        }
    }

    /**
     *
     */
    private String checkCameraHeadPic() {
        headPicPath = QdCamera.headPicPath;
        QdCamera.headPicPath = "";
        return headPicPath;
    }

    /**
     * 底部菜单的实现
     */

    private void showDialog() {
        View view = getLayoutInflater().inflate(R.layout.photo_choose_dialog, null);
        initBottomMenu(view);
        mPopupMenuDialog = new Dialog(this, R.style.transparentFrameWindowStyle);
        mPopupMenuDialog.setContentView(view, new LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.WRAP_CONTENT));
        Window window = mPopupMenuDialog.getWindow();
        // 设置显示动画
        window.setWindowAnimations(R.style.main_menu_animstyle);
        WindowManager.LayoutParams wl = window.getAttributes();
        wl.x = 0;
        wl.y = getWindowManager().getDefaultDisplay().getHeight();
        // 以下这两句是为了保证按钮可以水平满屏
        wl.width = ViewGroup.LayoutParams.MATCH_PARENT;
        wl.height = ViewGroup.LayoutParams.WRAP_CONTENT;

        // 设置显示位置
        mPopupMenuDialog.onWindowAttributesChanged(wl);
        // 设置点击外围解散
        mPopupMenuDialog.setCanceledOnTouchOutside(true);
        mPopupMenuDialog.show();
    }


    private void  initBottomMenu(View v) {
        mTakePicButton = (Button) v.findViewById(R.id.button_bottom_menu_takePic);
        mChoicePicButton = (Button) v.findViewById(R.id.button_bottom_menu_choicePic);
        mCancelButton = (Button) v.findViewById(R.id.button_bottom_menu_cancel);

        mTakePicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mPopupMenuDialog != null && mPopupMenuDialog.isShowing()) {
                    mPopupMenuDialog.dismiss();
                }

                takeOrOpenPic = 1;

//                Intent intent = new Intent();
//                intent.setClass(mContext, MyHeadTakePic.class);
//                Bundle bundle = new Bundle();
//                bundle.putInt(GlobalConsts.Msg_Source,
//                        GlobalConsts.My_MyHead_takePic);
//                intent.putExtras(bundle);
//                startActivity(intent);

                intentFromBefor = new Intent();
                bundleFromBefor = new Bundle();
//                intentFromBefor.setClass(mContext, MyHeadTakePic.class);
                bundleFromBefor.putInt(GlobalConsts.Msg_Source,
                        GlobalConsts.My_MyHead_takePic);
                intentFromBefor.putExtras(bundleFromBefor);
                Msg_Src = bundleFromBefor.getInt(GlobalConsts.Msg_Source);
                openCamera();


            }
        });

        mChoicePicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mPopupMenuDialog != null && mPopupMenuDialog.isShowing()) {
                    mPopupMenuDialog.dismiss();
                }

                takeOrOpenPic = 2;

//                Intent intent1 = new Intent();
//                Bundle bundle1 = new Bundle();
//                intent1.setClass(mContext, MyHeadTakePic.class);
//                bundle1.putInt("src", 999);
//                bundle1.putInt(GlobalConsts.Msg_Source,
//                        GlobalConsts.My_MyHead_choicePic);
//                intent1.putExtras(bundle1);
//                startActivity(intent1);

                intentFromBefor = new Intent();
                bundleFromBefor = new Bundle();
//                intentFromBefor.setClass(mContext, MyHeadTakePic.class);
                bundleFromBefor.putInt("src", 999);
                bundleFromBefor.putInt(GlobalConsts.Msg_Source,
                        GlobalConsts.My_MyHead_choicePic);
                intentFromBefor.putExtras(bundleFromBefor);
                Msg_Src = bundleFromBefor.getInt(GlobalConsts.Msg_Source);
                openCamera();



            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mPopupMenuDialog != null && mPopupMenuDialog.isShowing()) {
                    mPopupMenuDialog.dismiss();
                }
            }
        });
    }


    //////////////save pic thread//////////


    private void SaveHeadPicThread() {
        new Thread(new Runnable() {

            @Override
            public void run() {

                // 重要
                String res = saveHeadPic();

                Message toMain = new Message();
                Bundle bundle = new Bundle();
                if (res != null) { //save success
                    toMain.what = 1;
                    bundle.putString("head_path", res);
                }

                else {
                    toMain.what = 2; // 失败
                    bundle.putString("head_path", res);
                }
                toMain.setData(bundle);
                mMyHandler.sendMessage(toMain);
            }
        }).start();
    }

    /**
     *
     * @return 保存之后的路径 ：mContext.getFilesDir() + File.separator +
     *         ConstsCommon.User_HeadPhoto_Path + File.separator + fileName
     */
    private String saveHeadPic() {

        String oldHeadPath = QdCamera.GetHeadFullPathName(mContext);

        File oldFile = new File(oldHeadPath);
        if (oldFile == null) {
            return "";
        }

        if (!oldFile.exists()) {
            try {
                oldFile = FileEngine.createFile(QdCamera.getHeadPhotoPath(mContext) , QdCamera.getHeadPhotoName()); //GlobalConsts.User_HeadPhoto_Name
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

//        File destFile = new File(oldHeadPath);

        try {
            if (FileEngine.moveFileTo(new File(headPicPath), oldFile)) {
                return oldFile.getAbsolutePath();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return null;

    }

    private void setImageView(String realPath) {
        QDLog.i(TAG, "setImageView ===================path======" + realPath);
        BitMapUtil.setImageSrc(mMyPhotoImageView, realPath);
    }

    private void takeOrOpenPic2Zero() {
//        mRequestCode = 0;
        takeOrOpenPic = 0;
    }

    private void UpdateImage(String path) {
        setImageView(path);
    }

    // ////////////save head handler ////
    MyHandler mMyHandler = new MyHandler();

    class MyHandler extends Handler {

        public void handleMessage(Message msg) {

            QDLog.i(TAG, "handleMessage===========  " + msg.what);
            if (msg.what == 1) {
                Bundle bundle = msg.getData();
                String head_path = (String) bundle.get("head_path");
                if (head_path != null) {
                    UpdateImage(head_path);
                }
                Toast.makeText(mContext, "设置头像成功!", Toast.LENGTH_SHORT).show();
            } else if (msg.what == 2) {
                Toast.makeText(mContext, "设置头像失败!", Toast.LENGTH_SHORT).show();
            }
            // 设置完成img后,这2个值要归0
            takeOrOpenPic2Zero();

        }
    }


    ///////////////////////拍照or选取照片,来自MyHeadTakePic/////////
    private QdCamera mQdCamera = null;

    private Intent intentFromBefor;
    private Bundle bundleFromBefor;
    private int Msg_Src;

    private String newPicFileFullName = "";  //choice时用它。

    private void openCamera() {
        if (null == mQdCamera ) {
            mQdCamera = new QdCamera((Activity) mContext, 1);
        }

        if (Msg_Src == GlobalConsts.My_MyHead_takePic) {
            mQdCamera.takePicture();
        } else if (Msg_Src == GlobalConsts.My_MyHead_choicePic) {
            mQdCamera.openAlbum();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        QDLog.i(TAG, "onActivityResult=====requestCode=======" + requestCode);
        QDLog.i(TAG, "onActivityResult=====resultCode=======" + resultCode);
        QDLog.i(TAG, "onActivityResult=====data=======" + data);

        if (requestCode == QdCamera.Take_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == ((Activity) mContext).RESULT_OK) {
                Log.e(TAG, "获取图片成功，path=" + mQdCamera.oldPicFileFullName);
                //保存
                //File file1 = new File(QdCamera.oldPicFileFullName);
                //File file2 = new File("new_" + QdCamera.oldPicFileFullName );
                mQdCamera.beginCrop(Uri.fromFile(new File(QdCamera.oldPicFileFullName)), Uri.fromFile(new File(QdCamera.oldPicFileFullName)));

            } else if (resultCode == ((Activity) mContext).RESULT_CANCELED) {
                // 用户取消了图像捕获
//                finish();
            } else {
                // 图像捕获失败，提示用户
                Log.i(TAG, "拍照失败， take fail");
                Toast.makeText(mContext, "拍照失败!", Toast.LENGTH_SHORT).show();
//                finish();
            }
        }

        //choice
        else if (requestCode == QdCamera.Choice_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == ((Activity) mContext).RESULT_OK) {
                Uri uri = data.getData();
                if (uri != null) {
                    String path = getRealPathFromURI(uri);
                    if (path == null) {
                        Toast.makeText(mContext,"获取照片失败",Toast.LENGTH_SHORT).show();
//                        finish();
                    }
                    else {
                        File oldfileName = new File(path);
                        if (oldfileName.exists()) {
                            newPicFileFullName = "head_" + oldfileName.getName();

                            try {
                                File newFile = FileEngine.createFile(oldfileName.getParent(), newPicFileFullName);
                                if (newFile != null && newFile.exists()) {
                                    newPicFileFullName = newFile.getAbsolutePath();
                                    mQdCamera.beginCrop(uri, Uri.fromFile(newFile));
                                }
                            } catch (IOException e) {
                                Toast.makeText(mContext,"获取照片失败",Toast.LENGTH_SHORT).show();
//                                finish();
                            }
                        }
                    }
                } else {
                    Toast.makeText(mContext,"获取照片失败",Toast.LENGTH_SHORT).show();
//                    finish();
                }
            } else {
                //例如，按back键。
                Toast.makeText(mContext, "放弃拍照!", Toast.LENGTH_SHORT).show();
//                finish();
            }
        }

        //cut
        else if (requestCode == QdCamera.Cut_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (Msg_Src == GlobalConsts.My_MyHead_takePic) {
                    QDLog.i(TAG, "mQdCamera.oldPicFileFullName 0:" + mQdCamera.oldPicFileFullName);
                    QdCamera.headPicPath = mQdCamera.oldPicFileFullName;
//                    finish();
                } else if (Msg_Src == GlobalConsts.My_MyHead_choicePic) {
                    QDLog.i(TAG, "mQdCamera.oldPicFileFullName 1:" + mQdCamera.oldPicFileFullName);
                    QDLog.i(TAG, "mQdCamera.newPicFileFullName 1:" + this.newPicFileFullName);
                    QdCamera.headPicPath = newPicFileFullName;
                    bundleFromBefor.putInt("src", resultCode);
                    bundleFromBefor.putInt("msg", requestCode);
                    bundleFromBefor.putString("pic_name", mQdCamera.oldPicFileFullName);
                    intentFromBefor.putExtras(bundleFromBefor);
//                    finish();
                }

            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.e(TAG, "放弃拍照！");
                Toast.makeText(mContext, "放弃拍照！", Toast.LENGTH_SHORT).show();
//                finish();
            } else {
                Log.e(TAG, "拍照失败， cut fail");
                Toast.makeText(mContext, "拍照失败!", Toast.LENGTH_SHORT).show();
//                finish();
            }
        }

    }

    /**
     * This method is used to get real path of file from from uri<br/>
     * http://stackoverflow.com/questions/11591825/how-to-get-image-path-just-
     * captured-from-camera
     *
     * @param contentUri
     * @return String
     */
    public String getRealPathFromURI(Uri contentUri) {
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            // Do not call Cursor.close() on a cursor obtained using this
            // method,
            // because the activity will do that for you at the appropriate time
            Cursor cursor = ((Activity) mContext).managedQuery(contentUri,
                    proj, null, null, null);
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } catch (Exception e) {
            return contentUri.getPath();
        }
    }

}

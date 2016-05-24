package cn.qdsc.msp.ui.activity.mainframe;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import cn.qdsc.msp.R;
import cn.qdsc.msp.ui.activity.base.BaseSlidingFragmentActivity;
import cn.qdsc.msp.util.FileEngine;
import cn.qdsc.msp.util.GlobalConsts;
import cn.qdsc.msp.util.QDLog;
import cn.qdsc.msp.util.QdCamera;


public class MyHeadTakePic extends BaseSlidingFragmentActivity {

    private static String TAG = "MyHeadTakePic";

    QdCamera mQdCamera;

    //1. 拍照； 2：选取
    int takeOrOpenPic = 0;
    //    
    Intent intentFromBefor;
    Bundle bundleFromBefor;
    int Msg_Src;

    String newPicFileFullName = "";  //choice时用它。


    int i = 0;

    @Override
    protected HearderView_Style setHeaderViewSyle() {
        return HearderView_Style.Image_Text_Image;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QDLog.i(TAG, " onCreate=======================" + i++);
        setContentView(R.layout.activity_myhead_takepic, "");

        intentFromBefor = this.getIntent();
        bundleFromBefor = intentFromBefor.getExtras();
        Msg_Src = bundleFromBefor.getInt(GlobalConsts.Msg_Source);
        newPicFileFullName = "";

        //下面这两句必须调用
//        setBehindContentView(R.layout.left_menu_frame);
//        getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);


        mQdCamera = new QdCamera((Activity) mContext, 1);
        if (Msg_Src == GlobalConsts.My_MyHead_takePic) {
            mQdCamera.takePicture();
        } else if (Msg_Src == GlobalConsts.My_MyHead_choicePic) {
            mQdCamera.openAlbum();
        }
    }


    @Override
    protected void onResume() {

        QDLog.i(TAG, " onResume=======================" + i);
        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        super.onResume();

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
                finish();
            } else {
                // 图像捕获失败，提示用户
                Log.i(TAG, "拍照失败， take fail");
                Toast.makeText(mContext, "拍照失败!", Toast.LENGTH_SHORT).show();
                finish();
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
                        finish();
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
                                finish();
                            }
                        }
                    }
                } else {
                    Toast.makeText(mContext,"获取照片失败",Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                //例如，按back键。
                Toast.makeText(mContext, "放弃拍照!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }

        //cut
        else if (requestCode == QdCamera.Cut_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (Msg_Src == GlobalConsts.My_MyHead_takePic) {
                    QDLog.i(TAG, "mQdCamera.oldPicFileFullName 0:" + mQdCamera.oldPicFileFullName);
                    ;
                    QdCamera.headPicPath = mQdCamera.oldPicFileFullName;
                    finish();
                } else if (Msg_Src == GlobalConsts.My_MyHead_choicePic) {
                    QDLog.i(TAG, "mQdCamera.oldPicFileFullName 1:" + mQdCamera.oldPicFileFullName);
                    ;
                    QDLog.i(TAG, "mQdCamera.oldPicFileFullName 1:" + this.newPicFileFullName);
                    QdCamera.headPicPath = newPicFileFullName;
                    bundleFromBefor.putInt("src", resultCode);
                    bundleFromBefor.putInt("msg", requestCode);
                    bundleFromBefor.putString("pic_name", mQdCamera.oldPicFileFullName);
                    intentFromBefor.putExtras(bundleFromBefor);
                    finish();
                }

            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.e(TAG, "放弃拍照！");
                Toast.makeText(mContext, "放弃拍照！", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Log.e(TAG, "拍照失败， cut fail");
                Toast.makeText(mContext, "拍照失败!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }

    }


    /**
     * 删除headpath目录中不是headFileFullName的其它文件
     *
     * @param headFileFullName
     */
    private void deleteFile(String headPath, String headFileFullName) {
        File f = new File(headPath);
        if (f == null || !f.exists()) {
            return;
        }

        File files[] = f.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile()) {
                    if (!headFileFullName.contains(files[i].getName())) {
                        FileEngine.deleteFile(files[i].getAbsolutePath());
                    }
                }
            }
        }

    }

//    //delete files
//    private void deleteFiles() {
//
//        //take
//        int index = mQdCamera.oldPicFileFullName.lastIndexOf("/");
//        String name = Application.mLoginUser.headphoto
//                .substring(index + 1);
//
//        String headPathStr = mContext.getFilesDir() + File.separator +IntentConts.User_HeadPhoto_Path;
//        String destFile = headPathStr + File.separator + name;
//        boolean b;
//
//        try {
//            b = FileEngine.copyFileTo(mQdCamera.oldPicFileFullName, destFile);
//            if (!b) {
//                return;
//            }
//        } catch (IOException e1) {
//            e1.printStackTrace();
//        }
//
//        deleteFile(headPathStr, destFile);
//    }

    /////////////


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

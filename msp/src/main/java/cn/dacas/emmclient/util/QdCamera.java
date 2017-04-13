package cn.dacas.emmclient.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;

import cn.dacas.emmclient.core.EmmClientApplication;


public class QdCamera {

    private static String TAG = "QdCamera";

    private Activity mActivity;
    private int from;
    public static String headPicPath = "";  //是在MyHeadTakePic中进行设置的。

    /**
     *
     * @param activity
     * @param from: activity的来源：
     * 1 ： 我的中的头像点击，为了保存头像
     * 2: 其他照片。
     */

    public QdCamera(Activity activity, int from) {
        mActivity = activity;
        this.from = from;
        headPicPath = "";
        oldPicFileFullName = "";
    }

    //////////////拍照////////////////////////////////////
    public static final int Take_IMAGE_ACTIVITY_REQUEST_CODE = 100;  //take pic
    public static final int Choice_IMAGE_ACTIVITY_REQUEST_CODE = 200; //choice pic
    public static final int Cut_IMAGE_ACTIVITY_REQUEST_CODE = 300; //裁剪后的消息
    public int takeOrOpenPic = 0;

    public static String oldPicFileFullName = "";

    // 拍照
    public void takePicture() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {

            File outDir = Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            if (!outDir.exists()) {
                outDir.mkdirs();
            }
            QDLog.i(TAG, "outDir=================================== " + outDir);

            String date = new SimpleDateFormat("yyyyMMddHHmmss").format(new java.util.Date());
            File outFile = new File(outDir, date + ".jpg");
            oldPicFileFullName = outFile.getAbsolutePath();
            //oldPicFileFullName = outDir + File.separator + date + ".jpg";
            QDLog.i(TAG, "outDir====outFile======= " + oldPicFileFullName);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(outFile));
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
            mActivity.startActivityForResult(intent, Take_IMAGE_ACTIVITY_REQUEST_CODE);
        } else {
            Toast.makeText(mActivity, "无SDcard！", Toast.LENGTH_SHORT).show();
            QDLog.e(TAG, "请确认已经插入SD卡");
        }
    }

    // 打开本地相册
    public void openAlbum() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
//        intent.setAction(Intent.ACTION_GET_CONTENT);
        mActivity.startActivityForResult(intent, Choice_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    /**
     * 裁剪图片方法实现
     *
     * @param uri
     */
    public void beginCrop(Uri uri,Uri cutImgUri) {
        QDLog.i(TAG, "uri" + uri);
        QDLog.i(TAG, "cutImgUri" + cutImgUri);

        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 4);
        intent.putExtra("aspectY", 5);
        // outputX outputY 是裁剪图片宽高，注意如果return-data=true情况下,其实得到的是缩略图，并不是真实拍摄的图片大小，
        // 而原因是拍照的图片太大，所以这个宽高当你设置很大的时候发现并不起作用，就是因为返回的原图是缩略图，但是作为头像还是够清晰了

        // 图片格式
        intent.putExtra("outputFormat", "JPEG");
        if (from == 1 ) {
            intent.putExtra("outputX", 150);
            intent.putExtra("outputY", 150);
        }else {
            // 裁剪后输出图片的尺寸大小
            intent.putExtra("outputX", 400);
            intent.putExtra("outputY", 500);
            // 取消人脸识别
            intent.putExtra("noFaceDetection", true);
        }

        //返回图片数据
        intent.putExtra("return-data", false); // true:不返回uri，false：返回uri
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cutImgUri);//写入截取的图片
        mActivity.startActivityForResult(intent, Cut_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    /////////下面这些与拍照本身无关，只是与拍照后在UI显示时用到路径有关//////

    public static String GetLastString(String srcStr, String separatorStr) {
        int indext = srcStr.lastIndexOf(separatorStr);
        String s = srcStr.substring(indext+1);
        return s;
    }

    public static String GetHeadFullPathName(Context context) {

        String headphotoName = getHeadPhotoPath(context)
                + File.separator + getHeadPhotoName();
        return headphotoName;
    }

    //for path
    public static String getHeadPhotoPath(Context context) {
        return context.getFilesDir() + File.separator + GlobalConsts.User_HeadPhoto_Path;
    }

    //for head Name
    public static String getHeadPhotoName() {

        return EmmClientApplication.mCheckAccount.getCurrentAccount()
                + "_" + GlobalConsts.User_HeadPhoto_Name;
    }


}

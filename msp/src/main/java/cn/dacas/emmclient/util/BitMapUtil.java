package cn.dacas.emmclient.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import cn.dacas.emmclient.ui.qdlayout.RoundImageView;

/**
 * Created by lenovo on 2015/11/25.
 */
public class BitMapUtil {
    private static final String TAG = "BitMapUtil";


    /**
     * 获取压缩图片。
     * @param context
     * @param resId
     * @return
     */
    //R.drawable.csr_qd
    public static Drawable getDrawableByZoom(Context context,int resId) {
//		String src = "mnt/sdcard/DSC.jpg";

        // 图片解析的配置
        BitmapFactory.Options options = new BitmapFactory.Options();
        // 不去真正解析图片，只是获取图片的宽高
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(context.getResources(), resId, options);
        int imageWidth = options.outWidth;
        int imageHeight = options.outHeight;
        QDLog.i(TAG, "图片宽 :" + imageWidth);
        QDLog.i(TAG, "图片高 :" + imageHeight);

        int scaleX = imageWidth / PhoneInfoExtractor.getWindowWidth(context);
        int scaleY = imageHeight / PhoneInfoExtractor.getWindowHeight(context);
        int scale = 1;
        if (scaleX >= scaleY && scaleX >= 1) {
            // 水平方向的缩放比例比竖直方向的缩放比例大，同时图片的宽要比手机屏幕要大,就按水平方向比例缩放
            QDLog.i(TAG, "按宽比例缩放 :");
            scale = scaleX;
        } else if (scaleY >= scaleX && scaleY >= 1) {
            // 竖直方向的缩放比例比水平方向的缩放比例大，同时图片的高要比手机屏幕要大，就按竖直方向比例缩放
            QDLog.i(TAG, "按高比例缩放 :");
            scale = scaleY;
        }
        QDLog.i(TAG, "缩放比例 :" + scale);
        // 真正解析图片
        options.inJustDecodeBounds = false;
        // 设置采样率
        options.inSampleSize = scale;
        try {
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resId, options);
            return bitmap2Drawable(bitmap);
        }catch (Exception e) {
            return null;
        }

    }

    //从资源中获取的Drawable --> Bitmap,不需要压缩
    public static Bitmap fetchBitmapFromRes(Context context, int resId) {
        Resources res = context.getResources();
        Bitmap bmp = BitmapFactory.decodeResource(res, resId);
        return bmp;
    }

    //Bitmap --> Drawable
    public static Drawable bitmap2Drawable(Bitmap bitmap) {
        return new BitmapDrawable(bitmap);
    }

    //Bitmap --> byte[]
    byte[] Bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    //byte[] --> Bitmap
    Bitmap Bytes2Bimap(byte[] b) {
        if (b.length != 0) {
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        } else {
            return null;
        }
    }

    public void write2File(Bitmap bitmap) {
        File file=new File("/sdcard/feng.png");
        try {
            FileOutputStream out=new FileOutputStream(file);
            if(bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)){
                out.flush();
                out.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    ///////////////////为拍照进行的/////////

    private static int   IMAGE_MAX_WIDTH  = 480;
    private static int   IMAGE_MAX_HEIGHT = 960;

    ////缩小
    /**
     * set image src
     *
     * @param imageView
     * @param imagePath
     */
    public static void setImageSrc(ImageView imageView, String imagePath) {
        BitmapFactory.Options option = new BitmapFactory.Options();
        option.inSampleSize = getImageScale(imagePath);
        Bitmap bm = BitmapFactory.decodeFile(imagePath, option);
        if (bm == null) {
            return;
        }
        imageView.setImageBitmap(bm);
    }

    /**
     * 将图片以圆形方式显示
     * @param context
     * @param imageView
     * @param imagePath
     */
    public static void setRoundImageSrc(Context context,ImageView imageView, String imagePath) {
        BitmapFactory.Options option = new BitmapFactory.Options();
        option.inSampleSize = getImageScale(imagePath);
        Bitmap bm = BitmapFactory.decodeFile(imagePath, option);
        if (bm == null) {
            //imageView.setImageBitmap(bitmap);
            return;
        }

        RoundImageView roundImageView = new RoundImageView(context);
        if (roundImageView == null) {
            return;
        }
        Bitmap bitmap = roundImageView.getCroppedRoundBitmap(bm, 60);
        if (bitmap == null) {
            return;
        }
        imageView.setImageBitmap(bitmap);
    }

    /**
     * scale image to fixed height and weight
     *
     * @param imagePath
     * @return
     */
    public static int getImageScale(String imagePath) {
        BitmapFactory.Options option = new BitmapFactory.Options();
        // set inJustDecodeBounds to true, allowing the caller to query the bitmap info without having to allocate the
        // memory for its pixels.
        option.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, option);

        int scale = 1;
        while (option.outWidth / scale >= IMAGE_MAX_WIDTH || option.outHeight / scale >= IMAGE_MAX_HEIGHT) {
            scale *= 2;
        }
        return scale;
    }

    public static int dp2px(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
    }
}


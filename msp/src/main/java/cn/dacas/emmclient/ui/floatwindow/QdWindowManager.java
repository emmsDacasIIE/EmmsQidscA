package cn.dacas.emmclient.ui.floatwindow;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by lenovo on 2015-12-28.
 */
public class QdWindowManager {
    private static FloatView floatView;
    private static WindowManager mWindowManager = null;
    private static WindowManager.LayoutParams windowManagerParams = null;

    public static WindowManager getWindowManager(Context context) {
        if (mWindowManager == null) {
            mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        }
        return mWindowManager;
    }

    public static WindowManager.LayoutParams getWindowManagerParams() {
        if (windowManagerParams == null) {
            windowManagerParams = new WindowManager.LayoutParams();
            windowManagerParams.type = WindowManager.LayoutParams.TYPE_PHONE; // 设置window type
            windowManagerParams.format = PixelFormat.RGBA_8888; // 设置图片格式，效果为背景透明
            // 设置Window flag
            windowManagerParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                /*
                 * 注意，flag的值可以为：
                 * LayoutParams.FLAG_NOT_TOUCH_MODAL 不影响后面的事件
                 * LayoutParams.FLAG_NOT_FOCUSABLE  不可聚焦
                 * LayoutParams.FLAG_NOT_TOUCHABLE 不可触摸
                 */
            // 调整悬浮窗口至左上角，便于调整坐标
            windowManagerParams.gravity = Gravity.LEFT | Gravity.TOP;
            // 以屏幕左上角为原点，设置x、y初始值
            windowManagerParams.x = 0;
            windowManagerParams.y = 0;
            // 设置悬浮窗口长宽数据
            windowManagerParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            windowManagerParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        }
        return windowManagerParams;
    }

    public static void createfloatWindow(final Context context) {
        if (floatView == null) {
            floatView=new FloatView(context);

            floatView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PackageManager pm = context.getPackageManager();
                    Intent intent = new Intent();
                    try {
                        intent = pm.getLaunchIntentForPackage(context.getPackageName());
                        context.startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });


        }
        // 获取WindowManager
        WindowManager windowManager = getWindowManager(context);
        // 显示myFloatView图像
        windowManager.addView(floatView, getWindowManagerParams());
    }

    /**
     * 将小悬浮窗从屏幕上移除。
     *
     * @param context 必须为应用程序的Context.
     */
    public static void removeFloatWindow(Context context) {
        if (floatView != null) {
            WindowManager windowManager = getWindowManager(context);
            windowManager.removeView(floatView);
            floatView = null;
        }
    }

    public static boolean isWindowShowing() {
        return floatView!=null;
    }
}

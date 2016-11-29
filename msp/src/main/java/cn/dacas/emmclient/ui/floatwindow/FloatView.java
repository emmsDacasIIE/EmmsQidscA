package cn.dacas.emmclient.ui.floatwindow;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import cn.dacas.emmclient.R;
import cn.dacas.emmclient.ui.activity.mainframe.NewMainActivity;
import cn.dacas.emmclient.util.QDLog;

/**
 * Created by lenovo on 2015-12-28.
 */
public class FloatView extends RelativeLayout {
    private float mTouchX;
    private float mTouchY;
    private float x;
    private float y;
    private float mStartX;
    private float mStartY;
    private ImageView v1, v2;
    private OnTouchListener tl;

    private WindowManager windowManager;
    // 此windowManagerParams变量为获取的全局变量，用以保存悬浮窗口的属性
    private WindowManager.LayoutParams windowManagerParams;
    private Handler mHandler;
    Runnable r = new Runnable() {
        @Override
        public void run() {
            if (v2.getVisibility()==View.INVISIBLE)
                v1.setImageResource(R.mipmap.float_window_circle_transparent);
        }
    };

    private void startFadeOut() {
        if (mHandler != null) {
            mHandler.removeCallbacks(r);
            mHandler.postDelayed(r, 5000);
        }
    }

    public FloatView(final Context context) {
        super(context);
        windowManager = QdWindowManager.getWindowManager(context);
        windowManagerParams = QdWindowManager.getWindowManagerParams();
        mHandler = new Handler();
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.view_float_window, this);
        this.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        v1 = (ImageView) findViewById(R.id.float_window_circle);
        tl = new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v1.setImageResource(R.mipmap.float_window_circle_normal);
                startFadeOut();
                //获取到状态栏的高度
                Rect frame = new Rect();
                getWindowVisibleDisplayFrame(frame);
                int statusBarHeight = frame.top;
                System.out.println("statusBarHeight:" + statusBarHeight);
                // 获取相对屏幕的坐标，即以屏幕左上角为原点
                x = event.getRawX();
                y = event.getRawY() - statusBarHeight; // statusBarHeight是系统状态栏的高度
                QDLog.i("tag", "currX" + x + "====currY" + y);
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: // 捕获手指触摸按下动作
                        // 获取相对View的坐标，即以此View左上角为原点
                        mTouchX = event.getX();
                        mTouchY = event.getY();
                        mStartX = x;
                        mStartY = y;
                        QDLog.i("tag", "startX" + mTouchX + "====startY"
                                + mTouchY);
                        break;

                    case MotionEvent.ACTION_MOVE: // 捕获手指触摸移动动作
                        updateViewPosition();
                        break;

                    case MotionEvent.ACTION_UP: // 捕获手指触摸离开动作
//                        updateViewPosition();
//                        mTouchX = mTouchY = 0;
//                        if ((x - mStartX) < 5 && (y - mStartY) < 5) {
//                        }
                        break;
                }
                return false;
            }
        };
        v1.setOnTouchListener(tl);
        v1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v2.getVisibility() == View.VISIBLE)
                    v2.setVisibility(View.INVISIBLE);
                else if (v2.getVisibility() == View.INVISIBLE)
                    v2.setVisibility(View.VISIBLE);
            }
        });
        v2 = (ImageView) findViewById(R.id.float_window_items);
        v2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    intent.setClass(context, NewMainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    context.startActivity(intent);
            }
        });
        startFadeOut();
    }


    private void updateViewPosition() {
        // 更新浮动窗口位置参数
        windowManagerParams.x = (int) (x - mTouchX);
        windowManagerParams.y = (int) (y - mTouchY);
        windowManager.updateViewLayout(this, windowManagerParams); // 刷新显示
    }

}

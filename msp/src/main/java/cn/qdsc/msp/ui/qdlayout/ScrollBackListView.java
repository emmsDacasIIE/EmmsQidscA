package cn.qdsc.msp.ui.qdlayout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

public class ScrollBackListView extends ListView {


    private static final int MAX_SCROLL = 80;
    private static final float SCROLL_RATIO = 1f;// 阻尼系数

    public ScrollBackListView(Context context) {
        super(context);
    }

    public ScrollBackListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScrollBackListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            invalidate();
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX,
        int scrollY, int scrollRangeX, int scrollRangeY,
        int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        int newDeltaY = deltaY;
        int delta = (int) (deltaY * SCROLL_RATIO);
        if (delta != 0)
            newDeltaY = delta;
        return super.overScrollBy(deltaX, newDeltaY, scrollX, scrollY,
                scrollRangeX, scrollRangeY, maxOverScrollX, MAX_SCROLL,
                isTouchEvent);
    }


}

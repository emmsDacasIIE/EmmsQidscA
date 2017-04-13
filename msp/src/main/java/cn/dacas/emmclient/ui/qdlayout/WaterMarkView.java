package cn.dacas.emmclient.ui.qdlayout;

import android.content.Context;
import android.graphics.Canvas;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Base64;
import android.view.View;

import cn.dacas.emmclient.core.EmmClientApplication;

/**
 * TODO: document your custom view class.
 */
public class WaterMarkView extends View {
    private String name;
    private String waterMark;

    public WaterMarkView(Context context) {
        super(context);
        initWaterMark();
    }

    public WaterMarkView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initWaterMark();
    }

    public WaterMarkView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initWaterMark();
    }

    private void initWaterMark() {
        //String waterMark= MathUtils.genWaterMark(EmmClientApplication.getCurentAccount().getAccountName());
        name = EmmClientApplication.mCheckAccount.getCurrentName();
    }



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        QdWaterMarkPolicy waterMarkPolicy  = PolicyManager.getPolicyManager(EmmClientApplication.getContext()).getQdWaterMarkPolicy();
//        if (waterMarkPolicy!= null&&waterMarkPolicy.getDisplay()!=null){
//            name = waterMarkPolicy.getDisplay();
//        }else {
//            name = "机密文件，拷贝必究";
//        }
        waterMark = "机密文件，拷贝必究";
        int width=getWidth();
        int height=getHeight();

        TextPaint textPaint = new TextPaint();
        textPaint.setARGB(0x80, 0, 0, 0);
        textPaint.setTextSize(20.0F);
        textPaint.setAntiAlias(true);
        StaticLayout layout = new StaticLayout(name+"\r\n" + waterMark +"\r\n", textPaint,width,
                Layout.Alignment.ALIGN_NORMAL, 1.0F, 0.0F, true);
        float addWidth=36.0f,addHeight=24.0f;
        float[] x = new float[]{width / 4-addWidth , width * 3 / 4-addWidth, width / 4-addWidth,  width* 3 / 4-addWidth};
        float[] y = new float[]{height / 4-addHeight, height  / 4-addHeight, height*3 / 4-addHeight, height * 3 / 4-addHeight};
        for (int i = 0; i < 4; i++) {
            canvas.save();
            canvas.translate(x[i], y[i]);
            canvas.rotate(15);
            layout.draw(canvas);
            canvas.restore();
        }
    }

}

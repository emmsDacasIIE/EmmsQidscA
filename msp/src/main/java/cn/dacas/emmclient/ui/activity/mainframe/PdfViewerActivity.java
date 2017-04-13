package cn.dacas.emmclient.ui.activity.mainframe;

import android.app.Activity;
import android.os.Bundle;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;

import java.io.File;

import cn.dacas.emmclient.R;
import cn.dacas.emmclient.core.EmmClientApplication;
import cn.dacas.emmclient.ui.activity.base.BaseSlidingFragmentActivity;
import cn.dacas.emmclient.ui.qdlayout.WaterMarkView;

import static com.baidu.location.h.i.w;

/**
 * Created by Srx on 2017-3-3.
 * PdfView with waterMark
 */
public class PdfViewerActivity extends Activity { //BaseSlidingFragmentActivity
    private String display;
    private WaterMarkView wmView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_pdf_viewer, "安全文件");
        setContentView(R.layout.activity_pdf_viewer);
        //mLeftHeaderView.setImageView(R.mipmap.msp_titlebar_leftarrow_icon);
        PDFView pdfView=(PDFView)findViewById(R.id.pdfView);
        wmView = (WaterMarkView) findViewById(R.id.water_pdf);
        String fileFullName = getIntent().getStringExtra("fileFullName");
        String type=getIntent().getStringExtra("file_type");
        //QdWaterMarkPolicy waterMarkPolicy = PolicyManager.getPolicyManager(mContext).getQdWaterMarkPolicy();
        if("GENERAL".equals(type)){
            display = "";
            wmView.setVisibility(View.INVISIBLE);
        }else {
            String info="机密文件，拷贝必究";
//            if (waterMarkPolicy!=null&&waterMarkPolicy.getDisplay()!=null){
//                info =PolicyManager.getPolicyManager(mContext).getQdWaterMarkPolicy().getDisplay();
//            }

            //String waterMark= MathUtils.genWaterMark(EmmClientApplication.mCheckAccount.getCurrentName());
            String waterMark = EmmClientApplication.mCheckAccount.getCurrentName();
//            String user = Base64.encodeToString(waterMark.getBytes(), Base64.DEFAULT);
//            String u1=user.substring(0,10);
//            String u2=user.substring(10);
//            display=info+"\r\n"+u1+"\r\n"+u2;
            display=info+"\r\n"+waterMark;
        }
        File file=new File(fileFullName);
        try {
            pdfView.fromFile(file).load();
        } catch (Exception ex) {
            Toast.makeText(this,"文件不存在或已损坏",Toast.LENGTH_SHORT).show();
            finish();
        }
    }

//    @Override
//    protected HearderView_Style setHeaderViewStyle() {
//        return HearderView_Style.Image_Text_Null;
//    }
}



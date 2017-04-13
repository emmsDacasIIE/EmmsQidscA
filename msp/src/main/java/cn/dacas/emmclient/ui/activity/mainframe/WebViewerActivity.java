package cn.dacas.emmclient.ui.activity.mainframe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import cn.dacas.emmclient.R;
import cn.dacas.emmclient.ui.activity.base.BaseSlidingFragmentActivity;
import cn.dacas.emmclient.ui.qdlayout.WaterMarkView;


public class WebViewerActivity extends BaseSlidingFragmentActivity {
    private WaterMarkView waterMarkView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_txt_viewer, "安全文件");
        waterMarkView = (WaterMarkView) findViewById(R.id.water);
        Intent intent = getIntent();
        String type = intent.getStringExtra("file_type");
        if ("GENERAL".equals(type)){
            waterMarkView.setVisibility(View.GONE);
        }else{
            waterMarkView.setVisibility(View.VISIBLE);
        }
        mLeftHeaderView.setImageView(R.mipmap.msp_titlebar_leftarrow_icon);
        String fileFullName = getIntent().getStringExtra("fileFullName");
        WebView webView=(WebView)findViewById(R.id.qdWebView);
        webView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true); // javaScript可用
        settings.setDefaultTextEncodingName("utf-8");
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK); // 关闭webview中的缓存
        webView.setInitialScale(100);
        webView.loadUrl("file://"+fileFullName);
    }

    @Override
    protected HearderView_Style setHeaderViewStyle() {
        return HearderView_Style.Image_Text_Null;
    }
}

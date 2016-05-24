package cn.qdsc.msp.ui.activity.mainframe;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;

import cn.qdsc.msp.R;
import cn.qdsc.msp.core.EmmClientApplication;
import cn.qdsc.msp.core.mdm.PolicyManager;

/**
 *
 */
public class ComplianceActivity extends Activity {

    private Context mContext;
    protected static final int SCAN_LODING = 1;
    protected static final int FINSH_SCAN = 2;
    private ImageView imageScan,imageFinish,imageClose;
    private TextView textScore, textInfo,textView;
    private LinearLayout mMainLayout;

    private Handler handler = new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SCAN_LODING:
                    int scoreLoading=msg.arg1;
                    textScore.setText(String.valueOf(scoreLoading));
                    textScore.setTextColor(getColorByScore(scoreLoading));
                    break;
                case FINSH_SCAN:
                    int scoreOverall=msg.arg1;
                    textInfo.setText(getTextByScore(scoreOverall));
//                    textInfo.setTextColor(getColorByScore(scoreOverall));
                    textView.setVisibility(View.VISIBLE);
                    imageScan.clearAnimation();
                    imageFinish.setVisibility(View.VISIBLE);
                    break;
            }
        };
    };

    private  String getTextByScore(int score) {
        if (score==100) return getResources().getString(R.string.compliance_rank_1);
        else if (score>75 ) return getResources().getString(R.string.compliance_rank_2);
        else if (score>25) return getResources().getString(R.string.compliance_rank_3);
        else return getResources().getString(R.string.compliance_rank_4);
    }

    private  int getColorByScore(int score) {
        if (score==100) return getResources().getColor(R.color.compliance_rank_1);
        else if (score>75 ) return getResources().getColor(R.color.compliance_rank_2);
        else if (score>25) return getResources().getColor(R.color.compliance_rank_3);
        else return getResources().getColor(R.color.compliance_rank_4);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compliance);

        WindowManager.LayoutParams lp=getWindow().getAttributes();
        lp.dimAmount=0.5f;
        getWindow().setAttributes(lp);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        mContext=ComplianceActivity.this;
        textScore =(TextView)findViewById(R.id.text_compliance_score);
        textInfo =(TextView)findViewById(R.id.text_compliance_info);
        textView=(TextView)findViewById(R.id.text_compliance_view);
        textScore.setText("");
        textInfo.setText("");
        imageClose=(ImageView)findViewById(R.id.image_compliance_close);
        imageClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ComplianceActivity.this.finish();
            }
        });
        mMainLayout=(LinearLayout)findViewById(R.id.layout_compliance_main);
        mMainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (textView.getVisibility()==View.VISIBLE)
                    viewPolicy(v);
            }
        });
        //设置扫描动画
        imageScan=(ImageView)findViewById(R.id.image_compliance_scan);
        imageFinish=(ImageView)findViewById(R.id.image_compliance_finish);

        RotateAnimation animation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(2000);
        animation.setRepeatCount(Animation.INFINITE);
        imageScan.startAnimation(animation);

        startScan();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.finish();
        return super.onTouchEvent(event);
    }

    public void viewPolicy(View view) {
        mContext.startActivity(new Intent(mContext,PolicyAttentionActivity.class));
        finish();
    }

    private void startScan() {
        new Thread(){
            public void run() {
                int score= PolicyManager.getMPolicyManager(mContext).getPolicyScore();
                for (int i = 0; i <= score; i++) {
                    Message msg = Message.obtain();
                    msg.what = SCAN_LODING;
                    msg.arg1=i;
                    handler.sendMessage(msg);
                    EmmClientApplication.foregroundIntervals=0;
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Message msg = Message.obtain();
                msg.what = FINSH_SCAN;
                msg.arg1=score;
                handler.sendMessage(msg);
            };
        }.start();
    }

    public void onResume() {
        super.onResume();
        EmmClientApplication.runningBackground=false;
        MobclickAgent.onResume(this);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }


}

package cn.qdsc.msp.ui.qdlayout;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import com.daimajia.numberprogressbar.NumberProgressBar;

import cn.qdsc.msp.R;

/**
 * Created by lenovo on 2016-1-13.
 */
public class QdProgressDialog extends Dialog {

    private NumberProgressBar progressBar;
    private TextView textMessage,textTitle;
    private Button button;
    private boolean cancleable=false;
    private Context mContext;
    private View.OnClickListener mOnCancleListener;

    public QdProgressDialog(Context context) {
        super(context);
        mContext=context;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCancelable(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_progress);
        progressBar = (NumberProgressBar) findViewById(R.id.bar_progress);
        textTitle=(TextView)findViewById(R.id.text_progress_title);
        textMessage=(TextView)findViewById(R.id.text_progress_message);
        button=(Button)findViewById(R.id.button_progress);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Button btn = (Button) v;
                if (btn.getText().toString().equals("确认"))
                    QdProgressDialog.this.dismiss();
                else if (btn.getText().toString().equals("取消")) {
                    if (mOnCancleListener!=null)
                        mOnCancleListener.onClick(v);
                }
            }
        });
    }

    public void setOnCancleLisener(View.OnClickListener listner) {
        mOnCancleListener=listner;
    }

    public void setCancleable(boolean flag) {
        this.cancleable=flag;
    }

    public void setTitle(String text) {
        if (textTitle!=null)
            textTitle.setText(text);
    }

    public void setMessage(String text) {
        if (textMessage!=null)
            textMessage.setText(text);
    }

    public void setProgress(int progress) {
        if (progressBar!=null) {
            progressBar.setProgress(progress);
            if (progressBar.getProgress() == progressBar.getMax()) {
               button.setText("确认");
                button.setBackground(mContext.getResources().getDrawable(R.mipmap.button_progress_confirm));
            }
        }
    }


}

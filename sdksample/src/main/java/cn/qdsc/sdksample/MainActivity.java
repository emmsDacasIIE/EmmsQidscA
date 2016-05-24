package cn.qdsc.sdksample;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.sangfor.ssl.SangforAuth;

import cn.qdsc.mspsdk.QdSecureContainer;
import cn.qdsc.mspsdk.VPNListener;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private String TAG="VPN";
    Context mContext;
    EditText msg,name;
    TextView plain;
    Button btnEnc,btnDec,btnVpn1,btnVpn2;
    RadioGroup rg;
    RadioButton rb_aes_128,rb_aes_256,rb_sm4;
    QdSecureContainer container;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext=MainActivity.this.getApplicationContext();
        setContentView(R.layout.activity_main);

        msg=(EditText)findViewById(R.id.etMsg);
        name=(EditText)findViewById(R.id.etName);
        plain=(TextView)findViewById(R.id.tvPlain);
        btnEnc=(Button)findViewById(R.id.btnEncrypt);
        btnEnc.setOnClickListener(this);
        btnDec=(Button)findViewById(R.id.btnDecrypt);
        btnDec.setOnClickListener(this);
        btnVpn1=(Button)findViewById(R.id.vpn1);
        btnVpn1.setOnClickListener(this);
        btnVpn2=(Button)findViewById(R.id.vpn2);
        btnVpn2.setOnClickListener(this);
        rg=(RadioGroup)findViewById(R.id.rg_alg);
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_aes_128:
                        container.setAlgorithm("AES/128");
                        break;
                    case R.id.rb_aes_256:
                        container.setAlgorithm("AES/256");
                        break;
                    case R.id.rb_sm4:
                        container.setAlgorithm("SM4");
                        break;
                }
            }
        });
        rb_aes_128=(RadioButton)findViewById(R.id.rb_aes_128);
        rb_aes_256=(RadioButton)findViewById(R.id.rb_aes_256);
        rb_sm4=(RadioButton)findViewById(R.id.rb_sm4);
        container= QdSecureContainer.getInstance(mContext);
        container.setAlgorithm("AES/128");
        container.initVpn(new VPNListener() {
            @Override
            public void onLoginSuccess() {
                Toast.makeText(mContext, "VPN连接成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLoginFail() {
                Toast.makeText(mContext, "VPN连接失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLogout() {
                Toast.makeText(mContext, "VPN退出成功", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroy() {
        SangforAuth.getInstance().vpnQuit();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnEncrypt:
                String data=msg.getText().toString();
                String n=name.getText().toString();
                if (n==null || n.trim().equals("")||data==null||data.trim().equals(""))
                {
                    Toast.makeText(mContext, "名称和数据不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    container.encrypt(name.getText().toString(), data.getBytes());
                    Toast.makeText(mContext, "加密成功", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(mContext, "加密失败", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnDecrypt:
                try {
                    byte[] p=container.decryptToMemory(name.getText().toString());
                    plain.setText(new String(p));
                    Toast.makeText(mContext, "解密成功", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(mContext, "解密失败", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.vpn1:
                container.openVpn();
                break;
            case R.id.vpn2:
                container.closeVpn();
                break;
        }
    }

}

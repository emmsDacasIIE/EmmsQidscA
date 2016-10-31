package cn.qdsc.mspsdk;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.sangfor.ssl.IVpnDelegate;
import com.sangfor.ssl.SFException;
import com.sangfor.ssl.SangforAuth;
import com.sangfor.ssl.common.VpnCommon;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.NoSuchElementException;

import cn.qdsc.cipher.QdAes128Cipher;
import cn.qdsc.cipher.QdAes256Cipher;
import cn.qdsc.cipher.QdCipher;
import cn.qdsc.cipher.QdSMCipher;
import cn.qdsc.utils.ConvertUtils;
import cn.qdsc.utils.PKCS5Padding;
import cn.qdsc.utils.PrefUtils;

/**
 * VPN逻辑类；
 * Created by jzhou on 2015-11-26.
 */
public class QdSecureContainer implements IVpnDelegate {

    private KeyManager mKeyManager = null;
    private static QdSecureContainer mContainer = null;
    private String dirPath = null,dirPlainPath=null,dirTempPath=null;
    private final int BlockLength=16;
    private  static final int CacheSize=1024;
    private Context mContext;
    private QdCipher mCipher;
    private static String TAG="VPN";

    @Override
    public void vpnCallback(int vpnResult, int authType) {
        SangforAuth sfAuth = SangforAuth.getInstance();

        switch (vpnResult) {
            case IVpnDelegate.RESULT_VPN_INIT_FAIL:
                /**
                 * 初始化vpn失败
                 */
                Log.i(TAG, "RESULT_VPN_INIT_FAIL, error is " + sfAuth.vpnGeterr());
                if (vpnListener!=null)
                    vpnListener.onLoginFail();
                break;

            case IVpnDelegate.RESULT_VPN_INIT_SUCCESS:
                /**
                 * 初始化vpn成功，接下来就需要开始认证工作了
                 */
                Log.i(TAG, "RESULT_VPN_INIT_SUCCESS, current vpn status is " + sfAuth.vpnQueryStatus());
                doVpnLogin(IVpnDelegate.AUTH_TYPE_PASSWORD);
                break;

            case IVpnDelegate.RESULT_VPN_AUTH_FAIL:
                /**
                 * 认证失败，有可能是传入参数有误，具体信息可通过sfAuth.vpnGeterr()获取
                 */
                Log.i(TAG, "RESULT_VPN_AUTH_FAIL, error is " + sfAuth.vpnGeterr());
                if (vpnListener!=null)
                    vpnListener.onLoginFail();
                break;

            case IVpnDelegate.RESULT_VPN_AUTH_SUCCESS:
                /**
                 * 认证成功，认证成功有两种情况，一种是认证通过，可以使用sslvpn功能了，另一种是前一个认证（如：用户名密码认证）通过，
                 * 但需要继续认证（如：需要继续证书认证）
                 */
                if (authType == IVpnDelegate.AUTH_TYPE_NONE) {
                    Log.i(TAG, "welcom to msp vpn!");
                    if (vpnListener!=null)
                        vpnListener.onLoginSuccess();
                } else {
                    Log.i(TAG, "auth success, and need next auth, next auth type is " + authType);
                    doVpnLogin(authType);
                }
                break;
            case IVpnDelegate.RESULT_VPN_AUTH_LOGOUT:
                /**
                 * 主动注销（自己主动调用logout接口）或者被动注销（通过控制台把用户踢掉）均会调用该接口
                 */
                Log.i(TAG, "RESULT_VPN_AUTH_LOGOUT");
                if (vpnListener!=null)
                    vpnListener.onLogout();
                break;
            default:
                /**
                 * 其它情况，不会发生，如果到该分支说明代码逻辑有误
                 */
                Log.i(TAG, "RESULT_VPN_DEFAULT");
                break;
        }
    }

    @Override
    public void reloginCallback(int status, int result) {
        switch (status){

            case IVpnDelegate.VPN_START_RELOGIN:
                Log.e(TAG, "relogin callback start relogin start ...");
                break;
            case IVpnDelegate.VPN_END_RELOGIN:
                Log.e(TAG, "relogin callback end relogin ...");
                if (result == IVpnDelegate.VPN_RELOGIN_SUCCESS){
                    Log.e(TAG, "relogin callback, relogin success!");
                } else {
                    Log.e(TAG, "relogin callback, relogin failed");
                }
                break;
        }
    }

    @Override
    public void vpnRndCodeCallback(byte[] bytes) {

    }


    public interface Algorithm {
        int AES_128 = 0;
        int AES_256=1;
        int SM4 = 2;
    }

    public interface Mode {
        int Encrypt = 0;
        int Decrypt=1;
    }

    private interface EncrypState {
        int Idle=0;
        int Init=1;
        int Append = 2;
        int Final=3;
    }

    private QdSecureContainer(Context ctx) {
        mContext=ctx.getApplicationContext();
        mKeyManager = KeyManager.getInstance(mContext);
        mCipher= new QdSMCipher(mContext);
        checkDirs(ctx);
    }

    private void checkDirs(Context ctx) {
        dirPath = Environment.getExternalStorageDirectory().getPath() + "/msp/" + ctx.getApplicationContext().getPackageName();
        dirTempPath=dirPath+"/temp";
        dirPlainPath=dirPath+"/plain";
        File dir=new File(dirPath);
        File tempDir=new File(dirTempPath);
        File plainDir=new File(dirPlainPath);
        if (!dir.exists())
            dir.mkdirs();
        if (!tempDir.exists())
            tempDir.mkdirs();
        if (!plainDir.exists())
            plainDir.mkdirs();
        dirPath=dirPath+File.separator;
        dirTempPath=dirTempPath+File.separator;
        dirPlainPath=dirPlainPath+File.separator;
    }

    public static QdSecureContainer getInstance(Context ctx) {
        if (mContainer == null) {
            synchronized (QdSecureContainer.class) {
                if (mContainer == null)
                    mContainer = new QdSecureContainer(ctx);
            }
        }
        else
            mContainer.checkDirs(ctx);
        return mContainer;
    }

    private VPNListener vpnListener;

    public boolean initVpn(VPNListener listener) {
        SangforAuth sfAuth = SangforAuth.getInstance();
        try {
            sfAuth.init(mContext, this, SangforAuth.AUTH_MODULE_EASYAPP);
            sfAuth.setLoginParam(AUTH_CONNECT_TIME_OUT, String.valueOf(5));
            this.vpnListener=listener;
        } catch (SFException e) {
           return false;
        }
        return  true;
    }
    public void openVpn() {
        // 开始初始化VPN
        boolean result=false;
        String vpnSetting=PrefUtils.getVpnSettings(mContext);
        if (vpnSetting!=null) {
            try {
                JSONObject json = new JSONObject(vpnSetting);
                String addr = json.getString("vpnGatewayAddress");
                if (addr != null && initSslVpn(addr)) {
                   result=true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (!result && vpnListener!=null)
                vpnListener.onLoginFail();
    }

    public void closeVpn() {
        SangforAuth.getInstance().vpnLogout();
    }


    private InetAddress m_iAddr=null;
    private boolean initSslVpn(final String addr) {
        SangforAuth sfAuth = SangforAuth.getInstance();
        Log.i(TAG, "================ initSslVpn ===");
        m_iAddr = null;

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    m_iAddr = InetAddress.getByName(addr.split(":")[0]);
                    Log.i(TAG, "ip Addr is : " + m_iAddr.getHostAddress());
                } catch (UnknownHostException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (m_iAddr == null || m_iAddr.getHostAddress() == null) {
            Log.d(TAG, "vpn host error");
            return false;
        }
        long host = VpnCommon.ipToLong(m_iAddr.getHostAddress());
        int port=Integer.parseInt(addr.split(":")[1]);

        if (sfAuth.vpnInit(host, port) == false) {
            Log.d(TAG, "vpn init fail, errno is " + sfAuth.vpnGeterr());
            return false;
        }
        return true;
    }

    private boolean doVpnLogin(int authType) {
        Log.d(TAG, "doVpnLogin authType " + authType);

        boolean ret = false;
        SangforAuth sForward = SangforAuth.getInstance();

        switch (authType) {
            case IVpnDelegate.AUTH_TYPE_CERTIFICATE:
                sForward.setLoginParam(IVpnDelegate.CERT_PASSWORD, "123456");
                sForward.setLoginParam(IVpnDelegate.CERT_P12_FILE_NAME, "/sdcard/csh/csh.p12");
                ret = sForward.vpnLogin(IVpnDelegate.AUTH_TYPE_CERTIFICATE);
                break;
            case IVpnDelegate.AUTH_TYPE_PASSWORD:
                sForward.setLoginParam(IVpnDelegate.PASSWORD_AUTH_USERNAME, "qdsf");
                sForward.setLoginParam(IVpnDelegate.PASSWORD_AUTH_PASSWORD, "qdsc123");
                ret = sForward.vpnLogin(IVpnDelegate.AUTH_TYPE_PASSWORD);
                break;
            default:
                Log.w(TAG, "default authType " + authType);
                break;
        }
        return ret;
    }


    public void setAlgorithm(String algStr) throws  IllegalArgumentException {
        if (algStr.equalsIgnoreCase("AES/128")) {
            mCipher=new QdAes128Cipher(mContext);
            mKeyManager.setCipher(mCipher);
        }
        else if (algStr.equalsIgnoreCase("AES/256")) {
            mCipher=new QdAes256Cipher(mContext);
            mKeyManager.setCipher(mCipher);
        }
        else  if (algStr.equalsIgnoreCase("SM4")) {
            mCipher=new QdSMCipher(mContext);
            mKeyManager.setCipher(mCipher);
        }
        else throw new IllegalArgumentException();
    }

    public void encrypt( String fileName, byte[] data) throws  Exception {
        String fullPath = dirPath + fileName;
        File file = new File(fullPath);
        if (file.exists()) file.delete();
        RandomAccessFile targetF;
        FileChannel tFC = null;
        try {
            QdKey keyInfo = mKeyManager.getQdKey(Mode.Encrypt,fileName);
            data= PKCS5Padding.padding(data);
            byte[] dataEnc = mCipher.encrypt(keyInfo.getKey(), data);
            targetF = new RandomAccessFile(file, "rw");
            tFC = targetF.getChannel();
            ByteBuffer bf = ByteBuffer.wrap(dataEnc);
            int currentLen = 0;
            while (currentLen < dataEnc.length) {
                currentLen += tFC.write(bf, currentLen);
            }
        } finally {
            try {
                if (tFC != null) tFC.close();
            } catch (IOException e) {}
        }
    }

    public void encryptFromFile(String fileName) throws Exception {
        encryptFromFile(fileName,true);
    }

    public void encryptFromFile(String fileName,boolean deleteAfterEncrypt) throws Exception {
        File sFile=new File(dirTempPath+fileName);
        if (!sFile.exists()) throw new FileNotFoundException();
        File targetFile=new File(dirPath+fileName);
        if (targetFile.exists()) targetFile.delete();
        RandomAccessFile sourceF;
        RandomAccessFile targetF;
        FileChannel sFC=null;
        FileChannel tFC=null;
        try {
            QdKey keyInfo = mKeyManager.getQdKey(Mode.Encrypt, fileName);
            sourceF = new RandomAccessFile(sFile, "r");
            sFC=sourceF.getChannel();
            targetF = new RandomAccessFile(targetFile, "rw");
            tFC=targetF.getChannel();
            ByteBuffer byteData = ByteBuffer.allocate(CacheSize);
            byte[] byteList=null;
            while (true) {
                int len = sFC.read(byteData);
                if (len <= 0) {
                    byteList=PKCS5Padding.padding(byteList);
                }
                if (byteList!=null) {
                    byte[] dataEnc = mCipher.encrypt(keyInfo.getKey(), byteList);
                    ByteBuffer bf = ByteBuffer.wrap(dataEnc);
                    tFC.write(bf);
                }
                if (len <= 0) break;
                byteData.flip();
                byteList = new byte[byteData.remaining()];
                byteData.get(byteList, 0, byteList.length);
                byteData.clear();
            }
            if (deleteAfterEncrypt)
                sFile.delete();
        } finally {
            try {
                if (tFC != null) tFC.close();
                if (sFC != null) sFC.close();
            } catch (IOException e) {}
        }
    }

    private int state= EncrypState.Idle;
    private FileChannel tempFileChannel=null;
    private int tempCurrentLen=0;
    private QdKey tempQdKey=null;

    public void resetState() {
        state= EncrypState.Idle;
        tempCurrentLen=0;
        tempQdKey=null;
        try {
            if (tempFileChannel != null) tempFileChannel.close();
        } catch (IOException e) {}
    }

    public void encryptInit(String fileName, byte[] data) throws  Exception {
        if (data.length%BlockLength!=0 )
            throw new IllegalArgumentException();
        resetState();
        state= EncrypState.Init;
        String fullPath = dirPath + fileName;
        File file = new File(fullPath);
        if (file.exists()) file.delete();
        RandomAccessFile targetF;
        try {
            tempQdKey = mKeyManager.getQdKey(Mode.Encrypt,fileName);
            targetF = new RandomAccessFile(file, "rw");
            tempFileChannel= targetF.getChannel();
            byte[] dataEnc = mCipher.encrypt(tempQdKey.getKey(), data);
            ByteBuffer bf = ByteBuffer.wrap(dataEnc);
            while (tempCurrentLen < dataEnc.length) {
                tempCurrentLen += tempFileChannel.write(bf, tempCurrentLen);
            }
        } catch (Exception e) {
            resetState();
            throw e;
        }
    }

    public void encryptAppend(byte[] data) throws  Exception {
        if (data.length%BlockLength!=0 || (state != EncrypState.Init && state!=EncrypState.Append))
            throw new IllegalArgumentException();
        state= EncrypState.Append;
        try {
            byte[] dataEnc = mCipher.encrypt(tempQdKey.getKey(), data);
            ByteBuffer bf = ByteBuffer.wrap(dataEnc);
            int lastPosition=tempCurrentLen;
            while (tempCurrentLen < dataEnc.length+lastPosition) {
                tempCurrentLen += tempFileChannel.write(bf, tempCurrentLen);
            }
        } catch (Exception e) {
            resetState();
            throw  e;
        }
    }

    public void encryptFinal(byte[] data) throws  Exception {
        if ( state != EncrypState.Init && state!=EncrypState.Append) return;
        state= EncrypState.Final;
        try {
            data=PKCS5Padding.padding(data);
            byte[] dataEnc = mCipher.encrypt(tempQdKey.getKey(), data);
            ByteBuffer bf = ByteBuffer.wrap(dataEnc);
            int lastPosition=tempCurrentLen;
            while (tempCurrentLen < dataEnc.length+lastPosition) {
                tempCurrentLen += tempFileChannel.write(bf, tempCurrentLen);
            }
        }
        finally {
            resetState();
        }
    }

    public byte[] decryptToMemory(String fileName) throws  Exception {
        if (!mKeyManager.containsQdKey(fileName)) throw new NoSuchElementException();
        String fullPath = dirPath + fileName;
        File file = new File(fullPath);
        if (!file.exists()) throw new FileNotFoundException();
        RandomAccessFile sourceF = null;
        FileChannel sFC=null;
        byte[] result=null;
        try {
            QdKey keyInfo = mKeyManager.getQdKey(Mode.Decrypt,fileName);
            int alg=keyInfo.getAlgorithm();
            QdCipher cipher = null;
            if (alg == Algorithm.AES_128) {
                cipher = new QdAes128Cipher(mContext);
            } else if (alg == Algorithm.AES_256) {
                cipher = new QdAes256Cipher(mContext);
            } else if (alg == Algorithm.SM4) {
                cipher = new QdSMCipher(mContext);
            }
            sourceF = new RandomAccessFile(file, "r");
            sFC=sourceF.getChannel();
            ByteBuffer byteData = ByteBuffer.allocate(CacheSize);
            while (true) {
                int len=sFC.read(byteData);
                if (len<=0) break;
                byteData.flip();
                byte[] byteList = new byte[byteData.remaining()];
                byteData.get(byteList, 0, byteList.length);
                byte[] dataDec = cipher.decrypt(keyInfo.getKey(), byteList);
                result= result==null? dataDec: ConvertUtils.join(result, dataDec);
                byteData.clear();
            }
            result=PKCS5Padding.unPadding(result);
        } finally {
            try {
                if (sFC != null) sFC.close();
            } catch (IOException e) {}
        }
        return result;
    }

    public String decryptToFile(String fileName) throws  Exception {
        if (!mKeyManager.containsQdKey(fileName)) throw new NoSuchElementException();
        String fullPath = dirPath + fileName;
        File file = new File(fullPath);
        if (!file.exists())throw new FileNotFoundException();
        File targetFile=new File(dirPlainPath+fileName);
        if (targetFile.exists()) targetFile.delete();
        RandomAccessFile sourceF;
        RandomAccessFile targetF;
        FileChannel sFC=null;
        FileChannel tFC=null;
        String targetFullPath=null;
        try {
            QdKey keyInfo = mKeyManager.getQdKey(Mode.Decrypt,fileName);
            QdCipher cipher = null;
            int alg=keyInfo.getAlgorithm();
            if (alg == Algorithm.AES_128) {
                cipher = new QdAes128Cipher(mContext);
            } else if (alg == Algorithm.AES_256) {
                cipher = new QdAes256Cipher(mContext);
            } else if (alg == Algorithm.SM4) {
                cipher = new QdSMCipher(mContext);
            }
            sourceF = new RandomAccessFile(file, "r");
            sFC=sourceF.getChannel();
            targetF = new RandomAccessFile(targetFile, "rw");
            tFC=targetF.getChannel();
            ByteBuffer byteData = ByteBuffer.allocate(CacheSize);
            byte[] dataDec=null;
            while (true) {
                int len = sFC.read(byteData);
                if (len <= 0 && dataDec!=null) {
                    dataDec=PKCS5Padding.unPadding(dataDec);
                }
                if (dataDec!=null) {
                    ByteBuffer bf = ByteBuffer.wrap(dataDec);
                    tFC.write(bf);
                }
                if (len<=0) break;
                byteData.flip();
                byte[] byteList = new byte[byteData.remaining()];
                byteData.get(byteList, 0, byteList.length);
                dataDec = cipher.decrypt(keyInfo.getKey(), byteList);
                byteData.clear();
            }
            targetFullPath=targetFile.getAbsolutePath();
        } finally {
            try {
                if (tFC != null) tFC.close();
                if (sFC != null) sFC.close();
            } catch (IOException e) {}
        }
        return targetFullPath;
    }

    public void delete(String fileName) throws FileNotFoundException {
        String fullPath = dirPath + fileName;
        File file = new File(fullPath);
        if (file.exists()) {
            file.delete();
           mKeyManager.deleteQdKey(fileName);
        }
        else
         throw  new FileNotFoundException();
    }

    public String getDirTempPath() {
        return dirTempPath;
    }

    //0:不存在；1：下载完成；2：下载未完成
    public int getFileState(String fileName) {
        File file=new File(dirPath + fileName);
        if (file.exists())
            return 1;
        File tempFile=new File(dirTempPath+fileName);
        if (tempFile.exists())
            return 2;
        return 0;
    }




}

package cn.dacas.emmclient.worker;

import android.content.Context;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Map;
import java.util.Set;

public class DownLoadCertFromUrl {

    private Context mContext;
    private String urlP;
    private String fileName;

    public DownLoadCertFromUrl(Context context) {
        this.mContext = context;
    }

    private void downloadFile() {

        File certDirFile = mContext
                .getDir("cert", Context.MODE_PRIVATE);
        if (!certDirFile.exists()) {
            certDirFile.mkdir();
        }

        final String path = mContext.getDir("cert", Context.MODE_PRIVATE)
                .getAbsolutePath();

        try {
            URL url = new URL(urlP);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return;
        }
        Request<byte[]> request = new Request<byte[]>(Request.Method.GET, urlP, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }) {
            @Override
            protected Response<byte[]> parseNetworkResponse(NetworkResponse response) {
                try {
                    fileName = response.headers.get("filename");
                    if (fileName != null && !"".equals(fileName)) {
                        // 文件名解码
                        fileName = URLDecoder.decode(fileName, "utf-8");
                    } else {
                        // 如果无法获取文件名，则随机生成一个
                        fileName = "file_" + (int) (Math.random() * 10);
                    }
                    File certFile = new File(path, fileName);
                    if (certFile.exists())
                        certFile.delete();
                    FileOutputStream fos = new FileOutputStream(certFile);
                    fos.write(response.data);
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void deliverResponse(byte[] response) {

            }
        };
    }

    public static void startDownloadCertList(Context context,
                                             Map<String, String> nameUrl) {
        Set<String> fileTags = nameUrl.keySet();
        File certDirFile = context
                .getDir("cert", Context.MODE_PRIVATE);
        //清空文件夹
        String[] tempList = certDirFile.list();
        String path = context.getDir("cert", Context.MODE_PRIVATE)
                .getAbsolutePath();
        File temp = null;
        for (int i = 0; i < tempList.length; i++) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + tempList[i]);
            } else {
                temp = new File(path + File.separator + tempList[i]);
            }
            if (temp.isFile()) {
                temp.delete();
            }
        }
        //下载证书
        for (String fileTag : fileTags) {
            new DownLoadCertFromUrl(context).startDownload(fileTag,
                    nameUrl.get(fileTag));
        }
    }

    private void startDownload(String name, String ip) {
        // TODO Auto-generated method stub
        this.urlP = ip;
        this.fileName = name;
        downloadFile();
    }

}

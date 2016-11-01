package cn.dacas.emmclient.util;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import cn.dacas.emmclient.R;
import cn.dacas.emmclient.core.EmmClientApplication;
import cn.dacas.emmclient.core.mcm.FileOpener;
import cn.dacas.emmclient.ui.qdlayout.QdLoadingDialog;

/**
 * 有关文件操作的工具类
 *
 * @author lizy
 */
public class FileEngine {


    /**
     * 异步操作时，可能产生的Event
     */
    public static final int EVENT_CLEAR_DIR = 1;    //目录
    public static final int EVENT_MODIFY_FILE = 2;  //文件

    /**
     * 异步操作目录时，可能产生的结果
     */
    public static final int CLEAR_DIR_NOEXIST = 1;  //目录不存在
    public static final int CLEAR_DIR_NULL = 2;     //目录为空
    public static final int CLEAR_DIR_NONULL = 3;   //目录不为空

    /**
     * 异步操作文件时，可能产生的结果
     */
    public static final int MODIFY_FILE_NO_PATH = 4; //不是文件目录
    public static final int MODIFY_FILE_NO_FILE = 5; //文件不存在

    /**
     * 异步操作监视器
     */
    private FileEngineObserver observer;

    /**
     * 异步操作监视器接口
     */
    public interface FileEngineObserver {
        /**
         * @param EventType   可能是EVENTTYEP_CLEAR_DIR 、EVENTTYEP_MODIFY_FILE两种情况
         * @param finish      是否完成
         * @param evenValue02 MODIFY_FILE_NO_PATH为目录错误，
         *                    MODIFY_FILE_NO_FILE为文件不存在，
         *                    CLEAR_DIR_NULL为空目录，
         *                    CLEAR_DIR_NONULL为非空目录，
         *                    CLEAR_DIR_NOEXIST为目录不存在
         * @return void
         */
        public void onEvent(int EventType, boolean finish, int evenValue02);
    }

    ;

    /**
     * 构造函数
     */
    public FileEngine() {

    }

    /**
     * 构造函数，主要用于异步操作
     */
    public FileEngine(FileEngineObserver observer) {
        this.observer = observer;
    }

    /**
     * 新建文件，如果目录不存在，则会创建目录
     *
     * @param path     路径名
     * @param fileName 文件名
     * @return File 文件描述符
     * @throws IOException
     */
    public static File createFile(String path, String fileName) throws IOException {
        File parent = new File(path);
        File file = null;
        if (!parent.exists()) {
            parent.mkdirs();
        }
        if (parent.isDirectory()) {
            file = new File(parent, fileName);
            boolean isExist = file.createNewFile();
            return file;

        } else {
            return null;
        }
    }

    /**
     * 修改文件内容(异步)
     * MODIFY_FILE_NO_PATH为目录错误,MODIFY_FILE_NO_FILE为文件不存在
     *
     * @param filePath 文件路径
     * @param data     修改的数据
     * @param append   修改文件内容时，是否以追加方式进行
     * @return void
     */
    public void modifyFileAsync(final String filePath, final byte[] data,
                                final boolean append) {
        Runnable writeFile = new Runnable() {

            public void run() {
                File fileOld = new File(filePath);
                if (fileOld.exists()) {
                    if (fileOld.isFile()) {
                        // 修改文件
                        try {
                            FileOutputStream fileOutputStream = new FileOutputStream(
                                    fileOld, append);
                            fileOutputStream.write(data);
                            fileOutputStream.flush();
                            fileOutputStream.close();
                            if (observer != null) {
                                observer.onEvent(EVENT_MODIFY_FILE, true, 0);
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    } else {
                        if (observer != null) {
                            observer.onEvent(EVENT_MODIFY_FILE, false,
                                    MODIFY_FILE_NO_PATH);
                        }
                    }
                } else {
                    if (observer != null) {
                        observer.onEvent(EVENT_MODIFY_FILE, false,
                                MODIFY_FILE_NO_FILE);
                    }
                    return;
                }

            }
        };

        new Thread(writeFile).start();
    }

    /**
     * 修改文件内容
     *
     * @param filePath 文件路径
     * @param data     修改的数据
     * @param append   修改文件内容时，是否以追加方式进行
     * @return void
     */
    public void modifyFile(final String filePath, final byte[] data,
                           final boolean append) {
        File fileOld = new File(filePath);

        if (fileOld.exists()) {
            if (fileOld.isFile()) {
                // 修改文件
                try {
                    FileOutputStream fileOutputStream = new FileOutputStream(
                            fileOld, append);
                    fileOutputStream.write(data);
                    fileOutputStream.flush();
                    fileOutputStream.close();
                    return;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {
                return;
            }
        } else {
            return;
        }
    }

    /**
     * 删除一个文件
     *
     * @param filePath 文件名
     * @return true: 操作成功; false: 操作失败
     */
    public static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return false;
        }
        if (file.isFile()) {
            return file.delete();
        } else {
            return false;
        }
    }

    /**
     * 删除一个文件
     *
     * @param file 文件描述符
     * @return true: 操作成功; false: 操作失败
     */
    public static boolean deleteFile(File file) {
        if (null == file || !file.exists()) {
            return false;
        }
        if (file.isFile()) {

            return file.delete();
        } else {

            return false;
        }
    }

    /**
     * 新建目录
     *
     * @param path 目录名
     * @return true: 操作成功; false: 操作失败
     */
    public static boolean createDir(String path) {
        File file = new File(path);
        if (file.exists()) {
            return true;
        }
        if (file.mkdirs()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 删除指定目录（异步）
     * CLEAR_DIR_NULL为空目录，CLEAR_DIR_NONULL为非空目录，CLEAR_DIR_NOEXIST为目录不存在
     *
     * @param dirPath 目录路径
     * @return void
     */
    public void deleteDirAsync(final String dirPath) {

        Runnable clearDir = new Runnable() {

            public void run() {
                File file = new File(dirPath);
                if (file.exists()) {
                    if (file.delete()) {
                        if (observer != null) {
                            observer.onEvent(EVENT_CLEAR_DIR, true,
                                    CLEAR_DIR_NULL);
                        }
                    } else {
                        // 删除目录下的文件和文件夹
                        deleteDir(file.getAbsolutePath());
                        if (observer != null) {
                            observer.onEvent(EVENT_CLEAR_DIR, true,
                                    CLEAR_DIR_NONULL);
                        }
                    }
                } else {
                    if (observer != null) {
                        observer.onEvent(EVENT_CLEAR_DIR, true,
                                CLEAR_DIR_NOEXIST);
                    }
                    return;
                }
            }
        };

        new Thread(clearDir).start();
    }

    /**
     * 删除一个目录,包括其下所有的目录和文件
     *
     * @param path:全路径
     * @return true: 操作成功; false: 操作失败
     */
    public static boolean deleteDir(String path) {
        File f = new File(path);
        if (f == null || !f.exists()) {
            return false;
        }

        if (f.isFile())
            return f.delete();
        else {
            for (File file : f.listFiles()) {
                if (file.isFile()) {
                    file.delete();
                } else if (file.isDirectory()) {
                    deleteDir(file.getAbsolutePath());// 递归
                }
            }
        }
        f.delete();
        return true;

    }

    /**
     * 删除一个目录,包括其下所有的目录和文件
     *
     * @param dir 文件描述符
     * @return true: 操作成功; false: 操作失败
     */
    public static boolean deleteDir(File dir) {

        if (dir == null || !dir.exists()) {
            return false;
        }

        if (dir.isFile())
            return dir.delete();
        else {
            for (File file : dir.listFiles()) {
                if (file.isFile()) {
                    file.delete();
                } else if (file.isDirectory()) {
                    deleteDir(file);// 递归
                }
            }
        }
        dir.delete();
        return true;
    }

    /**
     * 修改文件或目录名
     *
     * @param oldName 原文件名
     * @param newName 新文件名
     * @return true: 操作成功; false: 操作失败
     */
    public static boolean renameFileOrDir(String oldName, String newName) {
        File oleFile = new File(oldName);
        File newFile = new File(newName);
        if (newFile.exists())
            return false;

        return oleFile.renameTo(newFile);
    }


    /**
     * 拷贝一个文件
     *
     * @param srcFile  源文件描述符
     * @param destFile 目标文件描述符
     * @return true: 操作成功; false: 操作失败
     * @throws IOException
     */
    public static boolean copyFileTo(File srcFile, File destFile)
            throws IOException {
        if (srcFile.isDirectory() || destFile.isDirectory())
            return false;// 判断是否是文件
        FileInputStream fis = new FileInputStream(srcFile);
        FileOutputStream fos = new FileOutputStream(destFile);
        int readLen = 0;
        byte[] buf = new byte[1024];
        while ((readLen = fis.read(buf)) != -1) {
            fos.write(buf, 0, readLen);
        }
        fos.flush();
        fos.close();
        fis.close();
        return true;
    }

    /**
     * 拷贝一个文件
     *
     * @param srcFile  源文件名
     * @param destFile 目标文件名
     * @return true: 操作成功; false: 操作失败
     * @throws IOException
     */
    public static boolean copyFileTo(String srcFile, String destFile)
            throws IOException {
        return copyFileTo(new File(srcFile), new File(destFile));
    }

    /**
     * 拷贝目录下的所有文件到指定目录
     *
     * @param srcDir  源目录
     * @param destDir 目标目录
     * @return true: 操作成功; false: 操作失败
     * @throws IOException
     */
    public static boolean copyAllFilesTo(File srcDir, File destDir)
            throws IOException {
        if (!srcDir.isDirectory())
            return false;// 判断是否是目录

        if (!destDir.exists())
            if (!destDir.mkdir()) {
                return false;
            }

        File[] srcFiles = srcDir.listFiles();
        for (int i = 0; i < srcFiles.length; i++) {
            if (srcFiles[i].isFile()) {
                // 获得目标文件
                File destFile = new File(destDir.getPath(), srcFiles[i].getName());
                boolean ok = copyFileTo(srcFiles[i], destFile);
            } else if (srcFiles[i].isDirectory()) {
                File theDestDir = new File(destDir.getPath(), srcFiles[i].getName());
                boolean ok = copyAllFilesTo(srcFiles[i], theDestDir);
            }
        }

        return true;
    }

    /**
     * 拷贝目录下的所有文件到指定目录
     *
     * @param srcDirName  源目录名
     * @param destDirName 目标目录名
     * @return true: 操作成功; false: 操作失败
     * @throws IOException
     */


    public static boolean copyAllFilesTo(String srcDirName, String destDirName)
            throws IOException {
        File srcDir = new File(srcDirName);
        File destDir = new File(destDirName);
        return copyAllFilesTo(srcDir, destDir);
    }

    /**
     * 移动目录下的所有文件到指定目录
     *
     * @param srcDir  源目录
     * @param destDir 目标目录
     * @return true: 操作成功; false: 操作失败
     * @throws IOException
     */
    public static boolean moveAllFilesTo(File srcDir, File destDir)
            throws IOException {
        if (!srcDir.isDirectory()) {
            return false;
        }

        if (!destDir.exists()) {
            boolean ok = destDir.mkdir();
            if (!ok) {
                return false;
            }
            if (!destDir.isDirectory()) {
                return false;
            }
        }

        File[] srcDirFiles = srcDir.listFiles();
        for (int i = 0; i < srcDirFiles.length; i++) {
            if (srcDirFiles[i].isFile()) {
                File oneDestFile = new File(destDir.getPath(), srcDirFiles[i].getName());

                boolean ok = moveFileTo(srcDirFiles[i], oneDestFile);
            } else if (srcDirFiles[i].isDirectory()) {
                File oneDestFile = new File(destDir.getPath(), srcDirFiles[i].getName());

                boolean ok = moveAllFilesTo(srcDirFiles[i], oneDestFile);

            }

        }

        deleteDir(srcDir);

        return true;
    }

    /**
     * 移动一个文件
     *
     * @param srcFile  源文件描述符
     * @param destFile 目标文件描述符
     * @return true: 操作成功; false: 操作失败
     * @throws IOException
     */
    public static boolean moveFileTo(File srcFile, File destFile)
            throws IOException {
        boolean ok = copyFileTo(srcFile, destFile);
        if (!ok)
            return false;
        deleteFile(srcFile);
        return true;
    }


    /**
     * 判断文件是否可读取文件
     *
     * @param file 文件描述符
     * @return true: 可读； false: 不可读
     */
    public static boolean canReadFile(File file) {
        boolean canRead = true;

        if (file == null || !file.exists() || !file.isFile() || !file.canRead()) {
            canRead = false;
        }
        return canRead;
    }

    /**
     * 按照编码格式获得reader
     *
     * @param dir      文件所在的路径
     * @param fileName 要读入的文件名
     * @param charset  编码格式
     * @return
     */
    public static BufferedReader getReader(String dir, String fileName,
                                           String charset) {
        BufferedReader reader = null;

        File file = new File(dir, fileName);
        if (file.exists()) {
            try {
                reader = new BufferedReader(new InputStreamReader(
                        new FileInputStream(file), charset));
            } catch (Exception e) {
                e.printStackTrace();
                reader = null;
            }
        }

        return reader;
    }

    /**
     * 按照编码格式获取某一文件的Writer,如果目录不存在会创建
     *
     * @param dir      文件所在的路径
     * @param fileName 要写入的文件名
     * @param append   追加与否
     * @param charset  编码格式
     * @return Writer, 若失败返回null
     */
    public static Writer getWriter(String dir, String fileName, boolean append,
                                   String charset) {
        Writer writer = null;

        File dirFile = new File(dir);
        if (!dirFile.exists()) {
            boolean created = dirFile.mkdirs();
            if (!created) {
                return null;
            }
        }

        File file = new File(dirFile, fileName);
        try {
            file.createNewFile();

            writer = new OutputStreamWriter(new BufferedOutputStream(
                    new FileOutputStream(file, append)), charset);
        } catch (Exception e) {
            e.printStackTrace();
            writer = null;
        }
        return writer;
    }

    /**
     * 删除指定目录下的所有以match为后缀的文件
     *
     * @param folder 指定目录
     * @param match  要匹配的后缀
     * @return void
     */
    public static void deleteFolderFile(String folder, String match) {
        File file = new File(folder);
        if (file.exists()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].getName().endsWith(match)) {
                    files[i].delete();
                }
            }
        }
    }

    /**
     * 获得某一目录下的所有目录 并按照 comparator排序
     *
     * @param dir        父目录
     * @param comparator 排序对象
     * @return
     */
    public static File[] getDirs(String dir, Comparator<File> comparator) {

        File rootPath = new File(dir);

        File[] fileDirs = rootPath.listFiles(new FileFilter() {

            public boolean accept(File file) {
                if (file.isDirectory()) {
                    return true;
                } else {
                    return false;
                }
            }
        });
        if (fileDirs != null) {
            Arrays.sort(fileDirs, comparator);
        }

        return fileDirs;
    }

    /**
     * 获得某一目录下的所有文件列表
     *
     * @param dir
     * @param comparator
     * @return
     */
    public static File[] getFiles(String dir, Comparator<File> comparator) {

        File rootPath = new File(dir);

        File[] files = rootPath.listFiles(new FileFilter() {

            public boolean accept(File file) {
                if (file.isFile()) {
                    return true;
                } else {
                    return false;
                }
            }
        });

        if (files != null) {
            Arrays.sort(files, comparator);
        }

        return files;
    }

    /**
     * 比对每个字符进行排序
     */
    public static Comparator<File> comparator = new Comparator<File>() {
        //Collator collator = Collator.getInstance(Locale.getDefault());

        public int compare(File file1, File file2) {
            String fileName1 = file1.getName();
            String fileName2 = file2.getName();

            if (!TextUtils.isEmpty(fileName1) && !TextUtils.isEmpty(fileName2)) {

                if (!fileName1.subSequence(0, 1).equals(fileName2)) {
                    int flag = 0;
                    for (int i = 0; i < fileName1.length(); i++) {
                        flag = compareString(fileName1, fileName2, i);
                        if (flag != 0) {
                            break;
                        }
                    }
                    return flag;
                } else {
                    return 0;
                }
            }

            return 0;
        }
    };

    static Collator collator = Collator.getInstance(Locale.getDefault());

    public static int compareString(String data1, String data2, int position) {
        int flage = 0;

        if (data1.length() >= position && data2.length() >= position + 1) {
            flage = collator.compare(data1.substring(position, position + 1), data2.substring(position, position + 1));
        } else {
            flage = 0;
        }

        return flage;
    }


    //////下面的函数需要有Context参数

    /**
     * 获得 应用文件目录下的文件对象
     *
     * @param context
     * @param fileName
     * @return File
     */
    public static File getDirFile(Context context, String fileName) {

        File file = new File(context.getFilesDir() + File.separator + fileName);

        return file;

    }

    /**
     * 获得 应用文件目录下的文件对象的路径
     *
     * @param context
     * @param fileName
     * @return file path,if file not found, return null
     */
    public static String getDirFilePath(Context context, String fileName) {
        File file = getDirFile(context, fileName);

        if (file != null) {
            return file.getAbsolutePath();
        } else {
            return null;
        }
    }

    /**
     * 获取文件输入流
     *
     * @param context
     * @param fileName
     * @return
     * @throws FileNotFoundException
     */
    public static InputStream getDirFileInputStream(Context context,
                                                    String fileName) throws FileNotFoundException {

        return context.openFileInput(fileName);

    }

    /**
     * get  files count of dir
     *
     * @param dir
     * @param match match file endwith(.apk, .doc, .txt......and so on)
     *              if match is null, means get all files count.
     * @return files count;
     * -1: dir param is error.
     */

    public static int getFilesCount(File dir, String match) {
        // long count=0;
        // long countd=0;
        int allFileCount = 0;
        int matchFileCount = 0;
        if (null == dir) {
            return -1;
        }
        LinkedList<File> list = new LinkedList<File>();
        File[] file = dir.listFiles();
        for (int i = 0; i < file.length; i++) {
            if (file[i].isDirectory()) {
                // add directory to list
                list.add(file[i]);
            } else {
                if (null == match) {
                    allFileCount++;

                } else if (file[i].getName().toLowerCase().endsWith(match)) {
                    // match file
                    matchFileCount++;
                }
            }
        }  // end for

        File tmp = null;
        // loop
        while (!list.isEmpty()) {
            // delete first record of list.
            tmp = list.removeFirst();
            if (tmp.isDirectory()) {
                // list files
                file = tmp.listFiles();
                if (file == null) {
                    continue;
                }
                for (int i = 0; i < file.length; i++) {
                    if (file[i].isDirectory()) {
                        //if fiele[i] is directory, add it to list.
                        list.add(file[i]);
                    } else {
                        if (null == match) {
                            allFileCount++;
//							FLog.i(TAG,"file[" + allFileCount + "]:"
//									+ file[i].getAbsolutePath());
                        } else if (file[i].getName().toLowerCase().endsWith(match)) {
                            // match file
                            //sdcardApkList.add(file[i]);
                            matchFileCount++;
                        }
                    }
                }
            } else {
                //countd++;
                //FLog.i("TAG", "dir:" + countd + "path:"
                //		+ tmp.getAbsolutePath());
            }
        }

        if (null == match) {
            return allFileCount;
        } else {
            return matchFileCount;
        }

    }

    /*
     * 删除给定字符串的后缀， 这个后缀由match来指定，返回操作完成后的字符串
     */
    public static String deleteSuffix(String str, String match) {
        if (str == null || TextUtils.isEmpty(str) || match == null || TextUtils.isEmpty(match)) {
            return null;
        }
        String subStr = null;
        int i = -1;
        i = str.indexOf(match);

        if (i > 0) {
            subStr = str.substring(0, i);
        }
        return subStr;
    }

    public static synchronized void openCipherFile(final Context ctx, final String name) {
        final QdLoadingDialog mLoadingDialog = new QdLoadingDialog(ctx, "解密中");
        mLoadingDialog.setCancelable(false);
        mLoadingDialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final String plainFilePath = EmmClientApplication.mSecureContainer.decryptToFile(name);
                    FileOpener opener = new FileOpener(ctx);
                    opener.openFile(new File(plainFilePath));
                    long time = 30 * 1000;
                    if (FileOpener.checkEndsWithInStringArray(plainFilePath, ctx.getResources().getStringArray(R.array.fileEndingVideo)))
                        time = 60 * 60 * 1000;
                    Timer mTimer = new Timer(true);
                    TimerTask mTimerTask = new TimerTask() {
                        public void run() {
                            File file = new File(plainFilePath);
                            if (file.exists())
                                file.delete();
                        }
                    };
                    mTimer.schedule(mTimerTask, time);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    mLoadingDialog.dismiss();
                }
            }
        }).start();
    }

    public static String getSDPath(){
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(android.os.Environment.MEDIA_MOUNTED);//判断sd卡是否存在
        if(sdCardExist)
        {
            sdDir = Environment.getExternalStorageDirectory();//获取跟目录
            return sdDir.getAbsolutePath();

        }
        return sdDir.toString();
    }

    public static String formatLen(long len) {
        int res = (int) len/1024/1024;
        String lenStr = "";
        if (res > 0) {
            lenStr = res + "M";
            return lenStr;
        }

        res = (int) len/1024;
        if (res > 0) {
            lenStr = res + "K";
            return lenStr;
        }

        lenStr = len + "b";
        return lenStr;

    }



}

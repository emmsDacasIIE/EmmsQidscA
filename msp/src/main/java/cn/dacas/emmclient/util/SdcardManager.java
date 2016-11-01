package cn.dacas.emmclient.util;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * SD卡管理工具类，静态类；
 * 主要功能包括获取内置外置SD卡路径，内置位置SD卡状态、路径，内置位置SD卡总大小，已用大小，剩余可用大小
 * @author xiecl
 *
 */
public class SdcardManager {

	private static final String TAG = "SdcardManager";
	
	/**
	 * 外置存储SD卡是否可用
	 * @return true false
	 */
    public static boolean externalMemoryAvailable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取外置存储总大小
     * @return long 
     */
    static public long getTotalExternalMemorySize() {
        File file = getSdcardFile();
        if (null == file) {
            return 0;
        }
        StatFs stat = new StatFs(file.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return totalBlocks * blockSize;
    }

    /**
     * 获取外置SD卡的路径
     * 
     * @return sdcardpath: if sdcard exist null: sdcard not found
     */
    public static String getSdcardPath() {
        String sdcard_path = "";
        if (externalMemoryAvailable()) {
            sdcard_path = Environment.getExternalStorageDirectory().getPath();
            QDLog.i(TAG, "sdcard_path=============:" + sdcard_path);
            return sdcard_path;
        } else if (Environment.getExternalStorageState().equals(Environment.MEDIA_REMOVED)) {
            return null;
        } else {
            return null;
        }
    }

    /**
     * 获取sd卡大小
     * @return SD卡大小
     */
    public static long getSdcardSize() {
    	return getTotalExternalMemorySize(getSdcardPath());
    }
    
    /**
     * 获取外置SD路径
     * 
     * @return sdcardFile:sdcard exist  null: sdcard not found
     */
    public static File getSdcardFile() {
        File sdcardFile = null;
        if (externalMemoryAvailable()) {
            sdcardFile = Environment.getExternalStorageDirectory();
        }
        return sdcardFile;
    }
    
    /**
     * 在外置SD卡中根据parent和fileName创建文件
     * 
     * @param context
     * @param parent
     * @param fileName
     * @return 创建成功的文件
     */
    public static File mkdir2SDFile(Context context, String parent, String fileName) {
        StringBuilder sb = new StringBuilder(Environment.getExternalStorageDirectory().getAbsolutePath());
        sb.append(File.separator);
        sb.append(parent);
        sb.append(File.separator);
        sb.append(fileName);

        File file = new File(sb.toString());
        File parentF = file.getParentFile();
        if (!parentF.exists() && parentF.mkdirs()) {
            return file;
        } else if (parentF.exists()) {
            return file;
        } else {
            return null;
        }
    }
    
    /**
     * 获取外置所有SD卡路径
     * @param context
     * @return String[] 外置sd路径数组
     */
    public static String[] getExternalPath(Context context) {
        String[] sdcardPath = null;
        if (Integer.valueOf(Build.VERSION.SDK_INT) > 7) {
            StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            try {
                Class<?>[] paramClasses = {};
                Method getVolumePathsMethod = storageManager.getClass().getMethod("getVolumePaths", paramClasses);
                getVolumePathsMethod.setAccessible(true);
                Object[] params = {};
                sdcardPath = (String[]) getVolumePathsMethod.invoke(storageManager, params);
                if (null != sdcardPath) {
                    List<String> tempArray = new ArrayList<String>();
                    for (String str : sdcardPath) {
                        if (!str.toLowerCase().contains("usb") && !str.toLowerCase().contains("removable") && !str.toLowerCase().contains("microsd") && !str.toLowerCase().contains("udisk") ) {
                            tempArray.add(str);
                        }
                    }

                    if (tempArray.size() > 0) {
                        String[] ret = new String[tempArray.size()];
                        for (int i = 0; i < ret.length; i++) {
                            ret[i] = tempArray.get(i);
                        }
                        return ret;
                    }
                }

            } catch (NoSuchMethodException e) {
            } catch (IllegalArgumentException e) {
            } catch (IllegalAccessException e) {
            } catch (InvocationTargetException e) {
            }
        }
        if (sdcardPath == null) {
            sdcardPath = new String[] { Environment.getExternalStorageDirectory().getAbsolutePath() };
        }
        return sdcardPath;
    }

   /**
    * 获取指定外置SD卡路径大小
    * @param path
    * @return long
    */
    public static long getTotalExternalMemorySize(String path) {
        StatFs stat = new StatFs(path);
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return totalBlocks * blockSize;
    }
    /**
     * 获取外置SD卡已用大小
     * @param path
     * @return 
     */
    public static long getUsedExternalMemorySize(String path) {
        return getTotalExternalMemorySize(path) - getAvailableExternalMemorySize(path);
    }
    
    /**
     * 获取外置SD卡可用大小
     * @param path
     * @return long 
     */
    public static long getAvailableExternalMemorySize(String path) {
        if (externalMemoryAvailable()) {
            StatFs stat = new StatFs(path);
            long blockSize = stat.getBlockSize();
            long totalBlocks = stat.getAvailableBlocks();
            return totalBlocks * blockSize;
        } else {
            return 0;
        }
    }
//
//    /**
//     * 获取内置SD卡大小
//     * @return
//     */
//    public static long getSysSdcardSize(Context context) {
//    	String sdcardPath = getSdcardPath();
//    	//没有sd卡
//    	if(TextUtils.isEmpty(sdcardPath)) {
//    		return 0;
//    	} else {
//    		String[] sdcardPaths = getExternalPath(context);
//    		//只有外置sd卡
//    		if(sdcardPaths.length == 1) {
//    			return 0;
//    		//有内置和外置sd卡
//    		} else if(sdcardPaths.length == 2) {
//    			//小米手机
//    			if("Xiaomi".equalsIgnoreCase(PhoneInfo.getBuildManufacturer())) {
//    				//说明没有外置sd卡，因为小米当有外置sd卡时sdcardPaths[0]返回就是外置sd卡的路径，当没有外置sd卡时
//    				//返回的就是内置sd卡的路径，这时候外置sd卡路径的size就是0,
//    				if(getTotalExternalMemorySize(sdcardPaths[1]) == 0) {
//    					return getTotalExternalMemorySize(sdcardPaths[0]);
//    				//存在外置sd卡
//    				} else {
//    					return getTotalExternalMemorySize(sdcardPaths[1]);
//    				}
//    			} else {
//    				return getTotalExternalMemorySize(sdcardPaths[0]);
//    			}
//    		} else {
//    			return 0;
//    		}
//    	}
//    }
//
//    /**
//     * 获取内置SD卡已用大小
//     * @return
//     */
//    public static long getSysSdcardUsedSize(Context context) {
//    	String sdcardPath = getSdcardPath();
//    	//没有sd卡
//    	if(TextUtils.isEmpty(sdcardPath)) {
//    		return 0;
//    	} else {
//    		String[] sdcardPaths = getExternalPath(context);
//    		//只有外置sd卡
//    		if(sdcardPaths.length == 1) {
//    			return 0;
//    		//有内置和外置sd卡
//    		} else if(sdcardPaths.length == 2) {
//    			//小米手机
//    			if("Xiaomi".equalsIgnoreCase(PhoneInfo.getBuildManufacturer())) {
//    				//说明没有外置sd卡，因为小米当有外置sd卡时sdcardPaths[0]返回就是外置sd卡的路径，当没有外置sd卡时
//    				//返回的就是内置sd卡的路径，这时候外置sd卡路径的size就是0,
//    				if(getTotalExternalMemorySize(sdcardPaths[1]) == 0) {
//    					return getTotalExternalMemorySize(sdcardPaths[0]) - getAvailableExternalMemorySize(sdcardPaths[0]);
//    				//存在外置sd卡
//    				} else {
//    					return getTotalExternalMemorySize(sdcardPaths[1]) - getAvailableExternalMemorySize(sdcardPaths[1]);
//    				}
//    			} else {
//    				return getTotalExternalMemorySize(sdcardPaths[0]) - getAvailableExternalMemorySize(sdcardPaths[0]);
//    			}
//    		} else {
//    			return 0;
//    		}
//    	}
//    }
////
//    /**
//     * 获取外置SD卡已用大小
//     * @return
//     */
//    public static long getExtSdcardSize(Context context) {
//    	String sdcardPath = getSdcardPath();
//    	//没有sd卡
//    	if(TextUtils.isEmpty(sdcardPath)) {
//    		return 0;
//    	} else {
//    		String[] sdcardPaths = getExternalPath(context);
//    		//只有外置sd卡
//    		if(sdcardPaths.length == 1) {
//    			return getSdcardSize();
//    		//有内置和外置sd卡
//    		} else if(sdcardPaths.length == 2) {
//    			//小米手机
//    			if("Xiaomi".equalsIgnoreCase(PhoneInfo.getBuildManufacturer())) {
//    				//说明没有外置sd卡，因为小米当有外置sd卡时sdcardPaths[0]返回就是外置sd卡的路径，当没有外置sd卡时
//    				//返回的就是内置sd卡的路径，这时候外置sd卡路径的size就是0,
//    				if(getTotalExternalMemorySize(sdcardPaths[1]) == 0) {
//    					return 0;
//    				//存在外置sd卡
//    				} else {
//    					return getTotalExternalMemorySize(sdcardPaths[0]);
//    				}
//    			} else {
//    				return getTotalExternalMemorySize(sdcardPaths[1]);
//    			}
//    		} else {
//    			return 0;
//    		}
//    	}
//    }
////
//    /**
//     * 获取外置SD卡已用大小
//     * @return
//     */
//    public static long getExtSdcardUsedSize(Context context) {
//    	String sdcardPath = getSdcardPath();
//    	//没有sd卡
//    	if(TextUtils.isEmpty(sdcardPath)) {
//    		return 0;
//    	} else {
//    		String[] sdcardPaths = getExternalPath(context);
//    		//只有外置sd卡
//    		if(sdcardPaths.length == 1) {
//    			return getSdcardSize() - getAvailableExternalMemorySize(sdcardPaths[0]);
//    		//有内置和外置sd卡
//    		} else if(sdcardPaths.length == 2) {
//    			//小米手机
//    			if("Xiaomi".equalsIgnoreCase(PhoneInfo.getBuildManufacturer())) {
//    				//说明没有外置sd卡，因为小米当有外置sd卡时sdcardPaths[0]返回就是外置sd卡的路径，当没有外置sd卡时
//    				//返回的就是内置sd卡的路径，这时候外置sd卡路径的size就是0,
//    				if(getTotalExternalMemorySize(sdcardPaths[1]) == 0) {
//    					return 0;
//    				//存在外置sd卡
//    				} else {
//    					return getTotalExternalMemorySize(sdcardPaths[0]) - getAvailableExternalMemorySize(sdcardPaths[0]);
//    				}
//    			} else {
//    				return getTotalExternalMemorySize(sdcardPaths[1]) - getAvailableExternalMemorySize(sdcardPaths[1]);
//    			}
//    		} else {
//    			return 0;
//    		}
//    	}
//    }
    
}

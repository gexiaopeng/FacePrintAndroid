package com.baidu.aip.fp.utils;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class ImageSaveUtil {


    public static final String SDCARD_PATH = Environment.getExternalStorageDirectory()
            .getAbsolutePath() + File.separator + "facesharp" + File.separator;


    /**
     * 存储照片
     *
     * @param context
     * @param image
     * @param filename
     */
    public static boolean saveBitmap(Context context, Bitmap image, String filename) {
        boolean saved = false;
        if (isMemeryOk(context)) {
            FileOutputStream fileOS = null;
            try {
                File file = new File(context.getFilesDir(), filename);
                if (file.exists()) {
                    file.delete();
                }
                fileOS = new FileOutputStream(file);
                Uri.fromFile(file);
                image.compress(Bitmap.CompressFormat.JPEG, 100, fileOS);
                file.getAbsolutePath();
                saved = true;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fileOS != null) {
                    try {
                        fileOS.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        } else {
            saveBitmapToSDCard(context, image, filename);
            saved = true;
        }
        return saved;
    }

    public static Uri saveBitmapToSDCard(Context context, Bitmap image, String filename) {
        Uri uri = null;
        FileOutputStream fileOS = null;
        String dir = SDCARD_PATH + DeviceUtil.getImei(context);
        try {
            File path = new File(dir);
            if (!path.exists()) {
                File filePhone = new File(context.getFilesDir(), filename);
                fileOS = new FileOutputStream(filePhone);
                uri = Uri.fromFile(filePhone);
                image.compress(Bitmap.CompressFormat.JPEG, 80, fileOS);

            } else {
                File file = new File(dir, filename);
                fileOS = new FileOutputStream(file);
                uri = Uri.fromFile(file);
                image.compress(Bitmap.CompressFormat.JPEG, 80, fileOS);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileOS != null) {
                try {
                    fileOS.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return uri;
    }

    public static boolean deleteBmpFromSDCard(Context context, String filename) {
        boolean deleted = false;
        try {
            File file = new File(context.getFilesDir(), filename);
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            String filePath = SDCARD_PATH + DeviceUtil.getImei(context) + File.separator + filename;
            File fileSdCard = new File(filePath);
            if (fileSdCard.isFile() && fileSdCard.exists()) {
                return fileSdCard.delete();
            }
            return deleted;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return deleted;
    }


    public static String appendFaceImageFullPath(Context context, String uuid) {
        String fullPath = context.getFilesDir() + File.separator + uuid;
        File file = new File(fullPath);
        if (!file.exists()) {
            fullPath = SDCARD_PATH + DeviceUtil.getImei(context) + File.separator + uuid;
        }
        return fullPath;
    }


    /**
     * 检查sdcard写状态 只能main线程调用
     *
     * @param context
     */
    public static boolean isSDCardOk(Context context) {

        boolean canWrite;
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            canWrite = true;
        } else {
            canWrite = false;
        }
        return canWrite;
    }

    public static Bitmap rotateBitmap(int angle, Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        // 旋转图片 动作
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        if (resizedBitmap != bitmap && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
        return resizedBitmap;
    }


    public static String saveCameraBitmap(Context context, Bitmap image, String filename) {
        if (image == null) {
            return "";
        }
        String fullPath = "";
        FileOutputStream fileOS = null;
        try {
            if (isMemeryOk(context)) {
                File file = new File(context.getFilesDir(), filename);
                if (file.exists()) {
                    file.delete();
                }
                fileOS = new FileOutputStream(file);
                Uri.fromFile(file);
                image.compress(Bitmap.CompressFormat.JPEG, 100, fileOS);
                fullPath = file.getAbsolutePath();
            } else {
                String dir = SDCARD_PATH + DeviceUtil.getImei(context);
                File file = new File(dir, filename);
                if (file.exists()) {
                    file.delete();
                }
                fileOS = new FileOutputStream(file);
                Uri.fromFile(file);
                image.compress(Bitmap.CompressFormat.JPEG, 100, fileOS);
                fullPath = file.getAbsolutePath();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileOS != null) {
                try {
                    fileOS.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return fullPath;
    }

    public static String loadCameraTempPath(Context context, String filename) {
        String filePath = context.getFilesDir() + File.separator + filename;
        File file = new File(filePath);
        if (!file.exists()) {
            filePath = SDCARD_PATH + DeviceUtil.getImei(context) + File.separator + filename;
        }
        return filePath;
    }


    public static Bitmap loadCameraBitmap(Context context, String filename) {
        Bitmap image = null;
        String filePath = context.getFilesDir() + File.separator + filename;
        File path = new File(filePath);
        if (!path.exists()) {
            filePath = SDCARD_PATH + DeviceUtil.getImei(context) + File.separator + filename;
        }

        File file = new File(filePath);
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inPreferredConfig = Bitmap.Config.RGB_565;
            image = BitmapFactory.decodeStream(is, null, opts);

        } catch (java.io.FileNotFoundException e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return image;
    }

    public static Bitmap loadBitmapFromPath(Context context, String filePath) {
        Bitmap image = null;
        File path = new File(filePath);
        if (!path.exists()) {
            return null;
        }

        InputStream is = null;
        try {
            is = new FileInputStream(path);
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inPreferredConfig = Bitmap.Config.RGB_565;
            image = BitmapFactory.decodeStream(is, null, opts);

        } catch (java.io.FileNotFoundException e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return image;
    }

    public static boolean isMemeryOk(Context context) { // 获取android当前可用内存大小

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        MemoryInfo mi = new MemoryInfo();
        am.getMemoryInfo(mi);
        // mi.availMem; 当前系统的可用内存
        long total = mi.availMem;

        return total > 9999 ? true : false;
    }

    public static boolean readSdCardPermission(Activity activity, int requestCode) {
        if (!isMemeryOk(activity)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    && ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        requestCode);
                // dialog.dismiss();
                return true;
            }
        }
        return true;
    }


}
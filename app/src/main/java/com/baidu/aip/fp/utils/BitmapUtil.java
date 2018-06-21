package com.baidu.aip.fp.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.FileOutputStream;
import java.io.InputStream;

public class BitmapUtil {
    static String TAG = "BitmapUtil";

    public static Bitmap getBitmap(String imageName, Context context) {
        try {
            AssetManager assetManager = context.getAssets();
            InputStream is = assetManager.open(imageName);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap rotateBitmap(Bitmap original, float degrees) {
        int width = original.getWidth();
        int height = original.getHeight();

        Matrix matrix = new Matrix();
        matrix.preRotate(degrees);
        return Bitmap.createBitmap(original, 0, 0, width, height, matrix, true);
    }

    public static Bitmap getGalleryBmp(Context context, Uri uri) {
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public static String getRealPathFromURI(Context context, Uri contentURI) {
        String result;
        Cursor cursor = context.getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    public static Bitmap cropOriginFace(Context context, Uri uri) {
        Bitmap bitmap = getGalleryBmp(context, uri);
        String realPath = getRealPathFromURI(context, uri);
        if (bitmap == null) {
            bitmap = BitmapFactory.decodeFile(realPath);
        }
        final int rotation = getOrientation(context, uri);
        if (rotation != 0) {
            bitmap = rotateBitmap(bitmap, rotation);
        }
        return bitmap;
    }

    public static Bitmap getFromGallery(Context context, Uri uri) {
        Bitmap bitmap = getGalleryBmp(context, uri);

        String realPath = getRealPathFromURI(context, uri);
        if (bitmap == null) {
            bitmap = BitmapFactory.decodeFile(realPath);
        }
        final int rotation = getOrientation(context, uri);
        if (rotation != 0) {
            bitmap = rotateBitmap(bitmap, rotation);
        }
        return bitmap;
    }


    public static int getOrientation(Context context, Uri photoUri) {
        int orientation = 0;
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(photoUri,
                    new String[]{MediaStore.Images.ImageColumns.ORIENTATION}, null, null, null);
            if (cursor != null) {
                if (cursor.getCount() != 1) {
                    return -1;
                }
                cursor.moveToFirst();
                orientation = cursor.getInt(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return orientation;
    }

    public static void saveBitmap(String outputPath, Bitmap bitmap) {
        // save image
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(outputPath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param context
     * @param bitmap
     * @return
     */
    public static Bitmap loadZoomBitmap(Context context, Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        int bmpHeight = bitmap.getHeight();
        int bmpWidth = bitmap.getWidth();
        int newHeight;
        int zoom;
        if (bmpWidth > 200) {
            zoom = bmpWidth / 200;
            newHeight = bmpHeight / zoom;
        } else {
            zoom = 1;
            newHeight = bmpHeight;
        }
        if (zoom <= 1) {
            return bitmap;
        }
        Bitmap bm = Bitmap.createScaledBitmap(bitmap, 200, newHeight, true);
        if (bitmap != null) {
            bitmap.recycle();
        }
        return bm;
    }
}
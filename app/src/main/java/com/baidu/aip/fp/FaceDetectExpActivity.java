package com.baidu.aip.fp;

import android.graphics.Bitmap;
import android.os.Bundle;

import com.baidu.aip.fp.utils.ImageSaveUtil;
import com.baidu.idl.face.platform.FaceStatusEnum;
import com.baidu.idl.face.platform.ui.FaceDetectActivity;

import java.util.HashMap;

import android.widget.Toast;


public class FaceDetectExpActivity extends FaceDetectActivity {

    public static final String BEST_IMG = "best_head.jpg";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDetectCompletion(FaceStatusEnum status, String message, HashMap<String, String> base64ImageMap) {
        super.onDetectCompletion(status, message, base64ImageMap);
        if (status == FaceStatusEnum.OK && mIsCompletion) {
            // showMessageDialog("人脸图像采集", "采集成功");
            Toast.makeText(this, "采集成功", Toast.LENGTH_SHORT).show();
            saveImage(base64ImageMap);
            setResult(RESULT_OK);
            finish();
        } else if (status == FaceStatusEnum.Error_DetectTimeout
                || status == FaceStatusEnum.Error_LivenessTimeout
                || status == FaceStatusEnum.Error_Timeout) {
            //  showMessageDialog("人脸图像采集", "采集超时");
            Toast.makeText(this, "采集超时", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveImage(HashMap<String, String> imageMap) {
        String bestimageBase64 = imageMap.get("bestImage0");
        Bitmap bmp = base64ToBitmap(bestimageBase64);
        ImageSaveUtil.saveCameraBitmap(this, bmp, BEST_IMG);

    }

    @Override
    public void finish() {
        super.finish();
    }

}

/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.aip.fp;

import java.io.File;

import com.baidu.aip.fp.utils.FileUtil;
import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;
import com.baidu.ocr.sdk.model.IDCardParams;
import com.baidu.ocr.sdk.model.IDCardResult;
import com.baidu.ocr.sdk.model.Word;
import com.baidu.ocr.ui.camera.CameraActivity;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.shinesun.face.R;
public class IdCardActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int REQUEST_CODE_CAMERA = 102;
    private static final int REQUEST_CODE_PICK_IMAGE = 100;
    private static final int PERMISSIONS_REQUEST_CAMERA = 800;
    private static final int PERMISSIONS_EXTERNAL_STORAGE = 801;

    private Button mannulInputBtn;
    private Button scanInputBtn;
    private Button nextBtn;
    private EditText usernameEt;
    private EditText idcardEt;
    private TextView tipTv;

    private String username = "";
    private String idnumber = "";

    private AlertDialog.Builder alertDialog;
    private boolean mannulInput = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_idcard_layout);
        alertDialog = new AlertDialog.Builder(this);
        findView();
        addListener();
        // 初始化OCR SDK 使用的license是aip.license，名字不能修改
        initOCRSDK();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(IdCardActivity.this,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSIONS_REQUEST_CAMERA);
            return;
        }
    }

    private void findView() {
        mannulInputBtn = (Button) findViewById(R.id.mannul_input_btn);
        scanInputBtn = (Button) findViewById(R.id.scan_input_btn);
        nextBtn = (Button) findViewById(R.id.next_btn);
        usernameEt = (EditText) findViewById(R.id.username_et);
        idcardEt = (EditText) findViewById(R.id.idnumber_et);
        tipTv = (TextView) findViewById(R.id.tip_tv);
    }

    private void addListener() {
        mannulInputBtn.setOnClickListener(this);
        scanInputBtn.setOnClickListener(this);
        nextBtn.setOnClickListener(this);
    }

    private void initOCRSDK() {
        OCR.getInstance().initAccessToken(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken result) {
                // 调用成功，返回AccessToken对象
                String token = result.getAccessToken();
                displayToastTip("accesstoken->" + token);
            }

            @Override
            public void onError(OCRError error) {
                // 调用失败，返回OCRError子类SDKError对象
                displayToastTip(error.getMessage());
            }
        }, getApplicationContext());
    }

    @Override
    public void onClick(View v) {

        // 控制是否手动输入身份证号和姓名
        if (v == mannulInputBtn) {
            usernameEt.setVisibility(usernameEt.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
            idcardEt.setVisibility(idcardEt.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);

            mannulInput = (usernameEt.getVisibility() == View.VISIBLE);
            scanInputBtn.setVisibility(mannulInput ? View.GONE : View.VISIBLE);
            nextBtn.setVisibility(mannulInput ? View.VISIBLE : View.GONE);

        } else if (v == scanInputBtn) {
            AccessToken accessToken = OCR.getInstance().getAccessToken();
            if (accessToken == null || TextUtils.isEmpty(accessToken.getAccessToken())) {
                initOCRSDK();
                Toast.makeText(this, "OCR token 正在拉取，请稍后再试 ", Toast.LENGTH_SHORT).show();
                return;
            }
            // 扫描输入

            // 生成intent对象
            Intent intent = new Intent(IdCardActivity.this, CameraActivity.class);
            // 设置临时存储
            intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH,
                    FileUtil.getSaveFile(getApplication()).getAbsolutePath());

            // 调用拍摄身份证正面的activity
            intent.putExtra(CameraActivity.KEY_CONTENT_TYPE, CameraActivity.CONTENT_TYPE_ID_CARD_FRONT);
            intent.putExtra(CameraActivity.KEY_NATIVE_TOKEN, OCR.getInstance().getLicense());
            intent.putExtra(CameraActivity.KEY_NATIVE_ENABLE, true);
            startActivityForResult(intent, REQUEST_CODE_CAMERA);
        } else if (v == nextBtn) {
            if (mannulInput) {
                username = usernameEt.getText().toString();
                idnumber = idcardEt.getText().toString();
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(IdCardActivity.this,
                        new String[]{Manifest.permission.CAMERA},
                        PERMISSIONS_REQUEST_CAMERA);
                return;
            }

            jumpToOnlineVerify();
        }
    }

    // 身份证识别成功后跳转到人脸离线活体检测
    private void jumpToOnlineVerify() {

        if (TextUtils.isEmpty(username)) {
            Toast.makeText(IdCardActivity.this, "用户名不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(idnumber)) {
            Toast.makeText(IdCardActivity.this, "身份证不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        // TODO 身份证号码合法校验，长度，字母数字

        // 调转到活体识别界面
        Intent faceIntent = new Intent(IdCardActivity.this, FaceOnlineVerifyActivity.class);
        faceIntent.putExtra("username", username);
        faceIntent.putExtra("idnumber", idnumber);
        startActivity(faceIntent);
    }

    /**
     * 识别身份证
     *
     * @param idCardSide
     * @param filePath
     */
    private void recIDCard(String idCardSide, String filePath) {
        displayTip("识别中...");
        IDCardParams param = new IDCardParams();
        param.setImageFile(new File(filePath));
        param.setIdCardSide(idCardSide);
        param.setDetectDirection(true);
        OCR.getInstance().recognizeIDCard(param, new OnResultListener<IDCardResult>() {
            @Override
            public void onResult(IDCardResult result) {
                if (result != null) {
                    Word idnumberWord = result.getIdNumber();
                    Word nameWord = result.getName();
                    if (idnumberWord != null) {
                        idnumber = idnumberWord.getWords();
                    }
                    if (nameWord != null) {
                        username = nameWord.getWords();
                    }

                    alertText("识别结果", "idnumber->" + idnumber + " name->" + username);
                }
                displayTip("");
            }

            @Override
            public void onError(OCRError error) {
                alertText("识别结果", error.getMessage());
                displayTip("");
            }
        });
    }

    private void alertText(final String title, final String message) {

        alertDialog.setTitle(title)
                .setMessage(message)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 身份证识别成功后跳转到人脸离线活体检测
                        jumpToOnlineVerify();
                    }
                }).show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_CAMERA && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                String contentType = data.getStringExtra(CameraActivity.KEY_CONTENT_TYPE);
                String filePath = FileUtil.getSaveFile(getApplicationContext()).getAbsolutePath();
                if (!TextUtils.isEmpty(contentType)) {
                    if (CameraActivity.CONTENT_TYPE_ID_CARD_FRONT.equals(contentType)) {
                        recIDCard(IDCardParams.ID_CARD_SIDE_FRONT, filePath);
                    } else if (CameraActivity.CONTENT_TYPE_ID_CARD_BACK.equals(contentType)) {
                        recIDCard(IDCardParams.ID_CARD_SIDE_BACK, filePath);
                    }
                }
            }
        }
    }

    private void displayTip(final String tip) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tipTv.setText(tip);
            }
        });
    }

    private void displayToastTip(final String tip) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(IdCardActivity.this, tip, Toast.LENGTH_SHORT).show();
            }
        });
    }
}

package com.baidu.aip.fp;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.aip.fp.exception.FaceException;
import com.baidu.aip.fp.model.ResponseResult;
import com.baidu.aip.fp.utils.BitmapUtil;
import com.baidu.aip.fp.utils.ImageSaveUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.UUID;


import com.shinesun.face.R;


/**
 * compare 人脸对比
 */

public class CompareActivity extends BaseActivity implements View.OnClickListener {

    public static final String REGISTER_NAME = "EXTRA_REG_NAME";

    private static final int PHOTO_REQUEST_GALLERY = 0; // 从相册中选择
    private static final int REQUEST_TRACK_FACE = 1;  // 实时采集
    private Bitmap mFirstBitmap;
    private Bitmap mSecondBitmap;
    private ImageView mImageView1;
    private ImageView mImageView2;
    private Button mCompareBtn;
    private Button mSelBtn;
    private Button mCollectBtn;
    private TextView mScoreTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compare);
        initView();
    }

    private void initView() {

        mImageView1 = (ImageView) findViewById(R.id.imageIv1);
        mImageView2 = (ImageView) findViewById(R.id.imageIv2);

        mFirstBitmap = BitmapUtil.getBitmap("ic_first.jpg", this);
        if (mFirstBitmap != null) {
            mImageView1.setImageBitmap(mFirstBitmap);
        }
        mSecondBitmap = BitmapUtil.getBitmap("ic_second.jpg", this);
        if (mSecondBitmap != null) {
            mImageView2.setImageBitmap(mSecondBitmap);
        }

        mCompareBtn = (Button) findViewById(R.id.compareBtn);
        mCompareBtn.setOnClickListener(this);

        mScoreTv = (TextView) findViewById(R.id.scoreTv);
        mScoreTv.setVisibility(View.GONE);
        mSelBtn = (Button) findViewById(R.id.selBtn);
        mCollectBtn = (Button) findViewById(R.id.collectBtn);
        mSelBtn.setOnClickListener(this);
        mCollectBtn.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.compareBtn:
                faceCompare(mFirstBitmap, mSecondBitmap);
                break;
            case R.id.selBtn:
                chooseFromGallery();
                break;
            case R.id.collectBtn:
                trackFace();
                break;
            default:
                break;
        }
    }

    private void trackFace() {
        Intent itTrack = new Intent(CompareActivity.this, FaceDetectExpActivity.class);
        startActivityForResult(itTrack, REQUEST_TRACK_FACE);
    }

    private void faceCompare(final Bitmap bitmap1, final Bitmap bitmap2) {
        File file1 = new File(getFilesDir(), UUID.randomUUID().toString() + "c1" + ".jpg");
        BitmapUtil.saveBitmap(file1.getAbsolutePath(), bitmap1);

        File file2 = new File(getFilesDir(), UUID.randomUUID().toString() + "c2" + ".jpg");
        BitmapUtil.saveBitmap(file2.getAbsolutePath(), bitmap2);

        APIService.getInstance().faceCompare(new OnResultListener<ResponseResult>() {
            @Override
            public void onResult(ResponseResult result) {

                if (result != null) {
                    boolean isSame = parseResult(result.getJsonRes());
                    Log.d("CompareActivity", result.getJsonRes());
                } else {
                    mScoreTv.setVisibility(View.GONE);
                }

            }

            @Override
            public void onError(FaceException error) {
                mScoreTv.setVisibility(View.GONE);

                if (error != null && !TextUtils.isEmpty(error.getErrorMessage())) {
                    Toast.makeText(CompareActivity.this, error.getErrorMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CompareActivity.this, "人脸比对失败", Toast.LENGTH_SHORT).show();
                }


            }
        }, file1, file2);
    }


    private boolean parseResult(String result) {
        boolean isSame = false;
        if (TextUtils.isEmpty(result)) {
            mScoreTv.setVisibility(View.GONE);
            return isSame;
        }
        JSONObject obj = null;
        try {
            obj = new JSONObject(result);
            JSONObject resObj = obj.optJSONObject("result");
            if (resObj != null) {
                double score = resObj.getDouble("score");
                String str = getDecimalString(score);
                mScoreTv.setVisibility(View.VISIBLE);
                mScoreTv.setText("相似度分值:" + str);
                Log.d("CompareActivity", "score is:" + score);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return isSame;
    }

    private String getDecimalString(double f) {
        int n = String.valueOf(f).indexOf('.');
        String s = String.valueOf(f);
        if (f > 0) {
            if (n > 0 && n < (s.length() - 2)) {
                s = s.substring(0, n + 2);
            }
        } else if (f < 0) {
            if (n > 0 && n < (s.length() - 3)) {
                s = s.substring(0, n + 3);
            }
        }
        return s;
    }

    /**
     * 从相册选择图片
     */
    private void chooseFromGallery() {
        // 激活系统图库，选择一张图片
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        // 开启一个带有返回值的Activity，请求码为PHOTO_REQUEST_GALLERY
        startActivityForResult(intent, PHOTO_REQUEST_GALLERY);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PHOTO_REQUEST_GALLERY) {
            // 从相册返回的数据
            if (data != null) {
                Cursor cursor = null;
                boolean isException = false;
                Uri selectedImage = null;
                try {
                    selectedImage = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    cursor = getContentResolver().query(selectedImage,
                            filePathColumn, null, null, null); // 从系统表中查询指定Uri对应的照片
                    cursor.moveToFirst();
                    Bitmap cropBmp = BitmapUtil.getFromGallery(this, selectedImage);
                    Bitmap zoomBmp = BitmapUtil.loadZoomBitmap(this, cropBmp);
                    if (zoomBmp != null) {
                        mImageView1.setVisibility(View.VISIBLE);
                        mImageView1.setImageBitmap(zoomBmp);
                        mFirstBitmap = zoomBmp;

                    } else {
                        Toast.makeText(CompareActivity.this, "图片选择失败", Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    // TODO Auto-generatedcatch block
                    e.printStackTrace();
                    isException = true;
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
                if (isException) {
                    Toast.makeText(CompareActivity.this, "图片选择失败", Toast.LENGTH_SHORT).show();
                }
            }

        } else if (requestCode == REQUEST_TRACK_FACE) {

            if (resultCode == RESULT_OK) {
                Bitmap bmp = ImageSaveUtil.loadCameraBitmap(this, FaceDetectExpActivity.BEST_IMG);

                mImageView2.setVisibility(View.VISIBLE);
                mImageView2.setImageBitmap(bmp);
                mSecondBitmap = bmp;
            } else {
                Toast.makeText(CompareActivity.this, "采集图片失败", Toast.LENGTH_SHORT).show();
            }

        }
        super.onActivityResult(requestCode, resultCode, data);

    }


}
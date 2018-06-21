/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.aip.fp;

import java.io.File;

import com.baidu.aip.fp.model.AccessToken;
import com.baidu.aip.fp.model.DynamicParams;
import com.baidu.aip.fp.model.LivenessVsIdcardResult;
import com.baidu.aip.fp.model.MatchModel;
import com.baidu.aip.fp.parser.RegResultParser;
import com.baidu.aip.fp.utils.HttpUtil;
import com.baidu.aip.fp.utils.PoliceCheckResultParser;

import android.content.Context;
import android.util.Base64;

import static com.baidu.aip.fp.utils.Base64RequestBody.readFile;

/**
 * Created by wangtianfei01 on 17/7/13.
 */

public class APIService {


    private static final String ACCESS_TOEKN_URL = "https://aip.baidubce.com/oauth/2.0/token?";
    // 公安接口
    private static final String LIVENESS_VS_IDCARD_URL = "https://aip.baidubce.com/rest/2.0/face/v3/person/verify";

    private static final String FACE_COMPARE = "https://aip.baidubce.com/rest/2.0/face/v3/match";

    private String accessToken;

    private APIService() {

    }

    private static volatile APIService instance;

    public static APIService getInstance() {
        if (instance == null) {
            synchronized (APIService.class) {
                if (instance == null) {
                    instance = new APIService();
                }
            }
        }
        return instance;
    }

    public void init(Context context) {
        HttpUtil.getInstance().init();
    }

    /**
     * 设置accessToken 如何获取 accessToken 详情见:
     *
     * @param accessToken accessToken
     */
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    /**
     * 明文aksk获取token
     *
     * @param listener
     * @param ak
     * @param sk
     */
    public void initAccessTokenWithAkSk(final OnResultListener<AccessToken> listener, String ak,
                                        String sk) {

        StringBuilder sb = new StringBuilder();
        sb.append("client_id=").append(ak);
        sb.append("&client_secret=").append(sk);
        sb.append("&grant_type=client_credentials");
        HttpUtil.getInstance().getAccessToken(listener, ACCESS_TOEKN_URL, sb.toString());

    }


    public void policeVerify(String name, String idnumber, String file, OnResultListener<LivenessVsIdcardResult>
            listener) {
        DynamicParams params = new DynamicParams();

        String base64Img = "";
        try {
            byte[] buf = readFile(file);

            base64Img = new String(Base64.encode(buf, Base64.NO_WRAP));

        } catch (Exception e) {
            e.printStackTrace();
        }
        params.setImgType("BASE64");
        params.setBase64Img(base64Img);

        params.putParam("name", name);
        params.putParam("id_card_number", idnumber);
        // 参数可以根据实际业务情况灵活调节
        params.setQualityControl("NONE");
        params.setLivenessControl("NORMAL");

        PoliceCheckResultParser parser = new PoliceCheckResultParser();

        HttpUtil.getInstance().post(urlAppendCommonParams(LIVENESS_VS_IDCARD_URL), "image", params, parser, listener);
    }

    /**
     * URL append access token，sdkversion，aipdevid
     *
     * @param url
     * @return
     */
    private String urlAppendCommonParams(String url) {
        StringBuilder sb = new StringBuilder(url);
        sb.append("?access_token=").append(accessToken);

        return sb.toString();
    }

    /**
     * 人脸比较
     *
     * @param listener 回调
     * @param file1    人脸图片文件
     * @param file2    人脸图片文件
     */
    public void faceCompare(OnResultListener listener, File file1, File file2) {
        DynamicParams params = new DynamicParams();
        String jsonParams = MatchModel.getJson(file1, file2);
        params.setJsonParams(jsonParams);
        //  WriteFile.writeString(jsonParams);
        RegResultParser parser = new RegResultParser();
        HttpUtil.getInstance().post(urlAppendCommonParams(FACE_COMPARE),
                "images", params, parser, listener);
    }

}

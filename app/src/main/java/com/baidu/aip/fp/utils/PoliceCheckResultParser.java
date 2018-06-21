/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.aip.fp.utils;

import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.aip.fp.exception.FaceException;
import com.baidu.aip.fp.model.LivenessVsIdcardResult;
import com.baidu.ocr.sdk.exception.OCRError;

import android.util.Log;

public class PoliceCheckResultParser implements Parser<LivenessVsIdcardResult> {

    @Override
    public LivenessVsIdcardResult parse(String json) throws FaceException {

        Log.i("PoliceCheckResultParser", "LivenessVsIdcardResult->" + json);
        try {
            JSONObject jsonObject = new JSONObject(json);

            if (jsonObject.has("error_code")) {
                FaceException error = new FaceException(jsonObject.optInt("error_code"),
                        jsonObject.optString("error_msg"));
                if (error.getErrorCode() != 0) {
                    throw error;
                }
            }

            LivenessVsIdcardResult result = new LivenessVsIdcardResult();
            result.setLogId(jsonObject.optLong("log_id"));
            result.setJsonRes(json);

            JSONObject resultObject = jsonObject.optJSONObject("result");
            if (resultObject != null) {
                double score = resultObject.optDouble("score");
                result.setScore(score);
            }

            return result;
        } catch (JSONException e) {
            e.printStackTrace();
            FaceException error = new FaceException(FaceException.ErrorCode.JSON_PARSE_ERROR,
                    "Json parse error:" + json, e);
            throw error;
        }
    }
}

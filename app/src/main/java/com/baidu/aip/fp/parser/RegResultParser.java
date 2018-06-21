package com.baidu.aip.fp.parser;

import org.json.JSONException;
import org.json.JSONObject;


import android.util.Log;

import com.baidu.aip.fp.exception.FaceException;
import com.baidu.aip.fp.model.ResponseResult;
import com.baidu.aip.fp.utils.Parser;

public class RegResultParser implements Parser<ResponseResult> {


    @Override
    public ResponseResult parse(String json) throws FaceException {
        Log.e("xx", "oarse:" + json);
        try {
            JSONObject jsonObject = new JSONObject(json);

            if (jsonObject.has("error_code")) {
                FaceException error = new FaceException(jsonObject.optInt("error_code"),
                        jsonObject.optString("error_msg"));
                if (error.getErrorCode() != 0) {
                    throw error;
                }
            }

            ResponseResult result = new ResponseResult();
            result.setLogId(jsonObject.optLong("log_id"));
            result.setJsonRes(json);

            return result;
        } catch (JSONException e) {
            e.printStackTrace();
            FaceException error = new FaceException(FaceException.ErrorCode.JSON_PARSE_ERROR,
                    "Json parse error:" + json, e);
            throw error;
        }
    }
}
/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.aip.fp.utils;

import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.aip.fp.exception.FaceException;
import com.baidu.aip.fp.model.AccessToken;
import com.baidu.ocr.sdk.exception.OCRError;

public class AccessTokenParser implements Parser<AccessToken> {
    @Override
    public AccessToken parse(String json) throws FaceException {
        try {
            AccessToken accessToken = new AccessToken();
            accessToken.setJson(json);
            JSONObject jsonObject = new JSONObject(json);

            if (jsonObject != null) {

                accessToken.setAccessToken(jsonObject.optString("access_token"));
                accessToken.setExpiresIn(jsonObject.optInt("expires_in"));
                return accessToken;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            FaceException error = new FaceException(FaceException.ErrorCode.JSON_PARSE_ERROR, "Json parse error", e);
            throw error;
        }
        return null;
    }
}

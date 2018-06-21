package com.baidu.aip.fp.model;

import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import static com.baidu.aip.fp.utils.Base64RequestBody.readFile;

/**
 * 构建人脸比较json参数
 */
public class MatchModel {

    public static String getJson(File file1, File file2) {
        JSONObject obj1 = getMatchObj(file1);
        JSONObject obj2 = getMatchObj(file2);
        return getJson(obj1, obj2);
    }

    public static String getJson(JSONObject obj1, JSONObject obj2) {
        JSONArray array = new JSONArray();
        array.put(obj1);
        array.put(obj2);
        return array.toString();
    }

    public static JSONObject getMatchObj(File file) {
        JSONObject obj = new JSONObject();
        try {
            String base64Img = "";
            try {
                byte[] buf = readFile(file);
                base64Img = new String(Base64.encode(buf, Base64.NO_WRAP));

            } catch (Exception e) {
                e.printStackTrace();
            }
            obj.put("image", base64Img);
            obj.put("image_type", "BASE64");
            obj.put("face_type", "LIVE");
            // 活体及质量分数可根据自己实际情况设置
            obj.put("quality_control", "NORMAL");
            // obj.put("liveness_control", "NORMAL");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }


}
package com.carlShen.jsbridge.library;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by shenqiang on 16/8/5.
 */
public class FMJSBridgeMessage {

    private final static String OBJ = "obj";
    private final static String METHOD = "method";
    private final static String NATIVE_FUNCTION_ARGS_DATA = "nativeFunctionArgsData";
    private final static String CALLBACK_ID = "callbackId";
    private final static String JS_FUNCTION_ARGS_DATA = "jsFunctionArgsData";

    @SerializedName(OBJ)
    private String obj;

    @SerializedName(METHOD)
    private String method;



    @SerializedName(NATIVE_FUNCTION_ARGS_DATA)
    private String nativeFunctionArgsData;


    @SerializedName(JS_FUNCTION_ARGS_DATA)
    private Object jsFunctionArgsData;

    @SerializedName(CALLBACK_ID)
    private String callbackId;


    public String getObj() {
        return obj;
    }

    public void setObj(String obj) {
        this.obj = obj;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getNativeFunctionArgsData() {
        return nativeFunctionArgsData;
    }

    public void setNativeFunctionArgsData(String nativeFunctionArgsData) {
        this.nativeFunctionArgsData = nativeFunctionArgsData;
    }


    public Object getJsFunctionArgsData() {
        return jsFunctionArgsData;
    }

    public void setJsFunctionArgsData(Object jsFunctionArgsData) {
        this.jsFunctionArgsData = jsFunctionArgsData;
    }


    public String getCallbackId() {
        return callbackId;
    }

    public void setCallbackId(String callbackId) {
        this.callbackId = callbackId;
    }





    public String toJson() {
        Gson gson = new Gson();
       return gson.toJson(this);
    }

    public static FMJSBridgeMessage toObject(String jsonStr) {

        Gson gson = new Gson();

        FMJSBridgeMessage message = gson.fromJson(jsonStr, FMJSBridgeMessage.class);

        return message;
    }

    public static List<FMJSBridgeMessage> toArrayList(String jsonStr)  {
        List<FMJSBridgeMessage> messages = new ArrayList<FMJSBridgeMessage>();
        try {

            JSONArray jsonArray = new JSONArray(jsonStr);
            for (int i = 0; i <jsonArray.length() ; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                FMJSBridgeMessage message = new FMJSBridgeMessage();
                message.setObj(jsonObject.optString(OBJ, null));
                message.setMethod(jsonObject.optString(METHOD, null));
                message.setNativeFunctionArgsData(jsonObject.optString(NATIVE_FUNCTION_ARGS_DATA, null));
                message.setCallbackId(jsonObject.optString(CALLBACK_ID, null));
                messages.add(message);
            }

        } catch (JSONException e) {

        }
        return messages;
    }


}

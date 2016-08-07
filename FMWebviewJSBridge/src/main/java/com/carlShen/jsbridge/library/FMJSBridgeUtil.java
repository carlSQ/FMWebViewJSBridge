package com.carlShen.jsbridge.library;


import android.content.Context;
import android.webkit.WebView;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by shenqiang on 16/8/3.
 */
public class FMJSBridgeUtil {

    final static String FM_LOAD_JS = "FMWebViewJSBridge.js";

    final static String FM_OVERRIDE_SCHEMA = "fmscheme://";
    final static String FM_RETURN_DATA = FM_OVERRIDE_SCHEMA + "return/";
    final static String EMPTY_STR = "";
    final static String UNDERLINE_STR = "_";

    final static String CALLBACK_ID_FORMAT = "JAVA_CB_%s";
    final static String JS_HANDLE_MESSAGE_FROM_JAVA = "javascript:WebViewJavascriptBridge.handleMessageFromNative('%s');";
    final static String JAVA_FETCH_QUEUE_FROM_JS= "javascript:WebViewJavascriptBridge.fetchQueue();";
    static final Map<Class<?>, Map<String, Method>> JAVASCRIPT_INTERFACES_CACHE;

    static {
        JAVASCRIPT_INTERFACES_CACHE = new HashMap<>();
    }



    public static String getDataFromReturnUrl(String url) {
        if(url.startsWith(FM_RETURN_DATA)) {
            return url.replace(FM_RETURN_DATA, EMPTY_STR);
        }
        return null;
    }

    public static String assetFile2Str(Context c, String urlStr){
        InputStream in = null;
        try{
            in = c.getAssets().open(urlStr);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
            String line = null;
            StringBuilder sb = new StringBuilder();
            do {
                line = bufferedReader.readLine();
                if (line != null && !line.matches("^\\s*\\/\\/.*")) {
                    sb.append(line);
                }
            } while (line != null);

            bufferedReader.close();
            in.close();

            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
        return null;
    }

    static void injectInterface(WebView webView, Map<String, Object> javascriptInterfaces) {

        String jsContent = assetFile2Str(webView.getContext(), FM_LOAD_JS);

        if (javascriptInterfaces == null) {
            StringBuilder jsFunction = new StringBuilder("javascript:").append(jsContent).append("( ").append(")");
            webView.loadUrl(jsFunction.toString());
            return;
        }

        Set<String> keys = javascriptInterfaces.keySet();
        if (keys.isEmpty()) {
            return;
        }

        ArrayList<String> objNames = new ArrayList<>();
        objNames.addAll(keys);
        ArrayList<Set<String>> objMethods= new ArrayList<>();
        for (String objName : objNames) {
            Object javaInterface = javascriptInterfaces.get(objName);
            Class interfaceClass = javaInterface.getClass();
            Set<String> interfaceMethods = findJavaScriptInterface(interfaceClass);
            objMethods.add(interfaceMethods);
        }
//        StringBuilder jsFunction = new StringBuilder("javascript:").append(jsContent).append("( ").
//                append(new Gson().toJson(objNames)).
//                append(", ").append(new Gson().toJson(objMethods)).append(");");
        String jsFunction = "javascript:"+jsContent+"("+new Gson().toJson(objNames)+", "+new Gson().toJson(objMethods)+");";
        webView.loadUrl(jsFunction);
    }

    static Method getMethod(Class<?> interfaceClass, String methodName) {
        Map<String, Method> methodMap = JAVASCRIPT_INTERFACES_CACHE.get(interfaceClass);
        if (methodMap == null) {
            return null;
        }
        return methodMap.get(methodName);
    }

    private static Set<String> findJavaScriptInterface(Class interfaceClass) {
        Map<String, Method> methodMap = JAVASCRIPT_INTERFACES_CACHE.get(interfaceClass);
        if (methodMap == null) {
            methodMap = new HashMap<>();
            for (Method method : interfaceClass.getMethods()) {
                FMJavascriptInterface interfaceAnnotation = method.getAnnotation(FMJavascriptInterface.class);
                if (interfaceAnnotation != null) {
                    method.setAccessible(true);
                    methodMap.put(method.getName(), method);
                }
            }
            JAVASCRIPT_INTERFACES_CACHE.put(interfaceClass, methodMap);
        }
        return methodMap.keySet();
    }
}

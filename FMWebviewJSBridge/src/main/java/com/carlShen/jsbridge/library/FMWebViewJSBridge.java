package com.carlShen.jsbridge.library;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Looper;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.google.gson.Gson;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.os.SystemClock;

/**
 * Created by shenqiang on 16/8/1.
 */
public class FMWebViewJSBridge  {

    private WebView webView;
    private WebViewClient webViewClient;
    private Map<String, Object> javascriptInterfaces;

    private List<FMJSBridgeMessage> beforeInjectMessage = new ArrayList<FMJSBridgeMessage>();
    private Map<String, FMJSBridgeResponse> jsBridgeResponses = new HashMap<String, FMJSBridgeResponse>();

    private long uniqueId = 0;

    public  FMWebViewJSBridge(WebView webView, WebViewClient webViewClient) {
        super();
        this.webView = webView;
        this.webView.getSettings().setJavaScriptEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            this.webView.setWebContentsDebuggingEnabled(true);
        }
        this.webView.setWebViewClient(new FMWebViewClient(webViewClient, new FMWebViewClientBridge() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView webView, String url) {

                if (url.startsWith(FMJSBridgeUtil.FM_RETURN_DATA)) { // 如果是返回数据
                    handlerReturnData(webView,url);
                    return true;
                } else if (url.startsWith(FMJSBridgeUtil.FM_OVERRIDE_SCHEMA)) { //
                    fetchMessagQueue(webView);
                    return true;
                }
                return  false;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    setBeforeInjectMessage(new ArrayList<FMJSBridgeMessage>());
            }

            @Override
            public void onPageFinished(WebView webView, String url) {
                if (FMJSBridgeUtil.FM_LOAD_JS == null) {
                    return;
                }
                FMJSBridgeUtil.injectInterface(webView, javascriptInterfaces);
                if (getBeforeInjectMessage()!=null) {
                    for (FMJSBridgeMessage message:beforeInjectMessage) {
                        dispatchMessage(message);
                    }
                    setBeforeInjectMessage(null);
                }
            }
        }));
        this.webViewClient = webViewClient;
    }

    public void addJavascriptInterface(Object javaInterface, String jsName) {
        if (javascriptInterfaces == null) {
            javascriptInterfaces = new HashMap<String, Object>();
        }
        javascriptInterfaces.put(jsName, javaInterface);
    }

    public void callJSFunction(String object, String method, List<Object> args) {
        this.callJSFunction(object, method, args, null);
    }

    public void callJSFunction(String object, String method, List<Object> args,  FMJSBridgeResponse response) {
        FMJSBridgeMessage message = new FMJSBridgeMessage();
        message.setObj(object);
        message.setMethod(method);
        message.setJsFunctionArgsData(args);
        if (response != null) {
            String callbackStr = String.format(FMJSBridgeUtil.CALLBACK_ID_FORMAT, ++uniqueId + (FMJSBridgeUtil.UNDERLINE_STR + SystemClock.currentThreadTimeMillis()));
            jsBridgeResponses.put(callbackStr, response);
            message.setCallbackId(callbackStr);
        }
        if (getBeforeInjectMessage() != null) {
            getBeforeInjectMessage().add(message);
        } else  {
            dispatchMessage(message);
        }
    }

    public List<FMJSBridgeMessage> getBeforeInjectMessage() {
        return beforeInjectMessage;
    }

    public void setBeforeInjectMessage(List<FMJSBridgeMessage> beforeInjectMessage) {
        this.beforeInjectMessage = beforeInjectMessage;
    }

    private  void handlerReturnData(WebView view, String url) {
        String data = FMJSBridgeUtil.getDataFromReturnUrl(url);
        if (data != null) {
            this.flushMessageQueue(data);
        }
    }

    private  void  fetchMessagQueue(WebView view) {
        view.loadUrl(FMJSBridgeUtil.JAVA_FETCH_QUEUE_FROM_JS);
    }


    private void flushMessageQueue(String data) {

        List<FMJSBridgeMessage> list = FMJSBridgeMessage.toArrayList(data);

        if (list == null || list.size() == 0) {
            return;
        }

        for (FMJSBridgeMessage bridgeMessage: list
                ) {
            if (bridgeMessage.getObj()!=null && bridgeMessage.getMethod() != null) {

                this.callNativeFunction(bridgeMessage);

            } else if (bridgeMessage.getCallbackId() != null) {

                FMJSBridgeResponse response = jsBridgeResponses.get(bridgeMessage.getCallbackId());
                jsBridgeResponses.remove(bridgeMessage.getCallbackId());
                Class objectClass = response.getClass();
                Type[] types = objectClass.getGenericInterfaces();

                if (types.length>1) {
                    continue;
                }

                Type responseType = ((ParameterizedType)types[0]).getActualTypeArguments()[0];

                response.asynchronousResponse(new Gson().fromJson(bridgeMessage.getNativeFunctionArgsData(), responseType));
            }
        }

    }

    private void callNativeFunction(FMJSBridgeMessage message) {
        Object javascriptInterface = getJavaScriptInterface(message);
        if (javascriptInterface == null) {
            return;
        }
        String methodName = message.getMethod();
        if (methodName == null) {
            return;
        }
        Method method = FMJSBridgeUtil.getMethod(javascriptInterface.getClass(), methodName);
        if (method == null) {
            return;
        }
        try {
            Object[] args = getArgs(method.getParameterTypes(), message);
            Object returnData = method.invoke(javascriptInterface, args);
            if (returnData != null) {
                FMJSBridgeMessage responseMessage = new FMJSBridgeMessage();
                responseMessage.setJsFunctionArgsData(returnData);
                responseMessage.setCallbackId(message.getCallbackId());
                dispatchMessage(responseMessage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private Object getJavaScriptInterface(FMJSBridgeMessage message) {
        return javascriptInterfaces.get(message.getObj());
    }

    private Object[] getArgs( Class<?>[] parTypes,  FMJSBridgeMessage message) {
        if (parTypes.length == 0) {
            return null;
        }
        if (parTypes.length == 1) {
            if (parTypes[0].isAssignableFrom(FMJSBridgeResponse.class)) {
                Object[] args = {nativeResponse(message)};
                return args;
            }
            Object[] args = {new Gson().fromJson(message.getNativeFunctionArgsData(), parTypes[0])};
            return args;
        }
        Object[] args = {new Gson().fromJson(message.getNativeFunctionArgsData(), parTypes[0]), nativeResponse(message)};
        return args;
    }

    private  FMJSBridgeResponse<Object> nativeResponse(final FMJSBridgeMessage message) {
       return  new FMJSBridgeResponse() {
           @Override
           public void asynchronousResponse(Object data) {
               FMJSBridgeMessage responseMessage = new FMJSBridgeMessage();
               responseMessage.setJsFunctionArgsData(data);
               responseMessage.setCallbackId(message.getCallbackId());
               dispatchMessage(responseMessage);
           }
       };

    }

    private void dispatchMessage(FMJSBridgeMessage message) {
        String messageJson = message.toJson();
        messageJson = messageJson.replaceAll("(\\\\)([^utrn])", "\\\\\\\\$1$2");
        messageJson = messageJson.replaceAll("(?<=[^\\\\])(\")", "\\\\\"");
        String javascriptCommand = String.format(FMJSBridgeUtil.JS_HANDLE_MESSAGE_FROM_JAVA, messageJson);
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            webView.loadUrl(javascriptCommand);
        }
    }
}

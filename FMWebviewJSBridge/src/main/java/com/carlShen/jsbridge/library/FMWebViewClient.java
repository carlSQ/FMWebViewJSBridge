package com.carlShen.jsbridge.library;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Message;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.UnsupportedEncodingException;

import java.net.URLDecoder;

/**
 * Created by shenqiang on 16/8/1.
 */

interface FMWebViewClientBridge {

    boolean shouldOverrideUrlLoading(WebView webView, String url);

    void onPageStarted(WebView view, String url, Bitmap favicon);

    void onPageFinished(WebView webView, String url);
}


public class FMWebViewClient extends WebViewClient {

    private  WebViewClient webViewClient;

    private FMWebViewClientBridge clientBridge;

    public  FMWebViewClient(WebViewClient webViewClient, FMWebViewClientBridge clientBridge) {
        super();
        this.webViewClient = webViewClient;
        this.clientBridge = clientBridge;
    }


    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        try {
            url = URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (url.startsWith(FMJSBridgeUtil.FM_OVERRIDE_SCHEMA)) {
          return this.clientBridge.shouldOverrideUrlLoading(view, url);
        } else {
            if (this.webViewClient != null) {
                return this.webViewClient.shouldOverrideUrlLoading(view, url);
            } else {
                return super.shouldOverrideUrlLoading(view, url);
            }
        }
    }

    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        if (this.webViewClient != null) {
            this.webViewClient.onPageStarted(view, url, favicon);
        } else  {
            super.onPageStarted(view, url, favicon);
        }
        this.clientBridge.onPageStarted(view, url, favicon);
    }

    public void onPageFinished(WebView view, String url) {
        if (this.webViewClient != null) {
            this.webViewClient.onPageFinished(view, url);
        } else  {
            super.onPageFinished(view, url);
        }
        this.clientBridge.onPageFinished(view, url);
    }

    public void onLoadResource(WebView view, String url) {
        if (this.webViewClient != null) {
            this.webViewClient.onLoadResource(view, url);
            return;
        }
        super.onLoadResource(view, url);
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void onPageCommitVisible(WebView view, String url) {
        if (this.webViewClient != null) {
            this.webViewClient.onPageCommitVisible(view, url);
            return;
        }
        super.onPageCommitVisible(view, url);
    }

    public WebResourceResponse shouldInterceptRequest(WebView view,
                                                      String url) {
        if (this.webViewClient != null) {
            return this.webViewClient.shouldInterceptRequest(view, url);
        }
        return super.shouldInterceptRequest(view, url);
    }
    @TargetApi(Build.VERSION_CODES.M)
    public WebResourceResponse shouldInterceptRequest(WebView view,
                                                      WebResourceRequest request) {
        if (this.webViewClient != null) {
            return this.webViewClient.shouldInterceptRequest(view, request);
        }
        return  super.shouldInterceptRequest(view, request);

    }
    public void onTooManyRedirects(WebView view, Message cancelMsg,
                                   Message continueMsg) {

        if (this.webViewClient != null) {
            this.webViewClient.onTooManyRedirects(view, cancelMsg, continueMsg);
            return;
        }
        super.onTooManyRedirects(view, cancelMsg, continueMsg);

    }
    public void onReceivedError(WebView view, int errorCode,
                                String description, String failingUrl) {

        if (this.webViewClient != null) {
            this.webViewClient.onReceivedError(view, errorCode, description, failingUrl);
            return;
        }
        super.onReceivedError(view, errorCode, description, failingUrl);

    }
    @TargetApi(Build.VERSION_CODES.M)
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {

        if (this.webViewClient != null) {
            this.webViewClient.onReceivedError(view, request, error);
            return;
        }
        super.onReceivedError(view, request, error);

    }

    @TargetApi(Build.VERSION_CODES.M)
    public void onReceivedHttpError(
            WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
        if (this.webViewClient != null) {
            this.webViewClient.onReceivedHttpError(view, request, errorResponse);
            return;
        }
        super.onReceivedHttpError(view, request, errorResponse);
    }

    public void onFormResubmission(WebView view, Message dontResend,
                                   Message resend) {
        if (this.webViewClient != null) {
            this.webViewClient.onFormResubmission(view, dontResend, resend);
            return;
        }
        super.onFormResubmission(view, dontResend, resend);

    }

    public void doUpdateVisitedHistory(WebView view, String url,
                                       boolean isReload) {

        if (this.webViewClient != null) {
            this.webViewClient.doUpdateVisitedHistory(view, url, isReload);
            return;
        }
        super.doUpdateVisitedHistory(view, url, isReload);

    }

    public void onReceivedSslError(WebView view, SslErrorHandler handler,
                                   SslError error) {

        if (this.webViewClient != null) {
            this.webViewClient.onReceivedSslError(view, handler, error);
            return;
        }
        super.onReceivedSslError(view, handler, error);
    }


    public void onReceivedHttpAuthRequest(WebView view,
                                          HttpAuthHandler handler, String host, String realm) {
        if (this.webViewClient != null) {
            this.webViewClient.onReceivedHttpAuthRequest(view, handler, host, realm);
            return;
        }
        super.onReceivedHttpAuthRequest(view, handler, host, realm);

    }

    public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
        if (this.webViewClient != null) {
            return  this.webViewClient.shouldOverrideKeyEvent(view, event);
        }
        return  super.shouldOverrideKeyEvent(view, event);

    }

    public void onUnhandledKeyEvent(WebView view, KeyEvent event) {
        if (this.webViewClient != null) {
            this.webViewClient.onUnhandledKeyEvent(view, event);
            return;
        }
        super.onUnhandledKeyEvent(view, event);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void onUnhandledInputEvent(WebView view, InputEvent event) {
        if (this.webViewClient != null) {
            this.webViewClient.onUnhandledInputEvent(view, event);
            return;
        }
        super.onUnhandledInputEvent(view, event);

    }

    public void onScaleChanged(WebView view, float oldScale, float newScale) {
        if (this.webViewClient != null) {
            this.webViewClient.onScaleChanged(view, oldScale, newScale);
            return;
        }
        super.onScaleChanged(view, oldScale, newScale);

    }

    public void onReceivedLoginRequest(WebView view, String realm,
                                       String account, String args) {
        if (this.webViewClient != null) {
            this.webViewClient.onReceivedLoginRequest(view,realm,account,args);
            return;
        }

        super.onReceivedLoginRequest(view, realm, account, args);
    }

}

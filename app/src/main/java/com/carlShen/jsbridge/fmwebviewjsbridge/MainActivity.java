package com.carlShen.jsbridge.fmwebviewjsbridge;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import com.carlShen.jsbridge.library.FMJSBridgeResponse;
import com.carlShen.jsbridge.library.FMWebViewJSBridge;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;



public class MainActivity extends Activity implements View.OnClickListener  {

    private final String TAG = "MainActivity";

    WebView webView;

    Button button;

    TextView textView;

    int RESULT_CODE;
    FMWebViewJSBridge jsBridge;

    ValueCallback<Uri> mUploadMessage;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = (WebView) findViewById(R.id.webView);

        jsBridge = new FMWebViewJSBridge(webView, null);

        TestJavaInterface javaInterface = new TestJavaInterface(this);
        jsBridge.addJavascriptInterface(javaInterface, "javaInterface");
        button = (Button) findViewById(R.id.button);
        textView = (TextView) findViewById(R.id.text);

        button.setOnClickListener(this);

        webView.setWebChromeClient(new WebChromeClient() {

            public void openFileChooser(ValueCallback<Uri> uploadMsg, String AcceptType, String capture) {
                this.openFileChooser(uploadMsg);
            }

            public void openFileChooser(ValueCallback<Uri> uploadMsg, String AcceptType) {
                this.openFileChooser(uploadMsg);
            }

            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                mUploadMessage = uploadMsg;
                pickFile();
            }
        });

        webView.loadUrl("file:///android_asset/demo.html");



    }

    public void pickFile() {
        Intent chooserIntent = new Intent(Intent.ACTION_GET_CONTENT);
        chooserIntent.setType("image/*");
        startActivityForResult(chooserIntent, RESULT_CODE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == RESULT_CODE) {
            if (mUploadMessage == null) {
                return;
            }
            Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        }
    }

    public void onClick(View v) {
        User user = new User();
        Location location = new Location();
        location.address = "SDU";
        user.location = location;
        user.name = "Bruce";
        user.age = 8;
        List<Object> args = new ArrayList();
        args.add("i love you ");
        args.add(user);
        jsBridge.callJSFunction("testObj", "testMethod", args, new FMJSBridgeResponse<User>() {
            @Override
            public void asynchronousResponse(User data) {
                if (data != null) {
                    textView.setText(String.format("functionInJs %s+%s", data.age, data.name));
                }
            }
        });
    }

    static class Location {
        String address;

        @Override
        public String toString() {
            return "Location{" +
                    "address='" + address + '\'' +
                    '}';
        }
    }

    static class User {
        String name;
        int age;
        Location location;

        @Override
        public String toString() {
            return "User{" +
                    "name='" + name + '\'' +
                    ", location=" + location +
                    '}';
        }
    }

    static class Login {
        @SerializedName("username")
        String username;
        @SerializedName("password")
        String password;

        @Override
        public String toString() {
            return "Login{" +
                    "username='" + username + '\'' +
                    ", password='" + password + '\'' +
                    '}';
        }
    }

}

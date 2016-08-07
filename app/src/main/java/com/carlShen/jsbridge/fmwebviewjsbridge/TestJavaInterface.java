package com.carlShen.jsbridge.fmwebviewjsbridge;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.carlShen.jsbridge.library.FMJSBridgeResponse;
import com.carlShen.jsbridge.library.FMJavascriptInterface;
import com.google.gson.annotations.SerializedName;

/**
 * Created by shenqiang on 16/8/6.
 */
public class TestJavaInterface {

    private Activity activity;

    public TestJavaInterface(Activity activity) {
        this.activity = activity;
    }

    @FMJavascriptInterface
    public String test(String data, FMJSBridgeResponse<Object> result) {
        if (data == null) {
            data = "没有传参数过来";
        }
        Toast.makeText(activity, data.toString(), Toast.LENGTH_SHORT).show();
        return "调用了注入的方法";
    }

    @FMJavascriptInterface
    public void testNon() {
        Toast.makeText(activity, "没有传参数过来", Toast.LENGTH_SHORT).show();

    }

    @FMJavascriptInterface
    public boolean testBool(String data, FMJSBridgeResponse<Object> result) {
        Toast.makeText(activity, "参数 " + data.toString(), Toast.LENGTH_SHORT).show();
        return true;
    }

    @FMJavascriptInterface
    public int testInt(String data, FMJSBridgeResponse<Object> result) {
        Toast.makeText(activity, data.toString(), Toast.LENGTH_SHORT).show();
        return 1;
    }

    @FMJavascriptInterface
    public String[] testArray(String[] data, FMJSBridgeResponse<Object> result) {
        String info = "";
        for (String s : data) {
            info = info + " " + s;
        }
        Toast.makeText(activity, info, Toast.LENGTH_SHORT).show();
        String[] tests = {"1", "2"};
        return tests;
    }

    @FMJavascriptInterface
    public void testAsy(String data, final FMJSBridgeResponse<Object> result) {
        Toast.makeText(activity, data.toString(), Toast.LENGTH_SHORT).show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int tests = 888;
                        result.asynchronousResponse(tests);
                    }
                });
            }
        }).start();

    }

    @FMJavascriptInterface
    public void testNonParamsAsy(final FMJSBridgeResponse<Object> result) {
        Handler handler = new Handler(Looper.myLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String[] tests = {"sq", "ljm"};
                result.asynchronousResponse(tests);

            }
        }, 2000);
    }


    @FMJavascriptInterface
    public User testCustuomObject(User user) {
        Toast.makeText(activity, user.toString(), Toast.LENGTH_SHORT).show();
        user.setAge("8");
        return user;
    }

    class User {
        @SerializedName("name")
        private String name;
        @SerializedName("age")
        private String age;

        public User(String age, String name) {
            super();
            this.age = age;
            this.name = name;
        }

        String getName() {
            return name;
        }

        void setName(String name) {
            this.name = name;
        }

        String getAge() {
            return age;
        }

        void setAge(String age) {
            this.age = age;
        }

        @Override
        public String toString() {
            return "user [name=" + name + ", age=" + age + "]\n\n";
        }
    }

}

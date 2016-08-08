# FMWebViewJSBridge

-----

inspired and modified from [JsBridge](https://github.com/jacin1/JsBridge)

It provides safe and convenient way to call Java code from js and call js code from java. It also provides same interface in js with [FMWebViewJavascriptBridge](https://github.com/carlSQ/FMWebViewJavascriptBridge) 

![image](http://7xs4ye.com1.z0.glb.clouddn.com/jsbridge.png)


## Usage

## Dependencies


```groovy
repositories {
        jcenter()
    }
...
dependencies {
    compile 'com.carlShen.FMWebViewJSBridge:FMWebviewJSBridge:1.0.0'
}
```

## Use in Native

### Register a java inrterface object so that js can call


##### define java interface object

export methos to js need  add @FMJavascriptInterface

```java
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
```


##### register java interface 


```java

    TestJavaInterface javaInterface = new TestJavaInterface(this);
    jsBridge.addJavascriptInterface(javaInterface, "javaInterface");

```

### Js can call this java interface object

```javascript

    javaInterface.test("hello", function(data){
            alert("inject response: "+data);
         });
         
```


### Js register interface object so than java can call 


```javascript

function person() {
            this.testMethod = testMethod;
            function testMethod(name, user) {
                alert(name+user.name);
                return {name:"carl shen", age:"7"};

            }
        }

        testObject= new person();
        
        bridge.addNativeInterfaces('testObj',testObject);

```

## Notice

This lib will inject a WebViewJavascriptBridge Object to window object.
So in your js, before use WebViewJavascriptBridge, you must detect if WebViewJavascriptBridge exist.
If WebViewJavascriptBridge does not exit, you can listen to WebViewJavascriptBridgeReady event, as the blow code shows:

```javascript

    if (window.WebViewJavascriptBridge) {
        //do your work here
    } else {
        document.addEventListener(
            'WebViewJavascriptBridgeReady'
            , function() {
                //do your work here
            },
            false
        );
    }

```

## Js receive message 

js WebViewJavascriptBridge must call the blow method or native can not  call  js 
methos and receive response data from data

```javascript
bridge.receiveNativeMessage();
```

## Author

carl Shen

## License

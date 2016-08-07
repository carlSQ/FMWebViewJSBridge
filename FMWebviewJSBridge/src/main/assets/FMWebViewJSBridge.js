   (function (objs, objsMethods) {

       if (window.WebViewJavascriptBridge) {
           return;
       }
       var messagingIframe;
       var sendMessageQueue = [];

       var CUSTOM_PROTOCOL_SCHEME = 'fmscheme';
       var QUEUE_HAS_MESSAGE = '__FM_QUEUE_MESSAGE__';
       var receiveMessageFlushQueue = [];
       var responseCallbacks = {};

       var jsObjTable = {};

       var uniqueId = 1;


       function _createQueueReadyIframe(doc) {
           messagingIframe = doc.createElement('iframe');
           messagingIframe.style.display = 'none';
           messagingIframe.src = CUSTOM_PROTOCOL_SCHEME + '://' + QUEUE_HAS_MESSAGE;
           doc.documentElement.appendChild(messagingIframe);
       }

       function receiveNativeMessage() {
               if (!receiveMessageFlushQueue) {
                   return;
               }
               var receivedMessages = receiveMessageFlushQueue;
               receiveMessageFlushQueue = null;
               for (var i = 0; i < receivedMessages.length; i++) {
                   _dispatchMessageFromNative(receivedMessages[i]);
               }
       }

       function inject(objs, objsMethods) {
           for (var index = 0; index < objs.length;index++) {
               var obj = objs[index];
               var methods = objsMethods[index];
               injectEach(obj, methods);
           }
       }

       function addNativeInterfaces(objName, obj) {
            jsObjTable[objName]=obj;
       }

       function injectEach(obj, methods) {
           window[obj] = {};
           var jsObj = window[obj];
           var l = methods.length;
           for (var i = 0; i < l; i++) {
           (function () {
               var method = methods[i];
               jsObj[method] = function () {
                   if (arguments.length > 2) {
                       throw new Error('arguments Error');
                   }
                   if (arguments.length == 1) {
                       if (typeof arguments[0] == "function") {
                       return _callJavascriptInterface(obj, method, null, arguments[0]);
                   }
                   return _callJavascriptInterface(obj, method, arguments[0], null);

                   }
                   if (arguments.length == 0) {
                       return _callJavascriptInterface(obj, method, null, null);
                   }
                   return _callJavascriptInterface(obj, method, arguments[0], arguments[1]);
                   };
               })();
           }
       }

       function _callJavascriptInterface(obj, method, data, responseCallback) {
           if (data) {
               _dosSendToJavascriptInterface({
                   'obj': obj,
                   'method': method,
                   'nativeFunctionArgsData': data
               }, responseCallback);
           } else {
               _dosSendToJavascriptInterface({
                   'obj': obj,
                   'method': method
               }, responseCallback);
           }
       }

       function _dosSendToJavascriptInterface(message, responseCallback) {
           if (responseCallback) {
               var callbackId = 'cb_' + (uniqueId++) + '_' + new Date().getTime();
               responseCallbacks[callbackId] = responseCallback;
               message.callbackId = callbackId;
           }
           _doSend(message);
       }

       function _doSend(message) {
           sendMessageQueue.push(message);
           messagingIframe.src = CUSTOM_PROTOCOL_SCHEME + '://' + QUEUE_HAS_MESSAGE;
       }

       function fetchQueue() {
           var messageQueueString = JSON.stringify(sendMessageQueue);
           sendMessageQueue = [];
           messagingIframe.src = CUSTOM_PROTOCOL_SCHEME + '://return/' + encodeURIComponent(messageQueueString);
       }

       function _dispatchMessageFromNative(messageJSON) {
           setTimeout(function _timeoutDispatchMessageFromObjC() {
               var message = JSON.parse(messageJSON);
               var responseCallback;
               if(message.obj && message.method){

                   var args = message.jsFunctionArgsData;
                   var returnValue = jsObjTable[message.obj][message.method].apply(jsObjTable[message.obj][message.method],args);
                   if (returnValue && message.callbackId) {
                        _doSend({
                                   callbackId: message.callbackId,
                                    nativeFunctionArgsData: returnValue
                                 });
                   }

               } else if (message.callbackId) {
                   responseCallback = responseCallbacks[message.callbackId];
                   if (!responseCallback) {
                       return;
                   }
                   responseCallback(message.jsFunctionArgsData);
                   delete responseCallbacks[message.callbackId];
               }
           })
       }

       function handleMessageFromNative(messageJSON) {

            if(receiveMessageFlushQueue) {
                receiveMessageFlushQueue.push(messageJSON)
            } else {
             _dispatchMessageFromNative(messageJSON);
            }
       }

       window.WebViewJavascriptBridge = {
           receiveNativeMessage: receiveNativeMessage,
           addNativeInterfaces: addNativeInterfaces,
           inject: inject,
           fetchQueue: fetchQueue,
           handleMessageFromNative: handleMessageFromNative
       };

       var doc = document;
       _createQueueReadyIframe(doc);
       window.WebViewJavascriptBridge.inject(objs, objsMethods);
       var doc = document;
       var readyEvent = doc.createEvent('Events');
       readyEvent.initEvent('WebViewJavascriptBridgeInjectFinishedReady');
       readyEvent.bridge = WebViewJavascriptBridge;
       doc.dispatchEvent(readyEvent);
   })
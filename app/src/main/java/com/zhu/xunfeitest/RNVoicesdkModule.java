package com.zhu.xunfeitest;


import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

//语音识别所需要的包
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class RNVoicesdkModule extends ReactContextBaseJavaModule {

    private static final String TAG = "zhu";
    private ReactApplicationContext reactContext;

    public RNVoicesdkModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "RNVoicesdk";
    }

    private void onJSEvent(String eventName, String msgString) {
        if (reactContext != null) {
            reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit(eventName, msgString);
        }
    }

    private void onJSEvent(String eventName, WritableMap params) {
        if (reactContext != null) {
            reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit(eventName, params);
        }
    }

    @ReactMethod
    public void show(String message, int duration) {
        android.widget.Toast.makeText(getReactApplicationContext(), message, duration).show();
    }

    SpeechRecognizer mIat;

    @ReactMethod
    private void init() {
//        ActivityCompat.requestPermissions(reactContext,
//                new String[]{
//                        Manifest.permission.RECORD_AUDIO,
//                        Manifest.permission.READ_EXTERNAL_STORAGE,
//                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                        Manifest.permission.INTERNET,
//                        Manifest.permission.ACCESS_NETWORK_STATE,
//                },
//                110);
        SpeechUtility.createUtility(reactContext, SpeechConstant.APPID + "=5f08428f");
        //初始化识别无UI识别对象
        //使用SpeechRecognizer对象，可根据回调消息自定义界面；
        mIat = SpeechRecognizer.createRecognizer(reactContext, new InitListener() {
            @Override
            public void onInit(int code) {
                Log.i(TAG, "初始码 : " + code);
                if (code == 0) {
                    //rnCallback.invoke("");
                }
            }
        });

        //设置语法ID和 SUBJECT 为空，以免因之前有语法调用而设置了此参数；或直接清空所有参数，具体可参考 DEMO 的示例。
        mIat.setParameter(SpeechConstant.CLOUD_GRAMMAR, null);
        mIat.setParameter(SpeechConstant.SUBJECT, null);
        //设置返回结果格式，目前支持json,xml以及plain 三种格式，其中plain为纯听写文本内容
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");
        //此处engineType为“cloud”
        String engineType = SpeechConstant.TYPE_CLOUD;
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, engineType);
        //设置语音输入语言，zh_cn为简体中文
        mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        //设置结果返回语言
        mIat.setParameter(SpeechConstant.ACCENT, "mandarin");
        // 设置语音前端点:静音超时时间，单位ms，即用户多长时间不说话则当做超时处理
        //取值范围{1000～10000}
        mIat.setParameter(SpeechConstant.VAD_BOS, "4000");
        //设置语音后端点:后端点静音检测时间，单位ms，即用户停止说话多长时间内即认为不再输入，
        //自动停止录音，范围{0~10000}
        mIat.setParameter(SpeechConstant.VAD_EOS, "1000");
        //设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, "1");

    }

    Callback recognizerCallback;

    @ReactMethod
    public void startRecognizer(Callback recognizerCallback) {
        this.recognizerCallback = recognizerCallback;
        //开始识别，并设置监听器
        mIat.startListening(mRecognizerListener);
    }

    @ReactMethod
    public void stopRecognizer() {
        if (mIat != null) {
            mIat.stopListening();
            mIat.cancel();
        }
    }

    @ReactMethod
    public void release() {
        if (mIat != null) {
            mIat.destroy();
        }
    }

    /**
     * 听写监听器。
     */
    private RecognizerListener mRecognizerListener = new RecognizerListener() {

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            Log.i(TAG, "开始说话");
        }

        @Override
        public void onError(SpeechError error) {
            // Tips：
            // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。

            Log.i(TAG, error.getPlainDescription(true));

        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            Log.i(TAG, "结束说话");
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            String last = printResult(results);
            if (isLast) {
                // TODO 最后的结果
                //Message message = Message.obtain();
                //message.what = 0x001;
                //han.sendMessageDelayed(message,100);
                //Log.d(TAG, "isLast = > " + results.getResultString());
                if (recognizerCallback != null) {
                    recognizerCallback.invoke("" + last);
                }
            }
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            Log.i(TAG, "当前正在说话，音量大小：" + volume);
            //Log.d(TAG, "返回音频数据：" + data.length);
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }
    };

    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();

    private String printResult(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());

        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mIatResults.put(sn, text);

        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }
        Log.i(TAG, "printResult => " + resultBuffer.toString());
        //mResultText.setText(resultBuffer.toString());
        //mResultText.setSelection(mResultText.length());
        return resultBuffer.toString();
    }
    
    
    
    
    
}
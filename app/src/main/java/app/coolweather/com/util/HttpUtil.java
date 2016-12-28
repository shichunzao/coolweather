package app.coolweather.com.util;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by scz on 2016/12/26.
 * 从服务器获取全国省市县数据的工具类
 */

public class HttpUtil {
    private static final String TAG = "HttpUtil";

    public static void sendHttpRequest(final String address,
                                       final HttpCallbackListener listener){

        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(address);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    InputStream in = connection.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(in));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while((line = br.readLine()) != null){
                        sb.append(line);
                    }
                    if(listener != null){
                        Log.i(TAG, "response: "+sb.toString());
                        listener.onFinish(sb.toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if(listener != null){
                        listener.onError(e);
                    }
                }finally {
                    if(connection != null){
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }
}

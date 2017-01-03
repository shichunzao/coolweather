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
                Log.i(TAG, "HttpUtil当前线程："+Thread.currentThread().getName());
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(address);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    InputStream in = connection.getInputStream();
                    //1.
                    BufferedReader br = new BufferedReader(new InputStreamReader(in));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while((line = br.readLine()) != null){
                        sb.append(line);
                    }

                    //2.
//                    byte[] by = new byte[in.available()];
//                    //将数据读入到by数组，返回当前读取了几个字节
//                    in.read(by);
//                    String response = new String(by);

                    //3.
//                    byte[] by = new byte[1024];
//                    while(true){
//                        int temp= in.read(by, 0, by.length);
//                        if(temp == -1){
//                            break;
//                        }
//                        String response = new String(by);
//                        sb.append(response);
//                        response.trim();
//                    }

                    if(listener != null){
                        Log.i(TAG, " response: "+sb.toString());
                        listener.onFinish(sb.toString());
                    }
                } catch (Exception e) {
//                    e.printStackTrace();
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

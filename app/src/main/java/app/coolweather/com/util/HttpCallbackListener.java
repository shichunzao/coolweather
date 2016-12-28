package app.coolweather.com.util;

/**
 * Created by scz on 2016/12/26.
 */

public interface HttpCallbackListener {
    void onFinish(String response);

    void onError(Exception e);
}

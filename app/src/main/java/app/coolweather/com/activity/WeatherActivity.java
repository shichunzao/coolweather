package app.coolweather.com.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import app.coolweather.com.coolweather.R;
import app.coolweather.com.service.AutoUpdateService;
import app.coolweather.com.util.HttpCallbackListener;
import app.coolweather.com.util.HttpUtil;
import app.coolweather.com.util.Utility;

/**
 * Created by scz on 2016/12/28.
 * 启动一个活动来显示某地区的天气信息
 */

public class WeatherActivity extends Activity implements View.OnClickListener{
    private final String TAG = "WeatherActivity";
    private LinearLayout weatherInfoLayout;
    /*用于显示城市名*/
    private TextView cityNameText;
    /*用于显示发布时间*/
    private TextView publishText;
    /*用于显示天气描述信息*/
    private TextView weatherDespText;
    /*用于显示气温1*/
    private TextView temp1Text;
    /*用于显示气温2*/
    private TextView temp2Text;
    /*用于显示当前日期*/
    private TextView currentDateText;

    /*切换城市按钮*/
    private Button switchCity;
    /*更新天气按钮*/
    private Button refreshWeather;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.weather_layout);
        weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
        cityNameText = (TextView) findViewById(R.id.city_name);
        publishText = (TextView) findViewById(R.id.publish_text);
        weatherDespText = (TextView) findViewById(R.id.weather_desp);
        temp1Text = (TextView) findViewById(R.id.temp1);
        temp2Text = (TextView) findViewById(R.id.temp2);
        currentDateText = (TextView) findViewById(R.id.current_date);
        switchCity = (Button) findViewById(R.id.switch_city);
        refreshWeather = (Button) findViewById(R.id.refresh_weather);
        switchCity.setOnClickListener(this);
        refreshWeather.setOnClickListener(this);

        String countyCode = getIntent().getStringExtra("county_code");
        Log.i(TAG, "countyCode:"+countyCode);
        if(!TextUtils.isEmpty(countyCode)){
            /*有县级代号时就去查天气*/
            publishText.setText("同步中.....");
            weatherInfoLayout.setVisibility(View.INVISIBLE);
            cityNameText.setVisibility(View.INVISIBLE);
            queryWeatherCode(countyCode);
        }else{
            /*没有县级代号时就直接显示本地天气*/
            showWeather();
        }
    }

    /*根据县级代号查询所对应的天气代号*/
    private void queryWeatherCode(String countyCode) {
        String address = "http://www.weather.com.cn/data/list3/city"+countyCode+".xml";
        Log.i(TAG, "查询某县级天气代号address："+address);
        queryFromServer(address, "countyCode");
    }

    /*根据天气代号查询天气*/
    private void queryWeatherInfo(String weatherCode) {
        String address = "http://www.weather.com.cn/data/cityinfo/"+weatherCode+".html";
        Log.i(TAG, "查询天气address:"+address);
        queryFromServer(address, "weatherCode");
    }

    /*根据县级代号查询对应的天气代号
    * 根据天气代号查询天气*/
    private void queryFromServer(String address, final String type) {
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                if("countyCode".equals(type)){
                    if(!TextUtils.isEmpty(response)){
                        String[] array = response.split("\\|");
                        if(array != null && array.length ==2){ //保证接下来的数组操作不会越界
                            String weatherCode = array[1];
                            queryWeatherInfo(weatherCode);
                        }
                    }
                }else if("weatherCode".equals(type)){
                    if(!TextUtils.isEmpty(response)){
                        /*处理服务器返回的天气信息*/
                        Utility.handleWeatherResponse(WeatherActivity.this, response);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showWeather();
                            }
                        });
                    }
                }
            }

            @Override
            public void onError(Exception e) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        publishText.setText("同步失败.....");
                    }
                });
            }
        });
    }

    /*从SharePreference文件中读取存储的天气信息，并显示到界面上*/
    private void showWeather() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        cityNameText.setText(prefs.getString("city_name", ""));
        publishText.setText(prefs.getString("publish_time", ""));
        weatherDespText.setText(prefs.getString("weather_desp", ""));
        temp1Text.setText(prefs.getString("temp1", ""));
        temp2Text.setText(prefs.getString("temp2", ""));
        currentDateText.setText(prefs.getString("current_date", ""));
        weatherInfoLayout.setVisibility(View.VISIBLE);
        cityNameText.setVisibility(View.VISIBLE);

        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.switch_city:
                Intent intent = new Intent(this, ChooseAreaActivity.class);
                intent.putExtra("from_weather_activity", true);
                startActivity(intent);
                finish();
                break;
            case R.id.refresh_weather:
                publishText.setText("同步中...");
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                String weatherCode = prefs.getString("weather_code", "");
                if(!TextUtils.isEmpty(weatherCode)){
                    queryWeatherInfo(weatherCode);
                }
                break;
            default:
                break;
        }
    }
}

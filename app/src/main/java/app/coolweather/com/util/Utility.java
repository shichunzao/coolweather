package app.coolweather.com.util;

import android.text.TextUtils;
import android.util.Log;

import java.util.Arrays;

import app.coolweather.com.db.CoolWeatherDB;
import app.coolweather.com.model.City;
import app.coolweather.com.model.County;
import app.coolweather.com.model.Province;

/**
 * Created by scz on 2016/12/27.
 * 服务器返回的省县市数据都是“代号|城市，代号|城市”格式，
 * 解析规则: 先按逗号分隔，再按单竖线分隔
 * 处理：将解析出来的数据设置到实体类中，最后调用CoolWeatherDB的三个save（）方法存储到表中
 * Utility用于解析和处理该格式数据
 */

public class Utility {
    private static final String TAG = "Utility";
    /*解析和处理服务器返回的省级数据*/
    public synchronized static boolean handleProvincesReponse(CoolWeatherDB coolWeatherDB,
                                                              String response){
        if(!TextUtils.isEmpty(response)){
            String[] allProvinces = response.split(",");
            if(allProvinces != null && allProvinces.length >0){
                for(String p : allProvinces){
                    String[] array = p.split("\\|");
                    Province province = new Province();
                    province.setProvinceCode(array[0]);
                    province.setProvinceName(array[1]);
                    coolWeatherDB.saveProvince(province);
                }
                return true;
            }
        }
        return false;
    }

    /*解析和处理服务器返回的市级数据
    * 返回省Id为provinceId该省的所有市*/
    public static boolean handleCitiesResponse(CoolWeatherDB coolWeatherDB,
                                               String response, int provinceId){
        if(! TextUtils.isEmpty(response)){
            String[] allCities =response.split(",");
            if(allCities != null && allCities.length >0){
                for(String c : allCities){
                    String[] array = c.split("\\|");
                    Log.i(TAG, "array:"+ Arrays.toString(array));
                    City city = new City();
                    city.setCityCode(array[0]);
                    city.setCityName(array[1]);
                    city.setProvinceId(provinceId);
                    coolWeatherDB.saveCity(city);
                }
                return true;
            }
        }
        return false;
    }

    /*解析和处理服务器返回的县级数据
    * 返回市Id为cityId该市的所有县*/
    public static boolean handleCountiesResponse(CoolWeatherDB coolWeatherDB,
                                               String response, int cityId){
        if(! TextUtils.isEmpty(response)){
            String[] allCounties = response.split(",");
            if(allCounties != null && allCounties.length >0){
                for(String c : allCounties){
                    String[] array = c.split("\\|");
                    Log.i(TAG, "County:"+Arrays.toString(array));
                    County county = new County();
                    county.setCountyCode(array[0]);
                    county.setCountyName(array[1]);
                    county.setCityId(cityId);
                    coolWeatherDB.saveCounty(county);
                }
                return true;
            }
        }
        return false;
    }
}

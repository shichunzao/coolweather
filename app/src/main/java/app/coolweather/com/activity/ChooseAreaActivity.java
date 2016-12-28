package app.coolweather.com.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import app.coolweather.com.coolweather.R;
import app.coolweather.com.db.CoolWeatherDB;
import app.coolweather.com.model.City;
import app.coolweather.com.model.County;
import app.coolweather.com.model.Province;
import app.coolweather.com.util.HttpCallbackListener;
import app.coolweather.com.util.HttpUtil;
import app.coolweather.com.util.Utility;

/**
 * Created by scz on 2016/12/27.
 * 遍历省市县数据
 */

public class ChooseAreaActivity extends Activity {
    private final String TAG = "ChooseAreaActivity";
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;
    /*当前选中的级别*/
    private int currentLevel;

    private ProgressDialog progressDialog;
    private TextView titleText;
    private ListView listView;
    private ArrayAdapter<String> adapter;

    private CoolWeatherDB coolWeatherDB;
    private List<String> dataList = new ArrayList<String>();
    /*省列表*/
    private List<Province> provinceList;
    /*市列表*/
    private List<City> cityList;
    /*县列表*/
    private List<County> countyList;
    /*选中的省份*/
    private Province selectedProvince;
    /*选中的城市*/
    private City selectedCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.choose_area);
        titleText = (TextView) findViewById(R.id.title_text);
        listView = (ListView) findViewById(R.id.list_view);

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);

        coolWeatherDB = CoolWeatherDB.getInStance(this);
        queryProvinces();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(currentLevel == LEVEL_PROVINCE){
//                    Log.i(TAG, "currentLevel："+currentLevel+"  position:"+position);
                    selectedProvince = provinceList.get(position);
                    Log.i(TAG, "selectedProvince: "+selectedProvince.getProvinceName()+
                            " its code :"+selectedProvince.getProvinceCode()+" its id :"+selectedProvince.getId());
                    queryCities();
                }else if(currentLevel == LEVEL_CITY){
                    selectedCity = cityList.get(position);
                    Log.i(TAG, "selectedCity:"+selectedCity.getCityName()+" its code: "+selectedCity.getCityCode()+
                    " its id:"+selectedCity.getId());
                    queryCounties();
                }
            }
        });
    }

    /*查询全国所有的省，优先从数据库查询，如果没有再去服务器查询*/
    private void queryProvinces(){
        provinceList = coolWeatherDB.loadProvinces();
        if(provinceList.size() >0){
            dataList.clear();
            for(Province province : provinceList){
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText("中国");
            currentLevel = LEVEL_PROVINCE;
        }else{
            queryFromServer(null, "province");
        }
    }

    /*查询选中的省内所有的市，优先从数据库查询，如果没有再去服务器查询*/
    private void queryCities() {
        cityList = coolWeatherDB.loadCities(selectedProvince.getId());
        if(cityList.size() >0){
            dataList.clear();
            for(City city : cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedProvince.getProvinceName());
            currentLevel = LEVEL_CITY;
        }else{
            Log.i(TAG, "表为空，服务器上查找");
            queryFromServer(selectedProvince.getProvinceCode(), "city");
        }
    }

    /*查询选中的市内所有的县，优先从数据库查询，如果没有再去服务器查询*/
    private void queryCounties() {
        countyList = coolWeatherDB.loadCounties(selectedCity.getId());
        if(countyList.size() > 0){
            dataList.clear();
            for(County county : countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedCity.getCityName());
            currentLevel = LEVEL_COUNTY;
        }else{
            Log.i(TAG, "表为空，服务器上查找");
            queryFromServer(selectedCity.getCityCode(), "county");
        }
    }

    /*根据传入的代号和类型从服务器上查询省市县数据*/
    private void queryFromServer(final String code, final String type) {
        String address;
        if(!TextUtils.isEmpty(code)){
            address = "http://www.weather.com.cn/data/list3/city"+code+".xml";
            Log.i(TAG, "address:"+address);
        }else{
            address = "http://www.weather.com.cn/data/list3/city.xml";
        }
        showProgressDialog();
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                boolean result = false;
                if("province".equals(type)){
                    result = Utility.handleProvincesReponse(coolWeatherDB, response);
                }else if("city".equals(type)){
                    result = Utility.handleCitiesResponse(coolWeatherDB,
                            response, selectedProvince.getId());
                }else if("county".equals(type)){
                    result = Utility.handleCountiesResponse(coolWeatherDB,
                            response, selectedCity.getId());
                }
                if(result){
                    /*通过runOnUiThread()方法回到主线程处理逻辑*/
                    runOnUiThread(new Runnable(){

                        @Override
                        public void run() {
                            closeProgressDialog();      //能执行到这步，说明数据数据获取、处理、存储好了，就差显示出来
                            if("province".equals(type)){
                                queryProvinces();
                            }else if("city".equals(type)){
                                queryCities();
                            }else if("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
                /*通过runOnUiThread()方法回到主线程逻辑*/
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /*显示进度对话框*/
    private void showProgressDialog() {
        if(progressDialog == null){
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在加载");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /*关闭进度对话框*/
    private void closeProgressDialog(){
        if(progressDialog != null){
            progressDialog.dismiss();
        }
    }

    /*捕获Back按键，根据当前的级别来判断，此时应该返回市列表、省列表、还是直接退出*/
    @Override
    public void onBackPressed() {
        if(currentLevel == LEVEL_COUNTY){
            queryCities();
        }else if(currentLevel == LEVEL_CITY){
            queryProvinces();
        }else{
            finish();
        }
    }
}

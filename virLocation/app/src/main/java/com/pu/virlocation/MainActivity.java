package com.pu.virlocation;

import android.location.Geocoder;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.baidu.location.LocationClient;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.model.LatLng;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private String mProviderName = LocationManager.GPS_PROVIDER;
    private Button btn_ok;
    private LocationManager locationManager;
    private double latitude = 29.460828, longitude = 106.538093;
    private Thread thread;
    private Boolean isRun = true;
    private TextView tv_location;

    boolean isFirstLoc = true;  // 是否首次定位

    // 定位相关
    private LocationClient locationClient;
    private MyLocationConfiguration.LocationMode currentMode; //定位模式
    private BitmapDescriptor bitmapDescriptor;  // 定位图标
    private MapView mapView;
    private BaiduMap baiduMap;

    //初始化bitmap信息
    private BitmapDescriptor bd = BitmapDescriptorFactory.fromResource(R.mipmap.dingwei);
    private Marker mMarker;
    private LatLng curLatlng;
    private Geocoder mSearch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        btn_ok = (Button) findViewById(R.id.btn_location_ok);
        tv_location = (TextView) findViewById(R.id.tx_location_info);

        mapView = (MapView) findViewById(R.id.baidu_map);
        baiduMap = mapView.getMap();
        baiduMap.setMyLocationEnabled(true);
        locationClient = new LocationClient(this);
    }

    private void initListner() {
        btn_ok.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

    }
}

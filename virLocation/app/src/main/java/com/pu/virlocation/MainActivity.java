package com.pu.virlocation;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        BDLocationListener, BaiduMap.OnMapClickListener,
        BaiduMap.OnMarkerDragListener, OnGetGeoCoderResultListener {

    private String mProviderName = LocationManager.GPS_PROVIDER;
    private Button btn_ok;
    private LocationManager locationManager;
    private double latitude = 31.3029742, longitude = 120.6097126;// 默认常州
    private Thread thread;
    private Boolean isRun = true;
    private TextView tv_location;

    boolean isFirstLoc = true;  // 是否首次定位

    // 定位相关
    private LocationClient locationClient;
    private MyLocationConfiguration.LocationMode currentMode; //定位模式
    private BitmapDescriptor mCurrentMarker;  // 定位图标
    private MapView mapView;
    private BaiduMap baiduMap;

    //初始化bitmap信息
    private Marker mMarker;
    private LatLng curLatlng;
    private GeoCoder mSearch;
    private double myGpslatitude, myGpslongitude;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        initView();
        initListner();
        initData();
    }

    private void initView() {
        btn_ok = (Button) findViewById(R.id.btn_location_ok);
        tv_location = (TextView) findViewById(R.id.tx_location_info);

        mapView = (MapView) findViewById(R.id.baidu_map);
        baiduMap = mapView.getMap();
        baiduMap.setMyLocationEnabled(true); // 开启定位图层
        locationClient = new LocationClient(this);  // 定位初始化
    }

    private void initListner() {
        btn_ok.setOnClickListener(this);
        locationClient.registerLocationListener(this);
        baiduMap.setOnMapClickListener(this);
        baiduMap.setOnMarkerDragListener(this);

        // 初始化搜索模块，注册事件监听
        mSearch = GeoCoder.newInstance();
        mSearch.setOnGetGeoCodeResultListener(this);
    }

    private void initData() {
        initLocation();
        initMap();
    }

    /**
     * 初始化位置模拟
     */
    private void initLocation() {
        locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        locationManager.addTestProvider(mProviderName, false, true, false,
                false, true, true, true, 0, 5);
        locationManager.setTestProviderEnabled(mProviderName, true);
    }

    /**
     * 初始化地图
     */
    private void initMap() {
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
        currentMode = MyLocationConfiguration.LocationMode.NORMAL;
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(14.0f);
        baiduMap.setMapStatus(msu);

        baiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
                currentMode, true, mCurrentMarker));
        locationClient.setLocOption(option);
        locationClient.start();
        initOverlay();

        //  开启线程，修改gps
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRun) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    setLocation(longitude, latitude);
                }
            }
        });
        thread.start();
    }

    private void initOverlay() {
        LatLng ll = new LatLng(latitude, longitude);
        OverlayOptions oo = new MarkerOptions().position(ll)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.dingwei))
                .zIndex(9).draggable(true);
        mMarker = (Marker) baiduMap.addOverlay(oo);
    }

    private void setLocation(double longitude, double latitude) {
        Location location = new Location(mProviderName);
        location.setTime(System.currentTimeMillis());
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setAltitude(2.0f);
        location.setAccuracy(3.0f);
        locationManager.setTestProviderLocation(mProviderName, location);
    }

    private void setCurrentMapLatLng(LatLng latLng) {
        curLatlng = latLng;
        mMarker.setPosition(latLng);

        // 设置地图中心点为这是位置
        LatLng ll = new LatLng(latLng.latitude, latLng.longitude);
        MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
        baiduMap.animateMapStatus(u);
        // 根据经纬度坐标 找到实地信息，会在接口onGetReverseGeoCodeResult中呈现结果
        mSearch.reverseGeoCode(new ReverseGeoCodeOption().location(latLng));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_location_ok:
                latitude = curLatlng.latitude;
                longitude = curLatlng.longitude;
                break;
            default:
                break;
        }
    }

    @Override
    public void onReceiveLocation(BDLocation bdLocation) {
        if (bdLocation == null || mapView == null) {
            return;
        }
        if (isFirstLoc) {
            isFirstLoc = false;
            myGpslatitude = bdLocation.getLatitude();
            myGpslongitude = bdLocation.getLongitude();
            LatLng ll = new LatLng(myGpslatitude, myGpslongitude);
            setCurrentMapLatLng(ll);
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        setCurrentMapLatLng(latLng);
    }

    @Override
    public boolean onMapPoiClick(MapPoi mapPoi) {
        return false;
    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        setCurrentMapLatLng(marker.getPosition());
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    /**
     * onGetGeoCodeResult 搜索（根据实地信息-->经纬坐标）
     */
    @Override
    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {

    }

    /**
     * onGetReverseGeoCodeResult 搜索（根据坐标-->实地信息）
     */
    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            tv_location.setText("抱歉未能找到结果");
            return;
        }
        tv_location.setText(String.format("选定位置：%s", result.getAddress()));
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        isRun = false;
        thread = null;
        // 销毁定位
        locationClient.stop();
        // 关闭定位图层
        baiduMap.setMyLocationEnabled(false);
        mapView.onDestroy();
        super.onDestroy();
    }
}

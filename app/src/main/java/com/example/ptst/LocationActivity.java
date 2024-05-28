package com.example.ptst;

import android.os.Bundle;

import android.Manifest;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.MapView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.amap.api.maps2d.model.MyLocationStyle;

import java.util.concurrent.CountDownLatch;

public class LocationActivity extends AppCompatActivity implements LocationFragment.OnDataPass {


    private String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION
            , Manifest.permission.ACCESS_COARSE_LOCATION
            , Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS};
    private static final int OPEN_SET_REQUEST_CODE = 100;

    Fragment locationFragment = null;
    Fragment vibrationFragment = null;
    Fragment notificationsFragment = null;

    private MutableLiveData<String> dataFromLocationFragment = new MutableLiveData<>();

    private MutableLiveData<String> dataFromLocationFragment2 = new MutableLiveData<>();

    String module_id_global = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_location);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        CountDownLatch permissioncnt = new CountDownLatch(1);


        //先进行权限申请情况判断，没有权限申请权限
        if (lacksPermission(permissions)) {
            ActivityCompat.requestPermissions(this, permissions, OPEN_SET_REQUEST_CODE);
        } else {
            Log.i("LoginActivity", "");
        }

        AMapLocationClient.updatePrivacyShow(getApplicationContext(), true, true);
        AMapLocationClient.updatePrivacyAgree(getApplicationContext(), true);
        permissioncnt.countDown();

        try {
            permissioncnt.await(); // 等待计数器变为0,link上module后继续下面内容
        } catch (InterruptedException e) {
            e.printStackTrace();
        }




        BottomNavigationView navView = findViewById(R.id.bottom_navigation);
        navView.setOnNavigationItemSelectedListener(item -> {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            hideAllFragment(transaction);
            switch (item.getItemId()) {
                case R.id.navigation_location:
                    if (locationFragment == null) {
                        locationFragment = new LocationFragment();
                        transaction.add(R.id.fragment_container, locationFragment);
                    } else {
                        transaction.show(locationFragment);
                    }
                    break;
                case R.id.navigation_vibration:
                    if (vibrationFragment == null) {
                        vibrationFragment = new VibrationFragment();
                        transaction.add(R.id.fragment_container, vibrationFragment);
                    } else {
                        transaction.show(vibrationFragment);
                        TextView module_id = findViewById(R.id.tv_module_id);
                        module_id.setText(module_id_global);
                    }
                    break;
                case R.id.navigation_notifications:
                    if (notificationsFragment == null) {
                        notificationsFragment = new JITFragment();
                        transaction.add(R.id.fragment_container, notificationsFragment);
                    } else {
                        transaction.show(notificationsFragment);
                        TextView module_id = findViewById(R.id.jit_module_id);
                        module_id.setText(module_id_global);
                    }
                    break;
            }
            transaction.commit();
            return true;
        });



        // 默认选中第一个
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (locationFragment == null) {
            locationFragment = new LocationFragment();
            transaction.add(R.id.fragment_container, locationFragment);
            vibrationFragment = new VibrationFragment();
            transaction.add(R.id.fragment_container, vibrationFragment);
            notificationsFragment = new JITFragment();
            transaction.add(R.id.fragment_container, notificationsFragment);
            transaction.show(locationFragment);
            transaction.hide(vibrationFragment);
            transaction.hide(notificationsFragment);
        } else {
            transaction.show(locationFragment);
        }
        transaction.commit();






//        //获取地图控件引用
//        mMapView = (MapView) findViewById(R.id.mapview);
//        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
//        mMapView.onCreate(savedInstanceState);
//
//        if (aMap == null) {
//            aMap = mMapView.getMap();
//            aMap.setMapLanguage("en");
//            LatLng latLng = new LatLng(22.280102, 114.142767);//构造一个位置
//            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,16));
//        }
//
//        MyLocationStyle myLocationStyle;
//        myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
//        myLocationStyle.interval(2000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
//        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
//        aMap.getUiSettings().setMyLocationButtonEnabled(true); //设置默认定位按钮是否显示，非必需设置。
//        aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
//        myLocationStyle.showMyLocation(true);
    }


    private void hideAllFragment(FragmentTransaction transaction) {

        if (locationFragment != null) {
            transaction.hide(locationFragment);
        }
        if (vibrationFragment != null) {
            transaction.hide(vibrationFragment);
        }
        if (notificationsFragment != null) {
            transaction.hide(notificationsFragment);
        }
    }

    //判断是否获取权限
    public boolean lacksPermission(String[] permissions) {
        for (String permission : permissions) {
            //判断是否缺少权限，true=缺少权限
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onDataPass(String data) {
        // 在这里接收并处理从LocationFragment传递过来的数据
        if(data.contains("Vibration")){
            dataFromLocationFragment.postValue(data);
        }
        else{
            dataFromLocationFragment2.postValue(data);
        }
    }





    public LiveData<String> getDataFromLocationFragment() {
        return dataFromLocationFragment;
    }
    public LiveData<String> getDataFromLocationFragment2() {
        return dataFromLocationFragment2;
    }
//
//
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
//        mMapView.onDestroy();
//    }
//    @Override
//    protected void onResume() {
//        super.onResume();
//        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
//        mMapView.onResume();
//    }
//    @Override
//    protected void onPause() {
//        super.onPause();
//        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
//        mMapView.onPause();
//    }
//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
//        mMapView.onSaveInstanceState(outState);
//    }

}
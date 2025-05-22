package com.example.ptst;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.app.AppCompatActivity;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.MapView;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkRouteResult;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.amap.api.maps2d.model.MyLocationStyle;


import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.model.LatLng;

import android.speech.tts.TextToSpeech;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.transports.WebSocket;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import java.time.Instant;
import java.time.Duration;
import java.time.format.DateTimeFormatter;

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RouteSearch;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;


class SharedObject {
    boolean isConnected = false;
    int lable_status = 0;
}


public class LocationFragment extends Fragment implements AMapLocationListener{

    private MapView mMapView;
    private AMap aMap;

    MyLocationStyle myLocationStyle;

    private EditText et_module_id;

    public class Location {
        private double latitude;
        private double longitude;

        public Location(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public double getLatitude() {
            return latitude;
        }

        //set latitude
        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        //set longitude
        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }
    }

    Location gpslocation = new Location(0, 0);
    Location target_gpslocation = new Location(0, 0);
    private String[] permissions = new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION
            , android.Manifest.permission.ACCESS_COARSE_LOCATION
            , Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS};

    //声明mlocationClient对象
    public AMapLocationClient mlocationClient;
    //声明mLocationOption对象
    public AMapLocationClientOption mLocationOption = null;

    // Declare TextToSpeech variable
    private TextToSpeech textToSpeech;

//    List<double[]> coordinates = new ArrayList<>();

    List<double[]> coordinates_vib = new ArrayList<>();

    //确保线程安全
    private final Object lock = new Object();

//    public boolean isConnected = false;

    private MutableLiveData<Location> gpsLocationLiveData = new MutableLiveData<>();



    public interface OnDataPass {
        void onDataPass(String data);
    }

    private OnDataPass dataPasser;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        dataPasser = (OnDataPass) context;
    }

    public void passData(String data) {
        dataPasser.onDataPass(data);
    }


    public LocationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

//        coordinates.add(new double[]{113.6681, 22.2541});
//        coordinates.add(new double[]{113.9322, 22.2922});
//        coordinates.add(new double[]{114.0081, 22.3233});
//        coordinates.add(new double[]{114.1574, 22.3428});
//        coordinates.add(new double[]{114.1754, 22.3442});
//        coordinates.add(new double[]{114.2349, 22.3278});
//        coordinates.add(new double[]{114.143942, 22.282378});

        coordinates_vib.add(new double[]{113.3134,22.2455});
        coordinates_vib.add(new double[]{113.6681,22.2541});
        coordinates_vib.add(new double[]{113.9322,22.2922});
        coordinates_vib.add(new double[]{114.0081,22.3233});
        coordinates_vib.add(new double[]{114.1574,22.3428});
        coordinates_vib.add(new double[]{114.1754,22.3442});
        coordinates_vib.add(new double[]{114.2349,22.3278});



        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_location, container, false);
        mMapView = view.findViewById(R.id.mapview);
        mMapView.onCreate(savedInstanceState);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedObject sharedObject = new SharedObject();

        et_module_id = view.findViewById(R.id.et_module_id);


        Spinner spinnerStart = view.findViewById(R.id.spinner_start);
        Spinner spinnerEnd = view.findViewById(R.id.spinner_end);
        Button btnStartNavigation = view.findViewById(R.id.btn_start_navigation);
        Map<String, LatLng> locationMap = new HashMap<>();
        locationMap.put("Quon Hing Factory at Jiangmen", new LatLng(22.2044, 113.1107));
        locationMap.put("Hong Kong -Zhuhai-MacaoBridge (Zhuhai Port)", new LatLng(22.214028, 113.54465));
        locationMap.put("Export declaration", new LatLng(22.208883, 113.588472));
        locationMap.put("Hong Kong -Zhuhai -MacaoBridge (Hong Kong Port)", new LatLng(22.30165, 113.973144));


        LocationActivity LocationActivity = (LocationActivity) getActivity();

        // Initialize TextToSpeech
        textToSpeech = new TextToSpeech(getActivity(), status -> {
            if (status != TextToSpeech.ERROR) {
//                textToSpeech.setLanguage(Locale.US);
                textToSpeech.setLanguage(Locale.CHINESE);
            }
        });
        // hardcode for presentation



// 在 onViewCreated 方法中为 btn_scan 设置点击事件
        view.findViewById(R.id.btn_scan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator integrator = new IntentIntegrator(getActivity());
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
                integrator.setPrompt("Scan a QR Code");
                integrator.setCameraId(0); // 使用后置摄像头
                integrator.setBeepEnabled(true); // 扫描成功后播放提示音
                integrator.setBarcodeImageEnabled(false); // 不保存扫描的二维码图片
                integrator.setOrientationLocked(true); // 支持横竖屏
                integrator.forSupportFragment(LocationFragment.this).initiateScan();
            }
        });


        btnStartNavigation.setOnClickListener(v -> {
            String startLocation = spinnerStart.getSelectedItem().toString();
            String endLocation = spinnerEnd.getSelectedItem().toString();

            LatLng startLatLng = locationMap.get(startLocation);
            LatLng endLatLng = locationMap.get(endLocation);

            if (startLatLng != null && endLatLng != null) {
                planRoute(startLatLng, endLatLng);
            } else {
                Toast.makeText(getActivity(), "Invalid start or end location", Toast.LENGTH_SHORT).show();
            }
        });







        view.findViewById(R.id.btn_link).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String module_id = et_module_id.getText().toString();
                // GET LAT AND LONG

                CountDownLatch plan_lock = new CountDownLatch(1);
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MySharedPref", MODE_PRIVATE);
                        String token_global = sharedPreferences.getString("token", "");


                        OkHttpClient client = new OkHttpClient();

                        JSONObject jsonObjectdp = new JSONObject();
                        try {
                            jsonObjectdp.put("module_id", module_id);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

//                        String json = "{\"module_id\": \"" + module_id + "\"" + "}";

                        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObjectdp.toString());

                        Request request = new Request.Builder()
                                .url("http://43.154.250.117:3000/plan_module_get")
                                .addHeader("Content-Type", "application/json")
                                .addHeader("Authorization", "Bearer " + token_global)
                                .post(requestBody)
                                .build();

                        try (Response response = client.newCall(request).execute()) {
                            if (response.isSuccessful()){
                                try {
                                    String jsonData = response.body().string();
                                    JSONObject jsonObject = new JSONObject(jsonData);
                                    String val_plan = jsonObject.get("plan").toString();
                                    JSONObject js_plan = new JSONObject(val_plan);
                                    String val_location_arrival = js_plan.get("location_arrival").toString();
                                    JSONObject js_location_arrival = new JSONObject(val_location_arrival);
                                    String val_location = js_location_arrival.get("location").toString();
                                    JSONObject js_location = new JSONObject(val_location);
                                    target_gpslocation.setLatitude(Double.parseDouble(js_location.get("lat").toString()));
                                    target_gpslocation.setLongitude(Double.parseDouble(js_location.get("lng").toString()));
                                    plan_lock.countDown();
                                } catch (IOException | JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            else{
                                // show error message
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getActivity(), "error in get taget location", Toast.LENGTH_SHORT).show();
                                    }
                                });


                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

                try {
                    plan_lock.await(); // 等待计数器变为0,link上module后继续下面内容
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }




                if (module_id.isEmpty()) {
                    Toast.makeText(getActivity(), "Please enter the module ID", Toast.LENGTH_SHORT).show();
                } else {
                    if (sharedObject.isConnected) {
                        Toast.makeText(getActivity(), "Module"+ module_id +" is already connected", Toast.LENGTH_SHORT).show();
                        return; // 如果已经连接上了，就不再开新线程连接
                    } else {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    //WebSocket 链接
                                    //计时器
                                    CountDownLatch latch = new CountDownLatch(1);
                                    CountDownLatch departure = new CountDownLatch(1);

//                                    for(int i = 0; i < 10; i++) {
//                                        Instant pre = Instant.now();
//                                        Instant now = pre.plus(8, ChronoUnit.HOURS);
//                                        passData("JIT, " + now.toString());
//
//                                        passData("Late departure, " + now.toString());
//
//                                        passData("Vibration, " + now.toString());
//                                    }

                                    ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
                                    //注意前后端io版本问题
                                    IO.Options options = new IO.Options();
                                    options.transports = new String[]{WebSocket.NAME};
                                    options.reconnection = true; // 开启自动重连
                                    options.reconnectionAttempts = 10; // 设置自动重连尝试的次数
                                    options.reconnectionDelay = 1000; // 设置自动重连的延迟，单位是毫秒
                                    options.reconnectionDelayMax = 5000; // 设置自动重连的最大延迟，单位是毫秒
                                    Socket socket = IO.socket("http://43.154.250.117:3000", options);

                                    socket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                                        @Override
                                        public void call(Object... args) {
                                            Exception err = (Exception) args[0];
                                            Log.e("Socket.IO", "Connection Error: " + err.getMessage());
                                        }
                                    });

                                    // Connect to the server
                                    socket.connect();

                                    JSONObject jsonObject = new JSONObject();
                                    jsonObject.put("module_id", module_id);
                                    jsonObject.put("lat", 100.000);
                                    jsonObject.put("lng", 50.000);

                                    socket.emit("register_module_ID", jsonObject);

                                    // Listen for the 'message' event from the server
                                    socket.on("register_module_ID_done", new Emitter.Listener() {
                                        @Override
                                        public void call(Object... args) {
                                            // Handle the message from the server
                                            Log.println(Log.INFO, "messagesocket", args[0].toString());
                                            sharedObject.isConnected = true;
                                            latch.countDown(); // 计数器减1
                                            getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(getActivity(), "Connected to the module", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    });

                                    // show error message form server
                                    socket.on("error", new Emitter.Listener() {
                                        @Override
                                        public void call(Object... args) {
                                            // Handle the message from the server
                                            getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(getActivity(), args[0].toString(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    });

                                    socket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                                        @Override
                                        public void call(Object... args) {
                                            showDevMessage("Disconnected from the server");
                                        }
                                    });


                                    // 等待某俩车辆到达后计算ETA
                                    socket.on("ETA", new Emitter.Listener() {
                                        @Override
                                        public void call(Object... args) {
                                            // Handle the message from the server
                                            showDevMessage("Start Calculating ETA");
                                            double resETA = -1.0;
                                            try {
                                                // for test 22.208883, 113.588472  gpslocation.getLatitude(), gpslocation.getLongitude()
                                                resETA = calETA(gpslocation.getLatitude(), gpslocation.getLongitude());
                                            } catch (AMapException e) {
                                                throw new RuntimeException(e);
                                            }
                                            JSONObject jsonObjectEta = new JSONObject();
                                            try {
                                                jsonObjectEta.put("module_id", module_id);
                                                jsonObjectEta.put("eta", resETA/60); //resETA/60
                                            } catch (JSONException e) {
                                                throw new RuntimeException(e);
                                            }
                                            Log.println(Log.INFO, "messagedev", "ETA: " + resETA/60);
                                            showDevMessage("ETA Value: "+ resETA/60);
                                            socket.emit("send_ETA", jsonObjectEta);
                                        }
                                    });

                                    // 等待某俩车辆到达后计算ETA
                                    socket.on("slow_down", new Emitter.Listener() {
                                        @Override
                                        public void call(Object... args) {
                                            // Handle the message from the server
                                            showDevMessage("JIT Message Received");
                                            Log.println(Log.INFO, "messagedev", "Slow down");
                                            Instant pre = Instant.now();
                                            Instant now = pre.plus(8, ChronoUnit.HOURS);
                                            textToSpeech.setLanguage(Locale.CHINESE);
//                                            String message = "Please slow down, the car in front has not left the territory";
                                            String message = "请减速慢行，前面的车还没有离开工地";
                                            textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null, null);
                                            sendNotification(et_module_id.getText().toString(), "Remind_slow down", now.toString());
                                            passData("JIT, " + now.toString());
                                        }
                                    });


                                    try {
                                        latch.await(); // 等待计数器变为0,link上module后继续下面内容
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }

                                    //执行到这里说明module已经连接上了

                                    //kill the previous thread
//                                executorService.shutdown();
                                    //链接上了再设置module_id_global-------------------------------------------------------------
                                    assert LocationActivity != null;
                                    LocationActivity.module_id_global = module_id;

                                    // get departure time
                                    final String[] departure_time = new String[1];
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {

                                            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MySharedPref", MODE_PRIVATE);
                                            String token_global = sharedPreferences.getString("token", "");


                                            OkHttpClient client = new OkHttpClient();

                                            JSONObject jsonObjectdp = new JSONObject();
                                            try {
                                                jsonObjectdp.put("module_id", module_id);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }

//                                            String json = "{\"module_id\": \"" + module_id + "\"" + "}";

                                            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObjectdp.toString());

                                            Request request = new Request.Builder()
                                                    .url("http://43.154.250.117:3000/search_departure")
                                                    .addHeader("Content-Type", "application/json")
                                                    .addHeader("Authorization", "Bearer " + token_global)
                                                    .post(requestBody)
                                                    .build();

                                            try (Response response = client.newCall(request).execute()) {
                                                if (response.isSuccessful()){
                                                    try {
                                                        String jsonData = response.body().string();
                                                        JSONObject jsonObject = new JSONObject(jsonData);
                                                        String time_departure = jsonObject.get("plan").toString();
                                                        JSONObject jsonObjecttd = new JSONObject(time_departure);
                                                        String time_departure_final = jsonObjecttd.get("time_departure").toString();
                                                        departure_time[0] = time_departure_final;
                                                        departure.countDown();
                                                    } catch (IOException | JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                                else{
                                                    // show error message
                                                    getActivity().runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Toast.makeText(getActivity(), "error in departure_time", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                    sharedObject.isConnected = false;

                                                }
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }).start();

                                    try {
                                        departure.await(); // 等待计数器变为0,link上module后继续下面内容
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }

                                    String finalDeparture_time = departure_time[0];
                                    Log.println(Log.INFO, "departure_time", finalDeparture_time);


                                    final int[] vib_flag = {0};
                                    final int[] late_flag = {0};
//                                    final int[] test_flag = {0};

                                    // send_module_postion to backend, 同时进行JIT判断,   两秒一次运行
                                    executorService.scheduleWithFixedDelay(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                double local_latitude = -1.0;
                                                double local_longitude = -1.0;
                                                synchronized (lock) {
                                                    local_latitude = gpslocation.getLatitude();
                                                    local_longitude = gpslocation.getLongitude();
                                                }
                                                    JSONObject jsonObjectPos = new JSONObject();
                                                    jsonObjectPos.put("module_id", module_id);
                                                    jsonObjectPos.put("lat", local_latitude);
                                                    jsonObjectPos.put("lng", local_longitude);
                                                    socket.emit("send_module_postion", jsonObjectPos);
                                                    // 获取当前时间

                                                    Instant pre = Instant.now();
                                                    Instant now = pre.plus(8, ChronoUnit.HOURS);

//                                                    if(test_flag[0] < 10){
//                                                        passData("JIT, " + now.toString());
//
//                                                        passData("Late departure, " + now.toString());
//
//                                                        passData("Vibration, " + now.toString());
//                                                    }
//                                                    test_flag[0]++;



                                                    // 如果预计出发时间超过一分钟后手机坐标还在工厂坐标范围内的话， 给所有手机用户发送语音提示
                                                    // 22.2044, 113.1107
                                                    if (calculateDistance(local_latitude, local_longitude, 22.2044, 113.1107) < 300) {
                                                        if(late_flag[0] % 3 == 0) {
                                                            Instant givenTime = null;
                                                            if (sharedObject.lable_status != 0) {
                                                                updateModule(module_id, "in factory");
                                                                sharedObject.lable_status = 0;
                                                            }
                                                            givenTime = Instant.parse(finalDeparture_time);

                                                            // 计算时间差
                                                            Duration duration = Duration.between(givenTime, now);
                                                            if (duration.toMinutes() > 1) {
                                                                sendNotification(module_id, "Remind_delay deparature", duration.toString());
                                                                passData("Late departure, " + now.toString());
                                                            }
                                                            Log.println(Log.INFO, "time_gap", String.valueOf(duration.toMinutes()));
                                                        }
                                                        late_flag[0]++;
                                                    }
                                                    //判断车辆是否进入到工地坐标范围内
                                                    // 22.3264, 114.235   22.280572, 114.143484
                                                    else if(calculateDistance(local_latitude, local_longitude, target_gpslocation.getLatitude(), target_gpslocation.getLongitude()) < 300){

                                                        if(sharedObject.lable_status != 1){
                                                            updateModule(module_id, "arrived");
                                                            sharedObject.lable_status = 1;
                                                        }
                                                        JSONObject jsonObject_arrived = new JSONObject();
                                                        jsonObject_arrived.put("module_id", module_id);
                                                        socket.emit("send_arrived_info", jsonObject_arrived);
                                                        showDevMessage("The car has arrived at the destination");
                                                        sharedObject.isConnected = false;
//                                                        socket.disconnect();
                                                        executorService.shutdown();

                                                    }
                                                    else{

                                                        if(sharedObject.lable_status != 2){
                                                            updateModule(module_id, "en-route");
                                                            sharedObject.lable_status = 2;
                                                        }

                                                        double test_latitude = 22.280102;
                                                        double test_longitude = 114.142763;

                                                        //en-route 判断是否经过vibration点
                                                        for (double[] coordinate : coordinates_vib) {
                                                            // coordinate[1], coordinate[0]
                                                            double distance = calculateDistance(local_latitude, local_longitude, coordinate[1], coordinate[0]);
                                                            if (distance < 300) {
                                                                if( vib_flag[0] % 3 == 0){
                                                                    // The distance is less than 500 meters
                                                                    textToSpeech.setLanguage(Locale.CHINESE);
//                                                                    String message = "The vibration is too large, please drive carefully";
                                                                    String message = "振动太大，请小心驾驶";
                                                                    Log.println(Log.INFO, "Vibration", message);
                                                                    textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null, null);
                                                                    sendNotification(et_module_id.getText().toString(), "Remind_vibration", now.toString());
                                                                    try{
                                                                        passData("Vibration, " + now.toString());
                                                                    }
                                                                    catch (Exception e){
                                                                        throw new RuntimeException(e);
                                                                    }
                                                                }
                                                                vib_flag[0]++;
                                                            }
                                                        }
                                                    }


//                                                    getActivity().runOnUiThread(new Runnable() {
//                                                        @Override
//                                                        public void run() {
//                                                            Toast.makeText(getActivity(), gpslocation.getLatitude() + "," + gpslocation.getLongitude(), Toast.LENGTH_SHORT).show();
//                                                        }
//                                                    });

                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }, 0, 2, TimeUnit.SECONDS);  //两秒一次运行


//                                executorService.shutdown();

                                    // 保持链接就行，服务器那边会主动断开
//                                socket.disconnect();

                                    if(!sharedObject.isConnected){
                                        socket.disconnect();
                                    }


                                } catch (URISyntaxException e) {
                                    e.printStackTrace();
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }).start();
                    }
                }
            }
        });


        if (aMap == null) {
            aMap = mMapView.getMap();
            aMap.setMapLanguage("en");
//            LatLng latLng = new LatLng(22.280102, 114.142767);//构造一个位置
//            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,16));
            aMap.moveCamera(CameraUpdateFactory.zoomTo(20));
        }

        aMap.setMyLocationEnabled(true);

        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW);

        myLocationStyle.interval(2000);
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.getUiSettings().setMyLocationButtonEnabled(true);
        aMap.getUiSettings().setCompassEnabled(true);

        myLocationStyle.showMyLocation(true);


        try {
            mlocationClient = new AMapLocationClient(getActivity());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        //设置定位监听
        mlocationClient.setLocationListener(this);
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(5000);
        //设置定位参数
        mlocationClient.setLocationOption(mLocationOption);
        // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
        // 注意设置合适的定位时间的间隔（最小间隔支持为1000ms），并且在合适时间调用stopLocation()方法来取消定位请求
        // 在定位结束后，在合适的生命周期调用onDestroy()方法
        // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
        //启动定位
        mlocationClient.startLocation();

    }



    private void planRoute(LatLng startLatLng, LatLng endLatLng) {

    }



    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (amapLocation != null) {
            if (amapLocation.getErrorCode() == 0) {
                //定位成功回调信息，设置相关消息
                amapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表
                double latitude = amapLocation.getLatitude();//获取纬度
                double longitude = amapLocation.getLongitude();//获取经度

                synchronized(lock) {
                    gpslocation.setLatitude(latitude);
                    gpslocation.setLongitude(longitude);
                    gpsLocationLiveData.postValue(gpslocation);
                }

                amapLocation.getAccuracy();//获取精度信息

                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date(amapLocation.getTime());
                String strDate = df.format(date);//定位时间


            } else {
                //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                Log.e("AmapError","location Error, ErrCode:"
                        + amapLocation.getErrorCode() + ", errInfo:"
                        + amapLocation.getErrorInfo());
            }
        }
    }

    public void showDevMessage(String message){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public int calTransTime(double startLatitude, double startLongitude, double endLatitude, double endLongitude) throws AMapException {

        // 创建起始点和终点的LatLonPoint对象
        LatLonPoint startPoint = new LatLonPoint(startLatitude, startLongitude);
        LatLonPoint endPoint = new LatLonPoint(endLatitude, endLongitude);

        final int[] resduration = {-1};

        CountDownLatch ETA_cnt = new CountDownLatch(1);

        // 创建RouteSearch对象
        RouteSearch routeSearch = new RouteSearch(getActivity());

        // 设置起始和终点
        RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(startPoint, endPoint);
        // 创建驾车路线查询参数
        RouteSearch.DriveRouteQuery query = new RouteSearch.DriveRouteQuery(fromAndTo, RouteSearch.DRIVING_SINGLE_DEFAULT, null, null, "");

        // 设置路线搜索监听器
        routeSearch.setRouteSearchListener(new RouteSearch.OnRouteSearchListener() {
            @Override
            public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {

            }

            @Override
            public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int errorCode) {
                if (errorCode == 1000) { // 1000表示搜索成功
                    ETA_cnt.countDown();
                    if (driveRouteResult != null && driveRouteResult.getPaths() != null && driveRouteResult.getPaths().size() > 0) {
                        DrivePath drivePath = driveRouteResult.getPaths().get(0);
                        // 获取预估时间（单位：秒）
                        resduration[0] = (int) drivePath.getDuration();
                    }
                }
            }

            @Override
            public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int i) {

            }

            @Override
            public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {

            }


        });
        // 开始异步搜索驾车路线
        routeSearch.calculateDriveRouteAsyn(query);

        try {
            ETA_cnt.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return resduration[0];
    }


    public double calETA(double startLatitude, double startLongitude) throws AMapException {

        double resETA = 0;

        List<double []> coordinates = new ArrayList<>();
        coordinates.add(new double[]{22.2044, 113.1107});
        coordinates.add(new double[]{22.214028, 113.54465});
        coordinates.add(new double[]{22.208883, 113.588472});
        coordinates.add(new double[]{22.28995, 113.938726});
        coordinates.add(new double[]{22.30165, 113.973144});
        coordinates.add(new double[]{22.3264, 114.235});

        List<Double> factors = Arrays.asList(1.2192349332370986, 1.044889502762431, 1.2885491216655822, 1.5913370998116763, 1.1714654061022776);

        for (int i = 1; i < coordinates.size(); i++){
            if(resETA == 0 && startLongitude < coordinates.get(i)[1]){
                resETA += calTransTime(startLatitude, startLongitude,coordinates.get(i)[0], coordinates.get(i)[1]) * factors.get(i-1);
            }
            if(resETA != 0){
                resETA += calTransTime(coordinates.get(i-1)[0], coordinates.get(i-1)[1],coordinates.get(i)[0], coordinates.get(i)[1]) * factors.get(i-1);
            }
        }
        return resETA;
    }


    // Haversine公式计算距离
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        distance = Math.pow(distance, 2);

        return Math.sqrt(distance);
    }


    public void updateModule(String module_id,String module_status) {
        new Thread(() -> {

            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MySharedPref", MODE_PRIVATE);
            String token_global = sharedPreferences.getString("token", "");


            OkHttpClient client = new OkHttpClient();

            JSONObject jsonObject_notification = new JSONObject();
            try {
                jsonObject_notification.put("module_id", module_id);
                jsonObject_notification.put("module_status", module_status);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject_notification.toString());

            Request request = new Request.Builder()
                    .url("http://43.154.250.117:3000/module_update")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer " + token_global)
                    .post(requestBody)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()){
                    Log.println(Log.INFO, "Update Module", "Update " + module_status +" Successful");
                }
                else{
                    Log.println(Log.INFO, "Update Module", "Update " + module_status +" Fail");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }



    public boolean sendNotification(String module_id, String notification_type, String time) {
        new Thread(() -> {

            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MySharedPref", MODE_PRIVATE);
            String token_global = sharedPreferences.getString("token", "");


            OkHttpClient client = new OkHttpClient();

            JSONObject jsonObject_notification = new JSONObject();
            try {
                jsonObject_notification.put("time", time);
                jsonObject_notification.put("notification_type", notification_type);
                jsonObject_notification.put("module_id", module_id);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject_notification.toString());

            Request request = new Request.Builder()
                    .url("http://43.154.250.117:3000/send_notification")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer " + token_global)
                    .post(requestBody)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()){
                    Log.println(Log.INFO, "Send Notification", "Send " + notification_type +" Successful");
                }
                else{
                    Log.println(Log.INFO, "Send Notification", "Send " + notification_type +" Fail");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        return true;
    }


    // 在 Fragment 中重写 onActivityResult 方法处理扫描结果
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                // 将扫描结果填充到 et_module_id
                et_module_id.setText(result.getContents());
            } else {
                Toast.makeText(getActivity(), "No QR code found", Toast.LENGTH_SHORT).show();
            }
        }
    }


//    @Override
//    public void onMyLocationChange(android.location.Location location) {
//        double latitude = location.getLatitude();
//        double longitude = location.getLongitude();
//        Log.d("latitude", String.valueOf(latitude+','+longitude));
//        Toast.makeText(getActivity(), latitude + "," + longitude, Toast.LENGTH_SHORT).show();
//    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }
}
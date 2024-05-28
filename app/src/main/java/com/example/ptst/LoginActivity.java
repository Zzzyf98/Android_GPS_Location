package com.example.ptst;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.FormBody;
import okhttp3.Response;

import java.io.IOException;

public class LoginActivity extends AppCompatActivity {

    private EditText et_username;
    private EditText et_password;
    private SharedPreferences msharedPreferences;

    String token_global = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
//        requestWindowFeature(Window.FEATURE_NO_TITLE); //隐藏标题栏
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS); //布局延伸

//        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            getWindow().getDecorView().setSystemUiVisibility(
//                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
//        }
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        //5.0 全透明实现
        //getWindow.setStatusBarColor(Color.TRANSPARENT)
//        Window window = getWindow();
//        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
//        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//        window.setStatusBarColor(Color.TRANSPARENT);


        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

//        getSupportActionBar().hide();



        et_username = findViewById(R.id.et_username);
        et_password = findViewById(R.id.et_password);

        findViewById(R.id.loginbtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Runnable networkTask = new Runnable() {
                    @Override
                    public void run() {
                        String username = et_username.getText().toString();
                        String password = et_password.getText().toString();

                        if(TextUtils.isEmpty(username)||TextUtils.isEmpty(password)){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(LoginActivity.this, "Please enter username and password", Toast.LENGTH_SHORT).show();
                                }
                            });
                            return;
                        }

                        OkHttpClient client = new OkHttpClient();

                        String json = "{\"username\": \"" + username + "\", \"password\": " + password + "}";

                        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);

                        Request request = new Request.Builder()
                                .url("http://43.154.250.117:3000/login")
                                .addHeader("Content-Type", "application/json")
                                .post(requestBody)
                                .build();


                        try (Response response = client.newCall(request).execute()) {
                            if (response.isSuccessful()){

                                // get token
                                try {
                                    String jsonData = response.body().string();
                                    JSONObject jsonObject = new JSONObject(jsonData);
                                    // 现在你可以使用jsonObject.get("key")来获取JSON对象中的数据
                                    token_global = jsonObject.get("token").toString();
                                    SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
                                    SharedPreferences.Editor myEdit = sharedPreferences.edit();
                                    myEdit.putString("token", token_global);
                                    myEdit.apply();

                                } catch (IOException | JSONException e) {
                                    e.printStackTrace();
                                }


                                // jump to location page
                                Intent intent = new Intent(LoginActivity.this, LocationActivity.class);
                                startActivity(intent);
                            }
                            else{
                                // show error message
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(LoginActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } catch (IOException e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(LoginActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                                }
                            });
                            e.printStackTrace();
                        }
                    }
                };

                new Thread(networkTask).start();
            }
        });
    }




}
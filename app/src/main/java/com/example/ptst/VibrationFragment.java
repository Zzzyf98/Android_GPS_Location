package com.example.ptst;

import android.Manifest; // Added for permission
import android.content.Context;
import android.content.pm.PackageManager; // Added for permission
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat; // Added for permission
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper; // Good practice for Handler
import android.util.Log; // For logging
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast; // For permission feedback

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale; // For formatting

public class VibrationFragment extends Fragment {
    private static final String TAG = "VibrationFragment"; // For logging
    private RecyclerView recyclerView;
    private List<String> listData;
    private TextView GPS_value;
    private TextView ACC_value;
    private LocationManager locationManager;
    private SensorManager sensorManager;
    private float[] accelerometerValues = new float[3];
    private Handler handler = new Handler(Looper.getMainLooper()); // Specify Looper
    private Runnable gpsUpdateRunnable;

    private Location mCurrentLocation; // To store the latest location from listener
    private LocationListener mGpsLocationListener;
    private SensorEventListener mAccelerometerListener;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001; // For permission request

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vibration, container, false);

        LocationActivity locationActivity = (LocationActivity) getActivity();

        // TextView module_id = view.findViewById(R.id.tv_module_id); // This was not used in the logic you provided earlier
        GPS_value = view.findViewById(R.id.textView2);
        ACC_value = view.findViewById(R.id.textView3);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        TextView emptyView = view.findViewById(R.id.emptyView);

        listData = new ArrayList<>(); // Initialize listData

        // Initial state for RecyclerView
        if (listData.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            recyclerView.setAdapter(new MyAdapter(listData)); // Set adapter if data exists
        }


        if (locationActivity != null) {
            LiveData<String> dataFromLocationFragment = locationActivity.getDataFromLocationFragment();
            dataFromLocationFragment.observe(getViewLifecycleOwner(), new Observer<String>() {
                @Override
                public void onChanged(String data) {
                    if (listData.isEmpty() && recyclerView.getVisibility() == View.GONE) {
                        recyclerView.setVisibility(View.VISIBLE);
                        emptyView.setVisibility(View.GONE);
                    }
                    listData.add(data);
                    if (recyclerView.getAdapter() == null) {
                        recyclerView.setAdapter(new MyAdapter(listData));
                    } else {
                        // Notify adapter correctly
                        recyclerView.getAdapter().notifyItemInserted(listData.size() - 1);
                        // Optionally scroll to the new item
                        // recyclerView.scrollToPosition(listData.size() - 1);
                    }
                }
            });
        } else {
            Log.e(TAG, "LocationActivity is null in onCreateView");
        }


        locationManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);
        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);

        // Initialize and register listeners
        setupLocationListenerAndRequestUpdates();
        setupAccelerometerListener();

        // Define the timed task for updating GPS display
        gpsUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                Location locationToDisplay = null;
                // Check for location permission
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    // Prioritize location from the active listener
                    if (mCurrentLocation != null) {
                        locationToDisplay = mCurrentLocation;
                    } else {
                        // Fallback to getLastKnownLocation if listener hasn't provided one yet
                        try {
                            locationToDisplay = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (locationToDisplay == null) { // Try network provider as a fallback
                                locationToDisplay = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            }
                        } catch (SecurityException se) {
                            Log.e(TAG, "SecurityException in gpsUpdateRunnable while getting last known location", se);
                            GPS_value.setText("GPS: Security issue.");
                        }
                    }

                    if (locationToDisplay != null) {
                        updateGPS(locationToDisplay.getLatitude(), locationToDisplay.getLongitude());
                    } else {
                        updateGPS(null, null); // Show "Waiting..."
                    }

                } else {
                    GPS_value.setText("GPS: Permission needed.");
                    Log.w(TAG, "Location permission not granted in gpsUpdateRunnable.");
                }
                // Schedule the next run
                handler.postDelayed(this, 2000);
            }
        };

        // Start the timed task
        handler.post(gpsUpdateRunnable);

        return view;
    }

    private void setupLocationListenerAndRequestUpdates() {
        mGpsLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                Log.d(TAG, "onLocationChanged: " + location.getLatitude() + ", " + location.getLongitude());
                // Store the latest location. The runnable will pick it up for display.
                // DO NOT call updateGPS here to adhere to "update every 2 seconds by timer" requirement.
                mCurrentLocation = location;
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.d(TAG, "onStatusChanged: provider=" + provider + ", status=" + status);
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {
                Log.d(TAG, "onProviderEnabled: " + provider);
            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
                Log.d(TAG, "onProviderDisabled: " + provider);
                mCurrentLocation = null; // Location is no longer valid from this provider
                // The runnable will pick this up and show "Waiting..."
            }
        };

        // Check for permissions before requesting updates
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Request permissions
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            GPS_value.setText("GPS: Requesting permission...");
            return; // Wait for permission result before proceeding
        }

        startRequestingLocationUpdates();
    }

    private void startRequestingLocationUpdates() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "startRequestingLocationUpdates: Permissions still not granted.");
            GPS_value.setText("GPS: Permission denied.");
            return;
        }
        try {
            // Request updates from GPS provider
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, mGpsLocationListener);
            // Optionally, request from Network provider for faster first fix or indoor scenarios
            // locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, mGpsLocationListener);
            Log.d(TAG, "Requested location updates.");
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException on requesting location updates", e);
            GPS_value.setText("GPS: Security Exception.");
            Toast.makeText(getContext(), "GPS Security Exception: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    private void setupAccelerometerListener() {
        mAccelerometerListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    System.arraycopy(event.values, 0, accelerometerValues, 0, event.values.length);
                    updateACC(); // ACC updates can be immediate as they are frequent
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // Optional: handle accuracy changes
            }
        };

        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            sensorManager.registerListener(mAccelerometerListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            ACC_value.setText("ACC: Not available");
            Toast.makeText(getContext(), "Accelerometer sensor not available.", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Accelerometer sensor not available.");
        }
    }

    private void updateGPS(Double latitude, Double longitude) {
        if (!isAdded() || GPS_value == null) return; // Ensure fragment is attached and view is available

        String gpsData = (latitude != null && longitude != null)
                ? String.format(Locale.US, "GPS: Lat=%.6f, Lon=%.6f", latitude, longitude) // Format for readability
                : "GPS: Waiting...";
        GPS_value.setText(gpsData);
    }

    private void updateACC() {
        if (!isAdded() || ACC_value == null) return; // Ensure fragment is attached and view is available

        String accData = String.format(Locale.US, "ACC: X=%.3f, Y=%.3f, Z=%.3f",
                accelerometerValues[0], accelerometerValues[1], accelerometerValues[2]);
        ACC_value.setText(accData);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            boolean permissionGranted = false;
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    permissionGranted = true;
                    break;
                }
            }

            if (permissionGranted) {
                Log.d(TAG, "Location permission granted by user.");
                // Permission granted, now we can start requesting updates
                startRequestingLocationUpdates();
            } else {
                Log.w(TAG, "Location permission denied by user.");
                GPS_value.setText("GPS: Permission denied.");
                Toast.makeText(getContext(), "Location permission denied. GPS features may not work.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // If you were to stop updates in onPause, you would restart them here.
        // For this example, we start them in onCreateView (after permission check)
        // and stop them in onDestroyView.
        // If the handler was stopped in onPause, restart it:
        if (handler != null && gpsUpdateRunnable != null) {
            // Check if runnable is already scheduled to avoid multiple posts
            handler.removeCallbacks(gpsUpdateRunnable); // Remove any existing callbacks first
            handler.post(gpsUpdateRunnable); // Then post anew
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // To conserve battery, you might want to stop the handler here.
        // If you do, ensure you restart it in onResume().
        // handler.removeCallbacks(gpsUpdateRunnable);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView called.");
        if (handler != null && gpsUpdateRunnable != null) {
            handler.removeCallbacks(gpsUpdateRunnable);
            Log.d(TAG, "Removed gpsUpdateRunnable callbacks.");
        }
        if (locationManager != null && mGpsLocationListener != null) {
            locationManager.removeUpdates(mGpsLocationListener);
            mGpsLocationListener = null;
            Log.d(TAG, "Removed GPS location updates.");
        }
        if (sensorManager != null && mAccelerometerListener != null) {
            sensorManager.unregisterListener(mAccelerometerListener);
            mAccelerometerListener = null;
            Log.d(TAG, "Unregistered accelerometer listener.");
        }
        // Nullify views to help prevent memory leaks (though Fragment handles this for views from onCreateView)
        GPS_value = null;
        ACC_value = null;
        recyclerView = null;
    }

    // MyAdapter class - unchanged from original
    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
        private final List<String> data;

        MyAdapter(List<String> data) {
            this.data = data;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_list_item, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            // SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // This was not used for itemData
            // Date date = new Date(); // This was not used for itemData
            // String strDate = df.format(date); // This was not used for itemData

            String itemData = data.get(position);

            holder.statusText.setText(itemData); // This is the data from LocationFragment
            holder.messageText.setText("Your message has been sent."); // This seems to be a static message
            holder.imageView.setImageResource(R.drawable.baseline_announcement_24); // This is a static image
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            TextView statusText;
            TextView messageText;
            ImageView imageView;

            MyViewHolder(View itemView) {
                super(itemView);
                statusText = itemView.findViewById(R.id.status_text);
                messageText = itemView.findViewById(R.id.message_text);
                imageView = itemView.findViewById(R.id.my_image);
            }
        }
    }
}
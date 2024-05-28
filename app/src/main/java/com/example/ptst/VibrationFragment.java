package com.example.ptst;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class VibrationFragment extends Fragment {
    private RecyclerView recyclerView;
    private List<String> listData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vibration, container, false);

        LocationActivity LocationActivity = (LocationActivity) getActivity();

        TextView module_id = view.findViewById(R.id.tv_module_id);

        // Set the module ID
//        assert LocationActivity != null;
//        module_id.setText(LocationActivity.module_id_global);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        TextView emptyView = view.findViewById(R.id.emptyView);

        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);

        // Initialize your data
        listData = new ArrayList<>();


        LiveData<String> dataFromLocationFragment = LocationActivity.getDataFromLocationFragment();
        // 观察LiveData
        dataFromLocationFragment.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String data) {
                recyclerView.setVisibility(View.VISIBLE);
                emptyView.setVisibility(View.GONE);
                // 当LiveData的数据发生变化时，更新statusText
                listData.add(data);
                recyclerView.setAdapter(new MyAdapter(listData));
            }
        });

        // Set the adapter


        return view;
    }

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

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date();
            String strDate = df.format(date);//定位时间

            String itemData = data.get(position);

            holder.statusText.setText(itemData);
            holder.messageText.setText("Your message has been sent.");
            holder.imageView.setImageResource(R.drawable.baseline_announcement_24);
            // Set your image or other data here
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
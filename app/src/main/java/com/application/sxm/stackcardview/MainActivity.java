package com.application.sxm.stackcardview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.application.sxm.stackcardview.stackcard.StackCardView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private StackCardView mStackCard;
    private StackItemAdapter mAdapter;
    private ArrayList<Integer> mViewIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {

        mStackCard = findViewById(R.id.stackview);
        mViewIds.add(R.drawable.icon_001);
        mViewIds.add(R.drawable.icon_002);
        mViewIds.add(R.drawable.icon_003);
        mAdapter = new StackItemAdapter();
        mStackCard.setAdapter(mAdapter);
        mStackCard.post(new Runnable() {
            @Override
            public void run() {
                mStackCard.startAutoPlay();
            }
        });
    }

    private class StackItemAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mViewIds.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.stack_item, parent, false);
                holder = new ViewHolder();
                holder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.imageView.setImageResource(mViewIds.get(position));
            return convertView;
        }
    }

    public static class ViewHolder {
        public ImageView imageView;
    }
}

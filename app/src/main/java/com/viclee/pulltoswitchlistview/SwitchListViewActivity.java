package com.viclee.pulltoswitchlistview;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.Arrays;
import java.util.List;

public class SwitchListViewActivity extends Activity implements SwitchListView.SwitchListViewListener {
    private SwitchListView listView;
    private ContentAdapter listViewAdapter;

    private ImageView mWuseTopicDetailMask;

    private List<String> data = Arrays.asList(new String[]{"item1", "item2", "item3","item4", "item5", "item6","item7", "item8", "item9"});

    class ViewHolder {
        ImageView pic1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init() {
        mWuseTopicDetailMask = (ImageView) findViewById(R.id.screen_shot_scroll_layer);

        listView = (SwitchListView) findViewById(R.id.switchlistview);
        listView.setXListViewListener(this);

        listViewAdapter = new ContentAdapter(this);
        listView.setAdapter(listViewAdapter);
        listViewAdapter.setData(data);

        listView.setHeaderNoMore(false);

        listView.mHeaderViewContent.setBackgroundDrawable(getResources().getDrawable(R.drawable.switchlistview_item_background));
        listView.mFooterViewContent.setBackgroundDrawable(getResources().getDrawable(R.drawable.switchlistview_item_background));
    }

    @Override
    public void onSwitchListViewRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                listView.stopRefresh();
                listView.stopLoadMore();
                listView.scrollScreenShot(true, mWuseTopicDetailMask);
            }
        }, 1000);
    }

    @Override
    public void onSwitchListViewLoadMore() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                listView.stopRefresh();
                listView.stopLoadMore();
                listView.scrollScreenShot(false, mWuseTopicDetailMask);
            }
        }, 1000);
    }

    private class ContentAdapter extends BaseAdapter {
        private Context ctx;

        private LayoutInflater inflater;
        private List<String> data;

        public ContentAdapter(Context context) {
            ctx = context;
            inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void setData(List<String> list) {
            if (data != list) {
                data = list;
                notifyDataSetChanged();
            }
        }

        @Override
        public int getCount() {
            if (null != data) {
                return data.size();
            } else {
                return 0;
            }
        }

        @Override
        public Object getItem(int position) {
            if (null != data) {
                return data.get(position);
            } else {
                return position;
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.switchlistview_cell, null);
                holder = new ViewHolder();
                holder.pic1 = (ImageView) convertView
                        .findViewById(R.id.item_image);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            return convertView;
        }
    }
}

package com.example.apkextracter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class AppAdapter extends ArrayAdapter<AppList> {
    Context mContext;
    int resourceId;
    List<AppList> mData = null;
    public AppAdapter( Context context, int resource, List<AppList> data) {
        super(context, resource, data);
        this.mContext = context;
        this.resourceId = resource;
        this.mData = data;
    }
    extractButtonClickListener mCallBack;
    public interface extractButtonClickListener
    {
        void onClickExtractButton(View v, int position, String Path, String apkName);
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View row = convertView;
        AppHolder appHolder = null;
        if(row==null)
        {
            LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            row = layoutInflater.inflate(resourceId,parent,false);
            appHolder = new AppHolder();
            appHolder.appNameView = row.findViewById(R.id.nameTextView);
            appHolder.appImageView = row.findViewById(R.id.iconImageView);
            appHolder.extractBtn = row.findViewById(R.id.extractBtn);
            appHolder.shareBtn = row.findViewById(R.id.shareBtn);
            row.setTag(appHolder);
        }
        else
        {
            appHolder = (AppHolder)row.getTag();
        }
        final AppList appList = mData.get(position);
        appHolder.appNameView.setText(appList.name);
        appHolder.appImageView.setImageDrawable(appList.icon);
        appHolder.extractBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCallBack!=null)
                    mCallBack.onClickExtractButton(v,position,appList.apkPath,appList.name);
            }
        });
        appHolder.shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCallBack!=null)
                    mCallBack.onClickExtractButton(v,position,appList.apkPath,appList.name);
            }
        });
        /*row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext,appList.name,Toast.LENGTH_SHORT).show();
            }
        });*/
        return row;
    }

    @Override
    public AppList getItem(int position) {
        return super.getItem(position);
    }

    private static class AppHolder
    {
        TextView appNameView;
        ImageView appImageView;
        Button extractBtn;
        Button shareBtn;
    }
}

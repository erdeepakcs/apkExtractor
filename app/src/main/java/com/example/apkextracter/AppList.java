package com.example.apkextracter;

import android.graphics.drawable.Drawable;

public class AppList {
    public String name;
    public Drawable icon;
    public String apkPath;
    public String apkSize;

    public AppList(String _name,Drawable _icon,String _apkPath, String _apkSize)
    {
        this.name = _name;
        this.icon = _icon;
        this.apkPath = _apkPath;
        this.apkSize = _apkSize;
    }
}

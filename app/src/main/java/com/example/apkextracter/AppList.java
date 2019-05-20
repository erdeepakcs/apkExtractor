package com.example.apkextracter;

import android.graphics.drawable.Drawable;

public class AppList {
    public String name;
    public Drawable icon;
    public String apkPath;

    public AppList(String _name,Drawable _icon,String _apkPath)
    {
        this.name = _name;
        this.icon = _icon;
        this.apkPath = _apkPath;
    }

    public String getName()
    {
        return name;
    }

    public Drawable getIcon()
    {
        return icon;
    }
}

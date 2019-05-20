package com.example.apkextracter;


import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import android.Manifest;


/**
 * A simple {@link Fragment} subclass.
 */
public class AppListFragment extends Fragment implements AppAdapter.extractButtonClickListener{

    private static final int PERMISSION_REQUEST_CODE = 200;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myFragmentView = inflater.inflate(R.layout.fragment_app_list,container,false);
        return myFragmentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(!checkPermission())
            getPermission();
        int layout = android.R.layout.simple_list_item_activated_1;
        final ListView listView = getActivity().findViewById(R.id.installedListView);
        List<AppList> lists = getInstalledApps();
        AppAdapter appAdapter = new AppAdapter( getActivity().getApplicationContext(),R.layout.row,lists);
        appAdapter.mCallBack =this;
        if(listView!=null)
            listView.setAdapter(appAdapter);
    }
    private List<AppList> getInstalledApps()
    {
        List<AppList> res = new ArrayList<>();
        List<PackageInfo> packageInfos = getActivity().getPackageManager().getInstalledPackages(0);
        for(int i=0;i<packageInfos.size();i++)
        {
            PackageInfo pInfo = packageInfos.get(i);
            if(isSystemPackage(pInfo)==false)
            {
                String name = pInfo.applicationInfo.loadLabel(getActivity().getPackageManager()).toString();
                Drawable icon = pInfo.applicationInfo.loadIcon(getActivity().getPackageManager());
                File apkFile = new File(pInfo.applicationInfo.publicSourceDir);

                if(apkFile.exists())
                    res.add(new AppList(name,icon,apkFile.getAbsolutePath()));
                else
                    res.add(new AppList(name,icon,""));
            }
        }
        return res;
    }
    private boolean isSystemPackage(PackageInfo packageInfo)
    {
        return ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM)!=0)?true:false;
    }

    public void copyFile(File sourceFile, File destFile, String fileName)
    {
        try {
            String path = destFile.getAbsolutePath()+"/"+fileName+".apk";
            FileInputStream fRead = new FileInputStream(sourceFile);
            FileOutputStream fWrite = new FileOutputStream(new File(path));
            byte[] buffer = new byte[1024];
            int len;
            while ((len=fRead.read(buffer))>0)
                fWrite.write(buffer,0,len);
            fRead.close();
            fWrite.close();
        }
        catch (IOException ex){
            Toast.makeText(getActivity(),ex.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }
    private boolean checkPermission()
    {
        if(ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
            return false;
        else
            return true;
    }
    private  void getPermission()
    {
        ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},PERMISSION_REQUEST_CODE);
    }
    @Override
    public void onClickExtractButton(View v, int position, String sourcePath, String apkName) {
        try {
            switch (v.getId()) {
                case R.id.extractBtn:
                    File getApkFile = new File(sourcePath);
                    String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/MyAppFolder";
                    File destFile = new File(path);
                    if(!destFile.exists())
                        destFile.mkdir();
                    try
                    {
                        copyFile(getApkFile,destFile,apkName);
                        Toast.makeText(getActivity(),"APK created: "+apkName,Toast.LENGTH_SHORT).show();
                    }
                    catch (Exception ex)
                    {
                        Toast.makeText(getActivity(),ex.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.shareBtn:
                    path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/MyAppFolder/"+apkName+".apk";
                    File shareFile= new File(path);
                    if(shareFile.exists()) {
                        Intent shareIntent = new Intent();
                        shareIntent.setAction(Intent.ACTION_SEND);
                        Uri apkUri = FileProvider.getUriForFile(getActivity().getApplicationContext(), BuildConfig.APPLICATION_ID, shareFile);
                        shareIntent.putExtra(Intent.EXTRA_STREAM, apkUri);
                        shareIntent.setType("application/vnd.android.package-archive");
                        shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(Intent.createChooser(shareIntent, "Share Item"));
                    }
                    else{
                        Toast.makeText(getActivity(),"APK file not exits! Extract it", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
        catch (Exception ex)
        {
            Toast.makeText(getActivity(),ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}

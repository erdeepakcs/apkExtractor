package com.example.apkextracter;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import android.Manifest;


/**
 * A simple {@link Fragment} subclass.
 */
public class AppListFragment extends Fragment implements AppAdapter.extractButtonClickListener{

    private static final int PERMISSION_REQUEST_CODE = 200;
    private static final DecimalFormat format = new DecimalFormat("#.##");
    private static final long MiB = 1024 * 1024;
    private static final long KiB = 1024;
    private int currentViewMode = 0;
    static final int VIEW_MODE_LISTVIEW = 0;
    static final int VIEW_MODE_GRIDVIEW = 1;
    View myFragmentView;
    ListView listView;
    GridView gridView;
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.switchItem:
                Fragment fragment =  getActivity().getSupportFragmentManager().findFragmentById(R.id.container);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .detach(fragment)
                        .attach(fragment)
                        .commit();
                if(VIEW_MODE_LISTVIEW == currentViewMode) {
                    currentViewMode = VIEW_MODE_GRIDVIEW;
                    item.setIcon(R.drawable.ic_action_grid);
                }
                else {
                    currentViewMode = VIEW_MODE_LISTVIEW;
                    item.setIcon(R.drawable.ic_action_list);
                }

                switchView();
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("ViewMode",currentViewMode);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("currentViewMode",currentViewMode);
                editor.commit();
                break;
        }
        return true;
    }

    private void switchView() {
        List<AppList> lists = getInstalledApps();
        if(VIEW_MODE_LISTVIEW == currentViewMode)
        {
            //final ListView listView = getActivity().findViewById(R.id.installedListView);
            AppAdapter appAdapter = new AppAdapter( getActivity().getApplicationContext(),R.layout.row,lists);
            appAdapter.mCallBack =this;
            if(listView!=null)
                listView.setAdapter(appAdapter);
        }
        else
        {
            //final GridView gridView = getActivity().findViewById(R.id.myGridView);
            AppAdapter appAdapter = new AppAdapter( getActivity().getApplicationContext(),R.layout.grid,lists);
            appAdapter.mCallBack =this;
            if(gridView!=null)
                gridView.setAdapter(appAdapter);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("ViewMode", Context.MODE_PRIVATE);
        currentViewMode = sharedPreferences.getInt("currentViewMode", VIEW_MODE_LISTVIEW);
        if(VIEW_MODE_LISTVIEW == currentViewMode)
            myFragmentView = inflater.inflate(R.layout.fragment_app_list,container,false);
        else
            myFragmentView = inflater.inflate(R.layout.fragment_app_grid,container,false);
        return myFragmentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(!checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE))
            getPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        if(!checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE))
            getPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        listView = getActivity().findViewById(R.id.installedListView);
        gridView = getActivity().findViewById(R.id.myGridView);
        switchView();
        //final GridView gridView = getActivity().findViewById(R.id.myGridView);
        //List<AppList> lists = getInstalledApps();
        //AppAdapter appAdapter = new AppAdapter( getActivity().getApplicationContext(),R.layout.grid,lists);
        //final ListView listView = getActivity().findViewById(R.id.installedListView);
        //List<AppList> lists = getInstalledApps();
        //AppAdapter appAdapter = new AppAdapter( getActivity().getApplicationContext(),R.layout.row,lists);
        //appAdapter.mCallBack =this;
        //if(gridView!=null)
        //    gridView.setAdapter(appAdapter);
        //if(listView!=null)
          //  listView.setAdapter(appAdapter);
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
                    res.add(new AppList(name,icon,apkFile.getAbsolutePath(),getFileSize(apkFile)));
                else
                    res.add(new AppList(name,icon,"","0 MB"));
            }
        }
        return res;
    }
    private String getFileSize(File file)
    {
        final double length = file.length();
        if(length>MiB)
            return format.format(length/MiB)+" MB";
        if(length>KiB)
            return format.format(length/KiB)+" KB";
        return "0 MB";
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
            Toast.makeText(getActivity(),"APK created: "+fileName,Toast.LENGTH_SHORT).show();
        }
        catch (IOException ex){
            Toast.makeText(getActivity(),ex.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }
    private boolean checkPermission(String permissionMode)
    {
        if(ContextCompat.checkSelfPermission(getActivity(),permissionMode)!= PackageManager.PERMISSION_GRANTED)
            return false;
        else
            return true;
    }
    private  void getPermission(String permissionMode)
    {
        ActivityCompat.requestPermissions(getActivity(),new String[]{permissionMode},PERMISSION_REQUEST_CODE);
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
                        startActivity(Intent.createChooser(shareIntent, "Share APK File with"));
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

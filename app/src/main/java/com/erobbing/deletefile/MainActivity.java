package com.erobbing.deletefile;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class MainActivity extends Activity {
    private File file;
    private ArrayList<FileInfo> sortedList;
    private static final String PATH = "mnt/sdcard/log";//"/data/media/0/log";
    private static final int TIME_INTERVAL = 10 * 60 * 1000;//10 minutes
    private static final int SPACE_PRE = 300;//300 M

    private Handler delHandler = new Handler();
    private Runnable delRunnable = new Runnable() {
        @Override
        public void run() {
            if (getFolderSize(file) > SPACE_PRE) {
                clearFiles(file);
            }
            delHandler.postDelayed(delRunnable, TIME_INTERVAL);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        delHandler.postDelayed(delRunnable, TIME_INTERVAL);
        file = new File(PATH);
        Log.e("====", "==========file.isDirectory()=" + getFolderSize(file));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        delHandler.removeCallbacks(delRunnable);
    }

    /**
     * @param file
     * @desc remove files
     */
    private void clearFiles(File file) {
        getSortedList(file);
        int num = sortedList.size();
        for (int i = 0; i < sortedList.size(); i++) {
            if (i < num / 2) {
                File file1 = new File(PATH + "/" + sortedList.get(i).name);
                if (file1.exists() && !file1.isDirectory()) {
                    file1.delete();
                    Log.e("====", "=============name=" + file1.getName());
                }
            }
        }
    }

    /**
     * @param file
     * @return ArrayList<FileInfo>
     * @desc sort list by timestamp
     */
    private ArrayList<FileInfo> getSortedList(File file) {
        File[] fileList1 = file.listFiles();
        sortedList = new ArrayList<FileInfo>();
        for (int i = 0; i < fileList1.length; i++) {
            //File file = files[i];
            file = fileList1[i];
            FileInfo fileInfo = new FileInfo();
            fileInfo.name = file.getName();
            fileInfo.time = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date(file.lastModified()));
            sortedList.add(fileInfo);
            //Log.e("====","======getSortedList==" + fileInfo.time);
        }
        Collections.sort(sortedList, new FileComparator());
        return sortedList;
    }

    /**
     * @desc FileComparator compare files by timestamp
     */
    public class FileComparator implements Comparator<FileInfo> {
        public int compare(FileInfo file1, FileInfo file2) {
            if (Long.parseLong(file1.time) < Long.parseLong(file2.time)) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    /**
     * @desc list element FileInfo
     */
    private class FileInfo {
        String name;
        String time;
    }

    /**
     * @param file
     * @return long
     * @desc get target folder size
     */
    public long getFolderSize(File file) {
        long size = 0;
        try {
            File[] fileList = file.listFiles();
            for (int i = 0; i < fileList.length; i++) {
                if (fileList[i].isDirectory()) {
                    size = size + getFolderSize(fileList[i]);
                } else {
                    size = size + fileList[i].length();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size / 1024 / 1024;
    }
}

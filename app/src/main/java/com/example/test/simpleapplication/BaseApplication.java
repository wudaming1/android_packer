package com.example.test.simpleapplication;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.zip.ZipFile;

/**
 * Created by wudaming on 16/9/14.
 */
public class BaseApplication extends Application{

    public static final int SHORT_LENGTH = 2;
    public static final byte MAGIC[] = {'!','Z','X','K','!'};

    private static String channel;


    private static BaseApplication mInstance;

    public String getChannel() {
        if (TextUtils.isEmpty(channel)){
            channel = "没有comment";
        }
        return channel;
    }

    public static BaseApplication getInstance(){
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        Long current  = System.currentTimeMillis();
        initChannel();
        Log.e("wdm","耗时:"+(System.currentTimeMillis()-current));
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void initChannel() {
        ApplicationInfo appinfo = getApplicationInfo();
        String sourceDir = appinfo.sourceDir;
        File zipfile = null;
        try{

            zipfile = new File(sourceDir);
            channel = readZipComment(zipfile);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private String readZipComment(File file) throws IOException {
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(file, "r");
            long index = raf.length();
            Log.e("wdm","文件长度(byte):"+index);
            byte[] buffer = new byte[MAGIC.length];
            index -= MAGIC.length;
            // read magic bytes
            raf.seek(index);
            raf.readFully(buffer);
            Log.e("wdm","标识:"+Arrays.toString(buffer));
            // if magic bytes matched
            if (isMagicMatched(buffer)) {
                index -= SHORT_LENGTH;
                raf.seek(index);
                // read content length field
                int length = readShort(raf);
                if (length > 0) {
                    index -= length;
                    raf.seek(index);
                    // read content bytes
                    byte[] bytesComment = new byte[length];
                    raf.readFully(bytesComment);
                    return new String(bytesComment, "UTF-8");
                }
            }
        } finally {
            if (raf != null) {
                raf.close();
            }
        }
        return null;
    }

    private boolean isMagicMatched(byte[] buffer){
        boolean result = true;
        for (int i = 0;i<MAGIC.length;i++){
            if (MAGIC[i] != buffer[i]){
                result =false;
            }
        }
        Log.e("wdm","MAGIC标识:"+ Arrays.toString(MAGIC));
        return result;

    }

    private int readShort(RandomAccessFile file) throws IOException {
        byte[] length = new byte[SHORT_LENGTH];
        file.readFully(length);
        int size = ((length[0])|length[1]<<8);
        Log.e("wdm","渠道信息长度:"+size);
        return size;
    }
}

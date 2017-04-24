package com.yingwei.testing.testpenn2.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.yingwei.testing.testpenn2.SampleView;
import com.yingwei.testing.testpenn2.doodleback.DotBack;
import com.yingwei.testing.testpenn2.doodleback.SampleViewBack;
import com.yingwei.testing.testpenn2.trans.Dot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;

import static java.lang.System.in;

public class ReplayActivity extends AppCompatActivity {

    private static final String TAG = "ReplayActivity";

    String path;

    File downFile;

    public static String savePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/AUDIO/taskManagerFile";

    private SampleView mSampleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSampleView = new SampleView(getApplicationContext());
        setContentView(mSampleView);

        Intent intent = getIntent();
        if (intent != null) {
            path = intent.getStringExtra("path");
            downFile = new File(path);
        }


        readGZFile(path);
//        String fileContent = readFile();
//        unZipFile(fileContent);
        //TODO 1.解压缩
        //TODO 2.读取
//        Log.e(TAG, "fileContent: " + fileContent);
    }
    public int byteArrayToInt(byte[] b){
        int v0 = (b[3] & 0xff) << 24;
        int v1 = (b[2] & 0xff) << 16;
        int v2 = (b[1] & 0xff) << 8;
        int v3 = (b[0] & 0xff) ;
        return v0 + v1 + v2 + v3;
    }
    private void readGZFile(String fileName) {
        StringBuffer sb = new StringBuffer();
        if (!downFile.exists() || downFile.isDirectory()) {
            Log.e("TAG", "file not exist");
            downFile.mkdirs();
        }

        try {
            GZIPInputStream gis = new GZIPInputStream(new FileInputStream(downFile));

//            byte lengthData[] = new byte[4];
//            int count1 = gis.read(lengthData, 0, 4);
//            int firstValue = byteArrayToInt(lengthData);///(lengthData[3] << 24) & 0xffffffff | (lengthData[2] << 16)&0xffffff | lengthData[1] << 8 & 0xffff | lengthData[0] & 0xff;
//
//            byte timeData[] = new byte[4];
//            int count2 = gis.read(timeData, 0, 4);
//            int timeStamp = byteArrayToInt(timeData);
//
//            int length = firstValue - 8;
//            byte[] data = new byte[length];
//            int count3 = gis.read(data, 0, length);
//            String points = new String(data);

            int index = 0;
            ArrayList<Dot> dotList = new ArrayList<>();
            byte lengthData[] = new byte[4];
            int count = 0;
            while((count = gis.read(lengthData, 0, 4)) != -1){
                Log.e(TAG, "" + index++);
                if ( 1 == index){
                   Log.e(TAG, "index = " + index);
                }
                int length = byteArrayToInt(lengthData) - 8;
                byte timeData[] = new byte[4];
                gis.read(timeData, 0, 4);
                int timeStamp = byteArrayToInt(timeData);
//                int timeStemp = byteArrayToIntReverse(timeData);
                byte[] pointData = null;
                if(length > 4096){
                    pointData = new byte[4096];
                    gis.read(pointData, 0, 4096);
                }
                else{
                    pointData = new byte[length];
                    gis.read(pointData, 0, length);
                }

                String points = new String(pointData);
                String point[] = points.split(";");
                for ( String eachItem: point){ //1:12.37,12.24,34,17,1492150044850,-16777216 //2:12.38,12.22,97,18,1492150044858,-16777216 //
                    Dot dotback = Dot.unpack(eachItem);
                    if (dotback != null){
                        dotList.add(dotback);
                    }
                }

            }

            mSampleView.onTransaction("13260398606", dotList);
            String outFileName = fileName.substring(fileName.lastIndexOf("/")+1, fileName.lastIndexOf("."))+"file.txt";
//            File outDic = new File(savePath);
//            if (outDic.isDirectory() && !outDic.exists()){
//                outDic.mkdirs();
//            }
//            String localFile = outDic.getAbsolutePath() + "/" + outFileName;
//            File f = new File(savePath + outFileName);
//            File f2 = new File(localFile);
//            if (!f.exists()){
//                boolean res = f.createNewFile();
//                if (!res){
//                    Log.e(TAG, "createNewFile error!");
//                }
//            }
//            out = new FileOutputStream(f);

//            byte[] buf = new byte[1024];
//            int len;
//            while((len = in.read(buf, 0, buf.length)) != -1){
//                out.write(buf, 0, len);
//            }

//            BufferedReader br = new BufferedReader(new InputStreamReader(new BufferedInputStream(in), "utf-8"));
//            char[] chars = new char[4];
//            br.read(chars, 0, 4);
//            FileReader reader = new FileReader(downFile);
//            String temp = br.readLine();
//            while(temp != null){
//                sb.append(temp);
//                temp = br.readLine();
//            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
//            if (out != null){
//                try {
//                    out.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
        }
        Log.e(TAG, "end");
        Log.e(TAG, sb.toString());
    }


    private String readFile() {
        if (!downFile.exists() || downFile.isDirectory()) {
            Log.e("TAG", "file not exist");
        }

        BufferedReader br = null;
        StringBuffer sb = new StringBuffer();
        try {
            br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(downFile))));
//            br = new BufferedReader(new InputStreamReader(new FileInputStream(downFile)));
//            br = new BufferedReader(new FileReader(downFile));
            String temp = null;
            temp = br.readLine();
//            char[] chars = new char[10];
//            int len;
//            if ((len = br.read(chars)) != -1){
//
//            }
//            int reader0 = chars[0] & 0xff;
            while (temp != null) {
                sb.append(temp);
                temp = br.readLine();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    private void unZipFile(String content) {

        byte[] contentByte = content.getBytes();
        for (int i = 0; i < contentByte.length; i++) {
            int unsignedByte = contentByte[i] >= 0 ? contentByte[i] : contentByte[i] + 256;
//            Log.e(TAG, String.format("%04x", contentByte[i]));
            Log.e(TAG, String.format("%04x", unsignedByte));
        }

        Inflater inflater = new Inflater();
        inflater.setInput(contentByte, 0, contentByte.length);

        inflater.end();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy");
    }

    public int byteArrayToIntReverse(byte[] b){
        int v0 = (b[0] & 0xff) << 24;
        int v1 = (b[1] & 0xff) << 16;
        int v2 = (b[2] & 0xff) << 8;
        int v3 = (b[3] & 0xff) ;
        return v0 + v1 + v2 + v3;
    }
}

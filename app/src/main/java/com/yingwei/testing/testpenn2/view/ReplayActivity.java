package com.yingwei.testing.testpenn2.view;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.yingwei.testing.testpenn2.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ReplayActivity extends AppCompatActivity {

    private static final String TAG = "ReplayActivity";

    String path;

//    File downFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_replay);

        Intent intent = getIntent();
        if (intent != null) {
            path = intent.getStringExtra("path");
//            downFile = new File(path);
        }

        String fileContent = readFile(path);
        //TODO 1.解压缩
        //TODO 2.读取
        Log.e(TAG, "fileContent: " + fileContent);
    }

    private String readFile(String path) {

        File file = new File(path);
        if (!file.exists() || file.isDirectory()){
            Log.e("TAG", "file not exist");
        }

        BufferedReader br = null;
        StringBuffer sb = new StringBuffer();
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            String temp = null;
            temp = br.readLine();
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
}

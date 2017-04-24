package com.yingwei.testing.testpenn2.retrofitutil;


import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Created by jiahe008_lvlanlan on 2017/3/9.
 */
public class RetrofitWrapper {

    private static RetrofitWrapper instance;
    private Retrofit mRetrofit;

    private RetrofitWrapper(){
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

        OkHttpClient client = new OkHttpClient();
        client.newBuilder().connectTimeout(15, TimeUnit.SECONDS)
                .retryOnConnectionFailure(false)
                .cookieJar(new JavaNetCookieJar(cookieManager))
                .build();

        //不让回调回调在主线程中
        ExecutorService executorService = Executors.newFixedThreadPool(1);

        //初始化 添加转换工厂
        mRetrofit = new Retrofit.Builder()
                .baseUrl("http://115.28.141.237:8888/")
                .addConverterFactory(GsonConverterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
//                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .callbackExecutor(executorService) //默认CallBack回调在主线程进行,当设置下载大文件时需设置注解@Stream 不加这句话会报android.os.NetworkOnMainThreadException
                .build();
    }

    public static RetrofitWrapper getInstance(){
        if (instance == null){
            synchronized (RetrofitWrapper.class){
                if (instance == null){
                    instance = new RetrofitWrapper();
                }
            }
        }
        return instance;
    }

    public <T> T create(Class<T> service){
        return mRetrofit.create(service);
    }
}

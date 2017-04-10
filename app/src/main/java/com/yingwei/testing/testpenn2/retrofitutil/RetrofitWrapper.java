package com.yingwei.testing.testpenn2.retrofitutil;

import com.yw.testrecyclerview.utils.Constant;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by jiahe008_lvlanlan on 2017/3/9.
 */
public class RetrofitWrapper {

    private static RetrofitWrapper instance;
    private Retrofit mRetrofit;

    private RetrofitWrapper(){
        //初始化 添加转换工厂
        mRetrofit = new Retrofit.Builder().baseUrl(Constant.BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
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

package com.yingwei.testing.testpenn2.retrofitutil.intf;

import android.support.annotation.Nullable;

import com.yingwei.testing.testpenn2.model.BaseResponse;
import com.yingwei.testing.testpenn2.model.fetch.AddressDownLoadRequest;
import com.yingwei.testing.testpenn2.model.fetch.AddressSaveRequest;
import com.yingwei.testing.testpenn2.model.response.AddressDownLoad;
import com.yingwei.testing.testpenn2.model.response.MsgResponse;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.adapter.rxjava.Result;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * Created by jiahe008_lvlanlan on 2017/3/6.
 */
public interface IApiService { //<T, Q>

//    //自己的请求接口
//    @FormUrlEncoded 表单数据必须
//    @POST
//    Call<LineDetailResponse> lineDetail();

    //@Headers({"Content-type:application/json;charset=UTF-8"}) @Header("Content-type:application/json;charset=UTF-8")
    @POST("/mobile/route/mockClient.jhtml") //如果请求地址与baseUrl相同，则这样写.
    Call<String> urlAddress();

    @POST("/mobile/teacher/white/whiteboardSave.jhtml")
    Call<BaseResponse<MsgResponse>> addressSave(@Body AddressSaveRequest request);

    @POST("/mobile/common/whiteboardURL.jhtml")
    Call<BaseResponse<AddressDownLoad>> addressDownLoad(@Body AddressDownLoadRequest request);

    // 定义一个下载文件的请求: option 2: using a dynamic URL
    @Streaming
    @GET
    Call<ResponseBody> downloadFileWithDynamicUrlSync(@Url String fileUrl);

    //直接请求类型：
    //1.直接请求
//    @GET("/record")
//    Call<PhoneResult> getResult();

    //2.组合后直接请求
//    @GET("/result/{id}")
//    Call<PhoneResult> getResult(@Path("id") String id);

    //3.带参数查询：
//    @GET("/otn/lcxxcx/query")
//    Call<Result> query(@Query("purpose_codes") String codes, @Query("queryDate") String date,
//                       @Query("from_station") String from, @Query("to_station") String to);
    //4.带Header型
//    @POST("/info")
//    Call<Object> updateInfo(@Header("device") String device, @Header("version") int version,
//                            @Field("id") String id);

    // Demo中的请求接口
//    @GET("/apistore/mobilenumber/mobilenumber")
//    Call<PhoneResult> getResult(@Header("apikey") String apikey, @Query("phone") String phone);

    //Demo中的请求接口，添加泛型返回类数据类型
//    @GET("/threads/counts.json")
//    Call<Q> getCommit(@Query("short_name") String shortName, @Query("threads") String threads);



}

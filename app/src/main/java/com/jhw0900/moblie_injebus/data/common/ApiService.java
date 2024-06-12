package com.jhw0900.moblie_injebus.data.common;

import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {
    @FormUrlEncoded
    @POST("login_proc.php")
    Call<HashMap<String, String>> login(@Field("id") String id, @Field("password") String password);

    @FormUrlEncoded
    @POST("reserve/time_select_proc.php")
    Call<HashMap<String, String>> getTime(@Field("lineCode") String lineCode, @Field("dateCode") String dateCode);

    @FormUrlEncoded
    @POST("reserve/day_select_proc.php")
    Call<HashMap<String, String>> getLine(@Field("dateCode") String dateCode);

    @FormUrlEncoded
    @POST("/reserve/select_seat.php")
    Call<ResponseBody> getBusCode(@Field("lineCode") String lineCode, @Field("timeCode") String timeCode);
}

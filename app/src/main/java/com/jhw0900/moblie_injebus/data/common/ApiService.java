package com.jhw0900.moblie_injebus.data.common;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {
    @FormUrlEncoded
    @POST("login_proc.php")
    Call<Void> login(@Field("id") String id, @Field("password") String password);
}

package com.jhw0900.moblie_injebus.data.service;

import com.jhw0900.moblie_injebus.data.common.ApiClient;
import com.jhw0900.moblie_injebus.data.common.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthenticationService {
    private ApiService apiService;

    public AuthenticationService(){
        apiService = ApiClient.getClient().create(ApiService.class);
    }

    public void login(String id, String password){
        Call<Void> loginCall = apiService.login(id, password);
        loginCall.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response){
                if(response.isSuccessful()) System.out.println("Login Successful!");
                else System.out.println("Login Failed.");
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
}

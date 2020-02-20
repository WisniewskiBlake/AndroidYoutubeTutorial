package com.example.networkingapp.rest.services;


import com.example.networkingapp.activity.LoginActivity;
import com.example.networkingapp.model.User;

import java.util.Map;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

public interface UserInterface {

    @POST("login")
    Call<Integer> singin(@Body LoginActivity.UserInfo userInfo);

    @GET("loadownprofile")
    Call<User> loadownProfile(@QueryMap Map<String, String> params);

    @POST("poststatus")
    Call<Integer> uploadStatus(@Body MultipartBody requestBody);

    @POST("uploadImage")
    Call<Integer> uploadImage(@Body MultipartBody requestBody);

}

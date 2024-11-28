package com.example.coursework;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiInterface {
    @Headers({
            "Content-Type: application/x-www-form-urlencoded",
    })
    @FormUrlEncoded
    @POST("/sendPayload")
    Call<Upload> getUserInformation(
            @Field("jsonpayload") String jsonPayload);
}
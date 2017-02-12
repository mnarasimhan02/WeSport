package com.my.game.wesport;
import com.my.game.wesport.POJO.Example;
import retrofit.http.GET;
import retrofit.Call;
import retrofit.http.Query;

interface RetrofitMaps {

    /*
     * Retrofit get annotation with our URL
     * And our method that will return us details of park.
     */
    @GET("api/place/nearbysearch/json?&key=AIzaSyBmEpSt0jy6YbuUXnwJT6GzgabYNeOjqJE")
    Call<Example> getNearbyPlaces(@Query("type") String type, @Query("location") String location, @Query("radius") int radius);

}
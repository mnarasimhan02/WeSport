package com.my.game.wesport.api;
import com.my.game.wesport.POJO.Example;
import retrofit.http.GET;
import retrofit.Call;
import retrofit.http.Query;

public interface RetrofitMaps {

    /*
     * Retrofit get annotation with our URL
     * And our method that will return us details of park.
     */
//    @GET("api/place/nearbysearch/json?&key=AIzaSyBmEpSt0jy6YbuUXnwJT6GzgabYNeOjqJE")
    @GET("api/place/nearbysearch/json?&key=AIzaSyAupE3BWQpqy8P3SyiECZYVPmrbYBKJM8c")
    Call<Example> getNearbyPlaces(@Query("type") String type, @Query("location") String location, @Query("radius") int radius,
                                  @Query("openow") String open);

}
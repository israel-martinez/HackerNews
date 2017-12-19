package com.israel_martinez.hackernewsalgolia.Services;

import com.israel_martinez.hackernewsalgolia.EDA.News;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by israel-martinez on 18-12-17.
 */

public interface NewsClient {

    @GET("search_by_date?query=android")
    Call<News> getNews();
}

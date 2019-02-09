package com.example.innews;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

public class NewsAPI {

    private static final String KEY = "8fca5a6d12fd4bf98757828ba3833f64";
    private static final String BASE_URL = "https://newsapi.org/";
    //https://newsapi.org/v2/everything?q=ArtificialIntelligence&from=2019-01-01&sortBy=publishedAt

    public static NewsService newsService = null;


    //for Singleton pattern
    public static NewsService getNewsService() {
        if (newsService == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            newsService = retrofit.create(NewsService.class);
        }
        return newsService;
    }

    //Create a interface
    //Provide methods for interface
    public interface NewsService {

        @GET("v2/everything?q=Android&from=2019-02-01&sortBy=publishedAt" + "&apiKey=" + KEY)
        Call<PostList> getPostList();
    }
}

package com.israel_martinez.hackernewsalgolia.Main;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.israel_martinez.hackernewsalgolia.EDA.News;
import com.israel_martinez.hackernewsalgolia.R;
import com.israel_martinez.hackernewsalgolia.Services.NewsClient;
import com.israel_martinez.hackernewsalgolia.Services.ServiceGenerator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private NewsClient newsClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        newsClient = ServiceGenerator.createService(NewsClient.class);

        loadNews();


    }

    public void loadNews(){
        Call<News> callNews = newsClient.getNews();
        callNews.enqueue(new Callback<News>() {
            @Override
            public void onResponse(Call<News> call, Response<News> response) {
                News newsResponse = response.body();
                try{
                    if(newsResponse != null){
                        Toast.makeText(MainActivity.this, "nbhits: " + newsResponse.getNbHits(), Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(MainActivity.this, "Failed to get News!", Toast.LENGTH_SHORT).show();
                    }
                }catch(Error e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<News> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Failed to chargue the news", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.hello_world:{
                loadNews();
                break;
            }
        }
    }
}

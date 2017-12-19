package com.israel_martinez.hackernewsalgolia.Main;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.israel_martinez.hackernewsalgolia.Adapters.HitsAdapter;
import com.israel_martinez.hackernewsalgolia.EDA.News;
import com.israel_martinez.hackernewsalgolia.R;
import com.israel_martinez.hackernewsalgolia.Services.NewsClient;
import com.israel_martinez.hackernewsalgolia.Services.ServiceGenerator;
import com.israel_martinez.hackernewsalgolia.WebView.WebViewActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{
    private NewsClient newsClient;
    private ListView hitsListView;
    private HitsAdapter hitsAdapter;
    private News news;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        newsClient = ServiceGenerator.createService(NewsClient.class);
        hitsListView = findViewById(R.id.hits_listView);

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
                        news = newsResponse;
                        hitsAdapter = new HitsAdapter(MainActivity.this, R.layout.hits_row, newsResponse.getHits());
                        hitsListView.setAdapter(hitsAdapter);
                        hitsListView.setOnItemClickListener(MainActivity.this);
                    }else{
                        Toast.makeText(MainActivity.this, "Failed to get News!", Toast.LENGTH_SHORT).show();
                    }
                }catch(Error e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<News> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Failed to charge the news", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
        //String url = "https://hn.algolia.com/?query=&sort=byPopularity&prefix&page=0&dateRange=all&type=story";
        Object storyUrl =  news.getHits().get(position).getStoryUrl();
        String url = null;
        if(storyUrl != null){
            url = storyUrl.toString();
        }
        intent.putExtra("url", url);

        Toast.makeText(MainActivity.this, "url: " + url,
                Toast.LENGTH_SHORT).show();
        startActivity(intent);
    }
}

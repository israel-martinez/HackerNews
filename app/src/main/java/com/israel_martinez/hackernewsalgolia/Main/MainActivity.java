package com.israel_martinez.hackernewsalgolia.Main;

import android.content.Intent;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
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

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener,
        SwipeRefreshLayout.OnRefreshListener{
    private NewsClient newsClient;
    private ListView hitsListView;
    private TextView textView;
    private SwipeRefreshLayout swipeLayout;
    private HitsAdapter hitsAdapter;
    private News news;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        newsClient = ServiceGenerator.createService(NewsClient.class);
        hitsListView = findViewById(R.id.hits_listView);
        textView = findViewById(R.id.list_state);
        textView.setVisibility(View.INVISIBLE);

        swipeLayout = findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(MainActivity.this);
        swipeLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        loadNews();
    }

    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override public void run() {
                swipeLayout.setRefreshing(false);
            }
        }, 5000);

        loadNews();
        Toast.makeText(MainActivity.this, "On refresh list!", Toast.LENGTH_SHORT).show();
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
                        textView.setVisibility(View.VISIBLE);
                    }
                }catch(Error e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<News> call, Throwable t) {
                textView.setVisibility(View.VISIBLE);
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

package com.israel_martinez.hackernewsalgolia.Main;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.israel_martinez.hackernewsalgolia.Adapters.HitsAdapter;
import com.israel_martinez.hackernewsalgolia.EDA.Hit;
import com.israel_martinez.hackernewsalgolia.EDA.News;
import com.israel_martinez.hackernewsalgolia.R;
import com.israel_martinez.hackernewsalgolia.Services.NewsClient;
import com.israel_martinez.hackernewsalgolia.Services.ServiceGenerator;
import com.israel_martinez.hackernewsalgolia.Storage.InternalStorage;
import com.israel_martinez.hackernewsalgolia.WebView.WebViewActivity;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Sample activity demonstrating swipe to remove on recycler view functionality.
 * The interesting parts are drawing while items are animating to their new positions after some items is removed
 * and a possibility to undo the removal.
 */
public class Main2Activity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{

    RecyclerView mRecyclerView;

    private NewsClient newsClient;
    private ListView hitsListView;
    private TextView textView;
    private TextView hitTextView;
    private SwipeRefreshLayout swipeLayout;
    private static News news = new News();
    private Animation animClickable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);


        newsClient = ServiceGenerator.createService(NewsClient.class);
        hitsListView = findViewById(R.id.hits_listView);
        textView = findViewById(R.id.list_state);
        textView.setVisibility(View.INVISIBLE);
        loadNews();
        storageNews();

        mRecyclerView = findViewById(R.id.recycler_view);

        swipeLayout = findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(Main2Activity.this);
        swipeLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        animClickable = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.clicked);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_item_undo_checkbox) {
            item.setChecked(!item.isChecked());
            ((TestAdapter)mRecyclerView.getAdapter()).setUndoOn(item.isChecked());
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpRecyclerView() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(new TestAdapter());
        mRecyclerView.setHasFixedSize(true);
        setUpItemTouchHelper();
        setUpAnimationDecoratorHelper();
    }

    /**
     * This is the standard support library way of implementing "swipe to delete" feature. You can do custom drawing in onChildDraw method
     * but whatever you draw will disappear once the swipe is over, and while the items are animating to their new position the recycler view
     * background will be visible. That is rarely an desired effect.
     */
    private void setUpItemTouchHelper() {

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            // we want to cache these and not allocate anything repeatedly in the onChildDraw method
            Drawable background;
            Drawable xMark;
            int xMarkMargin;
            boolean initiated;

            private void init() {
                background = new ColorDrawable(getResources().getColor(R.color.colorAccent));
                xMark = ContextCompat.getDrawable(Main2Activity.this, R.drawable.ic_clear_24dp);
                xMark.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                xMarkMargin = (int) Main2Activity.this.getResources().getDimension(R.dimen.ic_clear_margin);
                initiated = true;
            }

            // not important, we don't want drag & drop
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                int position = viewHolder.getAdapterPosition();
                TestAdapter testAdapter = (TestAdapter)recyclerView.getAdapter();
                if (testAdapter.isUndoOn() && testAdapter.isPendingRemoval(position)) {
                    return 0;
                }
                return super.getSwipeDirs(recyclerView, viewHolder);
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                int swipedPosition = viewHolder.getAdapterPosition();
                TestAdapter adapter = (TestAdapter)mRecyclerView.getAdapter();
                boolean undoOn = adapter.isUndoOn();
                if (undoOn) {
                    adapter.pendingRemoval(swipedPosition);
                } else {
                    adapter.remove(swipedPosition);
                }
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;

                // not sure why, but this method get's called for viewholder that are already swiped away
                if (viewHolder.getAdapterPosition() == -1) {
                    // not interested in those
                    return;
                }

                if (!initiated) {
                    init();
                }

                // draw red background
                background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                background.draw(c);

                // draw x mark
                int itemHeight = itemView.getBottom() - itemView.getTop();
                int intrinsicWidth = xMark.getIntrinsicWidth();
                int intrinsicHeight = xMark.getIntrinsicWidth();

                int xMarkLeft = itemView.getRight() - xMarkMargin - intrinsicWidth;
                int xMarkRight = itemView.getRight() - xMarkMargin;
                int xMarkTop = itemView.getTop() + (itemHeight - intrinsicHeight)/2;
                int xMarkBottom = xMarkTop + intrinsicHeight;
                xMark.setBounds(xMarkLeft, xMarkTop, xMarkRight, xMarkBottom);

                xMark.draw(c);

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

        };
        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);
    }

    /**
     * We're gonna setup another ItemDecorator that will draw the red background in the empty space while the items are animating to thier new positions
     * after an item is removed.
     */
    private void setUpAnimationDecoratorHelper() {
        mRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {

            // we want to cache this and not allocate anything repeatedly in the onDraw method
            Drawable background;
            boolean initiated;

            private void init() {
                background = new ColorDrawable(getResources().getColor(R.color.colorAccent));
                initiated = true;
            }

            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {

                if (!initiated) {
                    init();
                }

                // only if animation is in progress
                if (parent.getItemAnimator().isRunning()) {

                    // some items might be animating down and some items might be animating up to close the gap left by the removed item
                    // this is not exclusive, both movement can be happening at the same time
                    // to reproduce this leave just enough items so the first one and the last one would be just a little off screen
                    // then remove one from the middle

                    // find first child with translationY > 0
                    // and last one with translationY < 0
                    // we're after a rect that is not covered in recycler-view views at this point in time
                    View lastViewComingDown = null;
                    View firstViewComingUp = null;

                    // this is fixed
                    int left = 0;
                    int right = parent.getWidth();

                    // this we need to find out
                    int top = 0;
                    int bottom = 0;

                    // find relevant translating views
                    int childCount = parent.getLayoutManager().getChildCount();
                    for (int i = 0; i < childCount; i++) {
                        View child = parent.getLayoutManager().getChildAt(i);
                        if (child.getTranslationY() < 0) {
                            // view is coming down
                            lastViewComingDown = child;
                        } else if (child.getTranslationY() > 0) {
                            // view is coming up
                            if (firstViewComingUp == null) {
                                firstViewComingUp = child;
                            }
                        }
                    }

                    if (lastViewComingDown != null && firstViewComingUp != null) {
                        // views are coming down AND going up to fill the void
                        top = lastViewComingDown.getBottom() + (int) lastViewComingDown.getTranslationY();
                        bottom = firstViewComingUp.getTop() + (int) firstViewComingUp.getTranslationY();
                    } else if (lastViewComingDown != null) {
                        // views are going down to fill the void
                        top = lastViewComingDown.getBottom() + (int) lastViewComingDown.getTranslationY();
                        bottom = lastViewComingDown.getBottom();
                    } else if (firstViewComingUp != null) {
                        // views are coming up to fill the void
                        top = firstViewComingUp.getTop();
                        bottom = firstViewComingUp.getTop() + (int) firstViewComingUp.getTranslationY();
                    }

                    background.setBounds(left, top, right, bottom);
                    background.draw(c);

                }
                super.onDraw(c, parent, state);
            }

        });
    }

    public void storageNews() {
        if (news != null){
            try {
                InternalStorage.writeObject(Main2Activity.this, "last_news", news);
            } catch (IOException e) {
                e.printStackTrace();
                readNews();
            }
        }
    }

    public void readNews(){
        try {
            Object readObject = InternalStorage.readObject(Main2Activity.this, "last_news");
            if(readObject != null){
                news = (News) readObject;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
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
                        setUpRecyclerView();
                    }else{
                        textView.setVisibility(View.VISIBLE);
                        Toast.makeText(Main2Activity.this, "Charging storage news ...", Toast.LENGTH_SHORT).show();
                        readNews();
                        textView.setVisibility(View.INVISIBLE);
                    }
                }catch(Error e){
                    e.printStackTrace();
                    textView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<News> call, Throwable t) {
                textView.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override public void run() {
                swipeLayout.setRefreshing(false);
            }
        }, 5000);

        loadNews();
        textView.setVisibility(View.INVISIBLE);
        Toast.makeText(Main2Activity.this, "On refresh list!", Toast.LENGTH_SHORT).show();
    }

    /**
     * RecyclerView adapter enabling undo on a swiped away item.
     */
    class TestAdapter extends RecyclerView.Adapter {

        private static final int PENDING_REMOVAL_TIMEOUT = 3000; // 3sec

        List<Hit> items;
        List<Hit> itemsPendingRemoval;
        int lastInsertedIndex; // so we can add some more items for testing purposes
        boolean undoOn; // is undo on, you can turn it on from the toolbar menu

        private Handler handler = new Handler(); // hanlder for running delayed runnables
        HashMap<Hit, Runnable> pendingRunnables = new HashMap<>(); // map of items to pending runnables, so we can cancel a removal if need be

        public TestAdapter() {
            items = new ArrayList<>();
            itemsPendingRemoval = new ArrayList<>();
            // let's generate some items
            lastInsertedIndex = 20;
            // this should give us a couple of screens worth
            if (news != null) items = news.getHits();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new TestViewHolder(parent);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            TestViewHolder viewHolder = (TestViewHolder)holder;
            final Hit item = items.get(position);

            if (itemsPendingRemoval.contains(item)) {
                // we need to show the "undo" state of the row
                viewHolder.itemView.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                viewHolder.titleTextView.setVisibility(View.GONE);
                viewHolder.undoButton.setVisibility(View.VISIBLE);
                viewHolder.undoButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // user wants to undo the removal, let's cancel the pending task
                        Runnable pendingRemovalRunnable = pendingRunnables.get(item);
                        pendingRunnables.remove(item);
                        if (pendingRemovalRunnable != null) handler.removeCallbacks(pendingRemovalRunnable);
                        itemsPendingRemoval.remove(item);
                        // this will rebind the row in "normal" state
                        notifyItemChanged(items.indexOf(item));
                    }
                });
            } else {
                // we need to show the "normal" state
                viewHolder.itemView.setBackgroundColor(Color.WHITE);
                viewHolder.titleTextView.setVisibility(View.VISIBLE);
                viewHolder.titleTextView.setText(getTitle(item));
                viewHolder.hitTextView.setText(getAuthorAndCreatedAt(item));
                viewHolder.undoButton.setVisibility(View.GONE);
                viewHolder.undoButton.setOnClickListener(null);
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public String getTitle(Hit hit){
            try{
                if(hit.getStoryTitle() != null) return hit.getStoryTitle().toString();
                else
                if(hit.getTitle() != null) return hit.getTitle();

            }catch(Error error){
                error.printStackTrace();
            }

            return "";
        }

        public String getAuthorAndCreatedAt(Hit hit){
            return hit.getAuthor().concat(" - " + hit.getCreatedAt());
        }

        public void setUndoOn(boolean undoOn) {
            this.undoOn = undoOn;
        }

        public boolean isUndoOn() {
            return undoOn;
        }

        public void pendingRemoval(int position) {
            final Hit item = items.get(position);
            if (!itemsPendingRemoval.contains(item)) {
                itemsPendingRemoval.add(item);
                // this will redraw row in "undo" state
                notifyItemChanged(position);
                // let's create, store and post a runnable to remove the item
                Runnable pendingRemovalRunnable = new Runnable() {
                    @Override
                    public void run() {
                        remove(items.indexOf(item));
                    }
                };
                handler.postDelayed(pendingRemovalRunnable, PENDING_REMOVAL_TIMEOUT);
                pendingRunnables.put(item, pendingRemovalRunnable);
            }
        }

        public void remove(int position) {
            Hit item = items.get(position);
            if (itemsPendingRemoval.contains(item)) {
                itemsPendingRemoval.remove(item);
            }
            if (items.contains(item)) {
                items.remove(position);
                notifyItemRemoved(position);
            }
        }

        public boolean isPendingRemoval(int position) {
            Hit item = items.get(position);
            return itemsPendingRemoval.contains(item);
        }
    }

    /**
     * ViewHolder capable of presenting two states: "normal" and "undo" state.
     */
    class TestViewHolder extends RecyclerView.ViewHolder{

        TextView titleTextView;
        TextView hitTextView;
        Button undoButton;

        public TestViewHolder(ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_view, parent, false));
            titleTextView = itemView.findViewById(R.id.hit_title);
            hitTextView = itemView.findViewById(R.id.hit_text);
            undoButton = itemView.findViewById(R.id.undo_button);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemView.startAnimation(animClickable);
                    Intent intent = new Intent(v.getContext(), WebViewActivity.class);
                    Object storyUrl =  news.getHits().get(getPosition()).getStoryUrl();
                    Object commentText = news.getHits().get(getPosition()).getCommentText();

                    String url = null;
                    String comment = null;

                    if(storyUrl != null){url = storyUrl.toString();}
                    if(commentText != null){comment =  commentText.toString();}


                    intent.putExtra("url", url);
                    intent.putExtra("comment", comment);

                    if(url != null){
                        Toast.makeText(v.getContext(), "Loading url: " + url,
                                Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(v.getContext(), "Loading comment: " + comment,
                                Toast.LENGTH_SHORT).show();
                    }

                    startActivity(intent);
                }
            });

        }
    }

}

package com.example.innews;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.example.innews.Adapters.CustomAdapter;
import com.example.innews.Database.DbHelper;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

//API KEY - 8fca5a6d12fd4bf98757828ba3833f64
public class HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CustomAdapter adapter;
    private SQLiteDatabase database;
    private Cursor cursor;
    private static final int REQUEST_CODE = 100;
    private String country;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        country=getIntent().getStringExtra("country_name");

        fetchData();

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(HomeActivity.this));
        DbHelper dbHelper = new DbHelper(this);
        database = dbHelper.getWritableDatabase();

    }

    private Cursor getAllArticles() {
        return database.query("ARTICLES", null, null, null, null, null, null);
    }

    private void fetchData() {
        Call<PostList> postList = NewsAPI.getNewsService().getPostList();
        postList.enqueue(new Callback<PostList>() {
            @Override
            public void onResponse(Call<PostList> call, Response<PostList> response) {
                if (response.code() == 200) {
                    PostList list = response.body();
                    Toast.makeText(HomeActivity.this, "Success", Toast.LENGTH_SHORT).show();
                    if (list != null) {
                        insertToLocalDb(list);
                        cursor = getAllArticles();
                        adapter = new CustomAdapter(HomeActivity.this, cursor);
                        recyclerView.setAdapter(adapter);
                    } else {
                        Toast.makeText(HomeActivity.this, "List was null", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(HomeActivity.this, response.message(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<PostList> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Failure", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void insertToLocalDb(PostList postList) {
        List<Article> articles = postList.getArticles();
        for (int i = 0; i < articles.size(); i++) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("TITLE", articles.get(i).getTitle());
            contentValues.put("DESCRIPTION", articles.get(i).getDescription());
            contentValues.put("URL", articles.get(i).getUrl());
            contentValues.put("URLTOIMAGE", articles.get(i).getUrlToImage());
            contentValues.put("PUBLISHEDAT", articles.get(i).getPublishedAt());
            database.insert("ARTICLES", null, contentValues);
        }
    }
}

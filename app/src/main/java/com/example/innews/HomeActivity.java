package com.example.innews;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.innews.Adapters.CustomAdapter;
import com.example.innews.Database.DbHelper;

import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

//API KEY - 8fca5a6d12fd4bf98757828ba3833f64
public class HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FloatingActionButton searchBtn;
    private String toSearch;
    private EditText searchEditText;
    private CustomAdapter adapter;
    private SQLiteDatabase database;
    private Cursor cursor;
    private static final int REQUEST_CODE = 100;
    private String country;
    private SharedPreferences prefs;
    private String CHANNEL_ID = "100";
    private NotificationManagerCompat notificationManagerCompat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        searchEditText = findViewById(R.id.searchEditText);
        recyclerView = findViewById(R.id.recycler_view);
        searchBtn = findViewById(R.id.fab);
        recyclerView.setLayoutManager(new LinearLayoutManager(HomeActivity.this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(50);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);
        DbHelper dbHelper = new DbHelper(this);
        database = dbHelper.getWritableDatabase();
        country = getIntent().getStringExtra("country_name");
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getBoolean("isFirstTime", true)) {
            Toast.makeText(this, "first time", Toast.LENGTH_SHORT).show();
            fetchData();
            fetchExtraData();
            prefs.edit().putBoolean("isFirstTime", false).apply();
        } else {
            cursor = getAllArticles();
            if (cursor.moveToNext()) {
                adapter = new CustomAdapter(HomeActivity.this, cursor);
                recyclerView.setAdapter(adapter);
            }
        }

        showNotification();

        //Schedule Notification for 1:30 pm everyday
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 13);
        calendar.set(Calendar.MINUTE, 30);
        Intent intent = new Intent(getApplicationContext(), NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 10, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toSearch = searchEditText.getText().toString();
                cursor = getArticles(toSearch);
                adapter = new CustomAdapter(HomeActivity.this, cursor);
                recyclerView.setAdapter(adapter);
            }
        });

    }

    private void showNotification() {
        Cursor c = getAllArticles();
        String title = "", desc = "";
        if (c.moveToFirst()) {
            title = c.getString(c.getColumnIndex("TITLE"));
            desc = c.getString(c.getColumnIndex("DESCRIPTION"));
        }
        notificationManagerCompat = NotificationManagerCompat.from(this);
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(desc));
        createNotificationChannel();
        notificationManagerCompat.notify(1, mBuilder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Notification channel name";
            String description = "Notification channel description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private Cursor getArticles(String toSearch) {
        return database.rawQuery("SELECT * FROM ARTICLES WHERE TITLE LIKE '%" + toSearch + "%';", null);
    }

    private Cursor getAllArticles() {
        return database.query("ARTICLES", null, null, null, null, null, null);
    }

    private void fetchData() {
        Call<PostList> postList = NewsAPI.getNewsService().getPostList(country);
        postList.enqueue(new Callback<PostList>() {
            @Override
            public void onResponse(Call<PostList> call, Response<PostList> response) {
                if (response.code() == 200) {
                    PostList list = response.body();
                    Toast.makeText(HomeActivity.this, "Success", Toast.LENGTH_SHORT).show();
                    if (list != null) {
                        insertToLocalDb(list);

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

    private void fetchExtraData() {
        Call<PostList> postList = NewsAPI.getNewsService().getExtraPosts();
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
            contentValues.put("TITLE", articles.get(i).getTitle() != null ? articles.get(i).getTitle() : "");
            contentValues.put("DESCRIPTION", articles.get(i).getDescription() != null ? articles.get(i).getDescription() : "");
            contentValues.put("URL", articles.get(i).getUrl() != null ? articles.get(i).getUrl() : "");
            contentValues.put("URLTOIMAGE", articles.get(i).getUrlToImage() != null ? articles.get(i).getUrlToImage() : "");
            contentValues.put("PUBLISHEDAT", articles.get(i).getPublishedAt() != null ? articles.get(i).getPublishedAt() : "");
            database.insert("ARTICLES", null, contentValues);
        }
    }
}

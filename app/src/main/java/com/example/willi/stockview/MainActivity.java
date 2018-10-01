package com.example.willi.stockview;

import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.JsonReader;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    Thread updateThread = null;
    private DataBaseHandler db;
    RecyclerViewAdapter recyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DataBaseHandler(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getStockSymbol();
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.cardList);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);


        recyclerViewAdapter = new RecyclerViewAdapter(db);

        recyclerView.setAdapter(recyclerViewAdapter);

        updateThread = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Thread started");
                while (!Thread.currentThread().isInterrupted()){
                    List<String> symbols = db.getStockSymbols();
                    for(String symbol:symbols){
                        updateLatestPrice(symbol);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            recyclerViewAdapter.updateStockData();
                            (Objects.requireNonNull(((RecyclerView) findViewById(R.id.cardList)).getAdapter())).notifyDataSetChanged();
                        }
                    });

                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Data update");


                }
            }
        });
        //db.insertStock(new StockData("amzn",100,101,102));
        //recyclerViewAdapter.updateStockData();
    }

    public void getStockSymbol(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Stock symbol");

// Set up the input
        final EditText input = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                addStock(input.getText().toString());
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public void addStock(final String symbol){
        new Thread(new Runnable() {
            @Override
            public void run() {

                allData(symbol);

            }
        }).start();
        recyclerViewAdapter.updateStockData();
    }

    public void updateLatestPrice(String symbol){
        System.out.println("Symbol "+symbol);
        try {
            URL url = new URL("https://api.iextrading.com/1.0/stock/market/batch?types=quote&symbols="+symbol+"&filter=latestPrice");
            JSONObject object = convertUrlToJson(url);
            object = object.getJSONObject(symbol.toUpperCase());
            object = object.getJSONObject("quote");
            double latestPrice = object.getDouble("latestPrice");
            db.updateStockPrice(symbol, latestPrice);
        } catch (MalformedURLException | JSONException e) {
            e.printStackTrace();
        }
    }

    public void allData(String symbol){
        System.out.println("Symbol "+symbol);
        try {
            URL url = new URL("https://api.iextrading.com/1.0/stock/market/batch?types=quote&symbols="+symbol+"&filter=latestPrice,open,marketCap");
            JSONObject object = convertUrlToJson(url);
            object = object.getJSONObject(symbol.toUpperCase());
            object = object.getJSONObject("quote");
            double latestPrice = object.getDouble("latestPrice");
            double open = object.getDouble("open");
            double marketCap = object.getDouble("marketCap");
            db.insertStock(new StockData(symbol, latestPrice, open, marketCap));
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject convertUrlToJson(URL url){
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream(),"UTF-8"));
            StringBuilder responseStrBuilder = new StringBuilder();
            String inputStr;
            while((inputStr = bufferedReader.readLine())!=null){
                responseStrBuilder.append(inputStr);
            }
            return new JSONObject(responseStrBuilder.toString());
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        if(!updateThread.isAlive()) {
            updateThread.start();
        }

    }
    @Override
    public void onResume(){
        super.onResume();
        if(!updateThread.isAlive()) {
            updateThread.start();
        }
    }

    @Override
    public void onStop(){
        super.onStop();
        updateThread.interrupt();
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        updateThread.interrupt();
    }



    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}

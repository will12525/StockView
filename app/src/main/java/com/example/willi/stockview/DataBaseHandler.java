package com.example.willi.stockview;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DataBaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "StockData.db";
    private static final String STOCK_TABLE_NAME = "stocks";
    private static final String EMPTY_STOCKS = "empty_stocks";

    DataBaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table "+STOCK_TABLE_NAME+" (stockSymbol text primary key not null, latestPrice real, marketCap real, openPrice real)");
        System.out.println("Stock table created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS "+STOCK_TABLE_NAME);
        onCreate(db);
    }

    public boolean insertStock(StockData stockData){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("stockSymbol",stockData.getStockSymbol());
        contentValues.put("latestPrice",stockData.getLatestPrice());
        contentValues.put("marketCap", stockData.getCap());
        contentValues.put("openPrice",stockData.getOpenPrice());

        db.insert(STOCK_TABLE_NAME, null, contentValues);
        return true;
    }

    public boolean updateStock(StockData stockData){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("latestPrice",stockData.getLatestPrice());
        contentValues.put("marketCap", stockData.getCap());
        contentValues.put("openPrice",stockData.getOpenPrice());
        db.update(STOCK_TABLE_NAME, contentValues, "stockSymbol = ? ", new String[]{stockData.getStockSymbol()});
        return true;
    }
    public boolean updateStockPrice(String symbol, double price){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("latestPrice",price);
        db.update(STOCK_TABLE_NAME, contentValues, "stockSymbol = ? ", new String[]{symbol});
        return true;
    }

    public int deleteStock(String symbol){
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(STOCK_TABLE_NAME, "stockSymbol = ? ", new String[]{symbol});
    }

    public int stockCount(){
        return (int) DatabaseUtils.queryNumEntries(getWritableDatabase(), STOCK_TABLE_NAME);
    }

    public StockData getStock(String symbol){
        SQLiteDatabase db = getReadableDatabase();
        try {
            Cursor data = db.rawQuery("select * from " + STOCK_TABLE_NAME + " where stockSymbol=" + symbol, null);
            StockData stockData = dataToStockObject(data);
            data.close();

            return stockData;
        } catch (SQLiteException e){
            return null;
        }
    }

    public List<StockData> getAllStocks(){
        SQLiteDatabase db = getReadableDatabase();
        List<StockData> allStocks = new ArrayList<>();
        Cursor data = db.rawQuery("select * from "+STOCK_TABLE_NAME, null);
        data.moveToFirst();

        while (!data.isAfterLast()){
            allStocks.add(dataToStockObject(data));
            data.moveToNext();
        }
        data.close();
        return allStocks;
    }

    public List<String> getStockSymbols(){
        SQLiteDatabase db = getReadableDatabase();
        List<String> symbolList = new ArrayList<>();
        Cursor data = db.rawQuery("select stockSymbol from "+STOCK_TABLE_NAME, null);
        if(data.getCount()>0) {
            data.moveToFirst();
            while (!data.isAfterLast()) {
                symbolList.add(data.getString(data.getColumnIndex("stockSymbol")));
                data.moveToNext();
            }
        }
        data.close();
        return symbolList;
    }

    public StockData dataToStockObject(Cursor data){
        StockData stockData = new StockData();

        stockData.setStockSymbol(data.getString(data.getColumnIndex("stockSymbol")));
        stockData.setLatestPrice(data.getDouble(data.getColumnIndex("latestPrice")));
        stockData.setCap(data.getDouble(data.getColumnIndex("marketCap")));
        stockData.setOpenPrice(data.getDouble(data.getColumnIndex("openPrice")));

        return stockData;
    }



}

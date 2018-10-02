package com.example.willi.stockview;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class StockData {
    int ID;
    private String stockSymbol;
    private double latestPrice;
    private double openPrice;
    private double cap;
    private LineGraphSeries<DataPoint> series = new LineGraphSeries<>();


    StockData(){}

    public StockData(String stockSymbol, double latestPrice, double openPrice, double cap){
        this.stockSymbol = stockSymbol;
        this.latestPrice = latestPrice;
        this.openPrice = openPrice;
        this.cap = cap;
    }

    public String getStockSymbol() {
        return stockSymbol;
    }

    public void setStockSymbol(String stockSymbol) {
        this.stockSymbol = stockSymbol;
    }

    public double getLatestPrice() {
        return latestPrice;
    }

    public void setLatestPrice(double latestPrice) {
        this.latestPrice = latestPrice;
    }

    public double getOpenPrice() {
        return openPrice;
    }

    public void setOpenPrice(double openPrice) {
        this.openPrice = openPrice;
    }

    public double getCap() {
        return cap;
    }

    public void setCap(double cap) {
        this.cap = cap;
    }

    public LineGraphSeries<DataPoint> getSeries() {
        return series;
    }

    public void setSeries(LineGraphSeries<DataPoint> series){
        this.series = series;
    }

    public void addDataPoint(DataPoint dataPoint){
        series.appendData(dataPoint,true,40);
    }
    public void reset(StockData data){
        latestPrice = data.getLatestPrice();
        openPrice = data.getOpenPrice();
        cap = data.getCap();
    }
}


package com.example.willi.stockview;

public class StockData {
    private String stockSymbol;
    private double latestPrice;
    private double openPrice;
    private double cap;


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
}


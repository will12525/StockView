package com.example.willi.stockview;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder>{
    private List<StockData> dataSet;
    private DataBaseHandler db;

    public RecyclerViewAdapter(DataBaseHandler db){
        this.db = db;
        dataSet = db.getAllStocks();
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new MyViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_view, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        StockData data = dataSet.get(i);
        myViewHolder.stockSymbol.setText(data.getStockSymbol());
        myViewHolder.latestPrice.setText("".concat(""+data.getLatestPrice()));
        myViewHolder.openPrice.setText("".concat(""+data.getOpenPrice()));
        myViewHolder.marketCap.setText("".concat(""+data.getCap()));
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public void updateStockData(){
        dataSet = db.getAllStocks();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView stockSymbol;
        TextView latestPrice;
        TextView openPrice;
        TextView marketCap;
        MyViewHolder(View v){
            super(v);
            stockSymbol = v.findViewById(R.id.stock_symbol);
            latestPrice = v.findViewById(R.id.latest_price);
            openPrice = v.findViewById(R.id.market_open);
            marketCap = v.findViewById(R.id.market_cap);
        }
    }

}

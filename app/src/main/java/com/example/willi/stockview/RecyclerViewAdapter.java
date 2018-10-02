package com.example.willi.stockview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder>{
    private List<StockData> dataSet = new ArrayList<>();
    private List<StockData> removeSet = new ArrayList<>();
    private List<StockData> addStocks = new ArrayList<>();
   // private DataBaseHandler db;
    private Thread updateThread;
    final private int UPDATE_TIME_LATEST_PRICE = 1000*5;
    final private int UPDATE_TIME_OPENING = 1000*60*60;

    public RecyclerViewAdapter(final Context context){

        updateThread = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Thread started");
                long last_update_latest_price = 0;
                long last_update_opening = 0;
                while (!Thread.currentThread().isInterrupted()){

                    if (dataSet.size() > 0) {
                        if(System.currentTimeMillis()-last_update_latest_price> UPDATE_TIME_LATEST_PRICE) {

                            for (StockData data : dataSet) {
                                data.setLatestPrice(updateLatestPrice(data.getStockSymbol()));
                                // cardChanged(context,data.positionOnDisplay);
                            }
                            last_update_latest_price = System.currentTimeMillis();
                            updateView(context);
                        }

                        if(System.currentTimeMillis()-last_update_opening> UPDATE_TIME_OPENING) {
                            for (StockData data : dataSet) {
                                data.reset(gatherAllData(data.getStockSymbol()));
                                //cardChanged(context,data.positionOnDisplay);
                            }
                            last_update_opening = System.currentTimeMillis();
                            updateView(context);
                        }

                    }

                    if(removeSet.size()>0||addStocks.size()>0){
                        updateDataLists(0, null);
                       // dataSet.removeAll(removeSet);
                       // removeSet.clear();
                       // dataSet.addAll(addStocks);
                       // addStocks.clear();
                        updateView(context);
                    }




                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Log.e("UPDATE_THREAD","Error with update thread sleep");
                        e.printStackTrace();
                    }


                }
            }
        });
    }

    public void updateView(Context context){
        ((MainActivity)context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    public void cardChanged(Context context, final int position){
        ((MainActivity)context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Update called: "+position);
                notifyItemChanged(position);
            }
        });
    }

    public void startExtraThread(){
        if(!updateThread.isAlive()){
            updateThread.start();
        }
    }
    public void stopExtraThread(){
        updateThread.interrupt();
    }

    public synchronized void updateDataLists(int type, StockData item){
        if(type == 0){
            dataSet.removeAll(removeSet);
            removeSet.clear();
            dataSet.addAll(addStocks);
            addStocks.clear();
        } else if(type == 1){
            addStocks.add(item);
        } else if (type == 2){
            removeSet.add(item);
        }
    }

    public void addStock(final String symbol){
        new Thread(new Runnable() {
            @Override
            public void run() {
                StockData data = gatherAllData(symbol);
                if(data != null) {
                    data.setSeries(getGraphData(symbol));
                    data.ID = dataSet.size();
                    updateDataLists(1, data);
                    //addStocks.add(data);
                }
            }
        }) .start();
    }

    public double updateLatestPrice(String symbol){
        try {
            URL url = new URL("https://api.iextrading.com/1.0/stock/market/batch?types=quote&symbols="+symbol+"&filter=latestPrice");
            JSONObject object = convertUrlToJson(url);
            object = object.getJSONObject(symbol.toUpperCase());
            object = object.getJSONObject("quote");
            double latestPrice = object.getDouble("latestPrice");
            return latestPrice;
            //db.updateStockPrice(symbol, latestPrice);
        } catch (MalformedURLException | JSONException e) {
            Log.e("UPDATE_LATEST_PRICE","Error grabbing latest price for "+symbol);
            e.printStackTrace();
            return 0;
        }
    }

    public StockData gatherAllData(String symbol){
        try {
            URL url = new URL("https://api.iextrading.com/1.0/stock/market/batch?types=quote&symbols="+symbol+"&filter=latestPrice,open,marketCap");
            JSONObject object = convertUrlToJson(url);
            object = object.getJSONObject(symbol.toUpperCase());
            object = object.getJSONObject("quote");
            double latestPrice = object.getDouble("latestPrice");
            double open = object.getDouble("open");
            double marketCap = object.getDouble("marketCap");
            return new StockData(symbol, latestPrice, open, marketCap);
        } catch (IOException | JSONException e) {
            Log.e("GATHER_ALL_DATA","Error grabbing all data from "+symbol);
            e.printStackTrace();
            return null;
        }
    }

    public LineGraphSeries<DataPoint> getGraphData(String symbol){
        JSONArray objArray = null;
        LineGraphSeries<DataPoint> lineGraphSeries = new LineGraphSeries<>();
        DecimalFormat df = new DecimalFormat("#.##");

        try {
            URL url = new URL("https://api.iextrading.com/1.0/stock/"+symbol+"/chart/1d");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream(),"UTF-8"));
            StringBuilder responseStrBuilder = new StringBuilder();
            String inputStr;
            while((inputStr = bufferedReader.readLine())!=null){
                responseStrBuilder.append(inputStr);
            }

            objArray = new JSONArray(responseStrBuilder.toString());
            for(int x = 0; x < objArray.length(); x++){
                JSONObject pointData = objArray.getJSONObject(x);
                //average, close, high, low, marketAverage, marketClose, marketHigh, marketLow, marketOpen, open
                String time = pointData.get("minute").toString();
                int hour = Integer.parseInt(time.substring(0,time.indexOf(":")));
                int minute = Integer.parseInt(time.substring(time.indexOf(":")+1,time.length()));
                minute = (minute + hour * 60) - (9 * 60 + 30);
                double yValue = pointData.getDouble("average");
                yValue = Double.parseDouble(df.format(yValue));
               // System.out.println(minute+", "+yValue);
                if(yValue != -1) {
                    lineGraphSeries.appendData(new DataPoint(minute, yValue), false, 20);
                }
            }
            return lineGraphSeries;

        } catch (IOException | JSONException e) {
            Log.e("CONVERT_URL_TO_JSON","Error converting URL to JSON output");
            e.printStackTrace();
            return null;
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
            Log.e("CONVERT_URL_TO_JSON","Error reading from URL and converting to JSON output");
            e.printStackTrace();
            return null;
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new MyViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_view, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        //check if the passed viewHolder contains info, if it does recongifure, otherwise simple update
        final StockData data = dataSet.get(i);
       // System.out.println("UPDATE: "+i +", "+data.getStockSymbol()+", "+data.ID);
        myViewHolder.stockSymbol.setText(data.getStockSymbol());
        myViewHolder.latestPrice.setText("".concat(""+data.getLatestPrice()));
        myViewHolder.openPrice.setText("".concat(""+data.getOpenPrice()));
        myViewHolder.marketCap.setText("".concat(""+data.getCap()));
        myViewHolder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateDataLists(2, data);
                //removeSet.add(data);
            }
        });

       // if(myViewHolder.graphView.getSeries().isEmpty()) {
            System.out.println("UPDATE GRAPH: "+data.getStockSymbol());
            myViewHolder.graphView.removeAllSeries();
            //myViewHolder.graphView.getViewport().setXAxisBoundsManual(true);
            //myViewHolder.graphView.getGridLabelRenderer().setHumanRounding(false,true);
            //myViewHolder.graphView.getGridLabelRenderer().setHumanRounding(false);
            myViewHolder.graphView.addSeries(data.getSeries());
            //myViewHolder.graphView.getViewport().setMinX(data.getSeries().getLowestValueX());
           // myViewHolder.graphView.getViewport().setMaxX(data.getSeries().getHighestValueX());

        myViewHolder.graphView.getViewport().setXAxisBoundsManual(true);
        myViewHolder.graphView.getViewport().setMinX(myViewHolder.graphView.getViewport().getMinX(true));
        myViewHolder.graphView.getViewport().setMaxX(myViewHolder.graphView.getViewport().getMaxX(true)  + 1);

        //myViewHolder.graphView.getViewport().setYAxisBoundsManual(false);


        //}



    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView stockSymbol;
        TextView latestPrice;
        TextView openPrice;
        TextView marketCap;
        ImageView deleteButton;
        GraphView graphView;
        MyViewHolder(View v){
            super(v);
            stockSymbol = v.findViewById(R.id.stock_symbol);
            latestPrice = v.findViewById(R.id.latest_price);
            openPrice = v.findViewById(R.id.market_open);
            marketCap = v.findViewById(R.id.market_cap);
            deleteButton = v.findViewById(R.id.delete_button);
            graphView = v.findViewById(R.id.graph_view);

          //  graphView.getViewport().setXAxisBoundsManual(true);
          //  graphView.getGridLabelRenderer().setHumanRounding(false);

           /* graphView.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(){
                @Override
                public String formatLabel(double value, boolean isValueX){
                    if(isValueX){
                        return super.formatLabel(value, isValueX);
                    } else {
                        return  super.formatLabel(value, isValueX);
                    }
                }
            });*/
        }
    }

}

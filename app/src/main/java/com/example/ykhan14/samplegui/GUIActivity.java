package com.example.ykhan14.samplegui;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import android.widget.LinearLayout;
import android.widget.TextView;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.Random;


public class GUIActivity extends Activity {

    private GraphicalView waveform;
    private LineGraph lineGraph;

    private Random generator;

    final int graphRefreshXValue = 10;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gui);

        generator = new Random();

        //Finding the relevant text fields that will be set by the Message Displayer
        TextView temp = (TextView) findViewById(R.id.value_temperature);
        TextView hR = (TextView)findViewById(R.id.value_heart_rate);
        TextView dia = (TextView)findViewById(R.id.value_DIA);
        TextView sys = (TextView)findViewById(R.id.value_SYS);

        //Initializing the Graph
        lineGraph = new LineGraph(this);
        /*waveform =*/ lineGraph.getView();

        //Initializing the Threads in charge of displaying the graph and the values
        MessageDisplayer messageDisplayer = new MessageDisplayer(temp,hR,dia,sys);
        WaveformDisplayer waveformDisplayer = new WaveformDisplayer();

        messageDisplayer.start();
        waveformDisplayer.start();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.gui, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        //Adding the waveform to the layout
        LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
        layout.addView(waveform, new LinearLayout.LayoutParams
                (LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //Thread to change the values of the text fields
    class MessageDisplayer extends Thread{
        TextView temperature, heartRate, dia, sys;

        //Stores the references to the relevant text fields
        MessageDisplayer(TextView temperaturevalue, TextView heartRateValue, TextView diaValue, TextView sysValue){

            this.temperature=temperaturevalue;
            heartRate=heartRateValue;
            dia=diaValue;
            sys=sysValue;

        }

        //Runs the textsetter on the main thread that is incharge of the views
        @Override
        public void run() {

            try{
                while (!isInterrupted()) {
                    runOnUiThread(new TextSetter());
                    Thread.sleep(10000);
                }
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        }


        //Class to set the values in the text fields, used by the main threads incharge of the views
        class TextSetter implements Runnable{
            @Override
            public void run() {

                temperature.setText(generator.nextInt(60)+"\u00b0C");
                heartRate.setText(generator.nextInt(300)+" beats per minute");
                dia.setText(generator.nextInt(300) +" mmHG");
                sys.setText(generator.nextInt(300) +" mmHG");
                //setContentView(R.layout.activity_display_message);
            }
        }
    }


    //Thread responsible for displaying the graph
    class WaveformDisplayer extends Thread{

        XYSeries currentLine;

        //Stores references to activity that started the thread and the dataset that will be frequently changed
        WaveformDisplayer(){
            currentLine = lineGraph.getSeries();
        }

        @Override
        public void run(){
            int xValue =0, yValue;

            try {
                while (!isInterrupted()) {

                    //Drawing the next point with  random values
                    yValue = generator.nextInt(256);
                    currentLine.add(xValue, yValue);
                    waveform.repaint();

                    Thread.sleep(1000);

                    //When the line reaches the end of the graph, clears the graph and starts drawing from the initial postion
                    if (xValue == graphRefreshXValue) {

                        //Removes the previously drawn graph
                        /*waveform =*/ lineGraph.clearView();
                        currentLine = lineGraph.getSeries();

                        //Draws the last data point of the previous graph, as the first data point of the new graph
                        xValue = 0;
                        currentLine.add(xValue, yValue);
                        waveform.repaint();
                    }

                    xValue++;
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }



    class LineGraph {


        //private GraphicalView waveform;//The view that will be displayed
        private LinearLayout layout;//The Layout view in which the Graphical view stays
        private Activity gui_activity;//reference to activity where this Graph was constructed

        private XYSeries series; //The line graph, frequently initialised
        private XYMultipleSeriesDataset dataset; //Can store multiple lines

        private XYSeriesRenderer seriesRenderer; // renderer for the line
        private XYMultipleSeriesRenderer renderer;// Renderer for the entire chart


        public LineGraph(Activity activity) {

            gui_activity= activity;
            layout = (LinearLayout) gui_activity.findViewById(R.id.chart);

            //Creating a single dataset and then adding it to the set of graphs
            series = new XYSeries("WaveForm");
            dataset = new XYMultipleSeriesDataset();
            dataset.addSeries(series);

            //Render for  Customization for the line in the line graph
            seriesRenderer = new XYSeriesRenderer();
            seriesRenderer.setColor(Color.BLACK);
            seriesRenderer.setFillPoints(false);

            //Render for Customization of the chart
            renderer = new XYMultipleSeriesRenderer();
            //Axes labels, colour, font size, the maximum and minimum values
            renderer.setXTitle("Time");
            renderer.setYTitle("Signal");
            renderer.setLabelsTextSize(10);
            renderer.setAxisTitleTextSize(12);
            renderer.setAxesColor(Color.BLACK);
            renderer.setLabelsColor(Color.BLACK);
            renderer.setXAxisMin(0);
            renderer.setXAxisMax(graphRefreshXValue);
            renderer.setXLabelsColor(Color.BLACK);
            renderer.setYAxisMin(0);
            renderer.setYAxisMax(255);
            renderer.setYLabelsColor(0,Color.BLACK);

            //Miscellaneous customizations
            renderer.setBackgroundColor(Color.WHITE);
            renderer.setMarginsColor(Color.WHITE);
            renderer.setAntialiasing(true);
            renderer.setGridColor(Color.BLACK);


            // Add single seriesRenderer to multiple seriesRenderer
            renderer.addSeriesRenderer(seriesRenderer);
        }

        //Getter for the current line, used during initialization of the thread
        public XYSeries getSeries() {
            return series;
        }

        //Removes the previous Data points and athe
        public void clearView() {

            series.clear();
            gui_activity.runOnUiThread(new GraphClearer());


        }


        public void getView() {

            waveform = ChartFactory.getLineChartView(gui_activity, dataset, renderer);
            //return waveform;

        }

        class GraphClearer implements Runnable{
            @Override
            public void run() {
                layout.removeView(waveform);
                /*waveform = ChartFactory.getLineChartView(gui_activity, dataset, renderer);*/
                getView();
                layout.addView(waveform, new LinearLayout.LayoutParams
                        (LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            }
        }

    }

    }



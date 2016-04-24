package com.example.rachel.drones;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;

/**
 * Created by MarkLai on 4/24/16.
 */
public class QueryFlight extends AsyncTask<String, Void, ArrayList<FlightData>> {
    private final String LOG_TAG = "WEATHER";
    private String mJsonString = "";
    private ArrayList<FlightData> mFlightData = new ArrayList<FlightData>();

    MapsActivity mMapsActivity;
    InputStream mInputStream;


    public QueryFlight(MapsActivity activity, InputStream is){
        mMapsActivity = activity;
        mInputStream = is;
    }

    public ArrayList<FlightData> doInBackground(String... urls) {
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String forecastJsonStr = null;

        String format = "json";
        String appId = "06960b71f479f7e908176385f0cf9014";
        String lat = "35";
        String lng = "139";
//        String bbox = String.valueOf(deviceLon - 1)+","+String.valueOf(deviceLat - 1)+","+String.valueOf(deviceLon + 1)+","+String.valueOf(deviceLat + 1)+",10";
//        String bbox = "12,32,15,37,10";
        String cluster = "yes";
        int numDays = 14;
        Log.d("### forecastJsonStr : ", "doInBackground");
        try {
//            InputStream fis = new BufferedInputStream(new FileInputStream(""));
            // Read the input stream into a String
            InputStream inputStream = mInputStream;
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
//            Log.d("### forecastJsonStr : ", "StringBuffer");
            reader = new BufferedReader(new InputStreamReader(mInputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            forecastJsonStr = buffer.toString();
            Log.d("### forecastJsonStr : ", forecastJsonStr);
            mJsonString = forecastJsonStr;
            getWeatherDataFromJson(forecastJsonStr);
            return mFlightData;
        } catch (IOException e) {
            Log.d(LOG_TAG, " ### Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attemping
            // to parse it.
            return null;
        }
        catch (JSONException e){
//            Log.e(LOG_TAG, " ### " + e.getMessage(), e);
                e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
//                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        return null;
    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private void getWeatherDataFromJson(String forecastJsonStr)
            throws JSONException {

        try {
            JSONObject jsonRootObject = new JSONObject(forecastJsonStr);
            JSONArray flightArray = jsonRootObject.optJSONArray("flightPositions");
            for(int i=0; i<flightArray.length(); i++){
                JSONObject flightObj = flightArray.getJSONObject(i);
                JSONArray positionArr = flightObj.optJSONArray("positions");
                JSONObject positionObj = positionArr.getJSONObject(0);
                double lon = positionObj.getDouble("lon");
                double lat = positionObj.getDouble("lat");
//                Log.d("Flight","Flight: lon"+String.valueOf(lon)+", lat"+String.valueOf(lat));
                mFlightData.add(new FlightData(lat,lon));
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    @Override
    protected void onPostExecute(ArrayList<FlightData> result) {
        this.mMapsActivity.setFlightData(result);
    }
}


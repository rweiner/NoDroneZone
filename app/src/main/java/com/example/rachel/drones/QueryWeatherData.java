package com.example.rachel.drones;

import android.net.Uri;
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
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by MarkLai on 4/23/16.
 */
public class QueryWeatherData extends AsyncTask<String, Void, ArrayList<WeatherData>> {

    private final String LOG_TAG = "WEATHER";
    private String mJsonString = "";
    private ArrayList<WeatherData> mWeatherData = new ArrayList<WeatherData>();

    MapsActivity mMapsActivity;
    double deviceLat;
    double deviceLon;

    public QueryWeatherData(MapsActivity activity, double lat, double lon){
        mMapsActivity = activity;
        deviceLat = lat;
        deviceLon = lon;
    }

    public ArrayList<WeatherData> doInBackground(String... urls) {
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
        String bbox = String.valueOf(deviceLon - 1)+","+String.valueOf(deviceLat - 1)+","+String.valueOf(deviceLon + 1)+","+String.valueOf(deviceLat + 1)+",10";
//        String bbox = "12,32,15,37,10";
        String cluster = "yes";
        int numDays = 14;
        Log.d("### forecastJsonStr : ", "doInBackground");
        try {
            // Construct the URL for the OpenWeatherMap query
            // Possible parameters are avaiable at OWM's forecast API page, at
            // http://openweathermap.org/API#forecast
            final String FORECAST_BASE_URL =
                    "http://api.openweathermap.org/data/2.5/box/city?";
            final String LAT_PARAM = "lat";
            final String LNG_PARAM = "lon";
            final String BBOX_PARAM = "bbox";
            final String CLUSTER_PARAM = "cluster";
            final String API_KEY_PARAM = "appid";
            final String FORMAT_PARAM = "mode";

            Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(BBOX_PARAM, bbox)
                    .appendQueryParameter(CLUSTER_PARAM, cluster)
                    .appendQueryParameter(API_KEY_PARAM, appId)
                    .build();

            URL url = new URL(builtUri.toString());
//            Log.d("### forecastJsonStr : ", url.getHost()+url.getPath()+url.getQuery());
            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
//            Log.d("### forecastJsonStr : ", "urlConnection.connect();");
            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
//            Log.d("### forecastJsonStr : ", "StringBuffer");
            reader = new BufferedReader(new InputStreamReader(inputStream));

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
            return mWeatherData;
        } catch (IOException e) {
            Log.d(LOG_TAG, " ### Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attemping
            // to parse it.
            return null;
        } catch (JSONException e){
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
            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray("list");
            for(int i=0; i<weatherArray.length(); i++){
                // Get the JSON object representing the day
                JSONObject place = weatherArray.getJSONObject(i);
                JSONObject coord = place.getJSONObject("coord");
                Double lat = coord.getDouble("lat");
                Double lon = coord.getDouble("lon");

                JSONObject wind = place.getJSONObject("wind");
                Double speed = wind.getDouble("speed");
                mWeatherData.add(new WeatherData(lat, lon, speed));
//                Log.d(LOG_TAG, "lat: "+String.valueOf(lat)+", lon: "+String.valueOf(lon)+", WindSpped: "+String.valueOf(speed));
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    @Override
    protected void onPostExecute(ArrayList<WeatherData> result) {
        this.mMapsActivity.setWeatherData(result);
    }

}

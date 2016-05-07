package com.grafixartist.marshmallowsample.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.grafixartist.marshmallowsample.model.DeviceLocation;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by salagumalai on 07-05-2016.
 */
public class RestClient extends AsyncTask<Void, Void, String> {

    DeviceLocation location;
    public RestClient(DeviceLocation location){
        this.location = location;
    }

    private static final String TAG = "AndroidNetworkUtility";

    public boolean isConnected(Context ctx) {
        boolean flag = false;
        ConnectivityManager connectivityManager =
                (ConnectivityManager) ctx.getSystemService(ctx.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            flag = true;
        }
        return flag;
    }

    public String getHttpResponse() {
        StringBuilder sb = new StringBuilder();
        InputStream in = null;

        try {

            URL  url = new URL("http://10.0.2.2:8080/locations/device");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setAllowUserInteraction(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestMethod("GET");
            connection.connect();

            int resCode = connection.getResponseCode();
            Log.d(TAG, "Status code: " + resCode);
            if (resCode == HttpURLConnection.HTTP_OK) {
                in = connection.getInputStream();
            }

            BufferedReader reader =new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String data="";

            while ((data = reader.readLine()) != null){
                sb.append(data);
            }

            Log.d(TAG, "response: " + sb.toString());

        }  catch (MalformedURLException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            Log.d(TAG, "Status code: " + e.getMessage());
            e.printStackTrace();
        }

        return sb.toString();
    }

    public void post(){
        HttpURLConnection urlConnection = null;
        try {
            String postData = getPostDataFromForm();
            URL url = new URL("http://appdatahandler.azurewebsites.net/Api/location");
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");

            OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
            out.write(postData.getBytes());
            out.close();

            //urlConnection.setDoOutput(true);
            //urlConnection.setChunkedStreamingMode(1024);

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            String result = "";
            try
            {
                while(in.available()>0)
                {
                    // read the byte and convert the integer to character
                    char c = (char)in.read();

                    // print the characters
                    result += c;
                }
            }catch(Exception e){
                // if any I/O error occurs
                e.printStackTrace();
            }finally{
                // releases any system resources associated with the stream
                if(in!=null)
                    in.close();
            }
            // ((EditText)findViewById(R.id.editText)).setText(result);
        } catch (Exception ex) {
            String result = "";
            InputStream in = new BufferedInputStream(urlConnection.getErrorStream());

            try
            {
                while(in.available()>0)
                {
                    // read the byte and convert the integer to character
                    char c = (char)in.read();

                    // print the characters
                    result += c;
                }
            }catch(Exception e){
                // if any I/O error occurs
                e.printStackTrace();
            }

            String s = ex.getMessage();
        } finally {
            urlConnection.disconnect();
        }
    }

    private String getPostDataFromForm() {
        String result = "\n" +
                "{\n" +
                "\"UserName\": \""+location.getDevice()+"\",\n" +
                "\"Latitude\": \""+location.getLatitude()+"\",\n" +
                "\"Longitude\": \""+location.getLongitude()+"\"\n" +
                "}";
        return result;
    }

    @Override
    protected String doInBackground(Void... params) {
       post();
       // getHttpResponse();
        return "Called";
    }
}

package arduinoandroidblueprints.arduinowifi;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainScreen extends Activity {

    public static final String TAG = MainScreen.class.getSimpleName();

    final Activity activity = this;

    public static final String URL = "192.168.1.3";

    //Main Thread

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        //Declare our View Variables and assign them to the layout elements
        Button checkPowerButton = (Button) findViewById(R.id.checkPowerButton);
        Button openTheGateButton = (Button) findViewById(R.id.openGateButton);
        Button switchOnButton = (Button) findViewById(R.id.switchOnButton);
        Button switchOffButton = (Button) findViewById(R.id.switchOffButton);




        checkPowerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkAvailable()) {
                    checkPowerTask getPowerTask = new checkPowerTask();
                    getPowerTask.execute();
                }
            }
        });

        openTheGateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkAvailable()) {
                    SwitchOpenTask switchOpenTask = new SwitchOpenTask();
                    switchOpenTask.execute();
                }
            }
        });

        switchOnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkAvailable()) {
                    SwitchOnTask switchOnTask = new SwitchOnTask();
                    switchOnTask.execute();
                }
            }
        });

        switchOffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkAvailable()) {
                    SwitchOffTask switchOffTask = new SwitchOffTask();
                    switchOffTask.execute();
                }
            }
        });

    }



    private class SwitchOpenTask extends AsyncTask<Object,Void,String> {

        @Override
        protected String doInBackground(Object... arg0) {

            int responseCode = -1;

            try {
                URL restApiUrl = new URL("http://" + URL + "/mode/7/o");
                HttpURLConnection connection = (HttpURLConnection) restApiUrl.openConnection();
                connection.connect();

                responseCode = connection.getResponseCode();
                Log.i(TAG, "Code" + responseCode);
            }
            catch(MalformedURLException e) {
                Log.e(TAG, "Malformed Exception Caught:", e);
            }
            catch(IOException e) {
                Log.e(TAG, "IO Exception Caught:", e);
                e.printStackTrace();
            }
            catch(Exception e){
                Log.e(TAG, "Generic Exception Caught:", e);
            }

            return "Code: " + responseCode;

        }

    }

    private class SwitchOnTask extends AsyncTask<Object,Void,String> {

        @Override
        protected String doInBackground(Object... arg0) {

            int responseCode = -1;

            try {
                URL restApiUrl = new URL("http://" + URL + "/digital/7/1");
                HttpURLConnection connection = (HttpURLConnection) restApiUrl.openConnection();
                connection.connect();

                responseCode = connection.getResponseCode();
                Log.i(TAG, "Code" + responseCode);
            }
            catch(MalformedURLException e) {
                Log.e(TAG, "Malformed Exception Caught:", e);
            }
            catch(IOException e) {
                Log.e(TAG, "IO Exception Caught:", e);
                e.printStackTrace();
            }
            catch(Exception e){
                Log.e(TAG, "Generic Exception Caught:", e);
            }

            return "Code: " + responseCode;

        }

    }

    private class SwitchOffTask extends AsyncTask<Object,Void,String> {

        @Override
        protected String doInBackground(Object... arg0) {

            int responseCode = -1;

            try {
                URL restApiUrl = new URL("http://" + URL + "/digital/7/0");
                HttpURLConnection connection = (HttpURLConnection) restApiUrl.openConnection();
                connection.connect();

                responseCode = connection.getResponseCode();
                Log.i(TAG, "Code" + responseCode);
            }
            catch(MalformedURLException e) {
                Log.e(TAG, "Malformed Exception Caught:", e);
            }
            catch(IOException e) {
                Log.e(TAG, "IO Exception Caught:", e);
                e.printStackTrace();
            }
            catch(Exception e){
                Log.e(TAG, "Generic Exception Caught:", e);
            }

            return "Code: " + responseCode;

        }

    }

    private class checkPowerTask extends AsyncTask<Object,Void,String> {

        @Override
        protected String doInBackground(Object... arg0) {

            int responseCode = -1;
            String result = null;

            try {
                URL restApiUrl = new URL("http://" + URL + "/power");
                HttpURLConnection connection = (HttpURLConnection) restApiUrl.openConnection();
                connection.connect();
                responseCode = connection.getResponseCode();

                InputStream is = null;
                //http post
                try{
                    String postQuery = "http://" + URL + "/power";
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(postQuery);
                    HttpResponse response = httpclient.execute(httppost);
                    HttpEntity entity = response.getEntity();
                    is = entity.getContent();
                }catch(Exception e){
                    Log.e("log_tag", "Error in http connection "+e.toString());
                }

                //convert response to string
                try{
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is,"UTF-8"),8);
                    StringBuilder sb = new StringBuilder();
                    String line = null;

                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }

                    is.close();

                    result=sb.toString();
                    Log.v(TAG,result);

                } catch(Exception e){
                    Log.e("log_tag", "Error converting result "+e.toString());
                }

                //parse json data
                try {

                    JSONObject userObject = new JSONObject(result);
                    final String powerOutputText = userObject.getString("power");

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView powerOutput = (TextView) findViewById(R.id.powerOutput);
                            powerOutput.setText(powerOutputText + "W");

                        }
                    });

                } catch(JSONException e){
                    Log.e(TAG, "JSON Exception Caught:", e);
                }
            }
            catch(MalformedURLException e) {
                Log.e(TAG, "Malformed Exception Caught:", e);
            }
            catch(IOException e) {
                Log.e(TAG, "IO Exception Caught:", e);
                e.printStackTrace();
            }
            catch(Exception e){
                Log.e(TAG, "Generic Exception Caught:", e);
            }

            return "Code: " + responseCode;
        }

    }


    //Helper Methods
        private boolean isNetworkAvailable() {
            ConnectivityManager manager = (ConnectivityManager)
                    getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();

            boolean isAvailable = false;
            if (networkInfo != null && networkInfo.isConnected()) {
                isAvailable = true;
            }

            return isAvailable;
        }

    private void updateDisplayForError() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.error_title));
        builder.setMessage(getString(R.string.error_message));
        builder.setPositiveButton(android.R.string.ok, null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}

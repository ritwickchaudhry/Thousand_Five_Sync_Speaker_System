package five.thousand.thousandfive;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import five.thousand.thousandfive.Commands.Mrl;

public class MainActivity extends Activity {

    private RequestQueue requestQueue;
    public static String SERVER = "http://server.dns";
    private TextView status;
    private TextView banner;
    private String mrl;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        status = (TextView) findViewById(R.id.status);
        banner = (TextView) findViewById(R.id.banner);
        requestQueue = Volley.newRequestQueue(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        startService(new Intent(this, CommandServer.class));

        final Date deadlineDate;
        try {
            deadlineDate = (new SimpleDateFormat("yyyy-MM-dd HH:mm")).parse(this.getString(R.string.Surbahaar_Datetime));
            final Date currentDate = new Date();
            if (currentDate.before(deadlineDate)) {
                timer = new Timer(true);
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        long timeDiff = deadlineDate.getTime() - (new Date()).getTime();
                        long hours = timeDiff / (1000 * 60 * 60);
                        timeDiff -= (hours * 60 * 60 * 1000);
                        long minutes = timeDiff / (1000 * 60);
                        timeDiff -= (minutes * 60 * 1000);
                        long seconds = timeDiff / (1000);
                        final String statusStr = hours + " hours, " + minutes + " minutes, " + seconds + " seconds left to Surbahar,";
                        runOnUiThread(new Runnable(){
                            @Override
                            public void run() {
                                status.setText(statusStr);
                            }
                        });
                    }
                }, 1000, 1000);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        checkForServer();
    }

    private void checkForServer() {
        if (PlayerService.isPlaying()) {
            return;
        }

        StringRequest mrlRequest = new StringRequest(Request.Method.GET, SERVER + "/mrl",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Mrl m = (Mrl) CommandServer.readCommand(response);
                        mrl = m.mrl;
                        status.setText("Connected");
                        String bannerText = banner.getText().toString();
                        bannerText = bannerText.substring(0, 1).toUpperCase() + bannerText.substring(1);
                        banner.setText(bannerText);
                        submitID();
                        Intent intent = new Intent(getApplicationContext(), PlayerService.class);
                        intent.putExtra("mrl", mrl);
                        intent.putExtra("play", m.play);
                        startService(intent);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mrl = "";
                    }
                }
        );
        mrlRequest.setRetryPolicy(new DefaultRetryPolicy(1000, 3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(mrlRequest);
    }


    public void submitID() {
        final String id = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        if ( id.equals("") ) return;

        StringRequest postID = new StringRequest(Request.Method.POST, SERVER + "/id",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "Couldn't post ID to server", Toast.LENGTH_LONG).show();
                    }
                }
        ){
            @Override
            protected Map<String, String> getParams() {
                Map<String,String> params = new HashMap<String, String>();
                params.put("id", id);
                return params;
            }
        };
        requestQueue.add(postID);
    }

    @Override
    protected void onPause() {
        super.onPause();
        timer.cancel();
        timer.purge();
        timer = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopService(new Intent(this, CommandServer.class));
        stopService(new Intent(this, PlayerService.class));
    }
}
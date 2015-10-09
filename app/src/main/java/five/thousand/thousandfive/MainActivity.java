package five.thousand.thousandfive;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {

    private RequestQueue requestQueue;
    public static String SERVER = "http://server.dns";
    private LinearLayout idLayout;
    private Button button;
    private TextView textView;
    private String mrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        idLayout = (LinearLayout) findViewById(R.id.idText).getParent();
        textView = (TextView) findViewById(R.id.textView);
        button = (Button) findViewById(R.id.button);
        requestQueue = Volley.newRequestQueue(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        startService(new Intent(this, CommandServer.class));

        checkForServer();
    }

    public void buttonClicked(View v) {
        Intent intent;
        switch (button.getText().toString()) {
            case "Play":
                intent = new Intent(this, PlayerService.class);
                intent.putExtra("mrl", mrl);
                intent.putExtra("play", true);
                startService(intent);
                button.setText("Stop");
                break;
            case "Stop":
                if (PlayerService.isPlaying()) PlayerService.play(false);
                button.setText("Play");
                break;
            case "Recheck":
                checkForServer();
                break;
            case "Checking...":
            default:
        }
    }

    private void checkForServer() {
        if (PlayerService.isPlaying()) {
            button.setText("Stop");
            button.setEnabled(true);
            return;
        }

        button.setText("Checking...");
        button.setEnabled(false);

        StringRequest mrlRequest = new StringRequest(Request.Method.GET, SERVER + "/mrl",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        mrl = response;
                        textView.setVisibility(View.INVISIBLE);
                        idLayout.setVisibility(View.VISIBLE);
                        button.setText("Play");
                        button.setEnabled(true);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mrl = "";
                        textView.setText("Couldn't find streaming server.");
                        textView.setVisibility(View.VISIBLE);
                        button.setText("Recheck");
                        button.setEnabled(true);
                    }
                }
        );
        mrlRequest.setRetryPolicy(new DefaultRetryPolicy(1000, 3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(mrlRequest);
    }

    public void submitID(View v) {
        final EditText idText = (EditText) findViewById(R.id.idText);
        final String id = idText.getText().toString();

        if ( id.equals("") )return;

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
    protected void onDestroy() {
        super.onDestroy();

        stopService(new Intent(this, CommandServer.class));
        stopService(new Intent(this, PlayerService.class));
    }
}
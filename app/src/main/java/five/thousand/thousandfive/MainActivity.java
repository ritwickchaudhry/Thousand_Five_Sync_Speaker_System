package five.thousand.thousandfive;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class MainActivity extends Activity {

    private RequestQueue requestQueue;
    public static String SERVER = "http://server.dns/mrl";
    private Button button;
    private TextView textView;
    private String mrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.textView);
        button = (Button) findViewById(R.id.button);
        requestQueue = Volley.newRequestQueue(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

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
                button.setText("Pause");
                break;
            case "Pause":
                intent = new Intent(this, PlayerService.class);
                intent.putExtra("play", false);
                startService(intent);
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
            button.setText("Pause");
            button.setEnabled(true);
            return;
        }

        button.setText("Checking...");
        button.setEnabled(false);

        StringRequest mrlRequest = new StringRequest(Request.Method.GET, SERVER,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        mrl = response;
                        textView.setVisibility(View.INVISIBLE);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
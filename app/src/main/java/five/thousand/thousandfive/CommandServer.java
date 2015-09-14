package five.thousand.thousandfive;

import android.app.DownloadManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.JsonSerialization;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;

import java.io.IOException;

public class CommandServer extends Service {
    private Server server;
    private Context mContext;

    public CommandServer() {
        JsonSerialization js = new JsonSerialization();
        server = new Server(16384, 2048, js);
    }

    public class CommandRequest {
        public String command;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = this;

//        server.getKryo().register(CommandRequest.class);
        Log.set(Log.LEVEL_DEBUG);

        server.addListener(new Listener() {
            public void received(Connection conn, Object obj) {
                if (obj instanceof CommandRequest) {
                    CommandRequest command = (CommandRequest) obj;
                    Toast.makeText(mContext, "Got a command: " + command.command, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mContext, "Received something. Not a CommandRequest", Toast.LENGTH_LONG).show();
                }
                conn.close();
            }
        });

        try{
            server.bind(4443);
        }
        catch (IOException e)
        {
            Toast.makeText(this, "Couldn't Connect", Toast.LENGTH_LONG).show();
        }
        server.start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}

package five.thousand.thousandfive;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import java.io.IOException;

import five.thousand.thousandfive.Commands.Pause;
import five.thousand.thousandfive.Commands.Play;
import five.thousand.thousandfive.utils.CommandSerialization;

public class CommandServer extends Service {
    private Server server;

    public CommandServer() {
        CommandSerialization cs = new CommandSerialization();
        cs.register(Play.class);
        cs.register(Pause.class);
        server = new Server(16384, 2048, cs);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        server.addListener(new Listener() {
            @Override
            public void received(Connection conn, Object obj) {
                super.received(conn, obj);
                //TODO: Send control commands to player service
                conn.close();
            }
        });

        try{
            server.bind(4443);
        }
        catch (IOException e)
        {
            Toast.makeText(this, "Couldn't listen for commands", Toast.LENGTH_LONG).show();
        }
        server.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
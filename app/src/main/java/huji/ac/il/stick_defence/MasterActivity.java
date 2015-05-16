package huji.ac.il.stick_defence;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


public class MasterActivity extends Activity {
public static final int PORT=6666;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    private ArrayAdapter adapter;
    private ArrayList<Client> clients = new ArrayList<>();
    private ListView list;
    boolean serverOn = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_master);

        mManager = (WifiP2pManager) getSystemService(getApplicationContext().WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        list = (ListView) findViewById(R.id.clients);
        adapter = new clientsAdapter(this, android.R.layout.simple_list_item_1, clients);
        list.setAdapter(adapter);

        mManager.createGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.w("custom", "create group success!");

            }


            @Override
            public void onFailure(int reason) {
                Log.w("custom", "create group failed!");

            }
        });
        if (serverOn == false) {
            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... params) {
                    Log.w("custom", "starting server");
                    serverOn = true;
                    ServerSocket serverSocket = null;
                    try {
                        serverSocket = new ServerSocket(PORT);
                        while (serverOn) {

                            Socket socket = serverSocket.accept();
                            Log.w("custom", "client excepted!");
                            Client client = new Client(socket);
                            client.startListening();
                            clients.add(client);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.notifyDataSetChanged();
                                }
                            });

                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
        }


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
//        mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
//                    @Override
//                    public void onSuccess() {
//                        Log.w("custom", "group removed!");
//                    }
//
//                    @Override
//                    public void onFailure(int reason) {
//                        Log.w("custom", "group removed failed!");
//                    }
//                }
//        );
//        this.running=false;
    }

    //======================================Adapter classs==============================
    private class clientsAdapter extends ArrayAdapter<Client> {

        public clientsAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        public clientsAdapter(Context context, int resource, ArrayList<Client> items) {
            super(context, resource, items);
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(getContext());
                convertView = vi.inflate(android.R.layout.simple_list_item_1, null);
            }

            String title = getItem(position).getName();
            TextView titleView = (TextView) convertView.findViewById(android.R.id.text1);
            titleView.setText(title);
            return convertView;

        }
    }
}

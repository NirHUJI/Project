package huji.ac.il.stick_defence;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Formatter;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import java.io.IOException;
import java.net.Socket;
import java.util.Random;
import java.util.regex.Pattern;


public class MainMenu extends Activity implements DoProtocolAction {
    private Client client;// = Client.createClient(name);
    private boolean isCreateLeagueOptionsVisible = false;
    private boolean isInternet = false;
    private boolean stopSoundOnPause = true;
    private Sounds sounds;
    private String SAVED_IP = "SAVED_IP";
    private String NICKNAME= "NICKNAME";
    private String UNIQUE_ID= "UNIQUE_ID";
    private String SHARED_PREFERENCES= "SHARED_PREFERENCES";
    private final Pattern PARTIAl_IP_ADDRESS = Pattern.compile
             ("^((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[0-9])\\.){0,3}"+
              "((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[0-9])){0,1}$");
    private final String BUTTON_PUSHED_COLOR= "#FFFFCC";
    private final String BUTTON_RELEASED_COLOR="#000000";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        this.sounds = Sounds.create(this);
        super.onCreate(savedInstanceState);
        final SharedPreferences settings =
                getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        String name = settings.getString(NICKNAME, "");
        if(name.isEmpty()){
            showNicknameDialog(true/*initClient*/);
        }else{
            initClient();
        }
        stopSoundOnPause = true;
        FontsOverride.setDefaultFont(this, "SERIF", "Schoolbell.ttf");

        setContentView(R.layout.activity_main_menu);

        GameState.newGame();

        //===========================Player name================================
        TextView playerName = (TextView) findViewById(R.id.player_name);
        playerName.setText("Hello, " +
                settings.getString(NICKNAME, "friend") + "!");
        playerName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Sounds.playSound(Sounds.BUTTON_CLICK, false);
                showNicknameDialog(false);
            }
        });

        //==========================Single player===============================
        Button singlePlayer = (Button) findViewById(R.id.single_player);
        designButton(singlePlayer);
        singlePlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Sounds.playSound(Sounds.BUTTON_CLICK, false);
                stopSoundOnPause = false;
                Intent marketIntent = new Intent(getApplicationContext(),
                        Market.class);
                marketIntent.putExtra("isMultiplayer", false);
                startActivity(marketIntent);
                finish();
            }
        });

        //===========================CreateLeague===============================

        Button createLeague = (Button) findViewById(R.id.create_league);
        designButton(createLeague);
        createLeague.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Sounds.playSound(Sounds.BUTTON_CLICK, false);
                if (isCreateLeagueOptionsVisible) { //if league options are already visible,
                    isCreateLeagueOptionsVisible = false;
                    findViewById(R.id.create_league_options).setVisibility(View.GONE);
                    return;

                }
                final int MAX_N_PLAYERS = 8;
                final int MIN_N_PLAYERS = 2;
                final int[] nPlayers = {2};

                findViewById(R.id.create_league_options)
                        .setVisibility(View.VISIBLE);
                isCreateLeagueOptionsVisible = true;
                Button increasePlayersButton =
                        (Button) findViewById(R.id.more_players);
                Button decreasePlayersButton =
                        (Button) findViewById(R.id.less_players);
                Button startButton = (Button) findViewById(R.id.start);
                final TextView nPlayersText =
                        (TextView) findViewById(R.id.num_of_players);
                nPlayersText.setText(String.valueOf(nPlayers[0]));

                increasePlayersButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Sounds.playSound(Sounds.BUTTON_CLICK, false);
                        if (nPlayers[0] < MAX_N_PLAYERS) {
                            nPlayers[0]+=2;
                        }
                        nPlayersText.setText(String.valueOf(nPlayers[0]));
                    }
                });

                decreasePlayersButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Sounds.playSound(Sounds.BUTTON_CLICK, false);
                        if (nPlayers[0] > MIN_N_PLAYERS) {
                            nPlayers[0]-=2;
                        }
                        nPlayersText.setText(String.valueOf(nPlayers[0]));
                    }
                });


                startButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Sounds.playSound(Sounds.BUTTON_CLICK, false);
                        Server.createServer(nPlayers[0]);
                        int radioButtonId = ((RadioGroup)
                                findViewById(R.id.network_choice))
                                .getCheckedRadioButtonId();
                        RadioButton chosenButton =
                                (RadioButton) findViewById(radioButtonId);

                        if(chosenButton.getText().equals("WiFi")){
                            WifiP2pManager mManager = (WifiP2pManager)
                                    getSystemService(WIFI_P2P_SERVICE);
                            WifiP2pManager.Channel mChannel =
                                    mManager.initialize(getApplicationContext(),
                                                        getMainLooper(), null);
                            mManager.createGroup(mChannel, null);
                            mManager.requestConnectionInfo(mChannel,
                                    new WifiP2pManager.ConnectionInfoListener() {

                                @Override
                                public void onConnectionInfoAvailable(
                                                       final WifiP2pInfo info) {
                                    Log.w("custom", "groupInfo:");
                                    Log.w("custom", info.toString());
                                    if (info.groupOwnerAddress != null) {
                                        Log.w("custom", info.groupOwnerAddress
                                                .getHostAddress());
                                        new AsyncTask<Void, Void, Void>() {
                                            @Override
                                            protected Void doInBackground(Void... params) {
                                                try {
                                                    Socket socket = new Socket(info
                                                            .groupOwnerAddress
                                                            .getHostAddress(),
                                                            Server.PORT);
                                                    client.setServer(socket);
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                                return null;
                                            }
                                        }.executeOnExecutor(AsyncTask
                                                .THREAD_POOL_EXECUTOR, (Void)
                                                null);
                                    }

                                }
                            });

                        }

                        else{ //internet:
                            isInternet=true;
                            new AsyncTask<Void, Void, Void>() {
                                @Override
                                protected Void doInBackground(Void... params) {
                                    try {
                                        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
                                        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
                                        Socket socket = new Socket(ip, Server.PORT);
                                        client.setServer(socket);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    return null;
                                }
                            }.executeOnExecutor(AsyncTask
                                    .THREAD_POOL_EXECUTOR, (Void) null);
                        }

                    }
                });
            }

        });


        //===========================Join league================================
        Button joinLeague = (Button) findViewById(R.id.join_league);
        designButton(joinLeague);
        joinLeague.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Sounds.playSound(Sounds.BUTTON_CLICK, false);
                Intent createLeague = new Intent(getApplicationContext(),
                        JoinLeagueActivity.class);
                stopSoundOnPause = false;
                startActivity(createLeague);
                finish();
            }
        });

        //=====================Connect over lan button==========================
        Button connectOverLanButton = (Button) findViewById(R.id.enter_ip_button);
        designButton(connectOverLanButton);
        connectOverLanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Sounds.playSound(Sounds.BUTTON_CLICK, false);
                final Dialog dialog = new Dialog(MainMenu.this);
                dialog.setContentView(R.layout.enter_ip_dialog);
                dialog.setTitle("Enter server ip:");
                final EditText editText =
                        (EditText) dialog.findViewById(R.id.enter_ip_editText);
                final SharedPreferences settings =
                        getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
                String saved_ip= settings.getString(SAVED_IP,"10.0.0.0");
                editText.setText(saved_ip);
                editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count) {
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start,
                                                  int count, int after) {
                    }

                    private String mPreviousText = "";

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (PARTIAl_IP_ADDRESS.matcher(s).matches()) {
                            mPreviousText = s.toString();
                        } else {
                            s.replace(0, s.length(), mPreviousText);
                        }
                    }
                });
                Button connect = (Button) dialog.findViewById(R.id
                        .dialog_connect_button);
                connect.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Sounds.playSound(Sounds.BUTTON_CLICK, false);
                        dialog.dismiss();
                        final String ip= editText.getText().toString();
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(SAVED_IP, ip);
                        editor.commit();
                        new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... params) {
                                try {
                                    Socket socket = new Socket(ip, Server.PORT);
                                    client.setServer(socket);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                return null;
                            }
                        }.executeOnExecutor(AsyncTask
                                .THREAD_POOL_EXECUTOR, (Void) null);
                    }
                });
                dialog.show();
                
            }
        });
    }

    public void showNicknameDialog(final boolean initClient){
        final Dialog dialog = new Dialog(MainMenu.this);
        // We want to give the user option to regret if there is an old nickname
        dialog.setCancelable(!initClient);
        dialog.setContentView(R.layout.enter_nickname_dialog);
        dialog.setTitle("Choose your nickname:");

        final EditText editText =
                (EditText) dialog.findViewById(R.id.nickname_editText);
        final Button okButton =
                (Button) dialog.findViewById(R.id.nickname_ok_button);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name= editText.getText().toString();
                final SharedPreferences settings =
                        getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(NICKNAME, name);
                editor.commit();
                if (initClient){
                    initClient();
                } else {
                    client.changeName(name);
                }
                TextView playerName = (TextView) findViewById(R.id.player_name);
                playerName.setText("Hello, " +
                        settings.getString(NICKNAME, "friend") + "!");
                dialog.dismiss();

            }
        });
        okButton.setEnabled(false);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (editText.getText().toString().length() > 0) {
                    okButton.setEnabled(true);
                } else {
                    okButton.setEnabled(false);
                }
            }
        });
        dialog.show();
        generateRandomId();
    }

    private void initClient(){
        final SharedPreferences settings = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        String name = settings.getString(NICKNAME, "");
        String id =   settings.getString(UNIQUE_ID, "");
        this.client = Client.createClient(name,id);
        this.client.setCurrentActivity(this);
    }

    private  void generateRandomId(){
        Random rand = new Random(System.currentTimeMillis());
        long id= rand.nextInt(100000000);
        final SharedPreferences settings = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(UNIQUE_ID, String.valueOf(id));
        editor.commit();
    }

    @Override
    public void doAction(String rawInput) {
        Protocol.Action action = Protocol.getAction(rawInput);
        if (null == action){
            return;
        }
        switch (action) {
            case NAME_CONFIRMED:
                Log.w("custom", "going to marekt");
                Intent intent = new Intent(this, Market.class);
                intent.putExtra("isMultiplayer", true);
                if(isInternet){
                    intent.putExtra("internet", true);
                }
                stopSoundOnPause = false;
                startActivity(intent);
                finish();
                break;

            case LEAGUE_INFO:
                Log.w("custom", "going to marekt");
                Intent intentWithInfo = new Intent(this, Market.class);
                intentWithInfo.putExtra("isMultiplayer", true);
                String info = Protocol.getData(rawInput);
                intentWithInfo.putExtra("info", info);
                stopSoundOnPause = false;
                startActivity(intentWithInfo);
                finish();
                break;
        }
    }

    private void designButton(final Button button){
        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                button.setTextColor(Color.parseColor(BUTTON_PUSHED_COLOR));
                button.setShadowLayer(4, 0, 0,
                        Color.parseColor(BUTTON_RELEASED_COLOR));
                button.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                button.setTextColor(Color.parseColor(BUTTON_RELEASED_COLOR));
                button.setTypeface(Typeface.SERIF);
                button.setShadowLayer(0, 0, 0, 0);
            }
            return false;
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (stopSoundOnPause){
            sounds.stopTheme();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sounds.playTheme(Sounds.MAIN_THEME);
    }
}

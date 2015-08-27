package huji.ac.il.stick_defence;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;


public class Market extends Activity implements DoProtocolAction {
//TODO: CREATE A BUTTON THAT MOVES YOU INTO LEAGUE_INFO ACTIVITY AND IF THERE IS INFO, SEND EXTRA IN THE INTENT
    public static final int    ZOMBIE_BUY_PRICE = 100;
    public static final int    SWORDMAN_BUY_PRICE = 50;
    public static final int    BOMB_GRANDPA_BUY_PRICE = 100;
    public static final int    TANK_BUY_PRICE = 100;
    public static final int    BAZOOKA_BUY_PRICE = 100;
    public static final int    MATH_BOMB_PRICE = 100;
    public static final int    BIG_WOODEN_TOWER_PRICE = 100;
    public static final int    STONE_TOWER_PRICE = 100;
    public static final int    FORTIFIED_TOWER_PRICE = 100;
    public static final int    FOG_PRICE = 100;

    private static final String CREDITS = "Credits: ";
    private Tower.TowerTypes myTowerType;

    private String savedLeagueInfo = null;
    private GameState gameState;
    AlertDialog.Builder alertDialogBuilder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
       // Sounds.getInstance().playTheme(Sounds.MAIN_THEME);
        Client.getClientInstance().setCurrentActivity(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_market);
        gameState = GameState.getInstance();
        alertDialogBuilder = new AlertDialog.Builder(this);
        myTowerType = gameState.getLeftTowerType();
        final boolean isMultiplayer = getIntent().getBooleanExtra("Multiplayer", true);
        if (isMultiplayer) {
            Client.getClientInstance().
                    send(Protocol.stringify(Protocol.Action.GAME_OVER,
                            String.
                                    valueOf(GameState.getInstance().isLeftPlayerWin())));
        }
        Button continueButton = (Button) findViewById(R.id.market_play_button);

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isMultiplayer) {
                    Intent intent = new Intent(getApplicationContext(),
                            GameActivity.class);
                    intent.putExtra("Multiplayer", isMultiplayer);
                    intent.putExtra("NewGame", false);
                    startActivity(intent);
                    finish();
                } else { //go to league info
                    gameState.sendInfoToPartner(myTowerType);
                    Intent intent = new Intent(getApplicationContext(),
                            LeagueInfoActivity.class);
                    intent.putExtra("NewGame", false);
                    if (savedLeagueInfo != null) {
                        intent.putExtra("info", savedLeagueInfo);
                    }
                    startActivity(intent);
                    finish();
                }

            }
        });

        //Show credits
        int credits = gameState.getCredits(Sprite.Player.LEFT);
        final TextView creditsTv = (TextView) findViewById(R.id.market_credits_tv);
        creditsTv.setText(CREDITS + credits + "$");

        //Add buy buttons
        addButton(PlayerStorage.PurchasesEnum.ZOMBIE,
                  R.id.buy_zombie,
                  ZOMBIE_BUY_PRICE,
                  "Zombie",
                  Zombie.info());
        addButton(PlayerStorage.PurchasesEnum.BAZOOKA_SOLDIER,
                  R.id.buy_bazooka_soldier,
                  BAZOOKA_BUY_PRICE,
                  "Bazooka Soldier",
                  BazookaSoldier.info());
        addButton(PlayerStorage.PurchasesEnum.SWORDMAN,
                  R.id.buy_swordman,
                  SWORDMAN_BUY_PRICE,
                  "Swordman",
                  Swordman.info());
        addButton(PlayerStorage.PurchasesEnum.BOMB_GRANDPA,
                  R.id.buy_bomb_grandpa,
                  BOMB_GRANDPA_BUY_PRICE,
                  "Bomb Grandpa",
                  BombGrandpa.info());
        addButton(PlayerStorage.PurchasesEnum.TANK,
                  R.id.buy_tank,
                  TANK_BUY_PRICE,
                  "Tank",
                  Tank.info());
        if (isMultiplayer){
            addButton(PlayerStorage.PurchasesEnum.MATH_BOMB,
                    R.id.buy_math_bomb,
                    MATH_BOMB_PRICE,
                    "Math Bomb",
                    MathBomb.info());
            addButton(PlayerStorage.PurchasesEnum.FOG, R.id.buy_fog, FOG_PRICE,
                      "Fog", Fog.info());
        } else {
            LinearLayout linearLayout = (LinearLayout) findViewById(R.id.miscellaneous_list);
            linearLayout.setVisibility(View.INVISIBLE);
        }


        //BigWoodenTower
        final Button bigWoodenTowerButton =
                (Button) findViewById(R.id.buy_big_wooden_tower);
        if (gameState.isPurchased(PlayerStorage.PurchasesEnum.BIG_WOODEN_TOWER) ||
            gameState.isPurchased(PlayerStorage.PurchasesEnum.STONE_TOWER) ||
            gameState.isPurchased(PlayerStorage.PurchasesEnum.FORTIFIED_TOWER)) {
            bigWoodenTowerButton.setVisibility(View.INVISIBLE);
        } else {
            bigWoodenTowerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialogBuilder.setTitle("Big Wooden Tower");
                    alertDialogBuilder
                            .setMessage(BigWoodenTower.info())
                            .setCancelable(false)
                            .setPositiveButton("Buy",new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {
                                    int credits = gameState.getCredits(Sprite.Player.LEFT);
                                    if (credits >= BIG_WOODEN_TOWER_PRICE) {
                                        gameState.buyItem(PlayerStorage.PurchasesEnum.BIG_WOODEN_TOWER,
                                                BIG_WOODEN_TOWER_PRICE);
                                        credits -= BIG_WOODEN_TOWER_PRICE;
                                        creditsTv.setText(CREDITS + credits + "$");
                                        bigWoodenTowerButton.setVisibility(View.INVISIBLE);
                                        myTowerType = Tower.TowerTypes.BIG_WOODEN_TOWER;
                                    }
                                }
                            })
                            .setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {
                                    dialog.cancel();
                                }
                            });

                    // create alert dialog
                    AlertDialog alertDialog = alertDialogBuilder.create();

                    // show it
                    alertDialog.show();
                }
            });
        }

        //StoneTower
        final Button stoneTowerButton = (Button) findViewById(R.id.buy_stone_tower);
        if (gameState.isPurchased(PlayerStorage.PurchasesEnum.STONE_TOWER) ||
            gameState.isPurchased(PlayerStorage.PurchasesEnum.FORTIFIED_TOWER)) {
            stoneTowerButton.setVisibility(View.INVISIBLE);
        } else {
            stoneTowerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialogBuilder.setTitle("Stone Tower");
                    alertDialogBuilder
                            .setMessage(StoneTower.info())
                            .setCancelable(false)
                            .setPositiveButton("Buy",new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {
                                    int credits = gameState.getCredits(Sprite.Player.LEFT);
                                    if (credits >= STONE_TOWER_PRICE) {
                                        gameState.buyItem(PlayerStorage.PurchasesEnum.STONE_TOWER,
                                                STONE_TOWER_PRICE);
                                        credits -= STONE_TOWER_PRICE;
                                        creditsTv.setText(CREDITS + credits + "$");
                                        bigWoodenTowerButton.setVisibility(View.INVISIBLE);
                                        myTowerType = Tower.TowerTypes.STONE_TOWER;
                                    }
                                }
                            })
                            .setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {
                                    dialog.cancel();
                                }
                            });

                    // create alert dialog
                    AlertDialog alertDialog = alertDialogBuilder.create();

                    // show it
                    alertDialog.show();
                }
            });
        }

        //StoneTower
        Button fortifiedTowerButton = (Button) findViewById(R.id.buy_fortified_tower);
        if (gameState.isPurchased(PlayerStorage.PurchasesEnum.FORTIFIED_TOWER)) {
            fortifiedTowerButton.setVisibility(View.INVISIBLE);
        } else {
            fortifiedTowerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialogBuilder.setTitle("Fortified Tower");
                    alertDialogBuilder
                            .setMessage(FortifiedTower.info())
                            .setCancelable(false)
                            .setPositiveButton("Buy",new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {
                                    int credits = gameState.getCredits(Sprite.Player.LEFT);
                                    if (credits >= FORTIFIED_TOWER_PRICE) {
                                        gameState.buyItem(PlayerStorage.PurchasesEnum.FORTIFIED_TOWER,
                                                FORTIFIED_TOWER_PRICE);
                                        credits -= FORTIFIED_TOWER_PRICE;
                                        creditsTv.setText(CREDITS + credits + "$");
                                        bigWoodenTowerButton.setVisibility(View.INVISIBLE);
                                        stoneTowerButton.setVisibility(View.INVISIBLE);
                                        myTowerType = Tower.TowerTypes.FORTIFIED_TOWER;
                                    }
                                }
                            })
                            .setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {
                                    dialog.cancel();
                                }
                            });

                    // create alert dialog
                    AlertDialog alertDialog = alertDialogBuilder.create();

                    // show it
                    alertDialog.show();
                }
            });
        }
//        addButton(PlayerStorage.PurchasesEnum.BIG_WOODEN_TOWER,
//                R.id.buy_big_wooden_tower,
//                BIG_WOODEN_TOWER_PRICE);
//        addButton(PlayerStorage.PurchasesEnum.STONE_TOWER,
//                R.id.buy_stone_tower,
//                STONE_TOWER_PRICE);
//        addButton(PlayerStorage.PurchasesEnum.FORTIFIED_TOWER,
//                R.id.buy_fortified_tower,
//                FORTIFIED_TOWER_PRICE);

    }

    private void addButton(final PlayerStorage.PurchasesEnum item,
                           int iconId,
                           final int price,
                           final String title,
                           final String info) {
        final Button buyButton = (Button) findViewById(iconId);
        final TextView creditsTv = (TextView) findViewById(R.id.market_credits_tv);
        if (gameState.isPurchased(item)) {
            buyButton.setVisibility(View.INVISIBLE);
        } else {
            buyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int credits = gameState.getCredits(Sprite.Player.LEFT);
                    if (credits >= price) {
                        gameState.buyItem(item, price);
                        credits -= price;
                        creditsTv.setText(CREDITS + credits + "$");
                        v.setVisibility(View.INVISIBLE);
                    }

                    alertDialogBuilder.setTitle(title);
                    alertDialogBuilder
                            .setMessage(info)
                            .setCancelable(false)
                            .setPositiveButton("Buy",new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {
                                    int credits = gameState.getCredits(Sprite.Player.LEFT);
                                    if (credits >= price) {
                                        gameState.buyItem(item, price);
                                        credits -= price;
                                        creditsTv.setText(CREDITS + credits + "$");
                                        buyButton.setVisibility(View.INVISIBLE);
                                    }
                                }
                            })
                            .setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {
                                    dialog.cancel();
                                }
                            });

                    // create alert dialog
                    AlertDialog alertDialog = alertDialogBuilder.create();

                    // show it
                    alertDialog.show();
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_market, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.exit_to_main_menu) {
            File file = new File(getFilesDir(), PlayerStorage.FILE_NAME);
            if (!file.delete()) {
                Log.w("yahav", "Failed to delete file");
            }
            Intent intent = new Intent(getApplicationContext(), MainMenu.class);
            startActivity(intent);
            finish();
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Quit")
                .setMessage("Are you sure you want to quit to main menu?")
                .setPositiveButton(android.R.string.yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                File file = new File(getFilesDir(),
                                        PlayerStorage.FILE_NAME);
                                if (!file.delete()) {
                                    Log.w("yahav", "Failed to delete file");
                                }
                                Intent intent = new Intent(getApplicationContext(),
                                        MainMenu.class);
                                startActivity(intent);
                                finish();
                            }
                        })
                .setNegativeButton(android.R.string.no,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    public void doAction(String rawInput) {
        Protocol.Action action = Protocol.getAction(rawInput);
        String rawInfo = Protocol.getData(rawInput);


        switch (action) {
            case LEAGUE_INFO:
                savedLeagueInfo = Protocol.getData(rawInput);
                gameState.sendInfoToPartner(myTowerType); //we got the leauge info so we know now that we have
                //parnter and we need to send him information
                break;

            case PARTNER_INFO:
                GameState.getInstance().newPartnerInfo(rawInfo);
                break;
        }
    }

}

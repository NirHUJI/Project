package huji.ac.il.stick_defence;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;


public class Market extends Activity {

    private static final int BAZOOKA_BUY_PRICE = 100; // TODO - change to 1000
    private static final String CREDITS = "Credits: ";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_market);

        final boolean isMultiplayer = getIntent().getBooleanExtra("Multiplayer", true);
        Button continueButton = (Button) findViewById(R.id.market_play_button);

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),
                                           GameActivity.class);
                intent.putExtra("Multiplayer", isMultiplayer);
                intent.putExtra("NewGame", true);
                startActivity(intent);
                finish();
            }
        });

        final GameState gameState = GameState.getInstance();
        int credits = gameState.getCredits(Sprite.Player.LEFT);

        final TextView creditsTv = (TextView) findViewById(R.id.market_credits_tv);
        creditsTv.setText(CREDITS + credits + "$");
        Button buyBazookaSoldier = (Button) findViewById(R.id.buy_bazooka_soldier);

        if (gameState.isHaveSoldier(PlayerStorage.SoldiersEnum.BAZOOKA_SOLDIER)){
            buyBazookaSoldier.setVisibility(View.INVISIBLE);
        } else {
            buyBazookaSoldier.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    gameState.
                            buySoldier(PlayerStorage.SoldiersEnum.BAZOOKA_SOLDIER,
                                    BAZOOKA_BUY_PRICE);
                    int credits = gameState.getCredits(Sprite.Player.LEFT);
                    credits -= BAZOOKA_BUY_PRICE;
                    creditsTv.setText(CREDITS + credits + "$");
                    gameState.save();
                    v.setVisibility(View.INVISIBLE);
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
            if (!file.delete()){
                Log.w("yahav", "Failed to delete file");
            }
            Intent intent = new Intent(getApplicationContext(), MainMenu.class);
            startActivity(intent);
            finish();
        }


        return super.onOptionsItemSelected(item);
    }
}

package huji.ac.il.stick_defence;

import android.content.Context;

/**
 * This class represents a fortified tower
 */
public class FortifiedTower extends Tower {
    public static final int MAX_HP = 400;

    /**
     * Constructor
     * @param context the context
     * @param player the player
     */
    public FortifiedTower(Context context, Sprite.Player player){
        super(context, player, R.drawable.fortified_tower,
              R.drawable.fortified_tower, MAX_HP, TowerTypes.FORTIFIED_TOWER);
    }

    @Override
    public int getLeftX() {
        return (int) (super.getLeftX() * 1.15);
    }

    @Override
    public int getRightX() {
        return (int) (super.getRightX() * 0.85);
    }

    public static String info(){
        return "HP: " + MAX_HP + "\n" +
                "A small stronghold. Defends you against the strongest opponents.\n" +
                "Replaces the 'Wooden tower', the 'Big Wooden Tower' " +
                "and the 'Stone Tower'.\n\n" +
                "Price: " + Market.FORTIFIED_TOWER_PRICE;
    }
}

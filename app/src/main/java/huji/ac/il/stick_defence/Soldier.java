package huji.ac.il.stick_defence;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * This class represents an abstract soldier figure.
 * Extension of this class must handle the transition to the attack mode
 * by itself. This can be handled by attack() and isAttack() methods.
 */
public abstract class Soldier implements DrawableObject{
    public enum SoldierType{
        BASIC, BAZOOKA, BOMB_GRANDPA, SWORDMAN, TANK, ZOMBIE
    }

    //Epsilon to hit soldier from his center
    private static final int HIT_EPSILON = 10;

    protected GameState gameState = GameState.getInstance();

    private Sprite sprite;

    //Characteristics
    private SoldierType soldierType;
    private final Sprite.Player PLAYER;
    private final int DAMAGE_PER_SEC;
    private double runPixelsPerSec;
    private int hp, startHp;

    //Positions
    public static int leftId = 0, rightId = 0;
    private int id;
    private int screenWidth;
    private int screenHeight;
    private double soldierX;
    private double soldierY;
    private long lastUpdateTime;
    private boolean attack;
    private double secToCrossScreen;
    private double delayInSec;
    private Context context;
    protected int soundStream;
    protected int soundId;

    /**
     * Constructor
     * @param context the context
     * @param player the player
     * @param secToCrossScreen seconds it takes the soldier to cross screen
     * @param damagePerSec damage per second
     * @param soundId the soundId resource
     * @param delayInSec delay in seconds from other player
     * @param hp player's HP
     * @param soldierType the soldier's type
     */
    protected Soldier(Context context, Sprite.Player player,
                      double secToCrossScreen, int damagePerSec, int soundId,
                      double delayInSec, int hp, SoldierType soldierType) {
        this.PLAYER = player;
        this.soundId = soundId;
        this.screenWidth = GameState.getCanvasWidth();
        this.screenHeight = GameState.getCanvasHeight();
        this.context = context;
        this.hp = this.startHp = hp;
        this.DAMAGE_PER_SEC = damagePerSec;
        this.attack = false;
        lastUpdateTime = System.currentTimeMillis();
        this.delayInSec = delayInSec;
        this.secToCrossScreen = secToCrossScreen;

        if (Sprite.Player.LEFT == player){
            this.id = leftId++;
        } else {
            this.id = rightId++;
        }
        this.soldierType = soldierType;
    }

    /**
     * Initialization. Must be called after construction.
     * @param soldierPic the soldier picture
     * @param nFrames the number of franmes
     * @param screenPortion the screen portion that the soldier draw on
     * @param animationSpeed the animation speed
     */
    protected void initSprite(Bitmap soldierPic, int nFrames,
                              double screenPortion, int animationSpeed) {

        sprite = new Sprite();
        sprite.initSprite(soldierPic, nFrames, PLAYER, screenPortion);
        sprite.setAnimationSpeed(animationSpeed);

        //set the y on the bottom of the screen
        this.soldierY = screenHeight - (int) sprite.getScaledFrameHeight();

        //Set x
        if (this.PLAYER == Sprite.Player.LEFT) {
            soldierX = -sprite.getScaledFrameWidth(); //Start hidden
        } else {
            soldierX = screenWidth; //Start hidden
        }

        //Set speed
        if (this.PLAYER == Sprite.Player.LEFT) {
            this.runPixelsPerSec = ((double) screenWidth) /
                                    (secToCrossScreen - delayInSec);

        } else {
            this.runPixelsPerSec = - ((double) screenWidth) /
                                      (secToCrossScreen - delayInSec);
        }
    }

    /**
     * Reset the soldiers Id counter. This is necessary for consistency between
     * every two players.
     */
    public static void resetIds(){ leftId = rightId = 0; }

    protected Context getContext() { return this.context; }

    /**
     * Switch to attack mode
     * @param attackSoldierPic the attack picture
     * @param nFrames number of frames in the attack picture
     * @param fps the fps
     */
    protected void attack(Bitmap attackSoldierPic, int nFrames, int fps) {
        sprite.setPic(attackSoldierPic, nFrames);
        sprite.setAnimationSpeed(fps);
        attack();
    }

    /**
     * Switch to attack mode
     */
    protected void attack() {
        stopSound();
        this.attack = true;
        this.soldierY = screenHeight - (int) sprite.getScaledFrameHeight();
    }

    public int getId(){ return this.id; }

    protected double getScaledDownFactor() {
        return sprite.getScaleDownFactor();
    }

    protected boolean isAttack() { return this.attack; }

    @Override
    public void update(long gameTime) {
        sprite.update(gameTime);
        double passedTimeInSec = (double) (gameTime - lastUpdateTime) / 1000;
        lastUpdateTime = System.currentTimeMillis();
        if (attack) {
            gameState.hitTower(PLAYER, DAMAGE_PER_SEC * passedTimeInSec);
        } else {
            soldierX += (runPixelsPerSec * passedTimeInSec);
        }
    }

    @Override
    public void render(Canvas canvas) {
        sprite.render(canvas, getSoldierX(), getSoldierY());
        Paint paint = new Paint();
        paint.setColor(PLAYER == Sprite.Player.RIGHT ? Color.RED : Color.BLUE);
        paint.setStrokeWidth(10);
        float lifeStartX = (float) (getSoldierX() + (sprite.getScaledFrameWidth()
                / 2) - HIT_EPSILON);
        float lifeEndX = lifeStartX +
                (((float)hp/(float)startHp) * 2 * HIT_EPSILON);
        canvas.drawLine(lifeStartX, getSoldierY(),
                        lifeEndX, getSoldierY(), paint);
    }

    protected int getSoldierX() {
        return (int) Math.round(this.soldierX);
    }

    protected void setSoldierX(int soldierX) {
        this.soldierX = soldierX;
    }

    protected int getSoldierY() {
        return (int) Math.round(this.soldierY);
    }

    protected double getScaledFrameWidth() {
        return sprite.getScaledFrameWidth();
    }

    protected boolean isHitByArrow(Arrow arrow) {
        return (arrow.getPlayer() != this.getPlayer() &&
                this.soldierY <= arrow.getHeadY() &&
                Math.abs(this.soldierX + sprite.getScaledFrameWidth() / 2 -
                        arrow.getHeadX()) <= HIT_EPSILON);
    }

    /**
     * This method should be called only after a soldier hit by an arrow.
     * @param damage the damaged to do
     * @return true iff the soldier died
     */
    public boolean reduceHp(int damage){
        this.hp -= damage;
        return this.hp <= 0;
    }

    public Sprite.Player getPlayer() {
        return this.PLAYER;
    }

    public int getScreenWidth() {
        return this.screenWidth;
    }

    public int getCurrentFrame() {
        return sprite.getCurrentFrame();
    }

    public void playSound(){
       this.soundStream = Sounds.playSound(this.soundId, false);
    }
    public void stopSound(){
        Sounds.stopSound(this.soundStream);
    }

    public void setSound(int sound){
        stopSound();
        this.soundId = sound;

    }

    public SoldierType getSoldierType(){ return this.soldierType; }

}

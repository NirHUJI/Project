package huji.ac.il.stick_defence;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;

import java.io.Serializable;

/**
 * Created by Nir on 03/05/2015.
 */
public class Arrow implements Serializable{
    //===========================Arrow's abilities==============================
    private static final double SEC_TO_SCREEN_WIDTH = 0.021;
    private static final double SEC_TO_SCREEN_HEIGHT = 0.037;

    //============================Arrow's picture===============================
    private static Bitmap scaledArrowPic;
    private static Sprite sprite;

    private GameState     gameState = GameState.getInstance();
    private float         x;
    private float         y;
    private float         degree;
    private float         bm_offsetX;
    private float         bm_offsetY;
    private transient  Matrix matrix = new Matrix();
    private int           screenWidth;
    private int           screenHeight;
    private long          lastUpdateTime;
    private double        x_pixPerSec;
    private double        y_pixPerSec;
    private Sprite.Player player;

    public Arrow(Context context, float x, float y,
                 float[] tan, Sprite.Player player){
        this.screenWidth =
                context.getResources().getDisplayMetrics().widthPixels;
        this.screenHeight =
                context.getResources().getDisplayMetrics().heightPixels;
        this.x=x;
        this.y=y;
        this.player = player;
        this.bm_offsetX =scaledArrowPic.getWidth()/2;
        this.bm_offsetY= scaledArrowPic.getHeight()/2;
        this.degree =(float)(Math.atan2(tan[1], tan[0])*180.0/Math.PI);
        updateMatrix();

        x_pixPerSec = SEC_TO_SCREEN_WIDTH * screenWidth;
        y_pixPerSec = SEC_TO_SCREEN_HEIGHT * screenHeight;

        resetUpdateTime();

    }

    private void updateMatrix(){
        matrix.reset();
        matrix.postRotate(this.degree, bm_offsetX, bm_offsetY);
        matrix.postTranslate(this.x- bm_offsetX, this.y- bm_offsetY);

    }

    public void update(long gameTime){
        double passedTimeInSec = (double)(gameTime - lastUpdateTime) / 1000;
        this.x +=
                Math.cos(Math.toRadians(this.degree)) *
                        x_pixPerSec * passedTimeInSec;
        this.y +=
                Math.sin(Math.toRadians(this.degree)) *
                        y_pixPerSec * passedTimeInSec;
        updateMatrix();
        if(this.x>this.screenWidth || this.y>this.screenHeight){
            gameState.removeArrow(this);
        }
    }

    public void resetUpdateTime(){
        lastUpdateTime = System.currentTimeMillis();
    }

    public void render(Canvas canvas){
        canvas.drawBitmap(scaledArrowPic, matrix, null);
        Paint paint=new Paint();
        paint.setColor(this.player == Sprite.Player.RIGHT ? Color.RED : Color.BLUE);
        paint.setStrokeWidth(10);
        canvas.drawPoint(this.getHeadX(), this.getHeadY(), paint);

    }

    public static void init(Context context,double scaleDownFactor){

       Bitmap arrowPic = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.arrow); // Read resource only once

        if(sprite==null){
            sprite= new Sprite();
            sprite.initSprite(context, arrowPic, 1, Sprite.Player.LEFT, 1.0);
            sprite.setScaleDownFactor(scaleDownFactor);
        }

        if(scaledArrowPic==null){
            scaledArrowPic =
                    Bitmap.createScaledBitmap(arrowPic,
                    (int) sprite.getScaledFrameWidth(),
                    (int) sprite.getScaledFrameHeight(), false);
        }
    }

    public int  getHeadX(){
        return (int) (this.x + Math.cos(Math.toRadians(this.degree))
                             * scaledArrowPic.getWidth()/2);
    }

    public int  getHeadY(){
        return (int) (this.y + Math.sin(Math.toRadians(this.degree))
                             * scaledArrowPic.getWidth()/2);
    }

    public Sprite.Player getPlayer(){
        return this.player;
    }

}

package com.jtrofe.apps.lathe;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


public class AppPanel extends SurfaceView implements SurfaceHolder.Callback{
    public static final int WIDTH = 856;
    public static final int HEIGHT = 480;

    private float scaleX;
    private float scaleY;

    private MainThread thread;
    private Background bg;

    private Sculpture sculpture;

    private Gouge gouge;

    private boolean cutting;

    private Point lastCutPoint;

    private Vibrator vibrator;

    public AppPanel(Context context){
        super(context);

        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        //add the callback to the surfaceHolder to intercept events
        getHolder().addCallback(this);

        thread = new MainThread(getHolder(), this);

        //make gamePanel focusable so it can handle events
        setFocusable(true);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder){
        boolean retry = true;
        int counter = 0;
        while(retry && counter < 1000){
            counter ++;
            try{
                thread.setRunning(false);
                thread.join();
                retry = false;
            }catch(InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder){

        //TODO Initialize variables

        scaleX = getWidth()/(WIDTH * 1.f);
        scaleY = getHeight()/(HEIGHT * 1.f);

        Bitmap[] woodFrames = new Bitmap[3];

        woodFrames[0] = BitmapFactory.decodeResource(getResources(), R.drawable.light_wood_blur1);
        woodFrames[1] = BitmapFactory.decodeResource(getResources(), R.drawable.light_wood_blur2);
        woodFrames[2] = BitmapFactory.decodeResource(getResources(), R.drawable.light_wood_blur3);

        sculpture = new Sculpture(700, 110, 120, 240, woodFrames);

        gouge = new Gouge(new Point(600, 240), 100, new Point(120, 240));

        Bitmap b = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888);
        b.eraseColor(Color.BLACK);
        bg = new Background(b);

        cutting = false;

        //We can start came loop
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event){
        int eventX = (int) (event.getX()/scaleX);
        int eventY = (int) (event.getY()/scaleY);
        Rect sculptBounds = sculpture.getBounds();

        Point cutPoint = gouge.GetCutPoint();
        cutPoint = sculpture.NormalizePoint(cutPoint.x, cutPoint.y);

        if(event.getAction() == MotionEvent.ACTION_DOWN){
            cutting = true;
            gouge.SetPosition(new Point(eventX, eventY));cutPoint = gouge.GetCutPoint();
            cutPoint = sculpture.NormalizePoint(cutPoint.x, cutPoint.y);

            //lastCutPoint = sculpture.NormalizePoint(eventX, eventY);
            lastCutPoint = cutPoint;

            return true;
        }else if(event.getAction() == MotionEvent.ACTION_UP){
            sculpture.Fill();
            cutting = false;
            return true;
        }else if(event.getAction() == MotionEvent.ACTION_MOVE){
            if(!cutting) return true;

            gouge.SetPosition(new Point(eventX, eventY));


            //Point cutPoint = sculpture.NormalizePoint(eventX, eventY);


            System.out.println(cutPoint.toString());
            if(sculpture.ContainsPoint(cutPoint) || sculpture.ContainsPoint(lastCutPoint)){
                vibrator.vibrate(5);
            }

            sculpture.Cut(lastCutPoint, cutPoint);

            lastCutPoint = cutPoint;
        }
        return super.onTouchEvent(event);
    }

    public void update(){
        bg.update();
    }

    @Override
    public void draw(@NonNull Canvas canvas){

        final int savedState = canvas.save();
        canvas.scale(scaleX, scaleY);
        bg.draw(canvas);

        sculpture.draw(canvas, 100, 240);

        gouge.draw(canvas);

        canvas.restoreToCount(savedState);
    }
}

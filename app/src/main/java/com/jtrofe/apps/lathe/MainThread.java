package com.jtrofe.apps.lathe;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class MainThread extends Thread{
    private final SurfaceHolder surfaceHolder;
    private AppPanel appPanel;
    private boolean running;
    public static Canvas canvas;

    public MainThread(SurfaceHolder _surfaceHolder, AppPanel _appPanel){
        super();
        surfaceHolder = _surfaceHolder;
        appPanel = _appPanel;
    }

    @Override
    public void run(){
        long startTime;
        long timeMillis;
        long waitTime;
        //30 = target frames per second
        long targetTime = 1000/30;

        while(running){
            startTime = System.nanoTime();
            canvas = null;

            //try locking the canvas for pixel editing
            try{
                canvas = this.surfaceHolder.lockCanvas();
                synchronized (surfaceHolder){
                    this.appPanel.update();
                    this.appPanel.draw(canvas);
                }
            }catch (Exception e){
                e.printStackTrace();
            }finally{
                if(canvas != null){
                    try{
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }

            timeMillis = (System.nanoTime() - startTime)/1000000;
            waitTime = targetTime - timeMillis;

            try{
                sleep(waitTime);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void setRunning(boolean _running){
        running = _running;
    }
}

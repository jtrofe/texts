package com.jtrofe.apps.lathe;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;

import java.util.LinkedList;
import java.util.Queue;

public class Sculpture{
    Bitmap[] woodFrames;
    int currentFrame;
    int numFrames;
    Bitmap woodTexture;
    Bitmap crossSection;
    Bitmap cutMarks;
    Point cutOffset;
    private int length;
    private int height;

    private int x;
    private int y;

    public Rect getBounds(){
        return new Rect(x, y - height, x + length, y + height);
    }

    public Sculpture(int _length, int _height, int _x, int _y, Bitmap[] _woodFrames){
        length = _length;
        height = _height;
        x = _x;
        y = _y;

        numFrames = _woodFrames.length;
        currentFrame = 0;
        woodFrames = new Bitmap[numFrames];

        for(int i=0;i<numFrames;i++){
            woodFrames[i] = processFrame(_woodFrames[i]);
        }

        crossSection = Bitmap.createBitmap(length, height, Bitmap.Config.ARGB_8888);
        crossSection.eraseColor(Color.GRAY);

        cutOffset = new Point(40, 40);
        cutMarks = Bitmap.createBitmap(length + cutOffset.x * 2, height * 2 + cutOffset.y * 2, Bitmap.Config.ARGB_8888);
    }

    private Bitmap processFrame(Bitmap _frame){
        Bitmap frame = _frame.copy(Bitmap.Config.ARGB_8888, true);
        frame.setHasAlpha(true);
        frame = Bitmap.createScaledBitmap(frame, length, height * 2, true);

        return frame;
    }

    public Point NormalizePoint(int _x, int _y){
        //Takes points on the screen (scaled down) and converts them to points on the cross section
        _x = _x - x;
        _y = _y - (y - height);

        _y = Math.abs(_y - height);

        return new Point(_x, _y);
    }

    public void Cut(Point oldPoint, Point newPoint){
        Canvas canvas = new Canvas(crossSection);
        Canvas cutMarkCanvas = new Canvas(cutMarks);
        Paint paint = new Paint();
        paint.setColor(Color.YELLOW);
        paint.setStrokeWidth(3);


        canvas.drawLine(oldPoint.x, oldPoint.y, newPoint.x, newPoint.y, paint);


        cutMarkCanvas.drawLine(oldPoint.x + cutOffset.x, oldPoint.y + height + cutOffset.y,
                                newPoint.x + cutOffset.x, newPoint.y + height + cutOffset.y, paint);
        cutMarkCanvas.drawLine(oldPoint.x + cutOffset.x, -oldPoint.y + height + cutOffset.y,
                                newPoint.x + cutOffset.x, -newPoint.y + height + cutOffset.y, paint);
    }

    public void Fill(){
        //Bitmap filling = crossSection.copy(Bitmap.Config.ARGB_8888, true);
        new FloodFill().floodFill(crossSection, new Point(4, 4), Color.GRAY, Color.BLUE);

        ReplaceColor(crossSection, Color.GRAY, Color.TRANSPARENT);
        ReplaceColor(crossSection, Color.YELLOW, Color.TRANSPARENT);
        ReplaceColor(crossSection, Color.BLUE, Color.GRAY);

        //crossSection = filling.copy(Bitmap.Config.ARGB_8888, true);
        cutMarks.eraseColor(Color.TRANSPARENT);
    }

    private void ReplaceColor(Bitmap bitmap, int target, int replacement){
        int [] allPixels = new int [bitmap.getHeight()*bitmap.getWidth()];
        bitmap.getPixels(allPixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        for(int i = 0; i < allPixels.length; i++){
            if(allPixels[i] == target){
                allPixels[i] = replacement;
            }
        }
        bitmap.setPixels(allPixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
    }

    public boolean ContainsPoint(Point point){
        if(point.x <= 0 || point.x >= length || point.y <= 0 || point.y >= height) return false;
        int pixel = crossSection.getPixel(point.x, point.y);

        return pixel == Color.GRAY;
    }

    public void draw(Canvas canvas, int _x, int _y){
        //Create a mirror of the cross section
        Matrix m = new Matrix();
        m.preScale(1, -1);
        Bitmap mirror = Bitmap.createBitmap(crossSection, 0, 0, length, height, m, false);

        //Use the cross section and its mirror to mask the wood texture
        Bitmap frameImage = woodFrames[currentFrame].copy(Bitmap.Config.ARGB_8888, true);

        Canvas c = new Canvas(frameImage);
        Paint maskPaint = new Paint();
        maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        c.drawBitmap(crossSection, 0, height, maskPaint);
        c.drawBitmap(mirror, 0, 0, maskPaint);

        //Draw the result to the canvas
        //canvas.drawBitmap(crossSection, _x, _y, null);
        //canvas.drawBitmap(mirror, _x, _y - height, null);
        canvas.drawBitmap(frameImage, x, y - height, null);
        canvas.drawBitmap(cutMarks, x - cutOffset.x, y - height - cutOffset.y, null);

        currentFrame ++;
        if(currentFrame == numFrames) currentFrame = 0;

    }

    public class FloodFill {

        public void floodFill(Bitmap image, Point node, int targetColor, int replacementColor) {

            int width = image.getWidth();
            int height = image.getHeight();
            int target = targetColor;
            int replacement = replacementColor;

            if (target != replacement) {
                Queue<Point> queue = new LinkedList<Point>();
                do {

                    int x = node.x;
                    int y = node.y;
                    while (x > 0 && image.getPixel(x - 1, y) == target) {
                        x--;
                    }

                    boolean spanUp = false;
                    boolean spanDown = false;
                    while (x < width && image.getPixel(x, y) == target) {
                        image.setPixel(x, y, replacement);
                        if (!spanUp && y > 0
                                && image.getPixel(x, y - 1) == target) {
                            queue.add(new Point(x, y - 1));
                            spanUp = true;
                        } else if (spanUp && y > 0
                                && image.getPixel(x, y - 1) != target) {
                            spanUp = false;
                        }
                        if (!spanDown && y < height - 1
                                && image.getPixel(x, y + 1) == target) {
                            queue.add(new Point(x, y + 1));
                            spanDown = true;
                        } else if (spanDown && y < (height - 1)
                                && image.getPixel(x, y + 1) != target) {
                            spanDown = false;
                        }
                        x++;
                    }

                } while ((node = queue.poll()) != null);
            }
        }
    }
}
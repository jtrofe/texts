package com.jtrofe.apps.lathe;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

public class Gouge{
    private int length;

    private Point position;
    private Point centerPoint;
    private Point cutPoint;



    public Gouge(Point _position, int _length, Point _centerPoint){
        length = _length;
        centerPoint = _centerPoint;


        SetPosition(_position);

    }

    public Point GetCutPoint(){return new Point(cutPoint.x, cutPoint.y);}

    public void SetPosition(Point _position){
        position = _position;

        double dx = position.x - centerPoint.x;
        double dy = position.y - centerPoint.y;
        double rotation = Math.atan2(dx, -dy);

        int nx = (int) (position.x + Math.sin(rotation) * -length);
        int ny = (int) (position.y + Math.cos(rotation) * length);

        cutPoint = new Point(nx, ny);
    }

    public void draw(Canvas canvas){
        Paint paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(3);

        canvas.drawLine(position.x, position.y, cutPoint.x, cutPoint.y, paint);


        float dx = (cutPoint.x - position.x) * 0.6f;
        float dy = (cutPoint.y - position.y) * 0.6f;
        float hx = position.x + dx;
        float hy = position.y + dy;
        paint.setStrokeWidth(6);

        canvas.drawLine(position.x, position.y, hx, hy, paint);
    }
}
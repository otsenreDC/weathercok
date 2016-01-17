package io.bananalabs.common.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import io.bananalabs.common.R;

/**
 * Created by EDC on 1/24/15.
 */
public class CompassView extends View {

    private Paint paint = new Paint();
    public int radius;
    public int innerRadius;
    private Float mHeading;

    public CompassView(Context context) {
        super(context);

        preConfigureBrush();
    }

    public CompassView(Context context, AttributeSet attrs) {
        super(context, attrs);

        extractCustomAttributes(context, attrs);
        preConfigureBrush();
    }

    public CompassView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        extractCustomAttributes(context, attrs);
        preConfigureBrush();
    }

    private void extractCustomAttributes(Context context, AttributeSet attrs) {
        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.CompassView);

        int radius_attr = (int) arr.getDimension(R.styleable.CompassView_radius, 0);

        this.setRadius(radius_attr);

        int inner_radius_attr = (int) arr.getDimension(R.styleable.CompassView_innerRadius, 0);
        setInnerRadius(inner_radius_attr);

        arr.recycle();  // Do this when done.
    }

    private void preConfigureBrush() {
        paint.setColor(0xff000000);
        paint.setStrokeWidth(2);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setAntiAlias(true);
        paint.setTextSize(30);

    }

    protected void onDraw(Canvas canvas) {
        int textPading;
        int centerx = getWidth() / 2;
        int centery = getHeight() / 2;

        int radius = getRadius();
        int innerRadius = getInnerRadius();

        textPading = (radius - innerRadius ) / 2;

        if (this.getHeading() != null)
            canvas.rotate(this.getHeading(), centerx, centery);

        // Big circle
        paint.setColor(0xff4dc9ff);
        canvas.drawCircle(centerx, centery, radius, paint);

        // Inner circle
        paint.setColor(0xff80d9ff);
        canvas.drawCircle(centerx, centery, innerRadius, paint);

        paint.setColor(0xffffffff);
        // North
        int innercx = centerx - 8;
        int innercy = centery - (radius - textPading - 2);
        canvas.drawText("N", innercx - 2, innercy, paint);

        // South
        innercx = centerx - 8;
        innercy = centery + (radius - (textPading - 30));
        canvas.drawText("S", innercx - 2, innercy, paint);

        // East
        innercy = centery + 10;
        innercx = centerx + (radius - textPading + 2);
        canvas.drawText("E", innercx - 2, innercy, paint);

        // West
        innercy = centery + 12;
        innercx = centerx - (radius - (textPading - 25));
        canvas.drawText("W", innercx - 2, innercy, paint);

//        Path path = new Path();
//        path.addCircle(centerx, centery, radius, Path.Direction.CW);
//        canvas.drawTextOnPath("n\te\ts\tw", path, 0, 20, paint);

    }

    public void setHeading(Float heading) {
        this.mHeading = heading;
        this.invalidate();
    }

    public Float getHeading() {
        return this.mHeading;
    }

    public int getRadius() {
        if (this.radius == 0) {
            if (getHeight() > getWidth()) {
                this.radius = getWidth() / 2;
            } else {
                this.radius = getHeight() / 2;
            }
        }
        return this.radius;
    }

    public int getInnerRadius() {
        if (this.innerRadius == 0) {
            final float scale = getContext().getResources().getDisplayMetrics().density;
            int pixels =  pixelsFromDB(50);
            if (getHeight() > getWidth()) {
                this.innerRadius = getWidth() / 2 - pixels;
            } else {
                this.innerRadius = getHeight() / 2 - pixels;
            }
        }
        return this.innerRadius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public void setInnerRadius(int innerRadius) {
        this.innerRadius = innerRadius;
    }

    private int pixelsFromDB(int pixels) {
        float scale = getContext().getResources().getDisplayMetrics().density;

        return (int)(pixels * scale + 0.5f);
    }

}

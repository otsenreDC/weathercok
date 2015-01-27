package io.bananalabs.weathercok;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by EDC on 1/24/15.
 */
public class CompassView extends View {

    private Paint paint = new Paint();
    private Float mHeading;

    public CompassView(Context context) {
        super(context);

        preConfigureBrush();
    }

    public CompassView(Context context, AttributeSet attrs) {
        super(context, attrs);

        preConfigureBrush();
    }

    public CompassView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        preConfigureBrush();
    }

    private void preConfigureBrush() {
        paint.setColor(0xff000000);
        paint.setStrokeWidth(2);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setAntiAlias(true);
        paint.setTextSize(30);

    }

    protected void onDraw(Canvas canvas) {
        int height;
        int radius;
        int innerRadius;
        int textPading;
        int centerx = getWidth() / 2;
        int centery = getHeight() / 2;

        if (getWidth() > getHeight())
            radius = getHeight() / 2 - getPaddingTop();
        else
            radius = getWidth() / 2 - getPaddingTop();

        innerRadius = 3 * radius / 4;
        textPading = radius - innerRadius;

        if (this.getHeading() != null)
            canvas.rotate(this.getHeading(), centerx, centery);

        // Big circle
        paint.setColor(0xff80d9ff);
        canvas.drawCircle(centerx, centery, radius, paint);

        // Inner circle
        paint.setColor(0xff4dc9ff);
        canvas.drawCircle(centerx, centery, innerRadius, paint);

        paint.setColor(0xffffffff);
        // North
        int innercx = centerx - 8;
        int innercy = centery - (radius - textPading - 2);
        canvas.drawText("N", innercx - 2, innercy, paint);

        // South
        innercx = centerx - 8;
        innercy = centery + (radius -  ( textPading - 30) );
        canvas.drawText("S", innercx - 2, innercy, paint);

        // East
        innercy = centery + 10;
        innercx = centerx + (radius - textPading + 2);
        canvas.drawText("E", innercx - 2, innercy, paint);

        // West
        innercy = centery + 12;
        innercx = centerx - (radius -  ( textPading - 25 ));
        canvas.drawText("W", innercx - 2, innercy, paint);

    }

    public void setHeading(Float heading) {
        this.mHeading = heading;
        this.invalidate();
    }

    public Float getHeading() {
        return this.mHeading;
    }
}

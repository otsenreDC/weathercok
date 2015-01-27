package io.bananalabs.weathercok;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by EDC on 1/25/15.
 */
public class ArrowView extends View {

    Paint paint = new Paint();

    public ArrowView(Context context) {
        super(context);

        preConfigureBrush();
    }

    public ArrowView(Context context, AttributeSet attrs) {
        super(context, attrs);

        preConfigureBrush();
    }

    public ArrowView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        preConfigureBrush();
    }

    private void preConfigureBrush() {
        paint.setColor(0xffffffff);
        paint.setStrokeWidth(10);
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        int length = getHeight();

        int centerx = getWidth() / 2;
        int centery = getHeight() / 2;

        // Body
        canvas.drawLine(centerx,  1 + getPaddingTop() , centerx, length - 45 - getPaddingBottom(), paint) ;

        // Head
        canvas.drawLine(centerx - 20, 20  + getPaddingTop(), centerx + 3,  0 + getPaddingTop(), paint);
        canvas.drawLine(centerx - 3, 0 + getPaddingTop(), centerx + 20,  20 + getPaddingTop(), paint);

        // Tail bottom
        canvas.drawLine(centerx - 20, length - getPaddingBottom(), centerx + 3,  length - 20- getPaddingBottom(), paint);
        canvas.drawLine(centerx - 3, length - 20  - getPaddingBottom(), centerx + 20,  length - getPaddingBottom(), paint);
        // Tail top
        canvas.drawLine(centerx - 22, length - 25  - getPaddingBottom(), centerx,  length - 45  - getPaddingBottom(), paint);
        canvas.drawLine(centerx, length - 45  - getPaddingBottom(), centerx + 22,  length - 25  - getPaddingBottom(), paint);
        // Tail sides
        canvas.drawLine(centerx - 20, length - 28  - getPaddingBottom(), centerx - 20,  length  - getPaddingBottom() + 4, paint);
        canvas.drawLine(centerx + 20, length - 28  - getPaddingBottom(), centerx + 20,  length  - getPaddingBottom() + 4, paint);

    }
}

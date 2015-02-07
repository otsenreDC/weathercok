package io.bananalabs.weathercok.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import io.bananalabs.weathercok.R;

/**
 * Created by EDC on 1/25/15.
 */
public class ArrowView extends View {

    private Paint paint = new Paint();
    private float mThickness;

    public ArrowView(Context context) {
        super(context);

        preConfigureBrush();
    }

    public ArrowView(Context context, AttributeSet attrs) {
        super(context, attrs);

        extractCustomAttributes(context, attrs);
        preConfigureBrush();
    }

    public ArrowView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        extractCustomAttributes(context, attrs);
        preConfigureBrush();
    }

    private void extractCustomAttributes(Context context, AttributeSet attrs) {
        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.ArrowView);

        this.mThickness = arr.getDimension(R.styleable.ArrowView_thick, 1);

        arr.recycle();  // Do this when done.
    }

    private void preConfigureBrush() {
        paint.setColor(0xffffffff);
        paint.setStrokeWidth(pixelsFromDB(100));
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
    }


    @Override
    protected void onDraw(Canvas canvas) {

        this.paint.setStrokeWidth(getThick());
        int length = getLength();

        int centerx = getWidth() / 2;
        int centery = getHeight() / 2;

        int topX = centerx;
        int topY = centery - length / 2 + getPaddingTop();
        int bottomX = centerx;
        int bottomY = centery + length / 2 - getPaddingBottom();

        // Body
        canvas.drawLine(topX, topY, bottomX, bottomY, paint);

        // Head
        canvas.drawLine(centerx - pixelsFromDB(5),
                pixelsFromDB(5) + topY,
                centerx,
                topY,
                paint);
        canvas.drawLine(centerx,
                topY,
                centerx + pixelsFromDB(5),
                pixelsFromDB(5) + topY,
                paint);

        // Tail bottom
        canvas.drawLine(bottomX - pixelsFromDB(5),
                bottomY,
                bottomX,
                bottomY - pixelsFromDB(5),
                paint);
        canvas.drawLine(bottomX,
                bottomY - pixelsFromDB(5),
                bottomX + pixelsFromDB(5),
                bottomY,
                paint);
        // Tail top
        canvas.drawLine(bottomX - pixelsFromDB(5),
                bottomY,
                bottomX,
                bottomY - pixelsFromDB(20),
                paint);
        canvas.drawLine(bottomX,
                bottomY - pixelsFromDB(20),
                bottomX + pixelsFromDB(5),
                bottomY,
                paint);


    }

    public int getLength() {
        if (getHeight() > getWidth()) {
            return getWidth();
        } else {
            return getHeight();
        }
    }

    public float getThick() {
        if (this.mThickness <= 0) {
            return 1;
        } else {
            return this.mThickness;
        }
    }

    private int pixelsFromDB(int pixels) {
        float scale = getContext().getResources().getDisplayMetrics().density;

        return (int)(pixels * scale + 0.5f);
    }
}


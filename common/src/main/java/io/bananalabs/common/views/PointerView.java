package io.bananalabs.common.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import io.bananalabs.common.R;

/**
 * Created by EDC on 1/31/15.
 */
public class PointerView extends View {

    private Paint mPaint = new Paint();
    Path path = new Path();

    private float mLength;
    private float mAperture;
    private int mIntensity;
    private int mMaxIntensity;
    private int mMinIntensity;
    private float mStrokeWidth;

    public PointerView(Context context) {
        super(context);
        init();
    }

    public PointerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        customAttributes(context, attrs);
        init();
    }

    public PointerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        customAttributes(context, attrs);
        init();
    }

    private void init() {
        this.mPaint.setStrokeWidth(getStrokeWidth());
        this.mPaint.setColor(getMinIntensity());
        this.mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        this.mPaint.setAntiAlias(true);
    }

    private void customAttributes(Context context, AttributeSet attrs) {
        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.PointerView);

        this.setLength(arr.getDimension(R.styleable.PointerView_length, 0));
        this.setAperture(arr.getDimension(R.styleable.PointerView_aperture, 0));
        this.setStrokeWidth(arr.getDimension(R.styleable.PointerView_lineThick, 0));
        this.setMinIntensity(arr.getColor(R.styleable.PointerView_minIntensityColor, 0));
        this.setStrokeWidth(arr.getDimension(R.styleable.PointerView_lineThick, 0));
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);

        float centerX = getWidth() / 2;
        float centerY = getHeight() / 2;
        float halfLength = (getLength() - getPaddingTop()) / 2;

        path.moveTo(centerX, centerY - halfLength);
        path.lineTo(centerX - getAperture(), centerY + halfLength);
        path.lineTo(centerX, centerY + halfLength - getDimension(20));
        path.lineTo(centerX + getAperture(), centerY + halfLength);
        path.lineTo(centerX, centerY - halfLength);

        canvas.drawPath(path, getPaint());

    }

    private float getDimension(float d) {
        float scale = getContext().getResources().getDisplayMetrics().density;

        return (int)(d * scale + 0.5f);
    }

    // Accessors
    public Paint getPaint() {
        return mPaint;
    }

    public void setPaint(Paint mPaint) {
        this.mPaint = mPaint;
    }

    public float getLength() {

        if (mLength == 0)
            if (getHeight() > getWidth())
                return getWidth();
            else
                return getHeight();
        else
            return mLength;
    }

    public void setLength(float mLength) {
        this.mLength = mLength;
    }

    public float getAperture() {
        return mAperture;
    }

    public void setAperture(float mAperture) {
        this.mAperture = mAperture;
    }

    public int getIntensity() {
        return mIntensity;
    }

    public void setIntensity(int mIntensity) {
        this.mIntensity = mIntensity;
    }

    public int getMaxIntensity() {
        return mMaxIntensity;
    }

    public void setMaxIntensity(int mMaxIntensity) {
        this.mMaxIntensity = mMaxIntensity;
    }

    public int getMinIntensity() {
        return mMinIntensity;
    }

    public void setMinIntensity(int mMinIntensity) {
        this.mMinIntensity = mMinIntensity;
    }

    public float getStrokeWidth() {
        return mStrokeWidth;
    }

    public void setStrokeWidth(float mStrokeWidth) {
        this.mStrokeWidth = mStrokeWidth;
    }
}

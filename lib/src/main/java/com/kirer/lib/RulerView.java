package com.kirer.lib;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

/**
 * Created by xinwb on 2018/10/18.
 */

public class RulerView extends View {

    private static final String TAG = "RulerView3";
    private static final String SYMBOL = "%";
    private int mLineColor = Color.parseColor("#3C6FFF");
    private int mTextColor = Color.parseColor("#3C6FFF");
    private Paint mLinePaint;
    private TextPaint mTextPaint;
    private TextPaint mSymbolTextPaint;
    private float mTextAndLineSpacing = 20;
    private float mLineWidth = 5;
    private float mLineHeight = 50;
    private float mSelectedTextSize = 100;
    private Rect mSelectedTextRect;
    private Rect mSymbolRect;
    private Rect mTextRect;
    private LinearGradient mLeftGradient;
    private LinearGradient mRightGradient;
    private float mTextSize = 50;
    private float mDistance = 200;
    private float mTextBaseLineY;
    private float mLineStartY;
    private float mLineEndY;
    private int mMinValue = 5;
    private int mMaxValue = 45;
    private int mSelectedValue = 12;
    private float mOffset;

    private GestureDetector mGestureDetectorCompat;
    private boolean mFling;

    public RulerView(Context context) {
        super(context);
        init();
    }

    public RulerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RulerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setStrokeWidth(mLineWidth);
        mLinePaint.setColor(mLineColor);

        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextSize(mTextSize);

        mSymbolTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mSymbolTextPaint.setTextAlign(Paint.Align.CENTER);
        mSymbolTextPaint.setColor(mTextColor);

        mSelectedTextRect = new Rect();
        mTextPaint.setTextSize(mSelectedTextSize);
        mTextPaint.getTextBounds(String.valueOf(mSelectedValue), 0, String.valueOf(mSelectedValue).length(), mSelectedTextRect);
        mSymbolRect = new Rect();
        mSymbolTextPaint.getTextBounds(SYMBOL, 0, 1, mSymbolRect);
        mTextRect = new Rect();
        mGestureDetectorCompat = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (mSelectedValue == mMinValue && distanceX < 0) {
                    return false;
                }
                if (mSelectedValue == mMaxValue && distanceX > 0) {
                    return false;
                }
                setOffset(mOffset - distanceX);
                return true;
            }


            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float velocity = velocityX / 15;
                if (Math.abs(velocity) < mDistance) {
                    return false;
                }
                if (velocity > 0) {
                    velocity = velocity > (mSelectedValue - mMinValue) * mDistance ? (mSelectedValue - mMinValue) * mDistance : velocity;
                } else if (velocity < 0) {
                    velocity = Math.abs(velocity) > (mMaxValue - mSelectedValue) * mDistance ? -(mMaxValue - mSelectedValue) * mDistance : velocity;
                }
                mFling = true;
                fling(velocity);
                return true;
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
        mLeftGradient = new LinearGradient(getWidth() / 2, getHeight() / 2, 0, 0, new int[]{mTextColor, Color.TRANSPARENT}, null, Shader.TileMode.CLAMP);
        mRightGradient = new LinearGradient(getWidth() / 2, getHeight() / 2, getWidth(), getHeight(), new int[]{mTextColor, Color.TRANSPARENT}, null, Shader.TileMode.CLAMP);
        mTextBaseLineY = getPaddingTop() + mSelectedTextRect.height();
        mLineStartY = mTextBaseLineY + mTextAndLineSpacing;
        mLineEndY = mLineStartY + mLineHeight;
    }

    private int measureWidth(int widthMeasureSpec) {
        int measureMode = MeasureSpec.getMode(widthMeasureSpec);
        int measureSize = MeasureSpec.getSize(widthMeasureSpec);
        //View的最小值与背景最小值两者中的最大值（宽度）
        int result = getSuggestedMinimumWidth();
        switch (measureMode) {
            case MeasureSpec.AT_MOST:
            case MeasureSpec.EXACTLY:
                result = measureSize;
                break;
            default:
                break;
        }
        return result;
    }

    private int measureHeight(int heightMeasure) {
        int measureMode = MeasureSpec.getMode(heightMeasure);
        int measureSize = MeasureSpec.getSize(heightMeasure);
        int result;
        result = (int) (mSelectedTextRect.height() + mLineHeight + mLineWidth * 2 + mTextAndLineSpacing + getPaddingTop() + getPaddingBottom());
        switch (measureMode) {
            //设置了确切的高度
            case MeasureSpec.EXACTLY:
                result = Math.max(result, measureSize);
                break;
            //没有设置了确切的高度
            case MeasureSpec.AT_MOST:
                result = Math.min(result, measureSize);
                break;
            default:
                break;
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawCenter(canvas);
        drawLeft(canvas);
        drawRight(canvas);
    }

    private void drawCenter(Canvas canvas) {
        mTextPaint.setShader(null);
        mSymbolTextPaint.setShader(null);
        mLinePaint.setShader(null);
        mLinePaint.setStrokeWidth(mLineWidth + mLineWidth * (1 - Math.abs(mOffset) / mDistance));
        float textSize = mTextSize + (mSelectedTextSize - mTextSize) * (1 - Math.abs(mOffset) / mDistance);
        mTextPaint.setTextSize(textSize);
        mSymbolTextPaint.setTextSize(textSize * 0.8f);
        String text = String.valueOf(mSelectedValue);
        mTextPaint.getTextBounds(text, 0, text.length(), mTextRect);
        mSymbolTextPaint.getTextBounds(SYMBOL, 0, SYMBOL.length(), mSymbolRect);
        float centerX = getWidth() / 2;
        canvas.drawText(text, centerX + mOffset - mTextRect.width() / 2 - mLineWidth / 2, mTextBaseLineY, mTextPaint);
        canvas.drawText(SYMBOL, centerX + mOffset + mSymbolRect.width() / 2 + mLineWidth / 2, mTextBaseLineY, mSymbolTextPaint);
        canvas.drawLine(centerX + mOffset, mLineStartY, centerX + mOffset, mLineEndY, mLinePaint);
    }

    private void drawLeft(Canvas canvas) {
        mTextPaint.setShader(mLeftGradient);
        mSymbolTextPaint.setShader(mLeftGradient);
        mLinePaint.setShader(mLeftGradient);
        for (int i = mSelectedValue - 1; i >= mMinValue; i--) {
            if (i == mSelectedValue - 1 && mOffset > 0) {
                float textSize = mSelectedTextSize - (mSelectedTextSize - mTextSize) * (1 - Math.abs(mOffset) / mDistance);
                mTextPaint.setTextSize(textSize);
                mSymbolTextPaint.setTextSize(textSize * 0.8f);
                mLinePaint.setStrokeWidth(2 * mLineWidth - mLineWidth * (1 - Math.abs(mOffset) / mDistance));
            } else {
                mTextPaint.setTextSize(mTextSize);
                mSymbolTextPaint.setTextSize(mTextSize * 0.8f);
                mLinePaint.setStrokeWidth(mLineWidth);
            }
            String text = String.valueOf(i);
            mTextPaint.getTextBounds(text, 0, text.length(), mTextRect);
            mSymbolTextPaint.getTextBounds(SYMBOL, 0, SYMBOL.length(), mSymbolRect);
            float centerX = getWidth() / 2 - mDistance * (mSelectedValue - i);
            canvas.drawText(text, centerX + mOffset - mTextRect.width() / 2 - mLineWidth / 2, mTextBaseLineY, mTextPaint);
            canvas.drawText(SYMBOL, centerX + mOffset + mSymbolRect.width() / 2 + mLineWidth / 2, mTextBaseLineY, mSymbolTextPaint);
            canvas.drawLine(centerX + mOffset, mLineStartY, centerX + mOffset, mLineEndY, mLinePaint);
        }
        mLinePaint.setStrokeWidth(2 * mLineWidth);
        canvas.drawLine(getWidth() / 2, mLineEndY + mLineWidth, 0, mLineEndY + mLineWidth, mLinePaint);
    }

    private void drawRight(Canvas canvas) {
        mTextPaint.setShader(mRightGradient);
        mSymbolTextPaint.setShader(mRightGradient);
        mLinePaint.setShader(mRightGradient);
        for (int i = mSelectedValue + 1; i <= mMaxValue; i++) {
            if (i == mSelectedValue + 1 && mOffset < 0) {
                float textSize = mSelectedTextSize - (mSelectedTextSize - mTextSize) * (1 - Math.abs(mOffset) / mDistance);
                mTextPaint.setTextSize(textSize);
                mSymbolTextPaint.setTextSize(textSize * 0.8f);
                mLinePaint.setStrokeWidth(2 * mLineWidth - mLineWidth * (1 - Math.abs(mOffset) / mDistance));
            } else {
                mTextPaint.setTextSize(mTextSize);
                mSymbolTextPaint.setTextSize(mTextSize * 0.8f);
                mLinePaint.setStrokeWidth(mLineWidth);
            }
            String text = String.valueOf(i);
            mTextPaint.getTextBounds(text, 0, text.length(), mTextRect);
            mSymbolTextPaint.getTextBounds(SYMBOL, 0, SYMBOL.length(), mSymbolRect);
            float centerX = getWidth() / 2 - mDistance * (mSelectedValue - i);
            canvas.drawText(text, centerX + mOffset - mTextRect.width() / 2 - mLineWidth / 2, mTextBaseLineY, mTextPaint);
            canvas.drawText(SYMBOL, centerX + mOffset + mSymbolRect.width() / 2 + mLineWidth / 2, mTextBaseLineY, mSymbolTextPaint);
            canvas.drawLine(centerX + mOffset, mLineStartY, centerX + mOffset, mLineEndY, mLinePaint);
        }
        mLinePaint.setStrokeWidth(2 * mLineWidth);
        canvas.drawLine(getWidth() / 2, mLineEndY + mLineWidth, getWidth(), mLineEndY + mLineWidth, mLinePaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean resolve = mGestureDetectorCompat.onTouchEvent(event);
        if (!mFling && MotionEvent.ACTION_UP == event.getAction()) {
            adjustPosition();
            resolve = true;
        }
        return resolve || super.onTouchEvent(event);
    }

    private void setOffset(float offset) {
        if (Math.abs(offset) < mDistance) {
            this.mOffset = offset;
        } else {
            int index = (int) (Math.abs(offset) / mDistance);
            if (offset < 0) {
                mSelectedValue = mSelectedValue + index > mMaxValue ? mMaxValue : mSelectedValue + index;
                this.mOffset = offset + index * mDistance;
            } else if (offset > 0) {
                mSelectedValue = mSelectedValue - index < mMinValue ? mMinValue : mSelectedValue - index;
                this.mOffset = offset - index * mDistance;
            } else {
                this.mOffset = 0;
            }
        }
        postInvalidate();
    }

    private void adjustPosition() {
        ObjectAnimator animator;
        if (Math.abs(mOffset) > mDistance / 2) {
            animator = ObjectAnimator.ofFloat(this, "offset", mOffset, mOffset > 0 ? mDistance : -mDistance);
        } else {
            animator = ObjectAnimator.ofFloat(this, "offset", mOffset, 0);
        }
        animator.setDuration(100);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (null == mListener) {
                    return;
                }
                mListener.onSelected(mSelectedValue);
            }
        });
        animator.start();
    }

    private void fling(float velocityX) {
        ObjectAnimator flingAnimator = ObjectAnimator.ofFloat(this, "offset", mOffset, velocityX);
        flingAnimator.setDuration((long) Math.abs(velocityX));
        flingAnimator.setInterpolator(new DecelerateInterpolator());
        flingAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mFling = false;
                adjustPosition();
            }
        });
        flingAnimator.start();
    }

    private OnRulerListener mListener;

    public void setOnRulerListener(OnRulerListener onRulerListener) {
        this.mListener = onRulerListener;
    }

    public interface OnRulerListener {
        void onSelected(float value);
    }
}

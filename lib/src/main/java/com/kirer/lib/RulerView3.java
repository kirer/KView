package com.recycler.coverflow;

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
import android.support.v4.view.GestureDetectorCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

/**
 * Created by xinwb on 2018/10/18.
 */

public class RulerView3 extends View {

    private static final String TAG = "RulerView3";
    private int mLineColor = Color.parseColor("#3C6FFF");
    private int mTextColor = Color.parseColor("#3C6FFF");
    private Paint mLinePaint;
    private TextPaint mTextPaint;
    private float mTextAndLineSpacing = 20;
    private float mLineWidth = 5;
    private float mLineHeight = 50;
    private float mSelectedTextSize = 100;
    private Rect mSelectedTextRect;
    private Rect mTextRect;
    private LinearGradient mLeftGradient;
    private LinearGradient mRightGradient;
    private float mTextSize = 50;
    private float mDistance = 150;
    private float mTextBaseLineY;
    private float mLineStartY;
    private float mLineEndY;
    private int mMinValue = 5;
    private int mMaxValue = 45;
    private int mSelectedValue = 12;
    private float mOffset;

    private GestureDetector mGestureDetectorCompat;
    private boolean mFling;

    public RulerView3(Context context) {
        super(context);
        init();
    }

    public RulerView3(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RulerView3(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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

        mSelectedTextRect = new Rect();
        mTextPaint.setTextSize(mSelectedTextSize);
        mTextPaint.getTextBounds(String.valueOf(mSelectedValue), 0, String.valueOf(mSelectedValue).length(), mSelectedTextRect);
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

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
        mLeftGradient = new LinearGradient(getWidth() / 2, getHeight() / 2, 0, 0, new int[]{mTextColor, Color.TRANSPARENT}, null, Shader.TileMode.CLAMP);
        mRightGradient = new LinearGradient(getWidth() / 2, getHeight() / 2, getWidth(), getHeight(), new int[]{mTextColor, Color.TRANSPARENT}, null, Shader.TileMode.CLAMP);
        mTextBaseLineY = getPaddingTop() + mSelectedTextRect.height();
        mLineStartY = mTextBaseLineY + mTextAndLineSpacing;
        mLineEndY = mLineStartY + mLineHeight;
    }

    //测量宽度：处理MeasureSpec.UNSPECIFIED的情况
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

    //测量高度
    private int measureHeight(int heightMeasure) {
        int measureMode = MeasureSpec.getMode(heightMeasure);
        int measureSize = MeasureSpec.getSize(heightMeasure);
        int result;
        result = (int) (mSelectedTextRect.height() + mLineHeight + mLineWidth + getPaddingTop() + getPaddingBottom());
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
        mLinePaint.setShader(null);
        mTextPaint.setTextSize(mTextSize + (mSelectedTextSize - mTextSize) * (1 - Math.abs(mOffset) / mDistance));
        float centerX = getWidth() / 2;
        canvas.drawText(String.valueOf(mSelectedValue), centerX + mOffset, mTextBaseLineY, mTextPaint);
        canvas.drawLine(centerX + mOffset, mLineStartY, centerX + mOffset, mLineEndY, mLinePaint);
    }

    private void drawLeft(Canvas canvas) {
        mTextPaint.setShader(mLeftGradient);
        mLinePaint.setShader(mLeftGradient);
        for (int i = mSelectedValue - 1; i >= mMinValue; i--) {
            if (i == mSelectedValue - 1 && mOffset > 0) {
                mTextPaint.setTextSize(mSelectedTextSize - (mSelectedTextSize - mTextSize) * (1 - Math.abs(mOffset) / mDistance));
            } else {
                mTextPaint.setTextSize(mTextSize);
            }
            String text = String.valueOf(i);
            mTextPaint.getTextBounds(text, 0, text.length(), mTextRect);
            float centerX = getWidth() / 2 - mDistance * (mSelectedValue - i);
            canvas.drawText(text, centerX + mOffset, mTextBaseLineY, mTextPaint);
            canvas.drawLine(centerX + mOffset, mLineStartY, centerX + mOffset, mLineEndY, mLinePaint);
        }
        canvas.drawLine(getWidth() / 2, mLineEndY, 0, mLineEndY, mLinePaint);
    }

    private void drawRight(Canvas canvas) {
        mTextPaint.setShader(mRightGradient);
        mLinePaint.setShader(mRightGradient);
        for (int i = mSelectedValue + 1; i <= mMaxValue; i++) {
            if (i == mSelectedValue + 1 && mOffset < 0) {
                mTextPaint.setTextSize(mSelectedTextSize - (mSelectedTextSize - mTextSize) * (1 - Math.abs(mOffset) / mDistance));
            } else {
                mTextPaint.setTextSize(mTextSize);
            }
            String text = String.valueOf(i);
            mTextPaint.getTextBounds(text, 0, text.length(), mTextRect);
            float centerX = getWidth() / 2 - mDistance * (mSelectedValue - i);
            canvas.drawText(text, centerX + mOffset, mTextBaseLineY, mTextPaint);
            canvas.drawLine(centerX + mOffset, mLineStartY, centerX + mOffset, mLineEndY, mLinePaint);
        }
        canvas.drawLine(getWidth() / 2, mLineEndY, getWidth(), mLineEndY, mLinePaint);
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
                if(null == mListener){
                    return;
                }
                mListener.onSelected(mSelectedValue);
            }
        });
        animator.start();
    }

    private OnRulerListener mListener;

    public void setOnRulerListener(OnRulerListener onRulerListener) {
        this.mListener = onRulerListener;
    }

    public interface OnRulerListener {
        void onSelected(float value);
    }
}

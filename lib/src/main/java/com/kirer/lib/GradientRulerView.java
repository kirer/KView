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
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
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

public class GradientRulerView extends View {

    private static final String TAG = "GradientRulerView";
    private static final String SYMBOL = "%";
    private static final float DEFAULT_FLING_DAMPING = 10f;
    private static final float DEFAULT_SYMBOL_TEXT_SIZE_RATIO = 0.8f;
    private Paint mLinePaint;
    private Paint mBottomLinePaint;
    private TextPaint mTextPaint;
    private TextPaint mSymbolTextPaint;

    private int mLineColor = Color.parseColor("#3C6FFF");
    private float mLineWidth = 5;
    private float mLineHeight = 50;
    private float mLineStartY;
    private float mLineEndY;
    private float mBottomLineWidth = 10;
    private float mBottomLineY;

    private int mTextColor = Color.parseColor("#3C6FFF");
    private float mSelectedTextSize = 100;
    private float mTextSize = 50;
    private float mTextBaseLineY;
    private float mSymbolTextSizeRatio = DEFAULT_SYMBOL_TEXT_SIZE_RATIO;

    private int mCenterX;
    private float mTextAndLineSpacing = 20;
    private float mSpacing = 200;

    private Rect mSelectedTextRect;
    private Rect mSymbolRect;
    private Rect mTextRect;
    private LinearGradient mLeftGradient;
    private LinearGradient mRightGradient;

    private int mMinValue;
    private int mMaxValue;
    private int mSelectedValue;

    private float mOffset;
    private float mChanging;
    private int mScaler = 0;
    private boolean mFling;
    private float mFlingDamping = DEFAULT_FLING_DAMPING;
    private boolean isCancelFling = false;

    private ObjectAnimator mAdjustPositionAnimator;
    private ObjectAnimator mFlingAnimator;

    private GestureDetector mGestureDetectorCompat;

    public GradientRulerView(Context context) {
        super(context);
        init();
    }

    public GradientRulerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GradientRulerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setStrokeWidth(mLineWidth);
        mLinePaint.setColor(mLineColor);

        mBottomLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBottomLinePaint.setStrokeWidth(mBottomLineWidth);
        mBottomLinePaint.setColor(mLineColor);

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
                mChanging -= distanceX;
                setOffset(mChanging);
                return true;
            }


            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (Math.abs(velocityX / mFlingDamping) < mSpacing) {
                    return false;
                }
                mChanging = velocityX / mFlingDamping;
                if (mChanging > 0) {
                    mChanging = mChanging > (mSelectedValue - mMinValue) * mSpacing ? (mSelectedValue - mMinValue) * mSpacing : mChanging;
                } else if (mChanging < 0) {
                    mChanging = Math.abs(mChanging) > (mMaxValue - mSelectedValue) * mSpacing ? -(mMaxValue - mSelectedValue) * mSpacing : mChanging;
                }
                fling(mChanging);
                return true;
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
        int height = (int) (mSelectedTextRect.height() + mLineHeight + mBottomLineWidth + mTextAndLineSpacing + getPaddingTop() + getPaddingBottom());
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mCenterX = getWidth() / 2;
        mLeftGradient = new LinearGradient(mCenterX, getHeight() / 2, 0, getHeight() / 2, new int[]{mTextColor, Color.TRANSPARENT}, null, Shader.TileMode.CLAMP);
        mRightGradient = new LinearGradient(mCenterX, getHeight() / 2, getWidth(), getHeight() / 2, new int[]{mTextColor, Color.TRANSPARENT}, null, Shader.TileMode.CLAMP);
        mTextBaseLineY = getPaddingTop() + mSelectedTextRect.height();
        mLineStartY = mTextBaseLineY + mTextAndLineSpacing;
        mLineEndY = mLineStartY + mLineHeight;
        mBottomLineY = mLineEndY + mBottomLineWidth / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (int i = mMinValue; i <= mMaxValue; i++) {
            float x = mCenterX + mSpacing * (i - mSelectedValue) + mOffset;

            if (x <= mCenterX - mSpacing) {
                mTextPaint.setShader(mLeftGradient);
                mSymbolTextPaint.setShader(mLeftGradient);
                mLinePaint.setShader(mLeftGradient);
            } else if (x >= mCenterX + mSpacing) {
                mTextPaint.setShader(mRightGradient);
                mSymbolTextPaint.setShader(mRightGradient);
                mLinePaint.setShader(mRightGradient);
            } else {
                mTextPaint.setShader(null);
                mSymbolTextPaint.setShader(null);
                mLinePaint.setShader(null);
            }

            float progress = 1 - Math.abs(mOffset) / mSpacing;
            if (i == mSelectedValue) {
                mTextPaint.setTextSize(mTextSize + (mSelectedTextSize - mTextSize) * progress);
                mLinePaint.setStrokeWidth(mLineWidth + mLineWidth * progress);
            } else if (i == mSelectedValue - 1 && mOffset > 0 || i == mSelectedValue + 1 && mOffset < 0) {
                mTextPaint.setTextSize(mSelectedTextSize - (mSelectedTextSize - mTextSize) * progress);
                mLinePaint.setStrokeWidth(2 * mLineWidth - mLineWidth * progress);
            } else {
                mTextPaint.setTextSize(mTextSize);
                mLinePaint.setStrokeWidth(mLineWidth);
            }

            String text = String.valueOf(i);
            mTextPaint.getTextBounds(text, 0, text.length(), mTextRect);
            canvas.drawText(text, x - mTextRect.width() / 2 - mLinePaint.getStrokeWidth() / 2, mTextBaseLineY, mTextPaint);

            mSymbolTextPaint.setTextSize(mTextPaint.getTextSize() * mSymbolTextSizeRatio);
            mSymbolTextPaint.getTextBounds(SYMBOL, 0, SYMBOL.length(), mSymbolRect);
            canvas.drawText(SYMBOL, x + mSymbolRect.width() / 2 + mLinePaint.getStrokeWidth() / 2, mTextBaseLineY, mSymbolTextPaint);

            canvas.drawLine(x, mLineStartY, x, mLineEndY, mLinePaint);
        }

        mBottomLinePaint.setShader(mLeftGradient);
        canvas.drawLine(mCenterX, mBottomLineY, 0, mBottomLineY, mBottomLinePaint);
        mBottomLinePaint.setShader(mRightGradient);
        canvas.drawLine(mCenterX, mBottomLineY, getWidth(), mBottomLineY, mBottomLinePaint);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mFling) {
            mFlingAnimator.cancel();
        }
        boolean resolve = mGestureDetectorCompat.onTouchEvent(event);
        if (!mFling && MotionEvent.ACTION_UP == event.getAction()) {
            adjustPosition();
            resolve = true;
        }
        return resolve || super.onTouchEvent(event);
    }

    private void setOffset(float offset) {
        if (Math.abs(offset) < mSpacing) { // 偏移 少于 一个间距，直接赋值重绘
            this.mOffset = offset;
        } else if (Math.abs(offset) > mSpacing) { // 偏移 大于 一个间距，需要计数器辅助改变选中值
            if (mScaler != (int) (Math.abs(offset) / mSpacing)) { // 计数器发生改变，选中数值改变
                mScaler = (int) (Math.abs(offset) / mSpacing);
                if (offset < 0) {
                    mSelectedValue = mSelectedValue + 1 > mMaxValue ? mMaxValue : mSelectedValue + 1;
                } else if (offset > 0) {
                    mSelectedValue = mSelectedValue - 1 < mMinValue ? mMinValue : mSelectedValue - 1;
                }
            }
            // 去除 整间距
            if (offset < 0) {
                this.mOffset = offset + mScaler * mSpacing;
            } else if (offset > 0) {
                this.mOffset = offset - mScaler * mSpacing;
            }
        } else { // 偏移 等于 一个间距，选中值改变
            if (offset < 0) {
                mSelectedValue = mSelectedValue + 1 > mMaxValue ? mMaxValue : mSelectedValue + 1;
            } else if (offset > 0) {
                mSelectedValue = mSelectedValue - 1 < mMinValue ? mMinValue : mSelectedValue - 1;
            }
            mOffset = 0;
        }
        postInvalidate();
    }

    private void adjustPosition() {
        if (null != mAdjustPositionAnimator && mAdjustPositionAnimator.isRunning()) {
            mAdjustPositionAnimator.cancel();
            return;
        }
        if (Math.abs(mOffset) > mSpacing / 2) {
            mAdjustPositionAnimator = ObjectAnimator.ofFloat(this, "offset", mOffset, mOffset > 0 ? mSpacing : -mSpacing);
        } else {
            mAdjustPositionAnimator = ObjectAnimator.ofFloat(this, "offset", mOffset, 0);
        }
        mAdjustPositionAnimator.setDuration(100);
        mAdjustPositionAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mChanging = 0;
                if (null == mListener || mFling) {
                    return;
                }
                Log.e(TAG, "onSelected --> " + mSelectedValue);
                mListener.onSelected(mSelectedValue);
            }
        });
        mAdjustPositionAnimator.start();
    }

    private void fling(float velocityX) {
        mFlingAnimator = ObjectAnimator.ofFloat(this, "offset", mOffset, velocityX);
        mFlingAnimator.setDuration((long) Math.abs(velocityX));
        mFlingAnimator.setInterpolator(new DecelerateInterpolator());
        mFlingAnimator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mFling = true;
                isCancelFling = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                isCancelFling = true;
                mFling = false;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (isCancelFling) {
                    return;
                }
                mFling = false;
                isCancelFling = false;
                adjustPosition();
            }
        });
        mFlingAnimator.start();
    }

    public void setMinValue(int minValue) {
        this.mMinValue = minValue;
        postInvalidate();
    }

    public void setMaxValue(int maxValue) {
        this.mMaxValue = maxValue;
        postInvalidate();
    }

    public void setSelectedValue(int selectedValue) {
        this.mSelectedValue = selectedValue;
        postInvalidate();
    }

    public void setLineWidth(float lineWidth) {
        this.mLineWidth = lineWidth;
        mLinePaint.setStrokeWidth(lineWidth);
        postInvalidate();
    }

    public void setLineHeight(float lineHeight) {
        this.mLineHeight = lineHeight;
        requestLayout();
    }

    public void setBottomLineWidth(float bottomLineWidth) {
        this.mBottomLineWidth = bottomLineWidth;
        mBottomLinePaint.setStrokeWidth(mBottomLineWidth);
        requestLayout();
    }

    public void setLineColor(@ColorRes int lineColor) {
        this.mLineColor = lineColor;
        postInvalidate();
    }

    public void setTextSize(float textSize) {
        this.mTextSize = textSize;
        postInvalidate();
    }

    public void setSelectedTextSize(float selectedTextSize) {
        this.mSelectedTextSize = selectedTextSize;
        mTextPaint.setTextSize(mSelectedTextSize);
        mTextPaint.getTextBounds(String.valueOf(mSelectedValue), 0, String.valueOf(mSelectedValue).length(), mSelectedTextRect);
        requestLayout();
    }

    public void setTextColor(@ColorRes int textColor) {
        this.mTextColor = textColor;
        postInvalidate();
    }

    public void setSymbolTextSizeRatio(float ratio) {
        this.mSymbolTextSizeRatio = ratio;
        postInvalidate();
    }

    public void setTextAndLineSpacing(float textAndLineSpacing) {
        this.mTextAndLineSpacing = textAndLineSpacing;
        requestLayout();
    }

    public void setSpacing(float spacing) {
        this.mSpacing = spacing;
        postInvalidate();
    }

    public void setFlingDamping(float flingDamping){
        this.mFlingDamping = flingDamping;
    }

    private OnRulerListener mListener;

    public void setOnRulerListener(OnRulerListener onRulerListener) {
        this.mListener = onRulerListener;
    }

    public interface OnRulerListener {
        void onSelected(float value);
    }
}

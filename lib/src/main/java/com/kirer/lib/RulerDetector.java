package com.recycler.coverflow;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;

/**
 * Created by xinwb on 2018/10/19.
 */

public class RulerDetector extends GestureDetector.SimpleOnGestureListener {

    private boolean mFling;
    private int mMinValue = 5;
    private int mMaxValue = 45;
    private int mSelectedValue = 12;
    private float mOffset;
    private float mDistance = 150;
    private OnOffsetChangingListener mListener;

    public RulerDetector(OnOffsetChangingListener listener) {
        this.mListener = listener;
    }

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

    public void adjustPosition() {
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
                mListener.onStop( mSelectedValue);
            }
        });
        animator.start();
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
        if (null == mListener) {
            return;
        }
        mListener.onChanging(mOffset);
    }

    public boolean isFling() {
        return mFling;
    }

    public interface OnOffsetChangingListener {
        void onChanging(float offset);
        void onStop(int selectedValue);
    }
}

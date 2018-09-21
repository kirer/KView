package com.kirer.lib;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by xinwb on 2018/9/21.
 */

public class KEditText extends AppCompatEditText implements TextWatcher {

    private int paddingRight;
    private List<Action> mActionList;
    private List<Validator> mValidatorList;

    public KEditText(Context context) {
        super(context);
        init();
    }

    public KEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public KEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        paddingRight = getPaddingRight();
        addTextChangedListener(this);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (null == mActionList) {
            return;
        }
        for (int i = 0; i < mActionList.size(); i++) {
            Action action = mActionList.get(i);
            int left = w - (action.getIconSize() + action.getIconPadding()) * (i + 1) - paddingRight;
            int top = (h - action.getIconSize()) / 2;
            action.setBounds(new Rect(left, top, left + action.getIconSize(), top + action.getIconSize()));
        }
        int pr = w - mActionList.get(mActionList.size() - 1).getBounds().left;
        setPadding(getPaddingLeft(), getPaddingTop(), pr, getPaddingBottom());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (null == mActionList) {
            return super.onTouchEvent(event);
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            int eventX = (int) event.getX();
            int eventY = (int) event.getY();
            for (Action action : mActionList) {
                if (null != action.getBounds() && action.getBounds().contains(eventX, eventY)) {
                    action.onClick();
                }
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (null == mActionList) {
            return;
        }
        for (Action action : mActionList) {
            if (null != action.getIcon() && null != action.getBounds()) {
                canvas.drawBitmap(action.getIcon(), null, action.getBounds(), action.getPaint());
            }
        }
    }

    public void addAction(Action action) {
        if (null == mActionList) {
            mActionList = new ArrayList<>();
        }
        mActionList.add(action);
    }

    public void addValidator(Validator validator) {
        if (null == mValidatorList) {
            mValidatorList = new ArrayList<>();
        }
        mValidatorList.add(validator);
    }

    public boolean validate() {
        if (null == mValidatorList) {
            return true;
        }
        for (Validator validator : mValidatorList) {
            if (!validator.isValid(this)) {
                validator.onError(this);
                return false;
            }
            validator.onPass(this);
        }
        return true;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        validate();
    }

    public static abstract class Action {

        private KEditText mEditText;
        private Rect mBounds;
        private Paint mPaint;
        private int mIconSize;
        private int mIconPadding;

        public Action(KEditText editText) {
            this.mEditText = editText;
            mPaint = new Paint();
            mIconSize = dp2px(mEditText.getContext(), 24);
            mIconPadding = dp2px(mEditText.getContext(), 8);
        }

        public abstract Bitmap getIcon();

        public abstract void onClick();

        public Rect getBounds() {
            return mBounds;
        }

        public void setBounds(Rect bounds) {
            this.mBounds = bounds;
        }

        public KEditText getEditText() {
            return mEditText;
        }

        public Paint getPaint() {
            return mPaint;
        }

        public void setPaint(Paint mPaint) {
            this.mPaint = mPaint;
        }

        public int getIconSize() {
            return mIconSize;
        }

        public void setIconSize(int mIconSize) {
            this.mIconSize = mIconSize;
        }

        public int getIconPadding() {
            return mIconPadding;
        }

        public void setIconPadding(int mIconPadding) {
            this.mIconPadding = mIconPadding;
        }

        public int dp2px(Context context, float dpValue) {
            final float scale = context.getResources().getDisplayMetrics().density;
            return (int) (dpValue * scale);
        }

    }

    public static abstract class Validator {

        private Pattern mPattern;

        public Validator() {
        }

        public Validator(Pattern pattern) {
            this.mPattern = pattern;
        }

        public boolean isValid(KEditText et) {
            if (null == mPattern) {
                return false;
            }
            return mPattern.matcher(et.getText()).matches();
        }

        public void onPass(KEditText et) {
        }

        public void onError(KEditText et) {
        }
    }
}

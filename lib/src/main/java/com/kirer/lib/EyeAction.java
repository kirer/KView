package com.kirer.lib;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;

/**
 * Created by xinwb on 2018/9/6.
 */

public class EyeAction extends KEditText.Action {

    private boolean mVisibility;
    private Bitmap mEyeOpenedBitmap;
    private Bitmap mEyeClosedBitmap;

    public EyeAction(KEditText editText) {
        super(editText);
        mEyeOpenedBitmap = BitmapFactory.decodeResource(editText.getResources(), R.drawable.ic_eye_opend);
        mEyeClosedBitmap = BitmapFactory.decodeResource(editText.getResources(), R.drawable.ic_eye_closed);
        setVisibility(mVisibility);
    }

    @Override
    public Bitmap getIcon() {
        return mVisibility ? mEyeOpenedBitmap : mEyeClosedBitmap;
    }

    @Override
    public void onClick() {
        mVisibility = !mVisibility;
        setVisibility(mVisibility);
    }

    private void setVisibility(boolean visibility) {
        getEditText().setTransformationMethod(visibility ? HideReturnsTransformationMethod.getInstance() : PasswordTransformationMethod.getInstance());
    }

}

package com.kirer.lib;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

/**
 * Created by xinwb on 2018/9/6.
 */

public class ClearAction extends KEditText.Action {

    private Bitmap mClearBitmap;

    public ClearAction(KEditText editText) {
        super(editText);
        mClearBitmap = BitmapFactory.decodeResource(editText.getResources(), R.drawable.ic_clear);
    }

    @Override
    public Bitmap getIcon() {
        return TextUtils.isEmpty(getEditText().getText()) ? null : mClearBitmap;
    }

    @Override
    public void onClick() {
        getEditText().setText("");
    }

}

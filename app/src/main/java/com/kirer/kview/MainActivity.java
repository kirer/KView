package com.kirer.kview;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.TextView;

import com.kirer.lib.ClearAction;
import com.kirer.lib.EyeAction;
import com.kirer.lib.KEditText;
import com.kirer.lib.GradientRulerView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        KEditText ket = findViewById(R.id.k_et);
        final TextView tipTv = findViewById(R.id.tip_tv);


        ket.addAction(new EyeAction(ket));
        ket.addAction(new ClearAction(ket));

        ket.addValidator(new KEditText.Validator() {
            @Override
            public boolean isValid(KEditText et) {
                return !TextUtils.isEmpty(et.getText());
            }

            @Override
            public void onError(KEditText et) {
                super.onError(et);
                tipTv.setText("");
            }

            @Override
            public void onPass(KEditText et) {
                super.onPass(et);
                tipTv.setText("");
            }
        });
        ket.addValidator(new KEditText.Validator() {

            @Override
            public boolean isValid(KEditText et) {
                return TextUtils.getTrimmedLength(et.getText()) > 6;
            }

            @Override
            public void onError(KEditText et) {
                tipTv.setTextColor(ContextCompat.getColor(MainActivity.this, android.R.color.holo_red_light));
                tipTv.setText("Length is at least 6");
            }
        });
        ket.addValidator(new KEditText.Validator(Patterns.PHONE) {

            @Override
            public void onError(KEditText et) {
                tipTv.setTextColor(ContextCompat.getColor(MainActivity.this, android.R.color.holo_red_light));
                tipTv.setText("This is not Phone Number");
            }

            @Override
            public void onPass(KEditText et) {
                tipTv.setTextColor(ContextCompat.getColor(MainActivity.this, android.R.color.holo_green_light));
                tipTv.setText("Phone is ok --> " + et.getText().toString());
            }
        });


        GradientRulerView rulerView = findViewById(R.id.ruler_view);
        rulerView.setMinValue(2);
        rulerView.setMaxValue(25);
        rulerView.setSelectedValue(5);
        rulerView.setSelectedTextSize(100);
        rulerView.setTextSize(50);
        rulerView.setBottomLineWidth(10);
        rulerView.setSpacing(200);
        rulerView.setLineWidth(5);
        rulerView.setLineHeight(50);
        rulerView.setTextAndLineSpacing(30);
        rulerView.setFlingDamping(10);
        rulerView.setSymbolTextSizeRatio(0.8f);
        rulerView.setOnRulerListener(new GradientRulerView.OnRulerListener() {
            @Override
            public void onSelected(float value) {
                Log.e("MainActivity","onSelected --> " + value);
            }
        });
    }
}

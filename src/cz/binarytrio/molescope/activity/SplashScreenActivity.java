package cz.binarytrio.molescope.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import cz.binarytrio.molescope.R;

public class SplashScreenActivity extends Activity {

    private static final int DELAY_TIME = 1000;

    private static final Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActionBar() != null)
            getActionBar().hide();

        setContentView(R.layout.activity_splash_screen);

        RelativeLayout mainLayout = (RelativeLayout) findViewById(R.id.relative_splash);

        final Runnable looper = new Runnable() {
            @Override
            public void run() {
                runScanner();
            }
        };

        mHandler.postDelayed(looper, DELAY_TIME);
        if(mainLayout != null){
            mainLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("SMARTSCOPE", "Splash screen clicked.");
                    mHandler.removeCallbacks(looper);
                    runScanner();
                }
            });
        }
    }

    private void runScanner(){
        Intent intent = new Intent(SplashScreenActivity.this, ClassifierActivity.class);
        startActivity(intent);
    }
}

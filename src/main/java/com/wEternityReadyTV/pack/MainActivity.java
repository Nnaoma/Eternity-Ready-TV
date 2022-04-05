package com.wEternityReadyTV.pack;

import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.suddenh4x.ratingdialog.AppRating;

public class MainActivity extends BaseActivity implements WebViewContainer.ControlFullScreen {

    private FragmentManager manager;
    private FullScreenFragment fullScreenFragment;

    private View customView;
    private WebChromeClient.CustomViewCallback customViewCallback;

    private boolean isFullScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //SystemClock.sleep(500);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        manager = getSupportFragmentManager();

        if (savedInstanceState == null){
            fullScreenFragment = FullScreenFragment.newInstance();

            FragmentTransaction ft = manager.beginTransaction();
            ft.add(R.id.fragment_container, WebViewFragment.newInstance(), "WEBVIEW");
            ft.add(R.id.fragment_container, fullScreenFragment, "FULLSCREEN");
            ft.hide(fullScreenFragment);
            ft.commit();
        }else{
            fullScreenFragment = (FullScreenFragment) manager.findFragmentByTag("FULLSCREEN");
        }

        AppRating.Builder builder = new AppRating.Builder(this).setMinimumDays(3).setMinimumDaysToShowAgain(3).setMinimumLaunchTimes(6);
        builder.showIfMeetsConditions();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && !isFullScreen){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure you want to exit?").setTitle("Exit")
                    .setPositiveButton("yes", (dialog, which) -> finish())
                    .setNegativeButton("cancel", (dialog, which) -> dialog.dismiss()).setCancelable(false);
            builder.create().show();
            return true;
        } else if(keyCode == KeyEvent.KEYCODE_BACK){
            if(customViewCallback != null) {
                exitFullScreen();
                return true;
            }
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (isFullScreen) {
            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                try{
                    Rational rational = new Rational(customView.getWidth(), customView.getHeight());
                    PictureInPictureParams params = new PictureInPictureParams.Builder().setAspectRatio(rational).build();
                    enterPictureInPictureMode(params);
                }catch(IllegalStateException e){
                    e.printStackTrace();
                }
            }*/
            enterPictureInPictureMode();
        }
    }

    @Override
    public void goFullScreen(View v, WebChromeClient.CustomViewCallback callback) {
        customView = v;
        customViewCallback = callback;
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        isFullScreen = true;
        fullScreenFragment.updateViews(v);
        FragmentTransaction ft = manager.beginTransaction();
        ft.show(fullScreenFragment);
        ft.commit();
    }

    @Override
    public void exitFullScreen() {
        if (customView != null) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            isFullScreen = false;
            FragmentTransaction ft = manager.beginTransaction();
            ft.hide(fullScreenFragment);
            ft.commit();
            customViewCallback.onCustomViewHidden();
            customView = null;
            customViewCallback = null;
        }
    }
}
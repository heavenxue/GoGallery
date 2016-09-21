package com.aibei.lixue.gogallery;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;

import com.aibei.lixue.gogallery.widget.GalleryView;

public class MainActivity extends AppCompatActivity {
    private GalleryView mGalleryView;
    private int[] imageids = {R.mipmap.girl2,R.mipmap.girl3,R.mipmap.girl4,
    R.mipmap.girl5};

    private View seletedView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
    }

    private void initView(){
        mGalleryView = (GalleryView) findViewById(R.id.gallery_bar);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void initData() {
        mGalleryView.setAdapter(new ImageAdapter(getBaseContext(), imageids));
        final AnimatorSet animatorSet = new AnimatorSet();

        mGalleryView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("NewApi")
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                seletedView = v;


                ObjectAnimator imgScaleUpYAnim = ObjectAnimator.ofFloat(v, "scaleY", 0.7f, 1f);
                imgScaleUpYAnim.setDuration(600);
                //imgScaleUpYAnim.setInterpolator(DECCELERATE_INTERPOLATOR);
                ObjectAnimator imgScaleUpXAnim = ObjectAnimator.ofFloat(v, "scaleX", 0.7f, 1f);
                imgScaleUpXAnim.setDuration(600);
                animatorSet.playTogether(imgScaleUpYAnim, imgScaleUpXAnim);
                animatorSet.start();

//                for(int i = 0;i < parent.getChildCount();i++){
//                    Log.i("aaa","count:" + parent.getChildCount());
//                    if(parent.getChildAt(i) != v){
//                        Log.i("aaa",i + "");
//                        View s = parent.getChildAt(i);
//                        ObjectAnimator imgScaleDownYAnim = ObjectAnimator.ofFloat(s, "scaleY", 1f, 0.9f);
//                        imgScaleDownYAnim.setDuration(100);
//                        //imgScaleUpYAnim.setInterpolator(DECCELERATE_INTERPOLATOR);
//                        ObjectAnimator imgScaleDownXAnim = ObjectAnimator.ofFloat(s, "scaleX", 1f, 0.9f);
//                        imgScaleDownXAnim.setDuration(100);
//                        animatorSet.playTogether(imgScaleDownXAnim,imgScaleDownYAnim);
//                        animatorSet.start();
//                    }
//                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
    }
}

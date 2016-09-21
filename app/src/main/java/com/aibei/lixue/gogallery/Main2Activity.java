package com.aibei.lixue.gogallery;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class Main2Activity extends AppCompatActivity {
    private LinearLayout mLinearLayout;
    private int[] imageids = {R.mipmap.girl2,R.mipmap.girl3,R.mipmap.girl4,
            R.mipmap.girl5};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        initView();
        initData();
    }

    private void initView(){
        mLinearLayout = (LinearLayout) findViewById(R.id.mygallery);
    }

    private void initData(){
        for (int i = 0 ;i < imageids.length;i ++){
            LinearLayout layout = new LinearLayout(getApplicationContext());
            layout.setLayoutParams(new ViewGroup.LayoutParams(500, 500));
            layout.setGravity(Gravity.CENTER);

            ImageView imageView = new ImageView(getApplicationContext());
            imageView.setLayoutParams(new ViewGroup.LayoutParams(450, 450));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setImageResource(imageids[i]);

            layout.addView(imageView);
            mLinearLayout.addView(layout);
        }

    }

}

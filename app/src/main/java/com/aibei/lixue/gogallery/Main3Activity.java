package com.aibei.lixue.gogallery;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.aibei.lixue.gogallery.widget2.TosGallery;

public class Main3Activity extends AppCompatActivity {
    private TosGallery gallery;
    private int[] imageids = {R.mipmap.girl2,R.mipmap.girl3,R.mipmap.girl4,R.mipmap.girl5};
    private ImageAdapter imgAdapter;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        gallery = (TosGallery) findViewById(R.id.gallery);
        gallery.setAdapter( imgAdapter = new ImageAdapter(getBaseContext(),imageids));
        gallery.setUnselectedAlpha(0.3f);
        gallery.setHorizontalScrollBarEnabled(true);
        gallery.setSlotInCenter(true);
        gallery.setScrollCycle(true);
    }
}

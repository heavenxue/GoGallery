package com.aibei.lixue.gogallery;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.aibei.lixue.gogallery.widget2.EcoGallery;

public class Main3Activity extends AppCompatActivity {
    private EcoGallery gallery;
    private int[] imageids = {R.mipmap.girl2,R.mipmap.girl3,R.mipmap.girl4,
            R.mipmap.girl5};

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        gallery = (EcoGallery) findViewById(R.id.gallery);
        gallery.setAdapter(new ImageAdapter(getBaseContext(),imageids));
    }
}

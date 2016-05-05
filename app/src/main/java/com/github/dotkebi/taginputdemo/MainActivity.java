package com.github.dotkebi.taginputdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.github.dotkebi.taginput.TagInputer;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements TagInputer.OnInputTagListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TagInputer tagInputer = (TagInputer) findViewById(R.id.tagInputer);
        tagInputer.setOnInputTagListener(this);

        findViewById(R.id.add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              tagInputer.addTag("Test");
            }
        });
    }

    @Override
    public void onInputTagListener(String[] tags) {
        String message = "Tags : " + Arrays.toString(tags);

        Log.d(getClass().getName(), message);
        //Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}

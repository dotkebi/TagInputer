package com.github.dotkebi.taginput;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements TagInputer.OnInputTagListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TagInputer tagInputer = (TagInputer) findViewById(R.id.tagInputer);
        tagInputer.setOnInputTagListener(this);
    }

    @Override
    public void onInputTagListener(String[] tags) {
        String message = "Tags : " + Arrays.toString(tags);

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

    }
}

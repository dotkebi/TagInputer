package com.github.dotkebi.taginputdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.github.dotkebi.taginput.OnCurrentTagListener;
import com.github.dotkebi.taginput.TagInputer;

public class MainActivity extends AppCompatActivity implements OnCurrentTagListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TagInputer tagInputer = (TagInputer) findViewById(R.id.tagInputer);
        tagInputer.setOnCurrentTagListener(this);

        findViewById(R.id.add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              tagInputer.addTag("Test");
            }
        });

        findViewById(R.id.hasTag).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "hasTags : " + tagInputer.hasTags(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCurrentTagListener(String tags) {
        String message = "Tags : " + tags;

        Log.d(getClass().getName(), message);
        //Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}

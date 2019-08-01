package com.dds;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.dds.java.JavaActivity;
import com.dds.nodejs.NodejsActivity;
import com.dds.webrtc.R;

public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    public void nodejs(View view) {
        startActivity(new Intent(this, NodejsActivity.class));
    }

    public void java(View view) {
        startActivity(new Intent(this, JavaActivity.class));
    }
}

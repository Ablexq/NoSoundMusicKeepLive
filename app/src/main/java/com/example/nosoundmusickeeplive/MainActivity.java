package com.example.nosoundmusickeeplive;

import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.nosoundmusickeeplive.util.PermissionUtils;
import com.example.nosoundmusickeeplive.util.SettingUtils;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PermissionUtils.setPermission(this,
                PermissionUtils.PERMISSION_READ_EXTERNAL_STORAGE,
                PermissionUtils.REQUESTCODE_SINGLE);

        PermissionUtils.setPermission(this,
                PermissionUtils.PERMISSION_WRITE_EXTERNAL_STORAGE,
                PermissionUtils.REQUESTCODE_SINGLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, PlayerMusicService.class));
        } else {
            startService(new Intent(this, PlayerMusicService.class));
        }

        findViewById(R.id.btn1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingUtils.enterWhiteListSetting(MainActivity.this);
            }
        });
    }
}

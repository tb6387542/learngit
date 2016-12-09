package com.example.administrator.wzm;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.administrator.wzm.utils.DateUtil;
import com.example.administrator.wzm.utils.LogUtil;
import com.example.administrator.wzm.utils.Tools;

public class MainActivity extends AppCompatActivity {
    String TAG="MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button=(Button)findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtil.i(TAG,DateUtil.curDateFormatStandard());
                LogUtil.i(TAG,Tools.getVersion(MainActivity.this));
                Toast.makeText(MainActivity.this, DateUtil.curDateFormatStandard(),Toast.LENGTH_SHORT).show();
            }
        });



    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        return Tools.doubleClickExit(this,keyCode)==true?true:super.onKeyDown(keyCode, event);
    }
}

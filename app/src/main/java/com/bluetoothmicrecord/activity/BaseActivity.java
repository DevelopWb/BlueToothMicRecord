package com.bluetoothmicrecord.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.bluetoothmicrecord.R;
import com.bluetoothmicrecord.upload.uploadUtil.PreferenceUtil;
import com.bluetoothmicrecord.utils.ActivityManager;
import com.bluetoothmicrecord.utils.PubUtils;

import static com.bluetoothmicrecord.utils.PubUtils.scanResult;

public class BaseActivity extends AppCompatActivity {


    private boolean paused = false;
    private Dialog dialog_input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        ActivityManager.getInstance().addActivity(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (paused) {
            paused = false;
            if (scanResult) {
                scanResult = false;
                return;
            }
            if (dialog_input == null) {
                showPassWordInput();
            } else {
                if (!dialog_input.isShowing()) {
                    showPassWordInput();
                }
            }

        }
    }

    /**
     * 密码输入提示框
     */
    private void showPassWordInput() {

        View v = LayoutInflater.from(this).inflate(R.layout.password_input, null);
        dialog_input = new Dialog(this, R.style.DialogStyle);
        dialog_input.setCanceledOnTouchOutside(false);
        dialog_input.setCancelable(false);
        dialog_input.show();
        Window window = dialog_input.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        window.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        lp.width = PubUtils.dip2px(this, 300); // 宽度
        lp.height = PubUtils.dip2px(this, 240); // 高度
        // lp.alpha = 0.7f; // 透明度
        window.setAttributes(lp);
        window.setContentView(v);
        dialog_input.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {

                if (keyCode == event.KEYCODE_BACK) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        ActivityManager.getInstance().clearAllActivity();
                    }
                }
                return false;
            }
        });
        final EditText et_password = (EditText) v.findViewById(R.id.et_password);
        ImageButton btn_ok = (ImageButton) v.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String et_pwd = et_password.getText().toString().trim();
                if (et_password == null || TextUtils.isEmpty(et_pwd)) {
                    Toast.makeText(BaseActivity.this, "密码不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (getPwd().equals(et_pwd)) {
                    dialog_input.dismiss();
                } else {
                    Toast.makeText(BaseActivity.this, "密码不正确，请重新输入", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * 获取设置的验证密码
     *
     * @return
     */
    private String getPwd() {
        PreferenceUtil mPrefManager = PreferenceUtil.getInstance(this, "APPSET");
        String pwd = mPrefManager.getString("PASSWORD");
        if (TextUtils.isEmpty(pwd)) {
            pwd = "8888888";
        }
        return pwd;
    }

    @Override
    public void onBackPressed() {
        Intent intentBack = new Intent();
        setResult(PubUtils.ACTIVITY_FINISH, intentBack);
        finish();

    }

    @Override
    protected void onPause() {
        super.onPause();
        paused = true;
    }


}

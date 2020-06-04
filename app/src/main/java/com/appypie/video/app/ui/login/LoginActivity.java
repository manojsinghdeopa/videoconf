package com.appypie.video.app.ui.login;


import android.os.Bundle;

import com.appypie.video.app.R;
import com.appypie.video.app.base.BaseActivity;

public class LoginActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        if (savedInstanceState == null)
            getSupportFragmentManager().beginTransaction().add(R.id.loginContainer, new LoginFragment()).commit();

    }
}

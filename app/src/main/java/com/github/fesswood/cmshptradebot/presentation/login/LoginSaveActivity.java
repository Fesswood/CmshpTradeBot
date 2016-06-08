package com.github.fesswood.cmshptradebot.presentation.login;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import com.github.fesswood.cmshptradebot.R;
import com.github.fesswood.cmshptradebot.data.UserDetail.ProfileModel;
import com.github.fesswood.cmshptradebot.data.UserDetail.ProfileRepository;
import com.github.fesswood.cmshptradebot.presentation.router.Router;

import io.realm.Realm;


public class LoginSaveActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = LoginSaveActivity.class.getSimpleName();
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(ProfileRepository.isProfileExist()){
            Router.routeToTabbedActivity(this);
        }else {
            setContentView(R.layout.activity_login);
            mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
            mPasswordView = (EditText) findViewById(R.id.password);
            Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
            mEmailSignInButton.setOnClickListener(this);
        }
    }


    @Override
    public void onClick(View v) {
        ProfileRepository.saveProfile(mEmailView.getText().toString(),
                mPasswordView.getText().toString());
        Router.routeToTabbedActivity(this);
    }
}

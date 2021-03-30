package ru.hse.bookExchange.ui.login;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import com.androidnetworking.AndroidNetworking;

import ru.hse.bookExchange.R;
import ru.hse.bookExchange.data.DataHandler;
import ru.hse.bookExchange.data.LoginRepository;
import ru.hse.bookExchange.data.LoginResult;
import ru.hse.bookExchange.data.model.LoggedInUser;
import ru.hse.bookExchange.ui.main.MainActivity;

public class LoginActivity extends AppCompatActivity {

    public static final String EXTRA_USER = "user";
    private LoginViewModel loginViewModel;


    private boolean loginState = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginViewModel = new LoginViewModel(LoginRepository.getInstance(new DataHandler()));

        final EditText usernameEditText = findViewById(R.id.username);
        final EditText passwordEditText = findViewById(R.id.password);
        final Button loginButton = findViewById(R.id.login);
        final ProgressBar loadingProgressBar = findViewById(R.id.loading);
        final TextView switchTextView = findViewById(R.id.switchTextView);
        final EditText nameEditText = findViewById(R.id.nameEditText);

        SharedPreferences save = getSharedPreferences("SAVE", 0);
        usernameEditText.setText(save.getString("username", ""));
        passwordEditText.setText(save.getString("password", ""));

        switchTextView.setPaintFlags(switchTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        switchTextView.setOnClickListener((view) -> {
            if (loginState) {
                switchTextView.setText(R.string.signUpText);
                loginButton.setText(R.string.buttonSignUpText);
                nameEditText.setVisibility(View.VISIBLE);
            } else {
                switchTextView.setText(R.string.loginText);
                loginButton.setText(R.string.buttonLoginText);
                nameEditText.setVisibility(View.INVISIBLE);
            }
            loginState = !loginState;
        });


        loginViewModel.getLoginFormState().observe(this, loginFormState -> {
            if (loginFormState == null) {
                return;
            }
            loginButton.setEnabled(loginFormState.isDataValid());
            if (loginFormState.getUsernameError() != null) {
                usernameEditText.setError(getString(loginFormState.getUsernameError()));
            }
            if (loginFormState.getPasswordError() != null) {
                passwordEditText.setError(getString(loginFormState.getPasswordError()));
            }
        });

        loginViewModel.getLoginResult().observe(this, loginResult -> {
            if (loginResult == null) {
                return;
            }
            loadingProgressBar.setVisibility(View.GONE);
            if (loginResult.getError() != null) {
                showLoginFailed(loginResult.getError());
            }

            LoginResult result = loginViewModel.getLoginResult().getValue();
            if (result != null && result.getSuccess() != null) {
                LoggedInUser user = loginViewModel.getLoginResult().getValue().getSuccess();
                if (user != null) {
                    SharedPreferences.Editor editor = save.edit(); // создаем редактор shared preferences
                    editor.putString("username", String.valueOf(user.getUsername())); // сохраняем токен
                    editor.putString("password", String.valueOf(passwordEditText.getText())); // сохраняем токен
                    editor.apply(); // применяем редактирование shared preferences
                    setResult(Activity.RESULT_OK);
                }
            }
            if (loginResult.getSuccess() != null) {
                updateUiWithUser(loginResult.getSuccess());
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                loginViewModel.login(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
            return false;
        });

        AndroidNetworking.initialize(getApplicationContext());

        loginButton.setOnClickListener(v -> {
            loadingProgressBar.setVisibility(View.VISIBLE);
            if (loginState) {
                loginViewModel.login(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            } else {
                loginViewModel.signUp(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString(), nameEditText.getText().toString());
            }
        });
    }

    private void updateUiWithUser(LoggedInUser user) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(EXTRA_USER, user);
        startActivity(intent);
        finish();
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }
}
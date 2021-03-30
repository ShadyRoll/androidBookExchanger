package ru.hse.bookExchange.ui.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import ru.hse.bookExchange.R;
import ru.hse.bookExchange.data.LoginRepository;
import ru.hse.bookExchange.data.LoginResult;
import ru.hse.bookExchange.data.Result;
import ru.hse.bookExchange.data.model.LoggedInUser;

public class LoginViewModel extends ViewModel {

    Result<LoggedInUser> result;
    private MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    private MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private LoginRepository loginRepository;

    LoginViewModel(LoginRepository loginRepository) {
        this.loginRepository = loginRepository;
    }

    LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    public void login(final String username, final String password) {

        // can be launched in a separate asynchronous job
        Thread thread = new Thread(() -> {
            result = loginRepository.login(username, password);
            if (result instanceof Result.Success) {
                LoggedInUser user = ((Result.Success<LoggedInUser>) result).getData();
                loginResult.postValue(new LoginResult(user));
            } else {
                loginResult.postValue(new LoginResult(R.string.login_failed));
            }
        });
        thread.start();
    }

    public void signUp(final String username, final String password, final String name) {
        // can be launched in a separate asynchronous job
        Thread thread = new Thread(() -> {
            result = loginRepository.signUp(username, password, name);
            if (result instanceof Result.Success) {
                LoggedInUser user = ((Result.Success<LoggedInUser>) result).getData();
                loginResult.postValue(new LoginResult(user));
            } else {
                loginResult.postValue(new LoginResult(R.string.sign_up_failed));
            }
        });
        thread.start();
    }

    public void loginDataChanged(String username, String password) {
        if (!isUserNameValid(username)) {
            loginFormState.setValue(new LoginFormState(R.string.invalid_username, null));
        } else if (!isPasswordValid(password)) {
            loginFormState.setValue(new LoginFormState(null, R.string.invalid_password));
        } else {
            loginFormState.setValue(new LoginFormState(true));
        }
    }

    // A placeholder username validation check
    private boolean isUserNameValid(String username) {
        if (username == null) {
            return false;
        }
        return !username.trim().isEmpty();
    }

    // A placeholder password validation check
    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 3;
    }
}
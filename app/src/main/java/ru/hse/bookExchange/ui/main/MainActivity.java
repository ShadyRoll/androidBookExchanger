package ru.hse.bookExchange.ui.main;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.ads.MobileAds;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

import ru.hse.bookExchange.R;
import ru.hse.bookExchange.data.DataHandler;
import ru.hse.bookExchange.data.model.BookBase;
import ru.hse.bookExchange.data.model.LoggedInUser;
import ru.hse.bookExchange.ui.login.LoginActivity;

public class MainActivity extends AppCompatActivity {
    static LoggedInUser user;
    private static List<BookBase> bookBases;

    public static List<BookBase> getBookBases() {
        return bookBases;
    }

    public static void setBookBases(List<BookBase> bookBases) {
        MainActivity.bookBases = bookBases;
    }

    public static LoggedInUser getUser() {
        return user;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_search, R.id.navigation_profile)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
        NavigationUI.navigateUp(navController, appBarConfiguration);

        // Get the Intent that started this activity and extract the string
        Bundle bundle = getIntent().getExtras();
        if (bundle != null)
            user = (LoggedInUser) bundle.getSerializable(LoginActivity.EXTRA_USER);

        if (user == null || user.getToken() == null)
            throw new IllegalArgumentException("user is null!");

        Thread th = new Thread(() ->
                DataHandler.loadGenres(user));
        th.start();
    }

}
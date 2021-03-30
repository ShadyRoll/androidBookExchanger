package ru.hse.bookExchange.ui.main;

import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import ru.hse.bookExchange.R;
import ru.hse.bookExchange.data.DataHandler;
import ru.hse.bookExchange.data.Result;
import ru.hse.bookExchange.data.model.BookBase;
import ru.hse.bookExchange.data.model.LoggedInUser;
import ru.hse.bookExchange.ui.main.home.BookBaseAdapter;
import ru.hse.bookExchange.ui.main.home.HomeFragment;

public class ListShowActivity extends AppCompatActivity {
    List<BookBase> bookBases = new ArrayList<>();
    List<BookBase> filteredBookBases = new ArrayList<>();
    LoggedInUser user;
    RecyclerView rvBookBases;
    SearchView searchView;
    TextView initialTextView;

    BookBaseAdapter adapterBookBase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_show);

        ActionBar ab = getSupportActionBar();
        if (ab != null)
            ab.setDisplayHomeAsUpEnabled(true);

        initialTextView = findViewById(R.id.initialTextView);
        rvBookBases = findViewById(R.id.rvSearchBookBases);

        adapterBookBase = new BookBaseAdapter(filteredBookBases);
        // Attach the adapter to the recyclerview to populate items
        rvBookBases.setAdapter(adapterBookBase);
        // Set layout manager to position the items
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        rvBookBases.setLayoutManager(layoutManager);

        user = MainActivity.getUser();

        Bundle bundle = getIntent().getExtras();
        String req;
        if (bundle != null) {
            req = bundle.getString(HomeFragment.EXTRA_REQUEST_QUERY);
            Thread thLoader = new Thread(() -> {
                Result bookBasesResult = DataHandler.getBookBases(
                        req, user);
                if (bookBasesResult instanceof Result.Success) {
                    bookBases.clear();
                    filteredBookBases.clear();
                    bookBases.addAll((((Result.Success<List<BookBase>>) bookBasesResult).getData()));
                    filteredBookBases.addAll(bookBases);
                    if (!filteredBookBases.isEmpty())
                        initialTextView.setVisibility(View.INVISIBLE);
                    rvBookBases.post(() -> adapterBookBase.notifyDataSetChanged());
                }
            });
            thLoader.start();
        }
    }


    private void doSearch(String searchStr) {
        Thread thRecommendation = new Thread(() -> {
            String[] words = searchStr.toLowerCase(Locale.ROOT).split("\\s");

            filteredBookBases.clear();
            filteredBookBases.addAll(bookBases.stream().filter(bookBase -> {
                for (String word : words) {
                    if (bookBase.getAuthor().toLowerCase().contains(word)
                            || bookBase.getTitle().toLowerCase().contains(word)) {
                        return true;
                    }
                }
                return false;
            }).collect(Collectors.toList()));


            rvBookBases.post(() -> {
                if (filteredBookBases.isEmpty())
                    initialTextView.setVisibility(View.VISIBLE);
                else
                    initialTextView.setVisibility(View.INVISIBLE);
                adapterBookBase.notifyDataSetChanged();
            });
        });
        thRecommendation.start();
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);

        MenuItem myActionMenuItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) myActionMenuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.isEmpty())
                    return false;
                doSearch(query);
                if (!searchView.isIconified()) {
                    //searchView.setIconified(true);
                }
                //myActionMenuItem.collapseActionView();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (s.isEmpty()) {
                    filteredBookBases.clear();
                    filteredBookBases.addAll(bookBases);
                    initialTextView.setVisibility(View.INVISIBLE);
                    adapterBookBase.notifyDataSetChanged();
                }
                // UserFeedback.show( "SearchOnQueryTextChanged: " + s);
                return false;
            }
        });
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
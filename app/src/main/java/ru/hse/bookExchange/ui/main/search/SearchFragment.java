package ru.hse.bookExchange.ui.main.search;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ru.hse.bookExchange.R;
import ru.hse.bookExchange.data.DataHandler;
import ru.hse.bookExchange.data.Result;
import ru.hse.bookExchange.data.model.BookBase;
import ru.hse.bookExchange.data.model.LoggedInUser;
import ru.hse.bookExchange.ui.main.MainActivity;
import ru.hse.bookExchange.ui.main.home.BookBaseAdapter;

public class SearchFragment extends Fragment {
    List<BookBase> bookBases = new ArrayList<>();
    LoggedInUser user;
    RecyclerView rvBookBases;
    SearchView searchView;
    TextView initialTextView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View root = inflater.inflate(R.layout.fragment_search, container, false);

        initialTextView = root.findViewById(R.id.initialTextView);
        rvBookBases = root.findViewById(R.id.rvSearchBookBases);

        BookBaseAdapter adapterRecommendation = new BookBaseAdapter(bookBases);
        // Attach the adapter to the recyclerview to populate items
        rvBookBases.setAdapter(adapterRecommendation);
        // Set layout manager to position the items
        GridLayoutManager layoutManager = new GridLayoutManager(root.getContext(), 2);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        rvBookBases.setLayoutManager(layoutManager);

        user = MainActivity.getUser();

        return root;
    }

    private void doSearch(String searchStr) {
        Thread thSearch = new Thread(() -> {
            Result bookBasesResult = (new DataHandler()).getBookBases(
                    "/search?searchStr=" + searchStr, user);
            if (bookBasesResult instanceof Result.Success) {
                bookBases.clear();
                bookBases.addAll((((Result.Success<List<BookBase>>) bookBasesResult).getData()));
                if (!bookBases.isEmpty())
                    initialTextView.setVisibility(View.INVISIBLE);
                rvBookBases.post(() ->
                        Objects.requireNonNull(rvBookBases.getAdapter()).notifyDataSetChanged());
            }
        });
        thSearch.start();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_search, menu);

        MenuItem myActionMenuItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) myActionMenuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Toast like print
                doSearch(query);
                if (!searchView.isIconified()) {
                    //searchView.setIconified(true);
                }
                //myActionMenuItem.collapseActionView();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                // UserFeedback.show( "SearchOnQueryTextChanged: " + s);
                return false;
            }
        });
    }
}
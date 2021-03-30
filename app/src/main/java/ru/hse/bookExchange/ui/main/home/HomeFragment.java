package ru.hse.bookExchange.ui.main.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ru.hse.bookExchange.R;
import ru.hse.bookExchange.data.DataHandler;
import ru.hse.bookExchange.data.Result;
import ru.hse.bookExchange.data.model.BookBase;
import ru.hse.bookExchange.data.model.LoggedInUser;
import ru.hse.bookExchange.ui.main.ListShowActivity;
import ru.hse.bookExchange.ui.main.MainActivity;


public class HomeFragment extends Fragment {

    public static final String EXTRA_REQUEST_QUERY = "requestQuery";
    private ArrayList<BookBase> recommendationBookBases = new ArrayList<>();

    private ArrayList<BookBase> newestBookBases = new ArrayList<>();

    private ArrayList<BookBase> poemsBookBases = new ArrayList<>();

    private LoggedInUser user;
    private AdView adView;
    private TextView recommendationTextView;
    private TextView newestTextView;
    private TextView poemsTextView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        setRetainInstance(true);

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        recommendationTextView = root.findViewById(R.id.recommendationTextView);
        recommendationTextView.setOnClickListener((event) -> {
            Intent intent = new Intent(getContext(), ListShowActivity.class);
            intent.putExtra(EXTRA_REQUEST_QUERY, "?recommended=true");
            startActivity(intent);
        });
        newestTextView = root.findViewById(R.id.newestTextView);
        newestTextView.setOnClickListener((event) -> {
            Intent intent = new Intent(getContext(), ListShowActivity.class);
            intent.putExtra(EXTRA_REQUEST_QUERY, "?latest=true");
            startActivity(intent);
        });
        poemsTextView = root.findViewById(R.id.poemsTextView);
        poemsTextView.setOnClickListener((event) -> {
            Intent intent = new Intent(getContext(), ListShowActivity.class);
            intent.putExtra(EXTRA_REQUEST_QUERY, "/byGenres?genres=1");
            startActivity(intent);
        });

        final RecyclerView rvRecommendationBookBases = root.findViewById(R.id.rvRecommendationBookBases);
        final RecyclerView rvNewestBookBases = root.findViewById(R.id.rvNewestBookBases);
        final RecyclerView rvPoemsBookBases = root.findViewById(R.id.rvPoemsBookBases);

        for (int i = 0; i < 3; i++) {
            recommendationBookBases.add(new BookBase());
            newestBookBases.add(recommendationBookBases.get(i));
            poemsBookBases.add(recommendationBookBases.get(i));
        }

        BookBaseAdapter adapterRecommendation = new BookBaseAdapter(recommendationBookBases);
        // Attach the adapter to the recyclerview to populate items
        rvRecommendationBookBases.setAdapter(adapterRecommendation);
        // Set layout manager to position the items
        LinearLayoutManager layoutManager = new LinearLayoutManager(root.getContext());
        layoutManager.setOrientation(RecyclerView.HORIZONTAL);
        rvRecommendationBookBases.setLayoutManager(layoutManager);

        BookBaseAdapter adapterNewest = new BookBaseAdapter(newestBookBases);
        rvNewestBookBases.setAdapter(adapterNewest);
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(root.getContext());
        layoutManager1.setOrientation(RecyclerView.HORIZONTAL);
        rvNewestBookBases.setLayoutManager(layoutManager1);

        BookBaseAdapter adapterPoems = new BookBaseAdapter(poemsBookBases);
        rvPoemsBookBases.setAdapter(adapterPoems);
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(root.getContext());
        layoutManager2.setOrientation(RecyclerView.HORIZONTAL);
        rvPoemsBookBases.setLayoutManager(layoutManager2);

        user = MainActivity.getUser();

        Thread thRecommendation = new Thread(() -> {
            Result bookBasesResult = (new DataHandler()).getBookBases(
                    "?recommended=true&limit=5", user);
            if (bookBasesResult instanceof Result.Success) {
                recommendationBookBases.clear();
                recommendationBookBases.addAll((((Result.Success<List<BookBase>>) bookBasesResult).getData()));
                rvRecommendationBookBases.post(() ->
                        Objects.requireNonNull(rvRecommendationBookBases.getAdapter()).notifyDataSetChanged());
            }

            Result bookBasesNewestResult = (new DataHandler()).getBookBases(
                    "?latest=true&limit=5", user);
            if (bookBasesNewestResult instanceof Result.Success) {
                newestBookBases.clear();
                newestBookBases.addAll((((Result.Success<List<BookBase>>) bookBasesNewestResult).getData()));
                rvNewestBookBases.post(() ->
                        Objects.requireNonNull(rvNewestBookBases.getAdapter()).notifyDataSetChanged());
            }

            Result bookBasesPoemsResult = (new DataHandler()).getBookBases(
                    "/byGenres?genres=1&limit=5", user);
            if (bookBasesPoemsResult instanceof Result.Success) {
                poemsBookBases.clear();
                poemsBookBases.addAll((((Result.Success<List<BookBase>>) bookBasesPoemsResult).getData()));
                rvPoemsBookBases.post(() ->
                        Objects.requireNonNull(rvPoemsBookBases.getAdapter()).notifyDataSetChanged());
            }
        });
        thRecommendation.start();


        adView = root.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        return root;
    }

}
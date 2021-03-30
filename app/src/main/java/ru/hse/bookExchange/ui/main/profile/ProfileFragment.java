package ru.hse.bookExchange.ui.main.profile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ru.hse.bookExchange.R;
import ru.hse.bookExchange.data.DataHandler;
import ru.hse.bookExchange.data.Result;
import ru.hse.bookExchange.data.model.BookBase;
import ru.hse.bookExchange.data.model.LoggedInUser;
import ru.hse.bookExchange.ui.login.LoginActivity;
import ru.hse.bookExchange.ui.main.MainActivity;
import ru.hse.bookExchange.ui.main.home.BookBaseAdapter;

public class ProfileFragment extends Fragment {
    ImageView avatarImageVIew;
    TextView nameEditText;
    TextView usernameEditText;
    TextView roleEditText;
    LoggedInUser user;
    List<BookBase> favoriteBookBases = new ArrayList<>();
    RecyclerView rvFavoriteBookBases;
    BookBaseAdapter adapterFavorite;
    Button logoutButton;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        //setRetainInstance(true);
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        user = MainActivity.getUser();

        avatarImageVIew = root.findViewById(R.id.avatarImageView);
        Thread th = new Thread(() -> {
            if (user.getAvatarId() != null) {
                Result res = DataHandler.getAvatar(user.getAvatarId(), user);
                if (res instanceof Result.Success) {
                    final Bitmap bitmap = ((Result.Success<Bitmap>) res).getData();
                    root.post(() -> avatarImageVIew.setImageBitmap(bitmap));
                }
            }
        });
        th.start();

        nameEditText = root.findViewById(R.id.nameEditText);
        nameEditText.setText(user.getName());
        usernameEditText = root.findViewById(R.id.usernameEditText);
        usernameEditText.setText(user.getUsername());
        roleEditText = root.findViewById(R.id.roleEditText);
        roleEditText.setText(user.getRole());

        logoutButton = root.findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener((view) -> {
            Intent intent = new Intent(getContext(), LoginActivity.class);
            startActivity(intent);
            requireActivity().finish();
        });

        rvFavoriteBookBases = root.findViewById(R.id.rvFavoriteBookBases);

        for (int i = 0; i < user.getWishListIds().size(); i++) {
            favoriteBookBases.add(new BookBase());
        }

        adapterFavorite = new BookBaseAdapter(favoriteBookBases);
        // Attach the adapter to the recyclerview to populate items
        rvFavoriteBookBases.setAdapter(adapterFavorite);
        // Set layout manager to position the items
        LinearLayoutManager layoutManager = new LinearLayoutManager(root.getContext());
        layoutManager.setOrientation(RecyclerView.HORIZONTAL);
        rvFavoriteBookBases.setLayoutManager(layoutManager);

        user = MainActivity.getUser();

        return root;
    }

    private void loadFavorites() {
        Thread thFavorite = new Thread(() -> {
            favoriteBookBases.clear();
            List<Long> wishList = user.getWishListIds();
            for (int i = 0; i < wishList.size(); i++) {
                Result bookBaseResult = DataHandler.getBookBase(wishList.get(i), user);
                if (bookBaseResult instanceof Result.Success) {
                    favoriteBookBases.add((((Result.Success<BookBase>) bookBaseResult).getData()));
                    int finalI = i;
                    rvFavoriteBookBases.post(() ->
                            Objects.requireNonNull(rvFavoriteBookBases.getAdapter()).notifyDataSetChanged());
                }
            }
        });
        thFavorite.start();
    }


    @Override
    public void onResume() {
        loadFavorites();
        super.onResume();
    }
}
package ru.hse.bookExchange.ui.main;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import java.text.DecimalFormat;

import ru.hse.bookExchange.R;
import ru.hse.bookExchange.data.DataHandler;
import ru.hse.bookExchange.data.model.BookBase;
import ru.hse.bookExchange.data.model.LoggedInUser;
import ru.hse.bookExchange.ui.main.home.BookBaseAdapter;

public class BookBaseActivity extends AppCompatActivity {
    private final Object synchronizer = new Object();
    private BookBase bookBase;
    private ImageView bookPhotoImageView;
    private TextView titleTextView;
    private TextView authorTextView;
    private TextView ratingTextView;
    private TextView genreTextView;
    private TextView descriptionTextView;
    private RatingBar ratingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_base);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            bookBase = (BookBase) bundle.getSerializable(BookBaseAdapter.EXTRA_BOOK_BASE);
        }

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        ActionBar ab = getSupportActionBar();
        if (ab != null)
            ab.setDisplayHomeAsUpEnabled(true);


        bookPhotoImageView = findViewById(R.id.bookPhoto);
        bookPhotoImageView.setImageBitmap(DataHandler.getBookBasePhotoIfSaved(bookBase.getPhotoId()));

        titleTextView = findViewById(R.id.titleTextView);
        titleTextView.setText(bookBase.getTitle());

        authorTextView = findViewById(R.id.authorTextView);
        authorTextView.setText(bookBase.getAuthor());

        ratingTextView = findViewById(R.id.ratingTextView);
        ratingTextView.setText(new DecimalFormat("0.0").format(bookBase.getRating()));

        genreTextView = findViewById(R.id.genresTextView);
        StringBuilder genresStr = new StringBuilder("Genres: ");
        for (int i = 0; i < bookBase.getGenreIds().size(); i++) {
            genresStr.append(DataHandler.getGenre(bookBase.getGenreIds().get(i)));
        }
        genreTextView.setText(genresStr.toString());

        descriptionTextView = findViewById(R.id.descriptionTextView);
        descriptionTextView.setText(bookBase.getDescription());

        ratingBar = findViewById(R.id.ratingBar);
        ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            bookBase.addRate(rating);
            ratingTextView.setText(new DecimalFormat("0.0").format(bookBase.getRating()));
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        System.out.println(MainActivity.getUser().getWishListIds());
        getMenuInflater().inflate(R.menu.book_base_activity_action_bar, menu);
        if (MainActivity.getUser().getWishListIds().contains(bookBase.getId())) {
            menu.findItem(R.id.favorite).setIcon(ContextCompat.getDrawable(this,
                    R.drawable.baseline_favorite_white_24dp));
        } else {
            menu.findItem(R.id.favorite).setIcon(ContextCompat.getDrawable(this,
                    R.drawable.baseline_favorite_border_white_24dp));
        }
        return true;//super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.favorite:
                LoggedInUser user = MainActivity.getUser();
                System.out.println(user.getWishListIds());
                Thread wishListThread;
                if (user.getWishListIds().contains(bookBase.getId())) {
                    wishListThread = new Thread(() -> {
                        synchronized (synchronizer) {
                            user.getWishListIds().remove(bookBase.getId());
                            BookBaseActivity.this.runOnUiThread(() -> item.setIcon(ContextCompat.getDrawable(this,
                                    R.drawable.baseline_favorite_border_white_24dp)));
                            if (!DataHandler.removeBookBaseFromWishList(bookBase.getId(), user)) {
                                user.getWishListIds().add(bookBase.getId());
                                BookBaseActivity.this.runOnUiThread(() -> item.setIcon(ContextCompat.getDrawable(this,
                                        R.drawable.baseline_favorite_white_24dp)));
                            }

                        }
                    });
                } else {
                    wishListThread = new Thread(() -> {
                        synchronized (synchronizer) {
                            user.getWishListIds().add(bookBase.getId());
                            BookBaseActivity.this.runOnUiThread(() -> item.setIcon(ContextCompat.getDrawable(this,
                                    R.drawable.baseline_favorite_white_24dp)));
                            if (!DataHandler.addBookBaseToWishList(bookBase.getId(), user)) {
                                user.getWishListIds().remove(bookBase.getId());
                                BookBaseActivity.this.runOnUiThread(() -> item.setIcon(ContextCompat.getDrawable(this,
                                        R.drawable.baseline_favorite_border_white_24dp)));
                            }
                        }
                    });
                }
                wishListThread.start();
                return true;

            case android.R.id.home:
                finish();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
}
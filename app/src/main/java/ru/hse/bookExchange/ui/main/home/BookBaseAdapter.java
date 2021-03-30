package ru.hse.bookExchange.ui.main.home;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import ru.hse.bookExchange.R;
import ru.hse.bookExchange.data.DataHandler;
import ru.hse.bookExchange.data.Result;
import ru.hse.bookExchange.data.model.BookBase;
import ru.hse.bookExchange.ui.main.BookBaseActivity;
import ru.hse.bookExchange.ui.main.MainActivity;

public class BookBaseAdapter extends
        RecyclerView.Adapter<BookBaseAdapter.ViewHolder> {
    public static final String EXTRA_BOOK_BASE = "bookBase";
    private List<BookBase> mBookBases;
    private Context context;

    // Pass in the contact array into the constructor
    public BookBaseAdapter(List<BookBase> bookBases) {
        mBookBases = bookBases;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View bookBaseView = inflater.inflate(R.layout.item_book_base, parent, false);


        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(bookBaseView);
        return viewHolder;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the data model based on position
        BookBase bookBase = mBookBases.get(position);

        holder.bookBaseCard.setOnClickListener(v -> {
            final Intent intent = new Intent(context, BookBaseActivity.class);
            intent.putExtra(EXTRA_BOOK_BASE, bookBase);
            context.startActivity(intent);
        });

        // Set item views based on your views and data model
        TextView authorTextView = holder.authorTextView;
        authorTextView.setText(bookBase.getAuthor());
        TextView titleTextView = holder.titleTextView;
        titleTextView.setText(bookBase.getTitle());
        ImageView photoView = holder.photoView;

        TextView ratingTextView = holder.ratingTextView;

        ratingTextView.setText(new DecimalFormat("0.0").format(bookBase.getRating()));

        Long photoId = bookBase.getPhotoId();
        if (photoId == null)
            return;

        if (bookBase.getPhoto() != null) {
            photoView.setImageBitmap(bookBase.getPhoto());
            return;
        }
        Bitmap picTry = DataHandler.getBookBasePhotoIfSaved(photoId);
        if (picTry != null) {
            bookBase.setPhoto(picTry);
            photoView.setImageBitmap(picTry);
            return;
        }

        AtomicReference<Bitmap> pic = new AtomicReference<>();
        Thread pictureLoaderThread = new Thread(() -> {
            Result pictureResult = DataHandler.getBookBasePhoto(photoId, MainActivity.getUser());
            if (pictureResult instanceof Result.Success) {
                pic.set(((Result.Success<Bitmap>) pictureResult).getData());
            }

        });
        pictureLoaderThread.start();
        try {
            pictureLoaderThread.join();
            bookBase.setPhoto(pic.get());
            photoView.setImageBitmap(pic.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return mBookBases.size();
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView authorTextView;
        public TextView titleTextView;
        public TextView ratingTextView;
        public ImageView photoView;
        public CardView bookBaseCard;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            authorTextView = itemView.findViewById(R.id.authorTextView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            photoView = itemView.findViewById(R.id.photoView);
            ratingTextView = itemView.findViewById(R.id.ratingTextView);
            bookBaseCard = itemView.findViewById(R.id.bookBaseCard);
        }


    }
}

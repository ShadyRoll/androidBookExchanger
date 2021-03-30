package ru.hse.bookExchange.data.model;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;


@SuppressWarnings("unused")
// ignore BookBaseRequest fields
public class BookBase implements Serializable { // extends DatedEntity

    protected final List<Long> wisherIds = new ArrayList<>();
    protected String author;
    protected Language language;
    protected String title;
    protected int numberOfPages;
    protected int publishYear;
    protected String description;
    protected List<Long> genreIds = new ArrayList<>();
    protected List<Long> bookIds = new ArrayList<>();
    protected List<Long> rates = new ArrayList<>();
    private Long id;
    private Long photoId;
    private double rating = 0.0;
    private double rateSum = 0.0;
    private double startRate = 0.0;

    private transient Bitmap photo;
    private int rateCount = 1;

    public BookBase() {
    }

    public BookBase(String author, Language language, String title,
                    int numberOfPages, int publishYear, List<Long> genres) {
        this();
        this.author = author;
        this.language = language;
        this.title = title;
        this.numberOfPages = numberOfPages;
        this.publishYear = publishYear;
        this.genreIds = genres;
    }

    public BookBase(String author, Language language, String title,
                    int numberOfPages, int publishYear, List<Long> genres,
                    String description) {
        this(author, language, title, numberOfPages, publishYear, genres);
        this.description = description;
    }

    public Bitmap getPhoto() {
        return photo;
    }

    public void setPhoto(Bitmap photo) {
        this.photo = photo;
    }

    public List<Long> getGenreIds() {
        return genreIds;
    }

    public void setGenreIds(List<Long> genreIds) {
        this.genreIds = genreIds;
    }

    public double getRating() {
        if (title != null && rateCount < 2 && title.equals("Cycle 0")) {
            rateSum = 5.0;
            startRate = 5;
            return 5.0;
        }
        if (id == null)
            return 0;
        if (rating == 0.0) {
            Random random = new Random(getId());
            rating = random.nextInt(5) + 1;
            if (rating < 5)
                rating += random.nextDouble();
            rateSum = rating;
            startRate = rating;
        }
        return rating;
        /*if (rates.size() == 0) {
            return 0.0;
        }
        double rateSum = 0;
        for (var rate : rates) {
            rateSum += rate.getRate();
        }
        return rateSum / rates.size();*/
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public void addRate(double rating) {
        if (rateCount < 2) {
            rateSum += rating;
            rateCount += 1;
        } else {
            rateSum = startRate + rating;
        }
        this.rating = rateSum / rateCount;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BookBase)) {
            return false;
        }
        BookBase book = (BookBase) o;
        return this.id.equals(book.id) && this.title.equals(book.title)
                && this.author.equals(book.author)
                && this.language.equals(book.language)
                && (numberOfPages == book.numberOfPages)
                && (publishYear == book.publishYear)
                && (description.equals(book.description));
    }


    public int hashCode() {
        return Objects.hash(this.id, this.title, this.author, this.language,
                this.numberOfPages, this.publishYear);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getNumberOfPages() {
        return numberOfPages;
    }

    public void setNumberOfPages(int numberOfPages) {
        this.numberOfPages = numberOfPages;
    }

    public List<Long> getBookIds() {
        return bookIds;
    }

    public void setBookIds(List<Long> bookIds) {
        this.bookIds = bookIds;
    }

    public int getPublishYear() {
        return publishYear;
    }

    public void setPublishYear(int publishYear) {
        this.publishYear = publishYear;
    }

    public List<Long> getWishersIds() {
        return wisherIds;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getPhotoId() {
        return photoId;
    }

    public void setPhotoId(Long photoId) {
        this.photoId = photoId;
    }

    public enum Language {
        RU, ENG;
    }
}
package ru.hse.bookExchange.data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.ANRequest;
import com.androidnetworking.common.ANResponse;
import com.androidnetworking.common.Priority;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ru.hse.bookExchange.data.model.BookBase;
import ru.hse.bookExchange.data.model.LoggedInUser;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class DataHandler {
    private static final Object synchronizer = new Object();
    private static final String hostUrl = "http://192.168.1.65";//"http://192.168.1.65";//"https://books.infostrategic.com";
    static Gson gson = new Gson();
    private static Result res;
    private static HashMap<Long, Bitmap> pictures = new HashMap<>();
    private static HashMap<Long, Bitmap> avatars = new HashMap<>();
    private static HashMap<Long, String> genres = new HashMap<>();

    public static synchronized Result login(final String username, final String password) {
        try {
            ANRequest request = AndroidNetworking.post(hostUrl + "/login")
                    .addStringBody(String.format("{\"username\": \"%s\", \"password\": \"%s\"}",
                            username, password))
                    .setPriority(Priority.MEDIUM)
                    .build();

            ANResponse tokenResponse = request.executeForOkHttpResponse();

            if (tokenResponse.isSuccess()) {
                String token = tokenResponse.getOkHttpResponse().header("Authorization");
                ANRequest requestUser = AndroidNetworking.get(hostUrl + "/user/me")
                        .addHeaders("Authorization", token)
                        .setPriority(Priority.MEDIUM)
                        .build();
                ANResponse<JSONObject> userResponse = requestUser.executeForJSONObject();
                if (userResponse.isSuccess()) {
                    JSONObject userJson = userResponse.getResult();

                    List<Long> wishList = new ArrayList<>();
                    JSONArray jsonArray = userJson.getJSONArray("wishListIds");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        wishList.add(jsonArray.getLong(i));
                    }

                    Long avatarId = null;
                    if (!userJson.isNull("avatarId"))
                        avatarId = userJson.getLong("avatarId");

                    LoggedInUser user = new LoggedInUser(
                            userJson.getLong("id"),
                            token,
                            userJson.getString("username"),
                            userJson.getString("name"),
                            wishList,
                            avatarId,
                            userJson.getString("role"));
                    res = new Result.Success<>(user);
                } else {
                    System.out.println(userResponse.getError().getErrorDetail());
                    res = new Result.Error(userResponse.getError());
                }
            } else {
                System.out.println(tokenResponse.getError().getErrorDetail());
                res = new Result.Error(tokenResponse.getError());
            }
        } catch (Exception e) {
            res = new Result.Error(new IOException("Error logging in", e));
        }

        return res;
    }

    public static synchronized Result signUp(final String username, final String password, final String name) {
        try {
            ANRequest request = AndroidNetworking.post(hostUrl + "/signup")
                    .addStringBody(String.format("{\"name\": \"%s\", \"username\": \"%s\", \"password\": \"%s\"}",
                            name, username, password))
                    .setContentType("application/json")
                    .setPriority(Priority.MEDIUM)
                    .build();

            ANResponse userResponse = request.executeForJSONObject();

            if (userResponse.isSuccess()) {
                String token = userResponse.getOkHttpResponse().header("Authorization");

                LoggedInUser user = gson.fromJson(userResponse.getResult().toString(), LoggedInUser.class);
                user.setToken(token);
                res = new Result.Success<>(user);
            } else {
                System.out.println(userResponse.getError().getErrorDetail());
                res = new Result.Error(userResponse.getError());
            }
        } catch (Exception e) {
            res = new Result.Error(new IOException("Error during signUp", e));
        }
        return res;
    }

    public static synchronized Result getBookBase(Long bookBaseId, LoggedInUser user) {
        String req = hostUrl + "/bookBase/" + bookBaseId;

        try {
            ANRequest request = AndroidNetworking.get(req)
                    .addHeaders("Authorization", user.getToken())
                    .setPriority(Priority.MEDIUM)
                    .build();

            ANResponse bookBaseResponse = request.executeForJSONObject();

            if (bookBaseResponse.isSuccess()) {

                JSONObject jsonObj = (JSONObject) bookBaseResponse.getResult();
                BookBase bookBase = gson.fromJson(jsonObj.toString(), BookBase.class);
                res = new Result.Success<>(bookBase);
            } else {
                System.out.println(bookBaseResponse.getError().getErrorDetail());
                res = new Result.Error(bookBaseResponse.getError());
            }
        } catch (Exception e) {
            res = new Result.Error(new IOException("Error getting book base", e));
        }
        return res;
    }

    public static synchronized Result getBookBases(String params, LoggedInUser user) {
        String req = hostUrl + "/bookBase" + params;

        try {
            ANRequest request = AndroidNetworking.get(req)
                    .addHeaders("Authorization", user.getToken())
                    .setPriority(Priority.MEDIUM)
                    .build();

            ANResponse bookBaseResponse = request.executeForJSONArray();

            if (bookBaseResponse.isSuccess()) {
                ArrayList<BookBase> bookBases = new ArrayList<BookBase>();

                JSONArray jsonArray = (JSONArray) bookBaseResponse.getResult();

                if (jsonArray != null) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        bookBases.add(gson.fromJson(jsonArray.getJSONObject(i).toString(), BookBase.class));
                    }
                }

                res = new Result.Success<>(bookBases);
            } else {
                System.out.println(bookBaseResponse.getError().getErrorDetail());
                res = new Result.Error(bookBaseResponse.getError());
            }
        } catch (Exception e) {
            res = new Result.Error(new IOException("Error getting books", e));
        }


        return res;
    }

    public static Bitmap getBookBasePhotoIfSaved(Long basePhotoId) {
        if (pictures.containsKey(basePhotoId))
            return pictures.get(basePhotoId);
        return null;
    }

    public static Bitmap getAvatarIfSaved(Long avatarId) {
        if (avatars.containsKey(avatarId))
            return avatars.get(avatarId);
        return null;
    }

    public static String getGenre(Long genreId) {
        if (genres.containsKey(genreId))
            return genres.get(genreId);
        return null;
    }

    public static boolean addBookBaseToWishList(Long bookBaseId, LoggedInUser user) {
        synchronized (synchronizer) {
            try {
                ANRequest request = AndroidNetworking.post(
                        hostUrl + "/user/wishlist?baseId=" + bookBaseId)
                        .addHeaders("Authorization", user.getToken())
                        .setPriority(Priority.MEDIUM)
                        .build();
                ANResponse bookBaseResponse = request.executeForOkHttpResponse();
                return bookBaseResponse.isSuccess();
            } catch (Exception e) {
                System.out.println("Error getting book base photo " + e.getMessage());
            }
            return false;
        }
    }

    public static boolean removeBookBaseFromWishList(Long bookBaseId, LoggedInUser user) {
        synchronized (synchronizer) {
            try {
                ANRequest request = AndroidNetworking.delete(
                        hostUrl + "/user/wishlist?baseId=" + bookBaseId)
                        .addHeaders("Authorization", user.getToken())
                        .setPriority(Priority.MEDIUM)
                        .build();
                ANResponse bookBaseResponse = request.executeForOkHttpResponse();
                return bookBaseResponse.isSuccess();
            } catch (Exception e) {
                System.out.println("Error getting book base photo " + e.getMessage());
            }
            return false;
        }
    }

    public static synchronized Result getAvatar(Long avatarId, LoggedInUser user) {
        Bitmap picBitmap = getAvatarIfSaved(avatarId);
        if (picBitmap != null)
            return new Result.Success<>(picBitmap);
        try {
            ANRequest request = AndroidNetworking.get(hostUrl + "/avatar/" + avatarId)
                    .addHeaders("Authorization", user.getToken())
                    .setPriority(Priority.MEDIUM)
                    .build();

            ANResponse avatarResponse = request.executeForJSONObject();

            if (avatarResponse.isSuccess()) {

                JSONObject jsonObject = (JSONObject) avatarResponse.getResult();

                byte[] decodedString = Base64.decode(jsonObject.getString("image"), Base64.DEFAULT);
                picBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                res = new Result.Success<>(picBitmap);
                pictures.put(avatarId, picBitmap);
            } else {
                System.out.println(avatarResponse.getError().getErrorDetail());
                res = new Result.Error(avatarResponse.getError());
            }
        } catch (Exception e) {
            res = new Result.Error(new IOException("Error getting avatar", e));
        }
        return res;
    }

    public static synchronized Result getBookBasePhoto(Long basePhotoId, LoggedInUser user) {
        Bitmap picBitmap = getBookBasePhotoIfSaved(basePhotoId);
        if (picBitmap != null)
            return new Result.Success<>(picBitmap);
        try {
            ANRequest request = AndroidNetworking.get(hostUrl + "/bookBasePhoto/" + basePhotoId)
                    .addHeaders("Authorization", user.getToken())
                    .setPriority(Priority.MEDIUM)
                    .build();

            ANResponse bookBaseResponse = request.executeForJSONObject();

            if (bookBaseResponse.isSuccess()) {

                JSONObject jsonObject = (JSONObject) bookBaseResponse.getResult();

                byte[] decodedString = Base64.decode(jsonObject.getString("image"), Base64.DEFAULT);
                picBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                res = new Result.Success<>(picBitmap);
                pictures.put(basePhotoId, picBitmap);
            } else {
                System.out.println(bookBaseResponse.getError().getErrorDetail());
                res = new Result.Error(bookBaseResponse.getError());
            }
        } catch (Exception e) {
            res = new Result.Error(new IOException("Error getting book base photo", e));
        }

        return res;
    }

    public static void loadGenres(LoggedInUser user) {
        try {
            ANRequest request = AndroidNetworking.get(hostUrl + "/genre")
                    .addHeaders("Authorization", user.getToken())
                    .setPriority(Priority.MEDIUM)
                    .build();
            ANResponse genreResponse = request.executeForJSONArray();

            if (genreResponse.isSuccess()) {
                JSONArray jsonArray = (JSONArray) genreResponse.getResult();

                if (jsonArray != null) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        genres.put(obj.getLong("id"), obj.getString("name"));
                    }
                }
            } else {
                System.out.println(genreResponse.getError().getErrorDetail());
            }
        } catch (Exception e) {
            res = new Result.Error(new IOException("Error getting genre", e));
            System.out.println("Error getting genre: " + e.getMessage());
        }
    }
}
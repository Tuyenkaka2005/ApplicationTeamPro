package com.budgetwise.ad;

import android.content.Context;
import android.content.SharedPreferences;

public class UserSession {
    public static final String KEY_NAME = "name";
    static final String PREF_NAME = "UserSession";
    private static final String KEY_USER_ID = "userId";
    public static final String KEY_EMAIL = "user_email";
    public static final String KEY_CURRENCY = "currency";
    public static final String KEY_DARK_MODE = "dark_mode";

    public static void setCurrentUser(Context context, User user) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_USER_ID, user.getUserId()).apply();
    }

    public static String getCurrentUserId(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_USER_ID, null);
    }

    public static void clearSession(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().clear().apply();
    }
    public static String getCurrentUserName(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_NAME, null);
    }

    public static void clearUser(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Xóa tất cả các key liên quan đến người dùng
        editor.remove(KEY_USER_ID);
        editor.remove(KEY_NAME);
        editor.remove(KEY_EMAIL);
        editor.remove(KEY_CURRENCY);
        editor.remove(KEY_DARK_MODE);

        editor.apply();

    }
}
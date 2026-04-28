package com.revy.talkblogback.auth;

import com.revy.talkblogback.pojo.response.UserProfile;

public final class AuthContext {

    private static final ThreadLocal<UserProfile> CURRENT_USER = new ThreadLocal<>();

    private AuthContext() {
    }

    public static void set(UserProfile userProfile) {
        CURRENT_USER.set(userProfile);
    }

    public static UserProfile get() {
        return CURRENT_USER.get();
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}

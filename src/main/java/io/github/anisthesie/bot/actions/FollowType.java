package io.github.anisthesie.bot.actions;

public enum FollowType {

    HASHTAG, ACCOUNT, LOCATION;

    public static FollowType fromString(String type) {
        if (type.equalsIgnoreCase("hashtag")) return HASHTAG;
        if (type.equalsIgnoreCase("account")) return ACCOUNT;
        if (type.equalsIgnoreCase("location")) return LOCATION;
        return null;
    }

}

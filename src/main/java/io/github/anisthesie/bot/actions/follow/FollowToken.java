package io.github.anisthesie.bot.actions.follow;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

public class FollowToken {

    @Getter
    @Setter
    private FollowType type;

    @Getter
    @Setter
    private String value;

    public FollowToken(FollowType type, String value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FollowToken that = (FollowToken) o;
        return type == that.type && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }
}

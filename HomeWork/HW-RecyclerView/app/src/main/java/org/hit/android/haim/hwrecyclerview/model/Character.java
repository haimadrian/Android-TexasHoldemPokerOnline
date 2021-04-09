package org.hit.android.haim.hwrecyclerview.model;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @author Haim Adrian
 * @since 07-Apr-21
 */
public class Character {
    private String name;
    private String subName;
    private int imageResId;

    public Character(@NonNull String name, @DrawableRes int imageResId) {
        this(name, imageResId, null);
    }

    public Character(@NonNull String name, @DrawableRes int imageResId, @Nullable String subName) {
        this.name = name;
        this.imageResId = imageResId;
        this.subName = subName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @DrawableRes
    public int getImageResId() {
        return imageResId;
    }

    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }

    public String getSubName() {
        return subName;
    }

    public void setSubName(String subName) {
        this.subName = subName;
    }
}

package org.hit.android.haim.hwrecyclerview.model;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.RawRes;

import java.util.List;

/**
 * @author Haim Adrian
 * @since 07-Apr-21
 */
public class TvSeries {
    private final String id;
    private String name;
    private int imageResId;
    private int themeSongResId;
    private String date;
    private List<Character> characters;

    public TvSeries(@NonNull String id, @NonNull String name, @DrawableRes int imageResId, @RawRes int themeSongResId, @NonNull String date, @NonNull List<Character> characters) {
        this.id = id;
        this.name = name;
        this.imageResId = imageResId;
        this.themeSongResId = themeSongResId;
        this.date = date;
        this.characters = characters;

        characters.add(0, new Character(name, imageResId, date));
    }

    public List<Character> getCharacters() {
        return characters;
    }

    public void setCharacters(List<Character> characters) {
        this.characters = characters;
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

    public void setImageResId(@DrawableRes int imageResId) {
        this.imageResId = imageResId;
    }

    @RawRes
    public int getThemeSongResId() {
        return themeSongResId;
    }

    public void setThemeSongResId(@RawRes int themeSongResId) {
        this.themeSongResId = themeSongResId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getId() {
        return id;
    }
}

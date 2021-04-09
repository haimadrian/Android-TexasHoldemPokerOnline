package org.hit.android.haim.hwrecyclerview.model;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import java.util.List;

/**
 * @author Haim Adrian
 * @since 07-Apr-21
 */
public class TvSeries {
    private String name;
    private int imageResId;
    private String date;
    private List<Character> characters;

    public TvSeries(@NonNull String name, @DrawableRes int imageResId, @NonNull String date, @NonNull List<Character> characters) {
        this.name = name;
        this.imageResId = imageResId;
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

    public int getImageResId() {
        return imageResId;
    }

    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}

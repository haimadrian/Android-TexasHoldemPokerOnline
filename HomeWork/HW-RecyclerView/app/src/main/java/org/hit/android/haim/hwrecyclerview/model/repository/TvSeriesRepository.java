package org.hit.android.haim.hwrecyclerview.model.repository;

import org.hit.android.haim.hwrecyclerview.R;
import org.hit.android.haim.hwrecyclerview.model.TvSeries;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Haim Adrian
 * @since 07-Apr-21
 */
public class TvSeriesRepository {
    private static final TvSeriesRepository instance = new TvSeriesRepository();

    private final Map<String, TvSeries> tvSeriesByName = new HashMap<>();

    private TvSeriesRepository() {
        init();
    }

    public static TvSeriesRepository getInstance() {
        return instance;
    }

    public List<TvSeries> getAll() {
        List<TvSeries> tvSeries = new ArrayList<>(tvSeriesByName.values());
        tvSeries.sort(Comparator.comparing(TvSeries::getName));
        return tvSeries;
    }

    private void init() {
        TvSeries tvSeries = new TvSeries("Dragon Ball Z", R.drawable.dbz_title, "April 26, 1989", CharacterRepository.getInstance().getCharactersOf("dbz"));
        tvSeriesByName.put("dbz", tvSeries);
        tvSeries = new TvSeries("Yugi Oh", R.drawable.yugioh_title, "September 3, 1999", CharacterRepository.getInstance().getCharactersOf("yugioh"));
        tvSeriesByName.put("yugioh", tvSeries);
        tvSeries = new TvSeries("The Muppet Show", R.drawable.muppet_title, "September 5, 1976", CharacterRepository.getInstance().getCharactersOf("muppet"));
        tvSeriesByName.put("muppet", tvSeries);
        tvSeries = new TvSeries("X-Men", R.drawable.xmen_title, "October 31, 1992", CharacterRepository.getInstance().getCharactersOf("xmen"));
        tvSeriesByName.put("xmen", tvSeries);
        tvSeries = new TvSeries("Naruto", R.drawable.naruto_title, "September 21, 1999", CharacterRepository.getInstance().getCharactersOf("naruto"));
        tvSeriesByName.put("naruto", tvSeries);
        tvSeries = new TvSeries("Pokemon", R.drawable.pokemon_title, "April 1, 1997", CharacterRepository.getInstance().getCharactersOf("pokemon"));
        tvSeriesByName.put("pokemon", tvSeries);
    }
}

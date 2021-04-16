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

    public TvSeries get(String id) {
        return tvSeriesByName.get(id);
    }

    private void init() {
        TvSeries tvSeries = new TvSeries("dbz", "Dragon Ball Z", R.drawable.dbz_title, R.raw.dbz, "April 26, 1989", CharacterRepository.getInstance().getCharactersOf("dbz"));
        tvSeriesByName.put(tvSeries.getId(), tvSeries);
        tvSeries = new TvSeries("yugioh", "Yugi Oh", R.drawable.yugioh_title, R.raw.yugioh, "September 3, 1999", CharacterRepository.getInstance().getCharactersOf("yugioh"));
        tvSeriesByName.put(tvSeries.getId(), tvSeries);
        tvSeries = new TvSeries("muppet", "The Muppet Show", R.drawable.muppet_title, R.raw.muppets, "September 5, 1976", CharacterRepository.getInstance().getCharactersOf("muppet"));
        tvSeriesByName.put(tvSeries.getId(), tvSeries);
        tvSeries = new TvSeries("xmen", "X-Men", R.drawable.xmen_title, R.raw.xmen, "October 31, 1992", CharacterRepository.getInstance().getCharactersOf("xmen"));
        tvSeriesByName.put(tvSeries.getId(), tvSeries);
        tvSeries = new TvSeries("naruto", "Naruto", R.drawable.naruto_title, R.raw.naruto, "September 21, 1999", CharacterRepository.getInstance().getCharactersOf("naruto"));
        tvSeriesByName.put(tvSeries.getId(), tvSeries);
        tvSeries = new TvSeries("pokemon", "Pokemon", R.drawable.pokemon_title, R.raw.pokemon, "April 1, 1997", CharacterRepository.getInstance().getCharactersOf("pokemon"));
        tvSeriesByName.put(tvSeries.getId(), tvSeries);
    }
}

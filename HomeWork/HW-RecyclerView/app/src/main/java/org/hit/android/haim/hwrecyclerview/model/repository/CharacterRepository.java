package org.hit.android.haim.hwrecyclerview.model.repository;

import androidx.annotation.NonNull;

import org.hit.android.haim.hwrecyclerview.R;
import org.hit.android.haim.hwrecyclerview.model.Character;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Haim Adrian
 * @since 07-Apr-21
 */
public class CharacterRepository {
    private static final CharacterRepository instance = new CharacterRepository();

    private final Map<String, List<Character>> charactersPerTvSeries = new HashMap<>();

    private CharacterRepository() {
        init();
    }

    public static CharacterRepository getInstance() {
        return instance;
    }

    public List<Character> getCharactersOf(@NonNull String tvSeriesName) {
        // Sort tvs by name
        return new ArrayList<>(charactersPerTvSeries.get(tvSeriesName.toLowerCase()));
    }

    private void init() {
        initDbz();
        initYugiOh();
        initMuppet();
        initXmen();
        initNaruto();
        initPokemon();
    }

    private void initDbz() {
        List<Character> characters = new ArrayList<>();
        characters.add(new Character("Vegeta", R.drawable.dbz_vegeta));
        characters.add(new Character("Son Goku", R.drawable.dbz_goku));
        characters.add(new Character("Trunks", R.drawable.dbz_trunks));
        characters.add(new Character("Gohan", R.drawable.dbz_gohan));
        characters.add(new Character("Piccolo", R.drawable.dbz_piccolo));
        characters.add(new Character("Majin Buu", R.drawable.dbz_buu));
        characters.add(new Character("Cell", R.drawable.dbz_cell));
        characters.add(new Character("Android 17", R.drawable.dbz_android17));
        characters.add(new Character("Android 18", R.drawable.dbz_android18));
        characters.add(new Character("Frieza", R.drawable.dbz_frieza));
        charactersPerTvSeries.put("dbz", characters);
    }

    private void initYugiOh() {
        List<Character> characters = new ArrayList<>();
        characters.add(new Character("Yugi Muto", R.drawable.yugioh_yugimuto));
        characters.add(new Character("Yami Yugi", R.drawable.yugioh_yamiyugi));
        characters.add(new Character("Seto Kaiba", R.drawable.yugioh_setokaiba));
        characters.add(new Character("Joey Wheeler", R.drawable.yugioh_joeywheeler));
        characters.add(new Character("Mai Valentine", R.drawable.yugioh_maivalentine));
        characters.add(new Character("Ryou Bakura", R.drawable.yugioh_ryoubakura));
        characters.add(new Character("Maximillion Pegasus", R.drawable.yugioh_maximillionpagasus));
        charactersPerTvSeries.put("yugioh", characters);
    }

    private void initMuppet() {
        List<Character> characters = new ArrayList<>();
        characters.add(new Character("Kermit The Frog", R.drawable.muppet_kermitthefrog));
        characters.add(new Character("Miss Piggy", R.drawable.muppet_misspiggy));
        characters.add(new Character("Big Bird", R.drawable.muppet_bigbird));
        characters.add(new Character("Statler and Waldorf", R.drawable.muppet_statler_and_waldorf));
        characters.add(new Character("Animal (Muppet)", R.drawable.muppet_animal));
        charactersPerTvSeries.put("muppet", characters);
    }

    private void initXmen() {
        List<Character> characters = new ArrayList<>();
        characters.add(new Character("Cyclops", R.drawable.xmen_cyclops));
        characters.add(new Character("Jean Grey", R.drawable.xmen_jeangrey));
        characters.add(new Character("Wolverine", R.drawable.xmen_wolverine));
        characters.add(new Character("Storm", R.drawable.xmen_storm));
        characters.add(new Character("Rogue", R.drawable.xmen_rogue));
        characters.add(new Character("Charles Xavier", R.drawable.xmen_charlesxavier));
        characters.add(new Character("Magneto", R.drawable.xmen_magneto));
        charactersPerTvSeries.put("xmen", characters);
    }

    private void initNaruto() {
        List<Character> characters = new ArrayList<>();
        characters.add(new Character("Sasuke Ochiha", R.drawable.naruto_sasukeuchiha));
        characters.add(new Character("Orochimaru", R.drawable.naruto_orochimaru));
        characters.add(new Character("Naruto Uzumaki", R.drawable.naruto_narutouzumaki));
        characters.add(new Character("Sakura Haruno", R.drawable.naruto_sakuraharuno));
        characters.add(new Character("Kakashi Hatake", R.drawable.naruto_kakashihatake));
        characters.add(new Character("Jiraiya Shippuden", R.drawable.naruto_jiraiyashippuden));
        charactersPerTvSeries.put("naruto", characters);
    }

    private void initPokemon() {
        List<Character> characters = new ArrayList<>();
        characters.add(new Character("Ash Ketchum", R.drawable.pokemon_ashketchum));
        characters.add(new Character("Gary Oak", R.drawable.pokemon_garyoak));
        characters.add(new Character("Misty", R.drawable.pokemon_misty));
        characters.add(new Character("Brock", R.drawable.pokemon_brock));
        characters.add(new Character("Pikachu", R.drawable.pokemon_pikachu));
        characters.add(new Character("Charmander", R.drawable.pokemon_charmander));
        characters.add(new Character("Charmeleon", R.drawable.pokemon_charmeleon));
        characters.add(new Character("Charizard", R.drawable.pokemon_charizard));
        characters.add(new Character("Mega Charizard X", R.drawable.pokemon_megacharizardx));
        characters.add(new Character("Mega Charizard Y", R.drawable.pokemon_megacharizardy));
        charactersPerTvSeries.put("pokemon", characters);
    }
}

package org.hit.android.haim.hwrecyclerview.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.animation.OvershootInterpolator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.hit.android.haim.hwrecyclerview.R;
import org.hit.android.haim.hwrecyclerview.model.TvSeries;
import org.hit.android.haim.hwrecyclerview.model.repository.TvSeriesRepository;

import java.util.List;

import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView tvSeriesRecyclerView = findViewById(R.id.tvSeriesRecyclerView);
        tvSeriesRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        tvSeriesRecyclerView.setLayoutManager(layoutManager);
        tvSeriesRecyclerView.setItemAnimator(new SlideInUpAnimator(new OvershootInterpolator(1f)));

        List<TvSeries> data = TvSeriesRepository.getInstance().getAll();
        TvSeriesCardAdapter adapter = new TvSeriesCardAdapter(data, this, character -> {
            Uri uriUrl = Uri.parse("https://www.google.com/search?q=" + character.getName().replace(' ', '+'));
            Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
            startActivity(launchBrowser);
        });
        tvSeriesRecyclerView.setAdapter(adapter);
    }
}
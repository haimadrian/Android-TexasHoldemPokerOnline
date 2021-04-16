package org.hit.android.haim.chat.client.fragment;

import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.android.material.snackbar.Snackbar;

import org.hit.android.haim.chat.client.R;
import org.hit.android.haim.chat.client.activity.MainActivity;
import org.hit.android.haim.chat.client.bean.Channel;
import org.hit.android.haim.chat.client.web.ChatWebService;

import java.io.IOException;
import java.util.List;

import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;
import jp.wasabeef.recyclerview.animators.FlipInTopXAnimator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class FragmentLobby extends Fragment {

    private TextView textViewUserName;
    private TextView textViewUserEmail;

    private ChannelCardAdapter channelCardAdapter;
    private MainActivity mainActivity;
    private View fragmentView;

    public FragmentLobby() {
        Log.d("Lifecycle", this.toString() + ".new");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Lifecycle", this.toString() + ".onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lobby, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fragmentView = view;
        textViewUserName = view.findViewById(R.id.textViewUserName);
        textViewUserEmail = view.findViewById(R.id.textViewUserEmail);

        mainActivity = (MainActivity) getActivity();

        if (mainActivity != null && mainActivity.getUser() != null) {
            textViewUserName.setText(mainActivity.getUser().getName());
            textViewUserEmail.setText(mainActivity.getUser().getId());
        }

        ImageView imageViewAdd = view.findViewById(R.id.imageViewAdd);
        imageViewAdd.setOnClickListener(this::onAddButtonClicked);

        RecyclerView channelsRecyclerView = view.findViewById(R.id.channelsRecyclerView);
        channelsRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mainActivity, LinearLayoutManager.VERTICAL, false);
        channelsRecyclerView.setLayoutManager(layoutManager);
        channelsRecyclerView.setItemAnimator(new FlipInTopXAnimator(new OvershootInterpolator()));

        ChatWebService.getInstance().getApi().getAllChannels().enqueue(new Callback<JsonNode>() {
            @Override
            public void onResponse(Call<JsonNode> call, Response<JsonNode> response) {
                if (!response.isSuccessful()) {
                    mainActivity.serverErrorHandler(view, response);
                } else {
                    JsonNode body = response.body();
                    List<Channel> data = null;
                    try {
                        ObjectReader reader = ChatWebService.getInstance().getObjectMapper().readerFor(new TypeReference<List<Channel>>() {});
                        data = reader.readValue(body);
                    } catch (IOException e) {
                        Log.e("Web", "Failed parsing response. Response was: " + body, e);
                        Snackbar.make(view, "Unable to load channels. Reason: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                    }

                    if (data != null) {
                        initializeChannelsCardAdapter(data, view, channelsRecyclerView);
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonNode> call, Throwable t) {
                Log.e("DeleteChannel", "Error has occurred while trying to load channels", t);
                Snackbar.make(view, "Something went wrong while loading channels: " + t.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void initializeChannelsCardAdapter(List<Channel> data, View root, RecyclerView channelsRecyclerView) {
        channelCardAdapter = new ChannelCardAdapter(data,
            mainActivity,
            (imageView, channelPosition) -> {
                Channel channel = channelCardAdapter.getChannels().get(channelPosition);
                if (imageView.getId() == R.id.imageViewJoin) {
                    mainActivity.joinToChannel(channel);
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
                    builder.setTitle("Confirmation");

                    final TextView text = new TextView(mainActivity);
                    text.setText(String.format(mainActivity.getString(R.string.delete_channel_confirmation), channel.getName()));
                    builder.setView(text);

                    builder.setPositiveButton("Yes", (dialog, which) -> {
                        ChatWebService.getInstance().getApi().deleteChannel(channel.getName()).enqueue(new Callback<JsonNode>() {
                            @Override
                            public void onResponse(Call<JsonNode> call, Response<JsonNode> response) {
                                if (!response.isSuccessful()) {
                                    mainActivity.serverErrorHandler(root, response);
                                } else {
                                    channelCardAdapter.getChannels().remove((int) channelPosition);
                                    channelCardAdapter.notifyItemRemoved(channelPosition);
                                    channelCardAdapter.notifyItemRangeChanged(channelPosition, channelCardAdapter.getChannels().size());
                                    Toast.makeText(mainActivity, "Channel deleted successfully!", Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<JsonNode> call, Throwable t) {
                                Log.e("Delete", "Error has occurred while trying to delete channel", t);
                                Snackbar.make(root, "Failed deleting channel. Please refresh", Snackbar.LENGTH_LONG).show();
                            }
                        });
                    });
                    builder.setNegativeButton("No", (dialog, which) -> dialog.cancel());

                    builder.show();
                }
            });
        channelsRecyclerView.setAdapter(new ScaleInAnimationAdapter(new AlphaInAnimationAdapter(channelCardAdapter)));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("Lifecycle", this.toString() + ".onDestroy");
    }

    public void onAddButtonClicked(View view) {
        if (channelCardAdapter != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
            builder.setTitle("New Channel");

            final EditText input = new EditText(this.getActivity());
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
            input.setBackgroundResource(R.drawable.textfield_d);
            input.setHint("New channel name");
            input.setPadding(6, 2, 6, 2);
            input.setSingleLine();
            input.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            LinearLayout layout = new LinearLayout(this.getActivity());
            params.setMargins(0, 20, 0, 0);
            layout.setLayoutParams(params);
            layout.setPadding(20, 0, 20, 0);
            layout.addView(input);

            builder.setView(layout);

            builder.setPositiveButton("Create", (dialog, which) -> {
                String newChannelName = input.getText().toString().trim();
                if (newChannelName.isEmpty()) {
                    Toast.makeText(mainActivity, "Channel must have a name!", Toast.LENGTH_LONG).show();
                } else {
                    ChatWebService.getInstance().getApi().createChannel(Channel.builder().name(newChannelName).build()).enqueue(new Callback<JsonNode>() {
                        @Override
                        public void onResponse(Call<JsonNode> call, Response<JsonNode> response) {
                            if (!response.isSuccessful()) {
                                mainActivity.serverErrorHandler(fragmentView, response);
                            } else {
                                try {
                                    Channel channel = ChatWebService.getInstance().getObjectMapper().readValue(response.body().toString(), Channel.class);
                                    int channelPosition = channelCardAdapter.getItemCount();
                                    channelCardAdapter.getChannels().add(channel);
                                    channelCardAdapter.notifyItemInserted(channelPosition);
                                    channelCardAdapter.notifyItemRangeChanged(channelPosition, channelCardAdapter.getChannels().size());
                                    Toast.makeText(mainActivity, "Channel created successfully!", Toast.LENGTH_LONG).show();
                                } catch (JsonProcessingException e) {
                                    Log.e("CreateChannel", "Error has occurred while trying to create channel", e);
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<JsonNode> call, Throwable t) {
                            Log.e("CreateChannel", "Error has occurred while trying to create channel", t);
                            Snackbar.make(fragmentView, "Something went wrong while creating channel: " + t.getMessage(), Snackbar.LENGTH_LONG).show();
                        }
                    });
                }
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

            builder.show();
        } else {
            Toast.makeText(mainActivity, "Cannot create channels. Reload.", Toast.LENGTH_LONG).show();
        }
    }
}
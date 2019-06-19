package ch.japan_impact.japanimpactpos;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import ch.japan_impact.japanimpactpos.data.PosConfiguration;
import ch.japan_impact.japanimpactpos.data.PosConfigurationList;
import ch.japan_impact.japanimpactpos.network.BackendService;
import com.android.volley.AuthFailureError;
import dagger.android.AndroidInjection;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigurationPickerActivity extends AppCompatActivity {
    private static final String TAG = "ConfigurationPickerActivity";

    private TextView mErrorView;
    private SwipeRefreshLayout mRefreshLayout;
    private ConfigurationAdapter adapter;

    @Inject
    BackendService backend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration_picker);

        this.mErrorView = findViewById(R.id.error_message);
        RecyclerView recycler = findViewById(R.id.recycler);
        this.mRefreshLayout = findViewById(R.id.refresh_layout);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler.setLayoutManager(layoutManager);

        // Set up items borders
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                recycler.getContext(),
                layoutManager.getOrientation()
        );
        recycler.addItemDecoration(dividerItemDecoration);

        this.adapter = new ConfigurationAdapter();
        recycler.setAdapter(adapter);

        mRefreshLayout.setOnRefreshListener(this::refresh);
    }

    private void refresh() {
        this.mRefreshLayout.setRefreshing(true);

        try {
            backend.getConfigs(new BackendService.ApiCallback<List<PosConfigurationList>>() {
                @Override
                public void onSuccess(List<PosConfigurationList> data) {
                    Log.i(TAG, "Result from backend " + data.toString());
                    adapter.setConfigurations(
                            data.stream()
                                    .flatMap(configList -> configList.getConfigs()
                                            .stream()
                                            .map(config -> new PosConfiguration(config.getId(), config.getEventId(), configList.getEvent().getName() + " - " + config.getName(), config.isAcceptCards())))
                                    .collect(Collectors.toList())
                    );

                    mRefreshLayout.setRefreshing(false);
                    mErrorView.setVisibility(View.GONE);
                }

                @Override
                public void onFailure(List<String> errors) {
                    mRefreshLayout.setRefreshing(false);
                    mErrorView.setText(getResources().getString(R.string.loading_failed, errors));
                    mErrorView.setVisibility(View.VISIBLE);
                }
            });
        } catch (AuthFailureError authFailureError) {
            authFailureError.printStackTrace();

            Toast.makeText(this, R.string.requires_login, Toast.LENGTH_LONG).show();

            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.refresh();
    }

    private class ConfigurationAdapter extends RecyclerView.Adapter<ConfigurationViewHolder> {
        private List<PosConfiguration> configurations = Collections.emptyList();

        @NonNull
        @Override
        public ConfigurationViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            Context context = viewGroup.getContext();
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            View view = layoutInflater.inflate(R.layout.configuration_item, viewGroup, false);

            Log.i(TAG, "Create view holder...");

            return new ConfigurationViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ConfigurationViewHolder configurationViewHolder, int i) {
            configurationViewHolder.setConfiguration(configurations.get(i));

            Log.i(TAG, "Bind view holder to " + i);
            System.out.println(configurations);

        }

        @Override
        public int getItemCount() {
            return configurations.size();
        }

        public void setConfigurations(List<PosConfiguration> configurations) {
            this.configurations = configurations;
            this.notifyDataSetChanged();
        }
    }

    private class ConfigurationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private PosConfiguration configuration;
        private TextView configName;

        public ConfigurationViewHolder(View item) {
            super(item);

            this.configName = item.findViewById(R.id.configuration);
            item.setOnClickListener(this);
        }

        public void setConfiguration(PosConfiguration configuration) {
            this.configuration = configuration;
            this.configName.setText(configuration.getName());
        }

        @Override
        public void onClick(View v) {
            // TODO: Start new activity
            Toast.makeText(ConfigurationPickerActivity.this, "Clicked on " + configuration.getName() + " " + configuration.getId(), Toast.LENGTH_LONG).show();
        }
    }
}

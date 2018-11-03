package ch.japan_impact.japanimpactpos;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import ch.japan_impact.japanimpactpos.data.PosConfiguration;

import java.util.Collections;
import java.util.List;

public class ConfigurationPickerActivity extends AppCompatActivity {
    private static final String TAG = "ConfigurationPickerActivity";

    private ScrollView mPickerView;
    private ProgressBar mProgressBar;
    private TextView mProgressText;
    private View mLoadingView;
    private RecyclerView mRecycler;
    private ConfigurationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration_picker);

        this.mPickerView = findViewById(R.id.configuration_list);
        this.mLoadingView = findViewById(R.id.loading_progress);
        this.mRecycler = findViewById(R.id.recylcer);
        this.mProgressBar = findViewById(R.id.loading_progress_bar);
        this.mProgressText = findViewById(R.id.loading_text_view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecycler.setLayoutManager(layoutManager);

        // Set up items borders
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                mRecycler.getContext(),
                layoutManager.getOrientation()
        );
        mRecycler.addItemDecoration(dividerItemDecoration);

        this.adapter = new ConfigurationAdapter();
        this.mRecycler.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Reset progress bar state
        this.mPickerView.setVisibility(View.GONE); // Hide picker
        this.mProgressBar.setVisibility(View.VISIBLE);
        this.mProgressText.setVisibility(View.VISIBLE);
        this.mLoadingView.setVisibility(View.VISIBLE);
        this.mProgressText.setText(R.string.loading_text);

        BackendService.getInstance(this).getConfigs(list -> {
            if (list.isFirst()) {
                Log.i(TAG, "Result from backend " + list.first.toString());
                this.adapter.setConfigurations(list.first);

                this.mPickerView.setVisibility(View.VISIBLE);
                this.mLoadingView.setVisibility(View.GONE);
            } else {

                this.mProgressBar.setVisibility(View.GONE); // Hide loading spinner

                // TODO: add retry button

                this.mProgressText.setText(getResources().getString(R.string.loading_failed, list.second));
            }
        });
    }

    private static class ConfigurationAdapter extends RecyclerView.Adapter<ConfigurationViewHolder> {
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

    private static class ConfigurationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
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
        }
    }
}

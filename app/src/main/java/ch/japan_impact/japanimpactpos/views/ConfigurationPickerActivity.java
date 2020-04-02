package ch.japan_impact.japanimpactpos.views;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import ch.japan_impact.japanimpactpos.R;
import ch.japan_impact.japanimpactpos.data.AbstractConfiguration;
import ch.japan_impact.japanimpactpos.data.AbstractConfigurationList;
import ch.japan_impact.japanimpactpos.data.scan.ScanConfiguration;
import ch.japan_impact.japanimpactpos.network.BackendService;
import ch.japan_impact.japanimpactpos.network.exceptions.LoginRequiredException;
import ch.japan_impact.japanimpactpos.network.exceptions.NetworkException;
import ch.japan_impact.japanimpactpos.views.pos.POSActivity;
import ch.japan_impact.japanimpactpos.views.scan.ScanActivity;
import com.google.android.material.tabs.TabLayout;
import com.sumup.merchant.api.SumUpAPI;
import com.sumup.merchant.api.SumUpLogin;
import dagger.android.AndroidInjection;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigurationPickerActivity extends AppCompatActivity {
    public static final String CONFIG_ID = "CONFIG_ID";
    public static final String CONFIG_NAME = "CONFIG_NAME";
    public static final String EVENT_ID = "EVENT_ID";
    private static final String TAG = "ConfigurationPickerActivity";

    private TextView mErrorView;
    private TextView mSumUpStatus;
    private Button mSumUpLogout;
    private Button mSumUpSetup;
    private Button mJiLogout;
    private SwipeRefreshLayout mRefreshLayout;
    private ConfigurationAdapter posAdapter;
    private ConfigurationAdapter scanAdapter;

    @Inject
    BackendService backend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration_picker);

        this.mErrorView = findViewById(R.id.error_message);
        this.mSumUpStatus = findViewById(R.id.sumup_status);
        this.mSumUpLogout = findViewById(R.id.sumup_logout);
        this.mSumUpLogout.setOnClickListener(v -> {
            if (SumUpAPI.isLoggedIn()) {
                SumUpAPI.logout();
                sumUpRefresh();
            } else {
                SumUpAPI.openLoginActivity(this, SumUpLogin.builder(getString(R.string.sumup_affiliate_key)).build(), 1);
            }
        });

        this.mSumUpSetup = findViewById(R.id.sumup_setup);
        this.mSumUpSetup.setOnClickListener(v -> SumUpAPI.openPaymentSettingsActivity(this, 1));
        this.mJiLogout = findViewById(R.id.ji_logout);


        String CLIENT_ID = getResources().getString(R.string.auth_client_id);
        String API_URL = getResources().getString(R.string.auth_api_url);


        this.mJiLogout.setOnClickListener(v -> {
            backend.getStorage().logout();

            Intent i = new Intent(Intent.ACTION_VIEW)
                    .setData(Uri.parse(API_URL + "/logout?app=" + CLIENT_ID + "&tokenType=token"));
            startActivity(i);
        });
        this.mRefreshLayout = findViewById(R.id.refresh_layout);

        this.posAdapter = new ConfigurationAdapter();
        this.scanAdapter = new ConfigurationAdapter();
        setUpRecycler(R.id.configuration_list, this.posAdapter);
        setUpRecycler(R.id.scan_list, this.scanAdapter);

        ViewPager pager = findViewById(R.id.view_pager);
        pager.setAdapter(new ViewPagerAdapter());

        TabLayout layout = findViewById(R.id.tab_layout);
        layout.setupWithViewPager(pager);

        mRefreshLayout.setOnRefreshListener(this::refresh);
    }

    public class ViewPagerAdapter extends PagerAdapter {
        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            View v = findViewById(position == 0 ? R.id.pos_scroller : R.id.scan_scroller);
            container.addView(v);
            return v;
        }

        @Override
        public void destroyItem(ViewGroup collection, int position, Object view) {
            collection.removeView((View) view);
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return position == 0 ? "Point de vente" : "Scan";
        }
    }

    private void setUpRecycler(int resId, ConfigurationAdapter adapter) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        RecyclerView recycler = findViewById(resId);
        recycler.setLayoutManager(layoutManager);

        // Set up items borders
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                recycler.getContext(),
                layoutManager.getOrientation()
        );
        recycler.addItemDecoration(dividerItemDecoration);
        recycler.setAdapter(adapter);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        this.sumUpRefresh();
    }

    private void sumUpRefresh() {
        this.mSumUpStatus.setText(SumUpAPI.isLoggedIn() ? "SumUp: Connecté avec " + SumUpAPI.getCurrentMerchant().getMerchantCode() : "SumUp: Déconnecté");
        this.mSumUpLogout.setText(SumUpAPI.isLoggedIn() ? R.string.logout : R.string.login);
        this.mSumUpSetup.setVisibility(SumUpAPI.isLoggedIn() ? View.VISIBLE : View.GONE);
        ((LinearLayout.LayoutParams) this.mSumUpLogout.getLayoutParams()).weight = SumUpAPI.isLoggedIn() ? 1 : 2;
    }

    private void refresh() {
        this.mRefreshLayout.setRefreshing(true);
        this.sumUpRefresh();

        backend.getPosConfigs(createCallbackForAdapter(posAdapter));
        backend.getScanConfigs(createCallbackForAdapter(scanAdapter));
    }

    private <U extends AbstractConfigurationList<?>> BackendService.ApiCallback<List<U>> createCallbackForAdapter(ConfigurationAdapter adapter) {
        return new BackendService.ApiCallback<List<U>>() {
            @Override
            public void onSuccess(List<U> data) {
                adapter.setConfigurations(
                        data.stream()
                                .flatMap(configList ->
                                        configList.getConfigs().stream()
                                                .map(config -> config.updateName(configList.getEvent().getName() + " - " + config.getName())))
                                .collect(Collectors.toList())
                );

                mRefreshLayout.setRefreshing(false);
                mErrorView.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(NetworkException error) {
                if (error instanceof LoginRequiredException) {
                    Toast.makeText(ConfigurationPickerActivity.this, R.string.requires_login, Toast.LENGTH_LONG).show();

                    startActivity(new Intent(ConfigurationPickerActivity.this, LoginActivity.class));
                    finish();
                } else {

                    mRefreshLayout.setRefreshing(false);
                    mErrorView.setText(getResources().getString(R.string.loading_failed, error.getDescription()));
                    mErrorView.setVisibility(View.VISIBLE);
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.refresh();
    }

    private class ConfigurationAdapter extends RecyclerView.Adapter<ConfigurationViewHolder> {
        private List<AbstractConfiguration> configurations = Collections.emptyList();

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

        public void setConfigurations(List<AbstractConfiguration> configurations) {
            this.configurations = configurations;
            this.notifyDataSetChanged();
        }
    }

    private class ConfigurationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private AbstractConfiguration configuration;
        private TextView configName;

        public ConfigurationViewHolder(View item) {
            super(item);

            this.configName = item.findViewById(R.id.configuration);
            item.setOnClickListener(this);
        }

        public void setConfiguration(AbstractConfiguration configuration) {
            this.configuration = configuration;
            this.configName.setText(configuration.getName());
        }

        @Override
        public void onClick(View v) {
            Intent openIntent;

            if (configuration instanceof ScanConfiguration) {
                openIntent = new Intent(ConfigurationPickerActivity.this, ScanActivity.class);

            } else {
                openIntent = new Intent(ConfigurationPickerActivity.this, POSActivity.class);
            }
            openIntent.putExtra(CONFIG_ID, configuration.getId());
            openIntent.putExtra(EVENT_ID, configuration.getEventId());
            Toast.makeText(ConfigurationPickerActivity.this, "Ouverture de " + configuration.getName(), Toast.LENGTH_SHORT).show();

            startActivity(openIntent);
        }
    }
}

package ch.japan_impact.japanimpactpos.inject;

import android.app.Activity;
import android.app.Application;
import com.sumup.merchant.api.SumUpState;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;

import javax.inject.Inject;

/**
 * @author Louis Vialar
 */
public class JIPosApplication extends Application implements HasActivityInjector {
    @Inject
    DispatchingAndroidInjector<Activity> activityDispatchingAndroidInjector;

    public void initDaggerComponent() {
        DaggerApplicationComponent
                .builder()
                .application(this)
                .build()
                .inject(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        SumUpState.init(this);

        initDaggerComponent();
    }

    @Override
    public DispatchingAndroidInjector<Activity> activityInjector() {
        return activityDispatchingAndroidInjector;
    }
}

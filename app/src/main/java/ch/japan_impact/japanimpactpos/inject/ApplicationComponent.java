package ch.japan_impact.japanimpactpos.inject;

import android.app.Application;
import ch.japan_impact.japanimpactpos.network.NetworkModule;
import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjectionModule;
import dagger.android.support.AndroidSupportInjectionModule;

import javax.inject.Singleton;

/**
 * @author Louis Vialar
 */
@Component(modules = {
        AndroidInjectionModule.class,
        AndroidSupportInjectionModule.class,
        ActivitiesModule.class,
        ApplicationModule.class,
        NetworkModule.class})
@Singleton
public interface ApplicationComponent {
    void inject(JIPosApplication application);

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder application(Application application);

        ApplicationComponent build();
    }
}
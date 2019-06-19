package ch.japan_impact.japanimpactpos.inject;

import ch.japan_impact.japanimpactpos.ConfigurationPickerActivity;
import ch.japan_impact.japanimpactpos.LoginActivity;
import dagger.Module;
import dagger.android.AndroidInjectionModule;
import dagger.android.ContributesAndroidInjector;

/**
 * @author Louis Vialar
 */
@Module(includes = {
        AndroidInjectionModule.class
})
abstract class ActivitiesModule {
    @ContributesAndroidInjector
    abstract LoginActivity loginActivity();
    @ContributesAndroidInjector
    abstract ConfigurationPickerActivity pickerActivity();
}

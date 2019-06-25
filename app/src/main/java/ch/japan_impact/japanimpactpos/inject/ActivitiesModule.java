package ch.japan_impact.japanimpactpos.inject;

import ch.japan_impact.japanimpactpos.views.ConfigurationPickerActivity;
import ch.japan_impact.japanimpactpos.views.LoginActivity;
import ch.japan_impact.japanimpactpos.views.pos.CashPaymentActivity;
import ch.japan_impact.japanimpactpos.views.pos.POSActivity;
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
    @ContributesAndroidInjector
    abstract POSActivity posActivity();
    @ContributesAndroidInjector
    abstract CashPaymentActivity cashPaymentActivity();
}

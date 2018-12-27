package me.devsaki.hentoid.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.view.MenuItem;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import me.devsaki.hentoid.R;
import me.devsaki.hentoid.abstracts.BaseActivity;
import me.devsaki.hentoid.events.ImportEvent;
import me.devsaki.hentoid.fragments.LibRefreshLauncher;
import me.devsaki.hentoid.services.ImportService;
import me.devsaki.hentoid.services.UpdateCheckService;
import me.devsaki.hentoid.services.UpdateDownloadService;
import me.devsaki.hentoid.util.FileHelper;
import me.devsaki.hentoid.util.Helper;
import me.devsaki.hentoid.util.Preferences;

/**
 * Created by DevSaki on 20/05/2015.
 * Set up and present preferences.
 * <p>
 * Maintained by wightwulf1944 22/02/2018
 * updated class for new AppCompatActivity and cleanup
 */
public class PrefsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new MyPreferenceFragment())
                .commit();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onImportEventComplete(ImportEvent event) {
        if (ImportEvent.EV_COMPLETE == event.eventType && event.cleanupLogFile != null)
        {
            Snackbar snackbar = Snackbar.make(this.findViewById(android.R.id.content), R.string.cleanup_done, Snackbar.LENGTH_LONG);
            snackbar.setAction("READ LOG", v -> FileHelper.openFile(this, event.cleanupLogFile) );
            snackbar.show();
        }
    }

    public static class MyPreferenceFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);

            if ("advancedSettings".equals(rootKey)) {
                findPreference(Preferences.Key.PREF_DL_THREADS_QUANTITY_LISTS)
                        .setOnPreferenceChangeListener((preference, newValue) -> onPrefRequiringRestartChanged());
            } else {
                findPreference(Preferences.Key.PREF_HIDE_RECENT)
                        .setOnPreferenceChangeListener((preference, newValue) -> onPrefRequiringRestartChanged());

                findPreference(Preferences.Key.PREF_ANALYTICS_TRACKING)
                        .setOnPreferenceChangeListener((preference, newValue) -> onPrefRequiringRestartChanged());

                findPreference(Preferences.Key.PREF_USE_SFW)
                        .setOnPreferenceChangeListener((preference, newValue) -> onPrefRequiringRestartChanged());

                findPreference(Preferences.Key.PREF_APP_LOCK)
                        .setOnPreferenceClickListener(preference -> onAppLockPreferenceClick());
            }
        }

        @Override
        public boolean onPreferenceTreeClick(Preference preference) {
            String key = preference.getKey();
            if (key == null) return super.onPreferenceTreeClick(preference);
            switch (key) {
                case Preferences.Key.PREF_ADD_NO_MEDIA_FILE:
                    FileHelper.createNoMedia();
                    return true;
                case Preferences.Key.PREF_CHECK_UPDATE_MANUAL:
                    return onCheckUpdatePrefClick();
                case Preferences.Key.PREF_REFRESH_LIBRARY:
                    if (ImportService.isRunning()) {
                        Helper.toast("Import is already running");
                    } else {
                        LibRefreshLauncher.invoke(requireFragmentManager());
                    }
                    return true;
                default:
                    return super.onPreferenceTreeClick(preference);
            }
        }

        @Override
        public void onNavigateToScreen(PreferenceScreen preferenceScreen) {
            Bundle args = new Bundle();
            args.putString(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT, preferenceScreen.getKey());

            MyPreferenceFragment preferenceFragment = new MyPreferenceFragment();
            preferenceFragment.setArguments(args);

            requireFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, preferenceFragment)
                    .addToBackStack(null)
                    .commit();
        }

        private boolean onCheckUpdatePrefClick() {
            if (!UpdateDownloadService.isRunning()) {
                Intent intent = UpdateCheckService.makeIntent(requireContext(), true);
                requireContext().startService(intent);
            }
            return true;
        }

        private boolean onPrefRequiringRestartChanged() {
            Helper.toast(R.string.restart_needed);
            return true;
        }

        private boolean onAppLockPreferenceClick() {
            Intent intent = new Intent(requireContext(), RegisterPinActivity.class);
            startActivity(intent);
            return true;
        }
    }
}

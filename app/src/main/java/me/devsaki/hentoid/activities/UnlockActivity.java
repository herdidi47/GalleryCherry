package me.devsaki.hentoid.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.Vibrator;
import android.view.View;
import android.widget.TextView;

import me.devsaki.hentoid.R;
import me.devsaki.hentoid.abstracts.BaseActivity;
import me.devsaki.hentoid.fragments.PinEntryFragment;
import me.devsaki.hentoid.util.Preferences;

/**
 * This activity asks for a 4 digit pin if it is set and then transitions to another activity
 * <p>
 * TODO: Currently only used to protect app launch from launcher. Look for other ways to launch the
 * app which should be protected
 *
 * @see #wrapIntent(Context, Intent)
 */
public class UnlockActivity extends BaseActivity implements PinEntryFragment.Parent {

    private static final String EXTRA_DEFAULT = "default";

    /**
     * This is reset to false at an undefined time, usually due to process death.
     */
    private static boolean isUnlocked = false;

    /**
     * Creates an intent that launches this activity before launching the given wrapped intent
     *
     * @param context           used for creating the return intent
     * @param destinationIntent intent that refers to the next activity
     * @return intent that launches this activity which leads to another activity referred to by
     * {@code destinationIntent}
     */
    public static Intent wrapIntent(Context context, Intent destinationIntent) {
        Intent intent = new Intent(context, UnlockActivity.class);
        intent.putExtra(EXTRA_DEFAULT, destinationIntent);
        return intent;
    }

    private TextView instructionText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Preferences.getAppLockPin().isEmpty() || isUnlocked) {
            goToNextActivity();
            return;
        }

        setContentView(R.layout.activity_unlock);

        instructionText = findViewById(R.id.text_instruction);
    }

    @Override
    protected void onStop() {
        super.onStop();
        instructionText.setText(R.string.app_lock_pin);
    }

    @Override
    public void onPinAccept(String pin) {
        invokeVibrate();

        if (Preferences.getAppLockPin().equals(pin)) {
            isUnlocked = true;

            instructionText.setVisibility(View.GONE);

            View pinEntryFragment = findViewById(R.id.fragment_pin_entry);
            pinEntryFragment.setVisibility(View.GONE);

            View pinOkText = findViewById(R.id.text_pin_ok);
            pinOkText.setVisibility(View.VISIBLE);

            goToNextActivity();
        } else {
            instructionText.setText(R.string.pin_invalid);
        }
    }

    private void invokeVibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            vibrator.vibrate(100);
        }
    }

    private void goToNextActivity() {
        Parcelable parcelableExtra = getIntent().getParcelableExtra(EXTRA_DEFAULT);
        startActivity((Intent) parcelableExtra);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }
}

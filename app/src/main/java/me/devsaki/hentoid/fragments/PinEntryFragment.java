package me.devsaki.hentoid.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.devsaki.hentoid.R;

import static android.support.v4.view.ViewCompat.requireViewById;

public final class PinEntryFragment extends Fragment {

    private final StringBuilder pinValue = new StringBuilder(4);

    private Parent parent;

    private View placeholderImage1;

    private View placeholderImage2;

    private View placeholderImage3;

    private View placeholderImage4;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        parent = (Parent) context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pin_entry, container, false);

        placeholderImage1 = requireViewById(view, R.id.image_placeholder_1);

        placeholderImage2 = requireViewById(view, R.id.image_placeholder_2);

        placeholderImage3 = requireViewById(view, R.id.image_placeholder_3);

        placeholderImage4 = requireViewById(view, R.id.image_placeholder_4);

        View numpadLayout = requireViewById(view, R.id.layout_numpad);
        
        View button0 = requireViewById(numpadLayout, R.id.button_0);
        button0.setOnClickListener(v -> onKeyClick("0"));

        View button1 = requireViewById(numpadLayout, R.id.button_1);
        button1.setOnClickListener(v9 -> onKeyClick("1"));

        View button2 = requireViewById(numpadLayout, R.id.button_2);
        button2.setOnClickListener(v8 -> onKeyClick("2"));

        View button3 = requireViewById(numpadLayout, R.id.button_3);
        button3.setOnClickListener(v7 -> onKeyClick("3"));

        View button4 = requireViewById(numpadLayout, R.id.button_4);
        button4.setOnClickListener(v6 -> onKeyClick("4"));

        View button5 = requireViewById(numpadLayout, R.id.button_5);
        button5.setOnClickListener(v5 -> onKeyClick("5"));

        View button6 = requireViewById(numpadLayout, R.id.button_6);
        button6.setOnClickListener(v4 -> onKeyClick("6"));

        View button7 = requireViewById(numpadLayout, R.id.button_7);
        button7.setOnClickListener(v3 -> onKeyClick("7"));

        View button8 = requireViewById(numpadLayout, R.id.button_8);
        button8.setOnClickListener(v2 -> onKeyClick("8"));

        View button9 = requireViewById(numpadLayout, R.id.button_9);
        button9.setOnClickListener(v1 -> onKeyClick("9"));

        View buttonBackspace = requireViewById(numpadLayout, R.id.button_backspace);
        buttonBackspace.setOnClickListener(v -> onBackspaceClick());

        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
        clearPin();
    }

    private void onKeyClick(String s) {
        if (pinValue.length() == 4) return;

        pinValue.append(s);
        switch (pinValue.length()) {
            case 1: placeholderImage1.setVisibility(View.VISIBLE);
                break;
            case 2: placeholderImage2.setVisibility(View.VISIBLE);
                break;
            case 3: placeholderImage3.setVisibility(View.VISIBLE);
                break;
            case 4: submitPin();
                break;
        }
    }

    private void onBackspaceClick() {
        if (pinValue.length() == 0) return;

        pinValue.setLength(pinValue.length() - 1);
        switch (pinValue.length()) {
            case 0: placeholderImage1.setVisibility(View.INVISIBLE);
                break;
            case 1: placeholderImage2.setVisibility(View.INVISIBLE);
                break;
            case 2: placeholderImage3.setVisibility(View.INVISIBLE);
                break;
            case 3: placeholderImage4.setVisibility(View.INVISIBLE);
                break;
        }
    }

    private void submitPin() {
        parent.onPinAccept(pinValue.toString());
        pinValue.setLength(0);
        clearPin();
    }

    private void clearPin() {
        pinValue.setLength(0);
        placeholderImage1.setVisibility(View.INVISIBLE);
        placeholderImage2.setVisibility(View.INVISIBLE);
        placeholderImage3.setVisibility(View.INVISIBLE);
        placeholderImage4.setVisibility(View.INVISIBLE);
    }

    public interface Parent {
        void onPinAccept(String pin);
    }
}

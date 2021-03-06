package de.heoegbr.diabeatit.ui.setup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.NotNull;

import de.heoegbr.diabeatit.R;
import io.noties.markwon.Markwon;

public class SetupWelcomeFragment extends Fragment {
    private TextView textView;
    private Switch aggreeSwitch;
    private TextView aggreeSwitchTitle;
    private Markwon markwon;

    private int textResourceId;
    private boolean switchEnabled;
    private int switchTitleId;
    private CompoundButton.OnCheckedChangeListener listener;
    private boolean initialState;

    public SetupWelcomeFragment(@NotNull int textResourceId, boolean switchEnabled, int switchTitleId,
                                CompoundButton.OnCheckedChangeListener listener, boolean initialState) {
        this.textResourceId = textResourceId;
        this.switchEnabled = switchEnabled;
        this.switchTitleId = switchTitleId;
        this.listener = listener;
        this.initialState = initialState;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_setup_content, container, false);

        textView = root.findViewById(R.id.setup_fragment_text);
        markwon = Markwon.create(getContext());
        markwon.setMarkdown(textView, getString(textResourceId));

        aggreeSwitch = root.findViewById(R.id.setup_fragment_switch);
        aggreeSwitchTitle = root.findViewById(R.id.setup_fragment_switch_title);
        if (switchEnabled) {
            aggreeSwitch.setVisibility(View.VISIBLE);
            aggreeSwitch.setOnCheckedChangeListener(listener);
            aggreeSwitch.setChecked(initialState);
            aggreeSwitchTitle.setVisibility(View.VISIBLE);
            aggreeSwitchTitle.setText(switchTitleId);
        } else {
            aggreeSwitch.setVisibility(View.INVISIBLE);
            aggreeSwitchTitle.setVisibility(View.INVISIBLE);
        }

        return root;
    }

    void resetSwitch(){
        aggreeSwitch.setChecked(false);
    }
}

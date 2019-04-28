package kr.ac.snu.mobcomp_project;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

public class TabFragment2 extends Fragment {
    public TabFragment2() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ConstraintLayout layout = (ConstraintLayout) inflater.inflate(R.layout.tab_fragment_2, container, false);
        EditText phone = (EditText) layout.findViewById(R.id.editText);
        phone.setText(((MainActivity)getActivity()).designated_phone_number, TextView.BufferType.EDITABLE);
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String s_temp = s.toString();
                // Save to setting
                SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(getString(R.string.designated_phone_number),s_temp);
                editor.commit();
                // load from setting
                String defaultValue = getResources().getString(R.string.designated_phone_number_default);
                ((MainActivity)getActivity()).designated_phone_number =  sharedPref.getString(getString(R.string.designated_phone_number), defaultValue);
                //((MainActivity)getActivity()).designated_phone_number = "tel:" + s_temp;
                System.out.println(((MainActivity)getActivity()).designated_phone_number);
            }
        };
        phone.addTextChangedListener(textWatcher);
        return layout;
    }
}

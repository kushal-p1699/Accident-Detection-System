package com.example.accidentdetectionsystem.ui.profile;
import android.content.Context;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import java.util.List;

public class HintAdapter extends ArrayAdapter<String> {

    public HintAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }

    public HintAdapter(Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
    }

    public HintAdapter(Context context, int resource, String[] objects) {
        super(context, resource, objects);
    }

    public HintAdapter(Context context, int resource, int textViewResourceId, String[] objects) {
        super(context, resource, textViewResourceId, objects);
    }

    public HintAdapter(Context context, int resource, List<String> objects) {
        super(context, resource, objects);
    }

    public HintAdapter(Context context, int resource, int textViewResourceId, List<String> objects) {
        super(context, resource, textViewResourceId, objects);
    }

    @Override
    public int getCount() {
        // don't display last item. It is used as hint.
        int count = super.getCount();
        return count > 0 ? count - 1 : count;
    }
}

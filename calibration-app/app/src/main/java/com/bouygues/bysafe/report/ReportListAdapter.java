package com.bouygues.bysafe.report;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bouygues.bysafe.R;

import org.dpppt.android.sdk.internal.logger.Logger;
import org.w3c.dom.Text;

import java.util.ArrayList;

public class ReportListAdapter extends ArrayAdapter<ReportRow> {

    public ReportListAdapter(@NonNull Context context, ArrayList<ReportRow> rows) {
        super(context, 0, rows);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ReportRow row = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.report_list_item, parent, false);
        }

        TextView index = convertView.findViewById(R.id.report_list_item_index);
        index.setText(String.valueOf(row.index));

        TextView hour = convertView.findViewById(R.id.report_list_item_hour);
        hour.setText(row.hour);

        int i = 1;
        for (char ch : row.reports.toCharArray()) {
            int id = getContext().getResources().getIdentifier(String.format("report_list_item_detail_sample_%d", i), "id", getContext().getPackageName());
            ImageView sample = convertView.findViewById(id);
            if (i == 1) {

            }
            switch (ch) {
                case 'X':
                    sample.setColorFilter(Color.parseColor("#FA990C"));
                    break;
                case 'O':
                    sample.setColorFilter(Color.parseColor("#87EA4A"));
                    break;
                case '_':
                    sample.setColorFilter(Color.parseColor("#DBDEE1"));
                    break;
            }
            i++;
        }

        TextView exposedTime = convertView.findViewById(R.id.report_list_item_exposed_time);
        exposedTime.setText(String.format("%02d'00\"", row.exposedMinutes));

        return convertView;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }
}

package com.bouygues.bysafe.report;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bouygues.bysafe.R;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import org.dpppt.android.sdk.internal.logger.Logger;

import java.util.ArrayList;

public class TeamReportListAdapter extends ArrayAdapter<TeamReportRow> {

    public TeamReportListAdapter(@NonNull Context context, ArrayList<TeamReportRow> rows) {
        super(context, 0, rows);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TeamReportRow row = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.report_list_team_item, parent, false);
        }

        TextView name = convertView.findViewById(R.id.team_member_name);
        name.setText(row.name);

        TextView exposedTime = convertView.findViewById(R.id.total_team_member_exposure_time);
        exposedTime.setText(String.format("%02d:%02d", row.exposedHours, row.exposedMinutes));

        TextView exposedPercent = convertView.findViewById(R.id.team_member_exposed_percent);
        exposedPercent.setText(String.format("%d%%", row.percent));

        CircularProgressBar pb = convertView.findViewById(R.id.day_report_circular_progress_bar_team_member);
        pb.setProgress(row.percent);

        ListView lv = convertView.findViewById(R.id.report_list_team_member);
        ReportListAdapter itemsAdapter = new ReportListAdapter(getContext(), row.rows);
        lv.setAdapter(itemsAdapter);

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

package com.bouygues.bysafe.report;

import java.util.ArrayList;

public class TeamReportRow {
    ArrayList<ReportRow> rows;
    int percent;
    int exposedHours;
    int exposedMinutes;
    String name;

    public TeamReportRow(String name, int percent, int exposedHours, int exposedMinutes, ArrayList<ReportRow> rows) {
        this.rows = rows;
        this.name = name;
        this.percent = percent;
        this.exposedHours = exposedHours;
        this.exposedMinutes = exposedMinutes;
    }
}

package com.bouygues.bysafe.report;

public class ReportRow {
    int index;
    String hour;
    String reports;
    int exposedMinutes;

    public ReportRow(int index, String hour, String reports, int exposedMinutes) {
        this.exposedMinutes = exposedMinutes;
        this.hour = hour;
        this.reports = reports;
        this.index = index;
    }
}

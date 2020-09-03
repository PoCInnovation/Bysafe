package com.bouygues.bysafe.report;

public class ReportRow {
    String hour;
    String reports;
    int exposedMinutes;

    public ReportRow(String hour, String reports, int exposedMinutes) {
        this.exposedMinutes = exposedMinutes;
        this.hour = hour;
        this.reports = reports;
    }
}

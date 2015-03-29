package com.thijsdev.studentaanhuis;

import android.app.Application;

public class SAHApplication extends Application {
    public static boolean isActivityVisible() {
        return activityVisible;
    }

    public static void activityResumed() {
        activityVisible = true;
    }

    public static void activityPaused() {
        activityVisible = false;
    }

    public static boolean isTimerRunning() {
        return timerRunning;
    }

    public static void stopTimer() {
        timerRunning = false;
    }

    public static void startTimer() {
        timerRunning = true;
    }

    private static boolean activityVisible = false;
    private static boolean timerRunning = true;
}
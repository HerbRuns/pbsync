package com.herbruns.pbsync.util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Utility {
    // Lifted straight from the Runelite code for my own PB usage, ty!
    public static String secondsToTimeString(double seconds)
    {
        int hours = (int) (Math.floor(seconds) / 3600);
        int minutes = (int) (Math.floor(seconds / 60) % 60);
        seconds = seconds % 60;

        String timeString = hours > 0 ? String.format("%d:%02d:", hours, minutes) : String.format("%d:", minutes);

        // If the seconds is an integer, it is ambiguous if the pb is a precise
        // pb or not. So we always show it without the trailing .00.
        return timeString + (Math.floor(seconds) == seconds ? String.format("%02d", (int) seconds) : String.format("%05.2f", seconds));
    }
}

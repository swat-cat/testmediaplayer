package com.swat_cat.com.testmediaplayer.Utils;

import org.joda.time.Duration;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

/**
 * Created by Dell on 02.07.2015.
 */
public class Utils {

    public static String durationFormater(long duration){
        Duration yourDuration = new Duration(duration);
                Period period = yourDuration.toPeriod();
        PeriodFormatter minutesAndSeconds = new PeriodFormatterBuilder()
                .printZeroAlways()
                .appendMinutes().minimumPrintedDigits(2)
                .appendSeparator(":")
                .appendSeconds().minimumPrintedDigits(2)
                .toFormatter();
        return minutesAndSeconds.print(period);
    }

    public static String getFileExt(String fileName) {
        return fileName.substring((fileName.lastIndexOf(".") + 1), fileName.length());
    }
}

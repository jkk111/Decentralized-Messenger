package com.maximus.dm.decentralizedmessenger.helper;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Maximus on 17/03/2016.
 */
public class Utils {
    private final static String TAG = "Utils";

    private final static String FORMAT_SERVER_DATE = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    private final static String FORMAT_UNDER_DAY = "HH:mm";
    private final static String FORMAT_OVER_DAY = "HH:mm dd MMM";
    private final static String FORMAT_OVER_YEAR = "dd MMM yyyy";

    //2016-03-17T17:12:52.000Z
    public static String getTime(String dateTime) {
        String dateToReturn = "";
        /*
        Log.d(TAG, "Date/time pre: " + dateTime);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(FORMAT_SERVER_DATE);
        String format;

        Date dateOfMessage = null;
        try {
            dateOfMessage = simpleDateFormat.parse(dateTime);
        } catch(ParseException e) {
            System.err.print(e);
            e.printStackTrace();
        }

        Calendar dateTimeOfMessage = Calendar.getInstance();
        Calendar dateTimeNow = Calendar.getInstance();
        if (dateOfMessage != null) {
            dateTimeOfMessage.setTime(dateOfMessage);

            boolean sameYear = dateTimeNow.YEAR == dateTimeOfMessage.YEAR;
            boolean sameDay = sameYear && dateTimeNow.DAY_OF_YEAR == dateTimeOfMessage.DAY_OF_YEAR;

            if (sameDay) {
                format = FORMAT_UNDER_DAY;
            } else if (sameYear) {
                format = FORMAT_OVER_DAY;
            } else {
                format = FORMAT_OVER_YEAR;
            }

            SimpleDateFormat sdtReturn = new SimpleDateFormat(format);
            try {
                Date returnDate = sdtReturn.parse(dateTime);
                dateToReturn = returnDate.toString();
            } catch(ParseException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "Date to return: " + dateToReturn);
        */
        dateToReturn = dateTime.substring(0, 19);
        dateToReturn = dateToReturn.replace("T", " ");
        dateToReturn = dateToReturn.replace("-", "/");
        return dateToReturn;
    }

}

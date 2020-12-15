package com.example.mywatchface;

public class MyData {
    enum mDay {
        SUN, MON, TUE, WED, THU, FRI, SAT
    }

    enum mMonth {
        JANUARY,
        February,
        March,
        April,
        May,
        June,
        July,
        August,
        September,
        October,
        November,
        DECEMBER
    }

    public static mDay getDay(int day) {
        switch (day) {
            case 2:
                return mDay.MON;
            case 3:
                return mDay.TUE;
            case 4:
                return mDay.WED;
            case 5:
                return mDay.THU;
            case 6:
                return mDay.FRI;
            case 7:
                return mDay.SAT;
            default:
                return mDay.SUN;

        }
    }
    public  static mMonth getMonth(int month) {
        switch (month) {
            case 2:
                return mMonth.March;
            case 3:
                return mMonth.April;
            case 4:
                return mMonth.May;
            case 5:
                return mMonth.June;
            case 6:
                return mMonth.July;
            case 7:
                return mMonth.August;
            case 8:
                return mMonth.September;
            case 9:
                return mMonth.October;
            case 10:
                return mMonth.November;
            case 11:
                return mMonth.DECEMBER;
            case 12:
                return mMonth.February;
            default:
                return mMonth.JANUARY;
        }
    }
}

package cn.qdsc.msp.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by lizhongyi on 2016/1/23.
 */
public class DateTimeUtil {


    public static String Time2Date(String time) {
        SimpleDateFormat sdf =   new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = sdf.parse(time);
            String mdhm = MMddHHmm(date);
            return mdhm;

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String MMddHHmm(Date data) {
        Calendar cal =  Date2Calendar(data);
        int month=cal.get(Calendar.MONTH);//获取月份
        month+=1;

        int day=cal.get(Calendar.DATE);//获取日
        int hour=cal.get(Calendar.HOUR);//小时
        int minute=cal.get(Calendar.MINUTE);//分

        String monthStr = "" + month;
        String dayStr = "" + day;
        String hourStr = "" + hour;
        String minuteStr = "" + minute;

        //format


        if (month <10) {
            monthStr ="0"+month;
        }

        if (day <10) {
            dayStr ="0"+day;
        }

        if (hour <10) {
            hourStr ="0"+hour;
        }

        if (minute <10) {
            minuteStr ="0"+minute;
        }


        String resStr = monthStr + "-" + dayStr + " " + hourStr + ":" + minuteStr;
        return resStr;
    }

    public static Calendar Date2Calendar(Date data) {
        Date date=new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }


    //ex.input: "2016-02-25 04:15:48"
    public static String formatDateTime(String datetime) {
        String dt[] = datetime.split(" ");

        try {
            SimpleDateFormat sdf =   new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String now = sdf.format(new Date());
            String nowDt[] = now.split(" ");
            if (dt[0].equals(nowDt[0])) {
                return dt[1].substring(0,5);
            }

            String MMDD = dt[0].substring(5);
            String HHMM = dt[1].substring(0,5);
            return MMDD + " " + HHMM;
        }catch (Exception e) {
            return null;
        }

    }
}

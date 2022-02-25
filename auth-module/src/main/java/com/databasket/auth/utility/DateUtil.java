package com.databasket.auth.utility;

import java.sql.Time;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DateUtil {
	static final SimpleDateFormat FORMATTER = new SimpleDateFormat();
	static final DateFormatSymbols DATE_FORMAT_SYMBOLS = new DateFormatSymbols();
    static final String[] MONTH_NAMES = DATE_FORMAT_SYMBOLS.getMonths();
    
    static final String[] DAYS = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        
	public static Date removeTimestampInfo(Date date){
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}
	
	public static Date getAuditDate(Date date){
		Calendar calendarDate = Calendar.getInstance();
		calendarDate.setTime(date);
		
		Calendar nearestAuditDate = Calendar.getInstance();
		//nearestAuditDate.setTime(CommonContext.getAccountingDate());
		nearestAuditDate.set(Calendar.YEAR, calendarDate.get(Calendar.YEAR));
		nearestAuditDate.set(Calendar.MONTH, calendarDate.get(Calendar.MONTH));
		nearestAuditDate.set(Calendar.DAY_OF_MONTH, calendarDate.get(Calendar.DAY_OF_MONTH));
		if (nearestAuditDate.getTime().after(date)) {
			if (nearestAuditDate.getTime().getTime() - date.getTime() > 24*60*60*1000) {
				nearestAuditDate.add(Calendar.DATE, 1);
				return nearestAuditDate.getTime();
			}else {
				nearestAuditDate.add(Calendar.DATE, -1);
				return nearestAuditDate.getTime();
			}
		}
				
		if (nearestAuditDate.getTime().before(date)) {
			return nearestAuditDate.getTime();
		}
		
		return nearestAuditDate.getTime();
	}
	

	public static Date theDayBefore(Date date) {
		GregorianCalendar theDayBefore = new GregorianCalendar();
		theDayBefore.setTime(date);
		theDayBefore.add(Calendar.DAY_OF_MONTH, -1);
		return theDayBefore.getTime();
	}
	
	public static Time sqlTime(Date date){
		return new Time(date.getTime());
	}
	
	public static List<String> generateMonthYears(Date startDate, Date endDate){
		Calendar beginCalendar = Calendar.getInstance();
        Calendar finishCalendar = Calendar.getInstance();
        beginCalendar.setTime(startDate);
        beginCalendar.set(Calendar.DAY_OF_MONTH, 1);
        finishCalendar.setTime(endDate);
        
        List<String> monthYears = new ArrayList<>();
        while (!beginCalendar.after(finishCalendar)) {
        	monthYears.add(MONTH_NAMES[beginCalendar.get(Calendar.MONTH)] + " " + beginCalendar.get(Calendar.YEAR));
            beginCalendar.add(Calendar.MONTH, 1);
        }
        return monthYears;
	}
	
	public static String getCurrentMonthYear(Date date){
		Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return MONTH_NAMES[calendar.get(Calendar.MONTH)] + " " + calendar.get(Calendar.YEAR);
	}

	public static String getDay(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return DAYS[calendar.get(Calendar.DAY_OF_WEEK) - 1];		
	}
	
	public static Date get(String dateString, String pattern) {
		DateFormat formatter = new SimpleDateFormat(pattern);
		try {
			return formatter.parse(dateString);
		} catch (ParseException e) {
			log.debug("Failed to parse date.");
		}
		return null;
	}
	
	
}

package examples;

import com.cleo.lexicom.external.*;

/*******************************************************************************
 * This example demonstrates:
 * - How to set various types of schedules
 ******************************************************************************/
public class ConfigureSchedule {
  public static void main(String[] args)
  {
    new ConfigureSchedule();
  }
  public ConfigureSchedule()
  {
    try {
/*------------------------------------------------------------------------------
 *    Get a client instance of VLTrader
 *----------------------------------------------------------------------------*/
      ILexiCom lexicom = LexiComFactory.getVersaLex(LexiComFactory.VLTRADER,
                                                    "C:\\Program Files\\VLTrader",
                                                    LexiComFactory.CLIENT_ONLY);

      // get the current schedule
      ISchedule schedule = lexicom.getSchedule();
            
      ISchedule.Item item;
      ISchedule.Item.Calendar calendar;
      ISchedule.Item.Calendar.Time time;
      
/*------------------------------------------------------------------------------
 *    Schedule send to be autosend
 *----------------------------------------------------------------------------*/
      String[] path = new String[] {"Looptest FTP", "myMailbox", "send"};
      item = schedule.newItem(path);
      item.setOnlyIfFile(true, true);
      schedule.updateItem(item, false);
      
/*------------------------------------------------------------------------------
 *    Schedule receive to be weekdays 8am-4pm every 2 hours and weekends at noon
 *----------------------------------------------------------------------------*/
      path[ILexiCom.ACTION_INDEX] = "receive";
      item = schedule.newItem(path);
      item.setPeriod(ISchedule.WEEKLY);
      calendar = item.addCalendar();
      calendar.setDays(ISchedule.MONDAY + ISchedule.THROUGH + ISchedule.FRIDAY);
      time = calendar.addTime();
      time.setStart("8:00");
      time.setRecurring("2:00");
      time.setUntil("16:00");
      calendar = item.addCalendar();
      calendar.setDays(ISchedule.SUNDAY + ISchedule.SATURDAY);
      time = calendar.addTime();
      time.setStart("12:00");
      schedule.updateItem(item, false);

/*------------------------------------------------------------------------------
 *    Schedule report to be first monday of every month at 8am
 *----------------------------------------------------------------------------*/
      path[ILexiCom.ACTION_INDEX] = "report";
      item = schedule.newItem(path);
      item.setPeriod(ISchedule.MONTHLY);
      calendar = item.addCalendar();
      calendar.setMonths(ISchedule.JANUARY + ISchedule.THROUGH + ISchedule.DECEMBER);
      calendar.setDaysOfMonth(ISchedule.FIRST, ISchedule.MONDAY);
      time = calendar.addTime();
      time.setStart("8:00");
      schedule.updateItem(item, false);
      
/*------------------------------------------------------------------------------
 *    Save the settings
 *----------------------------------------------------------------------------*/
      schedule.save();

/*------------------------------------------------------------------------------
 *    Close down the VLTrader instance
 *----------------------------------------------------------------------------*/
      lexicom.close();

    } catch (Exception ex) {
      ex.printStackTrace();
    }

    System.exit(0);
  }
}
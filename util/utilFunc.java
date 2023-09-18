package util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class utilFunc {
    
    public static String getTime(){
        LocalDateTime current = LocalDateTime.now();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String timeStamp = current.format(format);    
        return timeStamp;
    }
}

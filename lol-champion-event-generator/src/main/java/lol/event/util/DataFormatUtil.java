package lol.event.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DataFormatUtil {
    /**
     * Main 을 실행 할 시 UTC 기준의 현재 시간을 출력하는 메서드.
     * @return yyyyMMdd 의 포맷형식을 가진 String 형 formattedNow 를 반환합니다.
     */
    public static String getToday() {
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        return now.format(formatter);
    }
}

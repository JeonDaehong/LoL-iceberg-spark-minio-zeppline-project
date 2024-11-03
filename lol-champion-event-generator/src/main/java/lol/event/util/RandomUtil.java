package lol.event.util;

import java.util.Random;

public class RandomUtil {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    public static final Random R = new Random();

    public static int getRandomPlayTime() {
        return R.nextInt(3600 - (1200) + 1) + (1200);
    }

    public static String getRandomNickname() {
        int length = 8 + R.nextInt(9); // 8 ~ 16 사이의 길이
        StringBuilder nickname = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = R.nextInt(CHARACTERS.length());
            nickname.append(CHARACTERS.charAt(index));
        }

        return nickname.toString();
    }

}

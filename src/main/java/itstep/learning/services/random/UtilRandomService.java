package itstep.learning.services.random;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.services.time.TimeService;

import java.util.Random;

@Singleton
public class UtilRandomService implements RandomService {
    private final Random random;
    private static final String RandomString = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final String FileName = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_-";

    @Inject
    public UtilRandomService(TimeService timeService) {
        this.random = new Random(timeService.getTimestamp());
    }

    @Override
    public int randomInt() {
        return random.nextInt(1000);
    }

    @Override
    public String randomString(int length) {
        return generateRandomString(length, RandomString);
    }

    @Override
    public String randomFileName(int length) {
        return generateRandomString(length, FileName);
    }

    private String generateRandomString(int length, String characterSet) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(characterSet.charAt(random.nextInt(characterSet.length())));
        }
        return sb.toString();
    }
}
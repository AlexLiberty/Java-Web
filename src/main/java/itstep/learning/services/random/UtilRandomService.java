package itstep.learning.services.random;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.services.time.TimeService;

import java.util.Random;

@Singleton
public class UtilRandomService implements RandomService {
    private final Random random;

    @Inject
    public UtilRandomService(TimeService timeService) {
        this.random = new Random(timeService.getTimestamp());
    }

    @Override
    public int randomInt() {
        return random.nextInt(1000);
    }
}
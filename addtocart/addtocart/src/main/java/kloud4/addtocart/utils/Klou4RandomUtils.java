package kloud4.addtocart.utils;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class Klou4RandomUtils {

	private static AtomicLong atomicCounter = new AtomicLong();

    public static String createCartId() {
        String currentCounter = String.valueOf(atomicCounter.getAndIncrement());
        String uniqueId = UUID.randomUUID().toString();

        return "cart-" + uniqueId + "-" + currentCounter;
    }
    
    public static String createShopperId() {
        String currentCounter = String.valueOf(atomicCounter.getAndIncrement());
        String uniqueId = UUID.randomUUID().toString();

        return "shopper-" + uniqueId + "-" + currentCounter;
    }
}

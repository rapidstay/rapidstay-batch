package com.rapidstay.xap.batch.job.hotelbeds;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Formatter;

/**
 * Hotelbeds API 인증용 시그니처 생성 유틸
 * signature = HMAC_SHA256(apikey + secret + timestamp)
 */
public class HotelbedsAuthUtil {

    public static String generateSignature(String apiKey, String secret, long timestamp) {
        try {
            String message = apiKey + secret + timestamp;
            Mac hasher = Mac.getInstance("HmacSHA256");
            hasher.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = hasher.doFinal(message.getBytes(StandardCharsets.UTF_8));

            Formatter formatter = new Formatter();
            for (byte b : hash) formatter.format("%02x", b);
            return formatter.toString();

        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to generate Hotelbeds signature", e);
        }
    }
}

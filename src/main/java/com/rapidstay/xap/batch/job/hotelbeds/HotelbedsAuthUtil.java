package com.rapidstay.xap.batch.job.hotelbeds;

import org.apache.commons.codec.digest.DigestUtils;

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
            String toSign = apiKey + secret + timestamp;
            return DigestUtils.sha256Hex(toSign);


        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to generate Hotelbeds signature", e);
        }
    }
}

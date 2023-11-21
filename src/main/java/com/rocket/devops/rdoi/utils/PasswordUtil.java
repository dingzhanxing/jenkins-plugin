package com.rocket.devops.rdoi.utils;

import org.jasypt.intf.service.JasyptStatelessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * To align with RDOp encryption
 */
public final class PasswordUtil {
    private static final Logger logger = LoggerFactory.getLogger(PasswordUtil.class);
    private static final JasyptStatelessService service = new JasyptStatelessService();

    private static final String RANDOM_SALT_GENERATOR = "org.jasypt.salt.RandomSaltGenerator";

    public static final String ENCRYPT_PASSWORD_PREFIX = "ENC(";

    public static String getPasswordKey() {
        String dogs = "The dogs bark and the caravan moves on";
        String[] a = dogs.split(" ");
        StringBuilder p = new StringBuilder("x");
        for (int i = a.length - 1; i >= 0; --i) {
            p.append((a[i] + i));
        }

        return p.toString();
    }

    /**
     * encrypt the content want wrap it with ENC()
     * @param str
     * @return
     */
    public static String encryptWithENC(String str) {
        String result = encrypt(str);
        return String.format("%s%s)", ENCRYPT_PASSWORD_PREFIX, result);
    }

    /**
     * encrypt the content with specific password
     * @param str
     * @return
     */
    public static String encrypt(String str) {
        return service.encrypt(str, getPasswordKey(), // password,
                null, null, null, // argumentValues.getProperty(ArgumentNaming.ARG_ALGORITHM),
                null, null, null, // argumentValues.getProperty(ArgumentNaming.ARG_KEY_OBTENTION_ITERATIONS),
                null, null, RANDOM_SALT_GENERATOR, // argumentValues.getProperty(ArgumentNaming.ARG_SALT_GENERATOR_CLASS_NAME),
                null, null, null, // argumentValues.getProperty(ArgumentNaming.ARG_PROVIDER_NAME),
                null, null, null, // argumentValues.getProperty(ArgumentNaming.ARG_PROVIDER_CLASS_NAME),
                null, null, null, // argumentValues.getProperty(ArgumentNaming.ARG_STRING_OUTPUT_TYPE),
                null, null);
    }
}

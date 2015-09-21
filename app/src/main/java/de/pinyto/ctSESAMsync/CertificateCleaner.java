package de.pinyto.ctSESAMsync;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Cleans pasted certificates.
 */
public class CertificateCleaner {

    public static String cleanCertificate(String pastedCertificate) {
        String certificateRegEx = "(-----BEGIN CERTIFICATE-----)" +
                "[\\s\\n]*([a-zA-Z0-9+/\\s\\n]+)[\\s\\n]*" +
                "(-----END CERTIFICATE-----)";
        Pattern pattern = Pattern.compile(certificateRegEx);
        Matcher matcher = pattern.matcher(pastedCertificate);
        if (matcher.matches()) {
            return matcher.group(1) + "\n" +
                    matcher.group(2).replaceAll("\\s","") + "\n" +
                    matcher.group(3);
        }
        return pastedCertificate;
    }

}

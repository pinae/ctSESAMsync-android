package de.pinyto.ctSESAMsync;

import junit.framework.TestCase;

/**
 * Test certificate cleaning.
 */
public class CertificateCleanerTest extends TestCase {

    public void testCleanCertificate() {
        String pasted = "-----BEGIN CERTIFICATE----- MIID+zCCAuOgAwIBAgICEAEwDQYJKoZIhvcNAQEFBQA" +
                "wfjELMAkGA1UEBhMCREUx FTATBgNVBAgMDExvd2VyIFNheG9ueTEbMBkGA1UECgwSRXJzYXR6d29yb" +
                "GQgVW5s dGQuMRgwFgYDVQQDDA9lcnNhdHp3b3JsZC5uZXQxITAfBgkqhkiG9w0BCQEWEmNh QGVyc2" +
                "F0endvcmxkLm5ldDAeFw0xNTA2MDIwODA0MDFaFw0yNTA2MDEwODA0MDFa MIGFMQswCQYDVQQGEwJE" +
                "RTEVMBMGA1UECAwMTG93ZXIgU2F4b255MRswGQYDVQQK DBJFcnNhdHp3b3JsZCBVbmx0ZC4xGDAWBg" +
                "NVBAMMD2Vyc2F0endvcmxkLm5ldDEo MCYGCSqGSIb3DQEJARYZd2VibWFzdGVyQGVyc2F0endvcmxk" +
                "Lm5ldDCCASIwDQYJ KoZIhvcNAQEBBQADggEPADCCAQoCggEBANE1oiilVhs0/Z7CcAKkmOtZVVgwjV" +
                "Dr zLsDRTFRS9TQApU9D3NRh4kkt7p5udm96SDfTPEb6DSa0UluzsLmkVedjno6SeBS eMDEuS82boW" +
                "3M6ZuevLBfSYaTc6JgBPnE0b0ua1ItNNDOYOZN4VsWT7V0svrRVGV PSDfVJcjGF/iHfLwfWeKZ6k8x" +
                "yAEHrWOYSgKgriVGE4YcS4DQDbNpqj3U8FZqGPF lPnNmFoC/XPIfNBl5fPwbUQkbClOXs0MmN34I0/" +
                "RI3hQQMzTm7/okoR8hTh4mLN5 9UVR0lX8nbzV0g68mA5rYvo3xzsAtZtu0NYO+DoknzBJsATszJPWT" +
                "msCAwEAAaN7 MHkwCQYDVR0TBAIwADAsBglghkgBhvhCAQ0EHxYdT3BlblNTTCBHZW5lcmF0ZWQg Q2" +
                "VydGlmaWNhdGUwHQYDVR0OBBYEFABoaceS84UrimZebPmCjIAxwwjxMB8GA1Ud IwQYMBaAFGtlqDTm" +
                "6IKPH8VgSjnJFOsZBhVtMA0GCSqGSIb3DQEBBQUAA4IBAQDB tDpwC7oDVbgq6kHp0R5ath72UP2vD2" +
                "woXy6JsJx14ZzpcpM9EWZbzVF6FSx+kZnn FzkR2xGHqxcEfY1TDnAt1xDz8UaIY6yz2BDgYtRqNlJT" +
                "leDlRbxvH5g5J9t7DgTu G3nV0XfddAlph04nkfVZW84LzyQWMNxxS/e2W/DZxUBbGImb50VsHMhgoM" +
                "NunxgY wlk8mEKWKfV8K5sbhqktCpHuGv3mcXgkxBdatzlpo5P6rhpg9AbZdlh+Wsp3/BkR hDoZmCP" +
                "2iZ5B8rm+H5zEMNCZ0UG1w5VPORXsCkdjJyhCylbB54WhBHWDT5R4Yzq7 Apqh1RZhrfv2hQtJS5Ir " +
                "-----END CERTIFICATE-----";
        assertEquals("-----BEGIN CERTIFICATE-----\nMIID+zCCAuOgAwIBAgICEAEwDQYJKoZIhvcNAQEFBQAwf" +
                        "jELMAkGA1UEBhMCREUxFTATBgNVBAgMDExvd2VyIFNheG9ueTEbMBkGA1UECgwSRXJzYXR6" +
                        "d29ybGQgVW5sdGQuMRgwFgYDVQQDDA9lcnNhdHp3b3JsZC5uZXQxITAfBgkqhkiG9w0BCQE" +
                        "WEmNhQGVyc2F0endvcmxkLm5ldDAeFw0xNTA2MDIwODA0MDFaFw0yNTA2MDEwODA0MDFaMI" +
                        "GFMQswCQYDVQQGEwJERTEVMBMGA1UECAwMTG93ZXIgU2F4b255MRswGQYDVQQKDBJFcnNhd" +
                        "Hp3b3JsZCBVbmx0ZC4xGDAWBgNVBAMMD2Vyc2F0endvcmxkLm5ldDEoMCYGCSqGSIb3DQEJ" +
                        "ARYZd2VibWFzdGVyQGVyc2F0endvcmxkLm5ldDCCASIwDQYJKoZIhvcNAQEBBQADggEPADC" +
                        "CAQoCggEBANE1oiilVhs0/Z7CcAKkmOtZVVgwjVDrzLsDRTFRS9TQApU9D3NRh4kkt7p5ud" +
                        "m96SDfTPEb6DSa0UluzsLmkVedjno6SeBSeMDEuS82boW3M6ZuevLBfSYaTc6JgBPnE0b0u" +
                        "a1ItNNDOYOZN4VsWT7V0svrRVGVPSDfVJcjGF/iHfLwfWeKZ6k8xyAEHrWOYSgKgriVGE4Y" +
                        "cS4DQDbNpqj3U8FZqGPFlPnNmFoC/XPIfNBl5fPwbUQkbClOXs0MmN34I0/RI3hQQMzTm7/" +
                        "okoR8hTh4mLN59UVR0lX8nbzV0g68mA5rYvo3xzsAtZtu0NYO+DoknzBJsATszJPWTmsCAw" +
                        "EAAaN7MHkwCQYDVR0TBAIwADAsBglghkgBhvhCAQ0EHxYdT3BlblNTTCBHZW5lcmF0ZWQgQ" +
                        "2VydGlmaWNhdGUwHQYDVR0OBBYEFABoaceS84UrimZebPmCjIAxwwjxMB8GA1UdIwQYMBaA" +
                        "FGtlqDTm6IKPH8VgSjnJFOsZBhVtMA0GCSqGSIb3DQEBBQUAA4IBAQDBtDpwC7oDVbgq6kH" +
                        "p0R5ath72UP2vD2woXy6JsJx14ZzpcpM9EWZbzVF6FSx+kZnnFzkR2xGHqxcEfY1TDnAt1x" +
                        "Dz8UaIY6yz2BDgYtRqNlJTleDlRbxvH5g5J9t7DgTuG3nV0XfddAlph04nkfVZW84LzyQWM" +
                        "NxxS/e2W/DZxUBbGImb50VsHMhgoMNunxgYwlk8mEKWKfV8K5sbhqktCpHuGv3mcXgkxBda" +
                        "tzlpo5P6rhpg9AbZdlh+Wsp3/BkRhDoZmCP2iZ5B8rm+H5zEMNCZ0UG1w5VPORXsCkdjJyh" +
                        "CylbB54WhBHWDT5R4Yzq7Apqh1RZhrfv2hQtJS5Ir\n-----END CERTIFICATE-----",
                CertificateCleaner.cleanCertificate(pasted));
    }

}

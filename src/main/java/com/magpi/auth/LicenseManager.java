package com.magpi.auth;

import java.io.*;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Enumeration;
import java.util.Scanner;

/**
 * Manages hardware fingerprinting and license validation.
 */
public class LicenseManager {

    private static final String LICENSE_FILE = "license.key";
    // This salt should be kept secret and ideally obfuscated
    private static final String SECRET_SALT = "VinzeMagnafieldControls_2025_Secure_Salt_#9928";

    /**
     * Generates a unique hardware fingerprint for this machine.
     * Combines MAC address, OS details, and CPU core count.
     */
    public static String getHardwareFingerprint() {
        try {
            StringBuilder sb = new StringBuilder();

            // 1. OS Information
            sb.append(System.getProperty("os.name"));
            sb.append(System.getProperty("os.arch"));
            sb.append(System.getProperty("os.version"));

            // 2. CPU Cores
            sb.append(Runtime.getRuntime().availableProcessors());

            // 3. MAC Address (of the first found non-loopback interface)
            try {
                Enumeration<NetworkInterface> networks = NetworkInterface.getNetworkInterfaces();
                while (networks.hasMoreElements()) {
                    NetworkInterface network = networks.nextElement();
                    byte[] mac = network.getHardwareAddress();
                    if (mac != null && mac.length > 0 && !network.isLoopback() && !network.isVirtual()) {
                        for (byte b : mac) {
                            sb.append(String.format("%02X", b));
                        }
                        break; // Use the first valid one
                    }
                }
            } catch (Exception e) {
                // Fallback if network interface fails
                sb.append("NoNetwork");
            }

            // 4. User Name (Optional - binds to specific user account)
            sb.append(System.getProperty("user.name"));

            // Hash the gathered info
            return hash(sb.toString());

        } catch (Exception e) {
            e.printStackTrace();
            return "UNKNOWN_ID";
        }
    }

    /**
     * Validates if the current machine has a valid license.
     */
    public static boolean isLicensed() {
        File file = new File(LICENSE_FILE);
        if (!file.exists()) {
            return false;
        }

        try (Scanner scanner = new Scanner(file)) {
            if (scanner.hasNextLine()) {
                String storedKey = scanner.nextLine().trim();
                return isValidKey(storedKey);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Checks if a given key is valid for this machine.
     */
    public static boolean isValidKey(String key) {
        String expectedKey = generateLicenseKey(getHardwareFingerprint());
        return expectedKey.equals(key);
    }

    /**
     * Generates the expected license key for a given hardware ID.
     * In a real scenario, this logic would ONLY exist on your key generator tool,
     * not in the client app. But for self-validation (symmetric), we include it
     * here.
     * To make this secure, you would use Asymmetric encryption (Public/Private
     * key).
     * 
     * For this implementation: Key = SHA256(HardwareID + Salt)
     */
    public static String generateLicenseKey(String hardwareId) {
        return hash(hardwareId + SECRET_SALT);
    }

    /**
     * Saves the license key to the local file.
     */
    public static void saveLicense(String key) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(LICENSE_FILE))) {
            writer.println(key);
        }
    }

    private static String hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString().toUpperCase();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

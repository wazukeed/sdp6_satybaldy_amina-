package org.example;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class SecurityRules {
    private static final Pattern CREDENTIAL_LINE = Pattern.compile(
            "(?i)\\b(password|passwd|secret|api[_-]?key|token)\\b\\s*[:=]\\s*['\"]?(\\$\\{[^}]+}|<[^>]+>|[^'\"\\s#;,}]+)");
    private static final Pattern PLACEHOLDER = Pattern.compile(
            "(?i)(\\$\\{[^}]+}|<[^>]+>|changeme|example|placeholder|your[_-].*)");
    private static final Pattern INSECURE_SETTING = Pattern.compile(
            "(?im)^\\s*(server\\.ssl\\.enabled|security\\.require-https|cookie\\.secure|debug|trust\\.all)\\s*[:=]\\s*(false|true)\\s*$");
    private static final Pattern EXPOSED_MANAGEMENT_ENDPOINTS = Pattern.compile(
            "(?im)^\\s*management\\.endpoints\\.web\\.exposure\\.include\\s*[:=]\\s*\\*\\s*$");

    private SecurityRules() {
    }

    static boolean hasHardcodedCredential(String content) {
        Matcher matcher = CREDENTIAL_LINE.matcher(content);
        while (matcher.find()) {
            if (!PLACEHOLDER.matcher(matcher.group(2)).matches()) {
                return true;
            }
        }
        return false;
    }

    static boolean hasInsecureSetting(String content) {
        Matcher matcher = INSECURE_SETTING.matcher(content);
        while (matcher.find()) {
            String setting = matcher.group(1).toLowerCase(Locale.ROOT);
            String value = matcher.group(2).toLowerCase(Locale.ROOT);
            if ((setting.equals("debug") || setting.equals("trust.all")) == value.equals("true")) {
                return true;
            }
            if (!setting.equals("debug") && !setting.equals("trust.all") && value.equals("false")) {
                return true;
            }
        }
        return EXPOSED_MANAGEMENT_ENDPOINTS.matcher(content).find();
    }
}

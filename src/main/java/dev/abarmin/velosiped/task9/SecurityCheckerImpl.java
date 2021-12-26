package dev.abarmin.velosiped.task9;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class SecurityCheckerImpl implements SecurityChecker {
    @Override
    public boolean isGoodAuthorization(String authorizationValue) {
        if (authorizationValue == null) {
            return false;
        } else {
            return new String(Base64.getDecoder().decode(authorizationValue.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8).equals("Batman");
        }
    }
}

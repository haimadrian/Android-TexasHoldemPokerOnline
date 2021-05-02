package org.hit.android.haim.texasholdem.server.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.log4j.Log4j2;
import org.hit.android.haim.texasholdem.server.model.bean.user.User;
import org.hit.android.haim.texasholdem.server.model.bean.user.UserImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;

/**
 * JWT utilities is used to generate JWT token from user, and parse a JWT token into user.
 *
 * @author Haim Adrian
 * @since 21-Mar-21
 */
@Component
@Log4j2
public class JwtUtils {
    private static final String USER_NAME = "userName";

    @Value("${server.ssl.key-store}")
    private String keyStoreFile;

    @Value("${server.ssl.key-store-password}")
    private String keyStorePass;

    @Value("${server.ssl.key-alias}")
    private String keyStoreAlias;

    /**
     * A secret key to use for signing the JWT.<br/>
     * We generate a new one every time the server starts up.
     */
    private Key key;

    /**
     * Tries to parse the specified String as a JWT token.<br/>
     * If successful, return User object with user identifier and user name (extracted from token).<br/>
     * If unsuccessful (token is invalid or not containing all required user properties), simply return null.
     *
     * @param jwtToken The JWT token to parse
     * @return The User object extracted from specified token or null if the token is invalid.
     */
    public User parseToken(String jwtToken) {
        initKeyIfNeeded();
        User user = null;

        try {
            //@formatter:off
         Claims body = Jwts.parserBuilder()
                           .setSigningKey(key)
                           .build()
                           .parseClaimsJws(jwtToken.replace("Bearer ", ""))
                           .getBody();
         //@formatter:on

            user = new UserImpl(body.getSubject(), String.valueOf(body.get(USER_NAME)));
        } catch (Exception e) {
            log.error("Error has occurred while parsing JWT token: " + e.toString(), e);
        }

        return user;
    }

    /**
     * Generates a JWT token containing userId as subject, and user name as additional claim.<br/>
     * Tokens validity is infinite.
     *
     * @param user The user for which the token will be generated
     * @return The JWT token
     */
    public String generateToken(User user) {
        initKeyIfNeeded();
        Claims claims = Jwts.claims().setSubject(user.getId());
        claims.put(USER_NAME, user.getName());
        return Jwts.builder().setClaims(claims).signWith(key).compact();
    }

    private void initKeyIfNeeded() {
        if (key == null) {
            synchronized (this) {
                if (key == null) {
                    try {
                        key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
                        log.info("Key has been loaded and ready for use by JwtUtils");
                    } catch (Exception e) {
                        log.error("Error has occurred while loading key store: " + e.toString(), e);
                    }
                }
            }
        }
    }
}


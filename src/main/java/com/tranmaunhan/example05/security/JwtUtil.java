package com.tranmaunhan.example05.security;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

@Component
public class JwtUtil {

    @Value("${jwt_secret}")
    private String secret;

    // ===============================
    // TẠO JWT
    // ===============================
    public String generateToken(String email)
            throws IllegalArgumentException, JWTCreationException {

        return JWT.create()
                .withSubject(email)
                .withClaim("email", email)
                .withIssuedAt(new Date())
                .withIssuer("Event Scheduler")
                .sign(Algorithm.HMAC256(secret));
    }


    public String validateTokenAndRetrieveSubject(String token)
            throws JWTVerificationException {

        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                .withIssuer("Event Scheduler")
                .build();

        DecodedJWT jwt = verifier.verify(token);
        System.out.println(token);

        // ưu tiên subject, fallback sang claim email cho an toàn
        String subject = jwt.getSubject();
        if (subject != null && !subject.isBlank()) {
            return subject;
        }

        return jwt.getClaim("email").asString();
    }
}

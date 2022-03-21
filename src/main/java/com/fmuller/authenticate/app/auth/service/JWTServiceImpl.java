package com.fmuller.authenticate.app.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fmuller.authenticate.app.auth.mixing.SimpleGrantedAuthorityMixin;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;

@Service
public class JWTServiceImpl implements IJWTService {

    public static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(Base64Utils.encodeToString("algunaLlaveSecretaasdasdasdasdwwdwdqdasdqwcqwrdqweqwtfqwfqwfqwr".getBytes()).getBytes());

    public final long EXPIRATION_DATE = 6000000L;

    public static final String TOKEN_PREFIX = "Bearer ";

    public static final String HEADER_STRING = "Authorization";

    private Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public String create(Authentication auth) throws JsonProcessingException {

        String username = ((User) auth.getPrincipal()).getUsername();

        Collection<? extends GrantedAuthority> roles = auth.getAuthorities();

        Claims claims = Jwts.claims();

        claims.put("authorities", new ObjectMapper().writeValueAsString(roles));

        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .signWith(SECRET_KEY)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_DATE))
                .compact();

        return token;
    }

    @Override
    public boolean validate(String token) {

        Claims claims = null;
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }

    }

    @Override
    public Claims getClaims(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(this.resolve(token))
                .getBody();

        return claims;
    }

    @Override
    public String getUsername(String token) {

        return getClaims(token).getSubject();
    }

    @Override
    public Collection<? extends GrantedAuthority> getRoles(String token) throws IOException {

        Object roles = getClaims(token).get("authorities");

        Collection<? extends GrantedAuthority> authority = Arrays.asList(new ObjectMapper()
                .addMixIn(SimpleGrantedAuthority.class, SimpleGrantedAuthorityMixin.class)
                .readValue(roles.toString().getBytes(), SimpleGrantedAuthority[].class));

        return authority;
    }

    @Override
    public String resolve(String token) {

        if (token != null && token.startsWith(TOKEN_PREFIX)) {

            return token.replace(TOKEN_PREFIX, "");
        }

        return null;
    }

}

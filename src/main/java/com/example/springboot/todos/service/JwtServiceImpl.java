package com.example.springboot.todos.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;


@Service
public class JwtServiceImpl implements JwtService {

    @Value("${spring.jwt.secret}")
    private String SECRET_KEY;
    @Value("${spring.jwt.expiration}")
    private long JWT_EXPIRATION;

    @Override
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);//JWT token'dan kullanıcı adını çıkarır.
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);//extractAllClaims ile token'daki tüm bilgileri alır

        return claimsResolver.apply(claims);//Parametre olarak gelen fonksiyon (claimsResolver) ile istenen spesifik veriyi seçer
    }

    private Claims extractAllClaims(String token) {
      /*  return Jwts.parser()//parser olusturuyorz
                .setSigningKey(getSigningKey())//imza doğrulama için signingkey set ediyoruz
                .build()//build
                .parseClaimsJws(token)//tokeni parse ediyoruz
                .getBody();//tokenin body'sindeki claimleri dönüyoruz*/

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    @Override
    public String generateToken(Map<String, Object> claims, UserDetails userDetails) {
        long currentTimeMillis = System.currentTimeMillis();
        String token = Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(currentTimeMillis))
                .expiration(new Date(currentTimeMillis + JWT_EXPIRATION))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
        return token;
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

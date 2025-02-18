package com.example.Shares.auth.config;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.function.Function;

@Component
public class JWTUtil {
//
//    // - Here, a private constant named **`jwtSignKey`** is declared to represent the secret key, This key is used for signing and verifying JWT tokens.
//    // - If the token contains this key then its from out backend system otherwise it maybe a token generated from a hacker
//    private final String jwtSignKey = "secret";
//
//
//    // - **`generateToken`** is a public method that takes a **`CustomeUserDetails`** object as a parameter.
//    // - It calls the private method **`doGenerateToken`** to generate a JWT token using the user's claims and username.
//    // - claims: the pieces of information or assertions that are encoded within a JWT like the user details (role, id, username, password, phone number, email)
//    public String generateToken(CustomUserDetails userDetails) {
//        return doGenerateToken(userDetails.getClaims(), userDetails.getUsername());
//    }
//
//    /*
//    - **`doGenerateToken`** is a private method responsible for creating a JWT token.
//    - It uses the **`Jwts.builder()`** to build a JWT token with specified claims.
//    - **`setClaims(claims)`** sets custom claims, typically containing user-related information.
//    - **`setSubject(subject)`** sets the subject, often the user's username.
//    - **`setIssuedAt(new Date(System.currentTimeMillis()))`** sets the token's issuance date.
//    - **`setExpiration(new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(24)))`** sets the token's expiration date, which is 24 hours from the issuance.
//    - **`signWith(SignatureAlgorithm.HS256, jwtSignKey).compact()`** signs the token using the HMAC SHA-256 algorithm and the provided signing key.
//    - HMAC SHA-256 is like a secret code maker for messages. It takes a message and a secret key and mixes them up to create a unique code (like a password) that only someone with the key can make. It helps ensure that messages haven't been tampered with during transmission and that only the right people can read them. It's like a special lock for your messages, keeping them safe and private.
//     */
//    private String doGenerateToken(Map<String, Object> claims, String subject) {
//        return Jwts.builder()
//                .setClaims(claims)
//                .setSubject(subject)
//                .setIssuedAt(new Date(System.currentTimeMillis())) /* this line mean when the token is created this will help us when we want to calculate the
//                 * expiration date for the token so we can know if the token valid or not
//                 */
//                .setExpiration(new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(24)))
//                .signWith(SignatureAlgorithm.HS256, jwtSignKey).compact();
//    }
//
//    /*
//    - **`getClaimFromToken`** is a generic method that extracts a specific claim from a JWT token.
//    - It takes a **`token`** and a **`claimsResolver`** function as parameters.
//    - It uses the **`getAllClaimsFromToken`** method to retrieve all claims from the token and then applies the **`claimsResolver`** function to obtain the desired claim value.
//    - **`claimsResolver`:**  a **`claimsResolver`** is like a decoder for the secret messages hidden inside the token. When you have a JWT token, it contains information called "claims," which are like notes or data. The **`claimsResolver`** is a function that helps you retrieve specific pieces of information (claims) from the token.
//
//    For example, if your JWT token contains your username, email, and role, you can use a **`claimsResolver`** to easily extract your username or email from the token without having to decode the entire token yourself. It simplifies the process of getting data from the token by providing a convenient way to access individual pieces of information stored within it.
//     */
//    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
//        final Claims claims = getAllClaimsFromToken(token);
//        return claimsResolver.apply(claims);
//    }
//
//    /*
//    - **`getAllClaimsFromToken`** is a private method that parses a JWT token and extracts all claims from it.
//    - It uses the **`Jwts.parser()`** method to create a JWT parser, sets the signing key, and then parses the token to retrieve its claims.
//     */
//    private Claims getAllClaimsFromToken(String token) {
//        return Jwts.parser().setSigningKey(jwtSignKey).parseClaimsJws(token).getBody();
//    }
//
//    /*
//    - **`getExpirationDateFromToken`** is a public method that returns the expiration date of a JWT token.
//    - It uses the **`getClaimFromToken`** method to extract the expiration claim from the token.
//     */
//    public Date getExpirationDateFromToken(String token) {
//        return getClaimFromToken(token, Claims::getExpiration);
//    }
//
//    /*
//    - **`isTokenExpired`** is a private method that checks if a JWT token has expired.
//    - It gets the token's expiration date and compares it to the current date to determine if it's before the current date.
//     */
//    private boolean isTokenExpired(String token) {
//        Date tokenExpirationDate = getExpirationDateFromToken(token);
//        return tokenExpirationDate.before(new Date());
//    }
//
//    /*
//    - **`isTokenValid`** is a public method that checks if a JWT token is valid.
//    - It uses the **`isTokenExpired`** method to check if the token has expired.
//    - If an exception occurs during the validation process, it returns **`false`** to indicate that the token is not valid.
//     */
//    public boolean isTokenValid(String token) {
//        try {
//            return !isTokenExpired(token);
//        } catch (Exception exception) {
//            return false;
//        }
//    }
//
//    /*
//    - **`getUsernameFromToken`** is a public method that extracts the username from a JWT token.
//- It uses the **`getClaimFromToken`** method to obtain the subject claim from the token, which typically contains the username.
//     */
//    public String getUsernameFromToken(String token) {
//        return getClaimFromToken(token, Claims::getSubject);
//    }


    @Value("${jwt.secret}")
    private String jwtSecretKey;

    // Generate Token
    public String generateToken(String civilId) {
        return Jwts.builder()
                .setSubject(civilId)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 hours validity
                .signWith(SignatureAlgorithm.HS256, jwtSecretKey)
                .compact();
    }

    // Extract Username from Token
    public String extractCivilId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extract a Specific Claim
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    // Extract All Claims
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(jwtSecretKey)
                .parseClaimsJws(token)
                .getBody();
    }

    // Validate Token
    public boolean isTokenValid(String token) {
        try {
            return !getAllClaimsFromToken(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return false; // Return false if the token is invalid or expired
        }
    }

}

package com.project.shopapp.components;

import com.project.shopapp.exceptions.InvalidParamException;
import com.project.shopapp.models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.security.SecureRandom;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class JwtTokenUtils {
    @Value("${jwt.expiration}")
    private int expiration;//save to an environment variable

    @Value("${jwt.secretKey}")
    private String secretKey;

    //tạo token cho người dùng khi đăng nhập thành công
    public String generateToken(User user) throws Exception {
        //properties => claims
        Map<String,Object> claims= new HashMap<>();
//        this.generateSecretKey();
        claims.put("phoneNumber",user.getPhoneNumber());
        claims.put("userId",user.getId());
        try {
            //mã hóa JWT Token:
            String token= Jwts.builder()
                    .setClaims(claims) // làm thế nào để trích xuât các claims từ đây?
                    .setSubject(user.getPhoneNumber())
                    .setExpiration(new Date(System.currentTimeMillis()+expiration * 1000L))
                    .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                    .compact();
            return token;
        }catch (Exception e){
            //sau nay co the dung Logger thay vi su dung System.err.println
            throw new InvalidParamException("Cannot create jwt token, error: "+ e.getMessage());
        }
    }

    private Key getSignInKey(){
        byte[] bytes= Decoders.BASE64.decode(secretKey);//Decoders.BASE64.decode(NRa5tDl17x9Qk/Ik0U/zg8OmVBHGM97ElNUZ8L2PIB4=)
        return Keys.hmacShaKeyFor(bytes);//hmacShaKeyFor(Decoders.BASE64.decode("NRa5tDl17x9Qk/Ik0U/zg8OmVBHGM97ElNUZ8L2PIB4="))
    }

    private String generateSecretKey(){
        SecureRandom random=new SecureRandom();
        byte[] keyBytes=new byte[32];
        random.nextBytes(keyBytes);
        String secretKey= Encoders.BASE64.encode(keyBytes);
        return secretKey;
    }

    //giải mã JWT Token:
    private Claims extractAllClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public <T> T extractClaim(String token , Function<Claims,T> claimsResolver){
        final Claims claims=this.extractAllClaims(token);
        return  claimsResolver.apply(claims);
    }

    //kiem tra han su dung cua token
    public boolean isTokenExpired(String token){
        Date expirationDate=this.extractClaim(token,Claims::getExpiration);
        return expirationDate.before(new Date());
    }

    public String extractPhoneNumber(String token){
        return extractClaim(token,Claims::getSubject);
    }

    public boolean validateToken(String token, UserDetails userDetails){
        String phoneNumber= extractPhoneNumber(token);
        return (phoneNumber.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}

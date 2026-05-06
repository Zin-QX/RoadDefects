package com.zinqx.roaddefectsbackend.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

public class JwtUtil {
    /**
     * 生成 jwt
     * 使用 HS256 算法，私匙使用固定秘钥
     *
     * @param secretKey jwt 秘钥
     * @param ttlMillis jwt 过期时间 (毫秒)
     * @param claims    设置的信息
     * @return
     */
    public static String createJWT(String secretKey, long ttlMillis, Map<String, Object> claims) {
        // 指定签名的时候使用的签名算法，也就是 header 那部分
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

        // 生成 JWT 的时间
        long expMillis = System.currentTimeMillis() + ttlMillis;
        Date exp = new Date(expMillis);

        // 确保密钥长度至少为 256 位（32 字节）
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            // 如果密钥太短，进行填充
            byte[] paddedKey = new byte[32];
            System.arraycopy(keyBytes, 0, paddedKey, 0, Math.min(keyBytes.length, 32));
            keyBytes = paddedKey;
        }

        // 设置 jwt 的 body
        JwtBuilder builder = Jwts.builder()
                // 如果有私有声明，一定要先设置这个自己创建的私有的声明，这个是给 builder 的 claim 赋值，一旦写在标准的声明赋值之后，就是覆盖了那些标准的声明的
                .setClaims(claims)
                // 设置签名使用的签名算法和签名使用的秘钥
                .signWith(signatureAlgorithm, keyBytes)
                // 设置过期时间
                .setExpiration(exp);

        return builder.compact();
    }

    /**
     * Token 解密
     *
     * @param secretKey jwt 秘钥 此秘钥一定要保留好在服务端，不能暴露出去，否则 sign 就可以被伪造，如果对接多个客户端建议改造成多个
     * @param token     加密后的 token
     * @return
     */
    public static Claims parseJWT(String secretKey, String token) {
        // 确保密钥长度至少为 256 位（32 字节）
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            // 如果密钥太短，进行填充
            byte[] paddedKey = new byte[32];
            System.arraycopy(keyBytes, 0, paddedKey, 0, Math.min(keyBytes.length, 32));
            keyBytes = paddedKey;
        }

        // 得到 DefaultJwtParser
        Claims claims = Jwts.parser()
                // 设置签名的秘钥
                .setSigningKey(keyBytes)
                // 设置需要解析的 jwt
                .parseClaimsJws(token).getBody();
        return claims;
    }

}

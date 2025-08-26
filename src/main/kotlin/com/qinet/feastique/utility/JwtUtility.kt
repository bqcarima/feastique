package com.qinet.feastique.utility

import com.qinet.feastique.model.entity.RefreshToken
import com.qinet.feastique.model.enums.AccountType
import com.qinet.feastique.response.token.AccessTokenResponse
import com.qinet.feastique.response.token.TokenPairResponse
import com.qinet.feastique.security.HashEncoder
import com.qinet.feastique.service.RefreshTokenService
import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.util.*

/**
 * This class handles token generation and token refresh.
 * It contains sensitive data which was not injected directly but rather
 * as an environmental variable. You will have to set this up yourself
 * for testing.
 * @param jwtSecret Gotten from the environment variable jwt.secret
 * @param hashEncoder
 * @param refreshTokenService
 *
 * @author Bassey Otudor
 */
@Component
class JwtUtility(
    @Value("\${JWT_SECRET}") // injecting the jwt.secret into the variable jwtSecret
    private val jwtSecret: String,
    private val hashEncoder: HashEncoder,
    private val refreshTokenService: RefreshTokenService
) {

    /**
     * This variable contains sensitive information
     * @param jwtSecret encoded secret stored in the environment variable.
     * @param SECRET decoded secret.
     */
    private var SECRET = Keys.hmacShaKeyFor(Base64.getDecoder().decode(jwtSecret))
    private val ACCESS_TOKEN_VALIDITY_MS = 15L * 60L * 1000L
    private val REFRESH_TOKEN_VALIDITY_MS = 30L * 24 * 60 * 60L * 1000L


    /**
     * This is a helper function  the is used to generate
     * either an access or a refresh token.
     * @param id for customers and vendor.
     * @param username varies with user; customerId for customers
     * and uniqueId for vendors.
     * @param type type of token to be generated.
     * @param expiry the expiry date of the generated token.
     * @return String
     */
    private fun generateToken(
        id: Long,
        username: String,
        type: String,
        userType: AccountType,
        expiry: Long
    ): String {

        val now = Date()
        val expiryDate = Date(now.time + expiry)
        val tokenIdentifier = UUID.randomUUID().toString()

        return Jwts.builder()
            .subject(id.toString())
            .claim("username", username)
            .claim("type", type)
            .claim("userType", userType)
            .claim("tokenIdentifier", tokenIdentifier)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(SECRET, Jwts.SIG.HS256)
            .compact()
    }

    /**
     * This function generates a short-lived access token.
     * @param username is the username of the customer or vendor
     * @param userType is the type of user the token will be generated for.
     * @return String
     */
    fun generateAccessToken(id: Long, username: String, userType: AccountType): String {
        return generateToken(id, username, "access", userType, ACCESS_TOKEN_VALIDITY_MS)
    }

    /**
     * This function generates a longer-lived refresh token.
     * @param username username of the customer or vendor.
     * @param userType is the type of user the token will be generated for.
     * @return String
     */
    fun generateRefreshToken(id: Long, username: String, userType: AccountType): String {
        return generateToken(id, username, "refresh", userType, REFRESH_TOKEN_VALIDITY_MS)
    }

    /**
     * This function gets the claims from a valid token
     * @param token
     * @return Claims
     * @throws JwtException
     * @throws IllegalArgumentException
     * @throws Exception
     */
    private fun getClaims(token: String): Claims? {
        val rawToken = if(token.startsWith("Bearer ")) {
            token.removePrefix("Bearer ")

        } else token
        return try {
            Jwts.parser()
                .verifyWith(SECRET)
                .build()
                .parseSignedClaims(rawToken)
                .payload

        } catch (e: JwtException) {
            print(e)
            return null

        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid token. ${e.message}")

        } catch (e: Exception) {
            throw Exception("An unexpected error occurred. ${e.message}")
        }
    }

    /**
     * This function checks if a token is a valid access token.
     * @param token
     * @return Boolean
     */
    fun validateAccessToken(token: String): Boolean {
        val claims = getClaims(token) ?: return false
        val tokenType = claims["type"] as? String ?: return false
        return tokenType == "access"
    }


    /**
     * This function checks if a token is a valid refresh token.
     * @param token
     * @return Boolean
     */
    fun validateRefreshToken(token: String): Boolean {
        val claims = getClaims(token) ?: return false
        val tokenType = claims["type"] as? String?: return false
        return tokenType == "refresh"
    }

    /**
     * This function gets the id from a valid token.
     * fun getUserId(token: String): Long = getClaims(token)?.get("subject") as Long
     * generates a null pointer exception because the "subject" is a standard JWT claim
     * and not a custom claim.
     *
     * @param token
     * @return Long
     *
     */
    fun getUserId(token: String): Long {
        val claims = getClaims(token) ?: throw JwtException("Could not parse claims.")
        return claims.subject.toLongOrNull() ?: throw JwtException("Invalid subject claim. NoN.")
    }

    /**
     * This function gets the username from a valid token
     * @param token
     * @return String
     */
    fun getUsername(token: String): String = getClaims(token)?.get("username") as String

    /**
     * This function gets the userType from a valid token
     * @param token
     * @return String
     */
    fun getUserType(token: String): String = getClaims(token)?.get("userType") as String

    /**
     * This function gets the tokenIdentifier from a valid token
     * @param token
     * @return String
     */

    fun getTokenIdentifier(token: String): String {
        val claims = Jwts.parser()
            .verifyWith(SECRET)
            .build()
            .parseSignedClaims(token)
            .payload
        return claims.get("tokenIdentifier", String::class.java)
    }

    /**
     * This function gets the token expiration from a valid token as epoch millis
     * @param token
     * @return String
     */
    fun getExpirationEpochMillis(token: String): Long {
        return try {
            val claims = Jwts.parser()
                .verifyWith(SECRET)
                .build()
                .parseSignedClaims(token)
                .payload
            claims.expiration?.time
                ?: throw Exception("Token expiration claim is missing.")
        } catch (ex: Exception) {
            throw Exception("Token expiration cannot be verified.", ex)
        }
    }
    /**
     * This function generates a refresh token object from a raw token
     * @param id
     * @param userType
     * @param rawRefreshToken
     * @return String
     */
    fun parseToken(id: Long, userType: String, rawRefreshToken: String): RefreshToken {

        return RefreshToken(
            customerId = if(userType == AccountType.CUSTOMER.name) id else null,
            vendorId = if(userType == AccountType.VENDOR.name) id else null,
            expiresAt = Date(System.currentTimeMillis() + REFRESH_TOKEN_VALIDITY_MS),
            hashedToken = hashEncoder.encode(rawRefreshToken),
            createdAt = Instant.now(),
        )
    }

    /**
     * This function is used to generate a new pair o
     * access and refresh tokens.
     * @param id
     * @param username
     * @param userType
     * @return TokenPairResponse
     * @throws IllegalArgumentException
     */
    fun generateTokenPair(id: Long, username: String, userType: AccountType): TokenPairResponse {

        val accessToken = generateAccessToken(id, username, userType)
        val refreshToken = generateRefreshToken(id, username, userType)
        // val parsedToken = parseToken(id, userType, refreshToken)

        // refreshTokenService.storeRefreshToken(parsedToken)
        return TokenPairResponse(accessToken, refreshToken)
    }

    /**
     * This function generated a new valid access token from a valid
     * refresh token.
     * @param rawRefreshToken
     * @return AccessTokenResponse
     * @throws ResponseStatusException
     * @throws IllegalArgumentException
     */
    @Transactional
    fun refresh(rawRefreshToken: String): AccessTokenResponse {
        if(!validateRefreshToken(rawRefreshToken)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token.")
        }

        val id = getUserId(rawRefreshToken)
        val username = getUsername(rawRefreshToken)
        val userType = getUserType(rawRefreshToken).uppercase()
        getTokenIdentifier(rawRefreshToken)

        // get stored record (customer or vendor)
        val storedRefreshToken = when (userType) {
            AccountType.CUSTOMER.name -> refreshTokenService.getTokenByCustomerId(id)
            AccountType.VENDOR.name -> refreshTokenService.getTokenByVendorId(id)
            else -> null
        } ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token.")

        // Compare hashed token
        if (!hashEncoder.matches(rawRefreshToken, storedRefreshToken.hashedToken)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token.")
        }

        // server-side expiry check
        if (storedRefreshToken.expiresAt.before(Date())) {
            // delete stored token
            refreshTokenService.deleteToken(storedRefreshToken)
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expired.")
        }

        val newAccessToken = generateAccessToken(id, username, AccountType.valueOf(userType))
        return AccessTokenResponse(newAccessToken)
    }
}


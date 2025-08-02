package com.qinet.feastique.utility

import com.qinet.feastique.model.entity.RefreshToken
import com.qinet.feastique.model.enums.AccountType
import com.qinet.feastique.repository.RefreshTokenRepository
import com.qinet.feastique.response.Token
import com.qinet.feastique.response.TokenPair
import com.qinet.feastique.security.HashEncoder
import com.qinet.feastique.service.RefreshTokenService
import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.util.*
import java.lang.IllegalArgumentException

/**
 * This class handles token generation and token refresh.
 * It contains sensitive data which was not injected directly but rather
 * as an environmental variable. You will have to set this up yourself
 * for testing.
 * @param jwtSecret Gotten from the environment variable jwt.secret
 *
 * @author Bassey Otudor
 */
@Component
class JwtUtility(
    @Value("\${JWT_SECRET}") // injecting the jwt.secret into the variable jwtSecret
    private val jwtSecret: String,
    private val hashEncoder: HashEncoder,
    private val refreshTokenService: RefreshTokenService,
    private val refreshTokenRepository: RefreshTokenRepository
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
        userType: String,
        expiry: Long
    ): String {
        val now = Date()
        val expiryDate = Date(now.time + expiry)

        return Jwts.builder()
            .subject(id.toString())
            .claim("username", username)
            .claim("type", type)
            .claim("userType", userType)
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
    fun generateAccessToken(id: Long, username: String, userType: String): String {
        return generateToken(id, username, "access", userType ,ACCESS_TOKEN_VALIDITY_MS)
    }

    /**
     * This function generates a longer-lived refresh token.
     * @param username username of the customer or vendor.
     * @param userType is the type of user the token will be generated for.
     * @return String
     */
    fun generateRefreshToken(id: Long, username: String, userType: String): String {
        return generateToken(id, username, "refresh", userType,  REFRESH_TOKEN_VALIDITY_MS)
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
        return try {
            Jwts.parser()
                .verifyWith(SECRET)
                .build()
                .parseSignedClaims(token)
                .payload

        } catch (e: JwtException) {
            print(e)
            return null

        } catch (e: IllegalArgumentException) {
            print(e)
            return null

        } catch (e: Exception) {
            print(e)
            return null
        }
    }

    /**
     * This function checks if a token is a valid access token.
     * @param token
     * @return Boolean
     */
    fun validateAccessToken(token: String): Boolean {

        val claims = getClaims(token) ?: return false
        val tokenType = claims["type"] as? String?:return false
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
     * This function generates a refresh token object from a raw token
     * @param id
     * @param userType
     * @param refreshToken
     * @return String
     */
    fun parseToken(id: Long, userType: String, refreshToken: String): RefreshToken {

        return RefreshToken(
            customerId = if(userType == AccountType.CUSTOMER.name) id else null,
            vendorId = if(userType == AccountType.VENDOR.name) id else null,
            expiresAt = Date(Date().time + 30L * 24 * 60 * 60L * 1000L),
            hashedToken = hashEncoder.encode(refreshToken),
            createdAt = Instant.now(),
        )
    }

    /**
     * This function generated a new valid access token from a valid
     * refresh token.
     * @param refreshToken
     * @return Token
     * @throws ResponseStatusException
     * @throws IllegalArgumentException
     */
    @Transactional
    fun refresh(refreshToken: String): Token {
        if(!validateRefreshToken(refreshToken)) {
            throw ResponseStatusException(HttpStatusCode.valueOf(401), "Invalid refresh token.")
        }

        val id = getUserId(refreshToken)
        val username = getUsername(refreshToken)
        val userType = getUserType(refreshToken)

        val validTypes = setOf(AccountType.CUSTOMER.name, AccountType.VENDOR.name)
        if(userType !in validTypes) {
            throw ResponseStatusException(HttpStatusCode.valueOf(401),"Invalid refresh token.")
        }

        if(userType == AccountType.CUSTOMER.name) {
            refreshTokenRepository.findByCustomerId(id)
                ?: throw ResponseStatusException(HttpStatusCode.valueOf(401), "Invalid refresh token.")

        } else {
            refreshTokenRepository.findByVendorId(id)
                ?: throw ResponseStatusException(HttpStatusCode.valueOf(401), "Invalid refresh token.")
        }

        val newAccessToken = generateAccessToken(
            id = id,
            username = username,
            userType = when(userType) {
                "CUSTOMER" -> AccountType.CUSTOMER.name
                "VENDOR" -> AccountType.VENDOR.name
                else -> { throw IllegalArgumentException("Invalid userType")}
            }
        )

        return Token(newAccessToken)
    }

    /**
     * This function is used to generate a new pair o
     * access and refresh tokens.
     * @param id
     * @param username
     * @param userType
     * @return TokenPair
     * @throws IllegalArgumentException
     */
    fun generateTokenPair(id: Long, username: String, userType: String): TokenPair {
        val accessToken = generateAccessToken(
            id = id,
            username = username,
            userType = when(userType) {
                "CUSTOMER" -> AccountType.CUSTOMER.name
                "VENDOR" -> AccountType.VENDOR.name
                else -> { throw IllegalArgumentException("Invalid userType")
                }
            }
        )

        val refreshToken = generateRefreshToken(
            id = id,
            username = username,
            userType = when(userType) {
                "CUSTOMER" -> AccountType.CUSTOMER.name
                "VENDOR" -> AccountType.VENDOR.name
                else -> { throw IllegalArgumentException("Invalid")}
            }
        )

        val parsedToken = parseToken(id, userType, hashEncoder.encode(refreshToken))

        refreshTokenService.storeRefreshToken(parsedToken)
        return TokenPair(accessToken, refreshToken)
    }
}
package com.qinet.feastique.service

import com.qinet.feastique.model.dto.LoginDto
import com.qinet.feastique.model.dto.LogoutDto
import com.qinet.feastique.model.dto.customer.SignupDto
import com.qinet.feastique.model.dto.vendor.VendorSignupDto
import com.qinet.feastique.model.entity.Customer
import com.qinet.feastique.model.entity.Vendor
import com.qinet.feastique.response.token.AccessTokenResponse
import com.qinet.feastique.response.token.TokenPairResponse
import com.qinet.feastique.service.customer.CustomerService
import com.qinet.feastique.service.vendor.VendorService
import com.qinet.feastique.utility.JwtUtility
import io.jsonwebtoken.MalformedJwtException
import org.springframework.stereotype.Service

@Service
class AuthenticationService(
    private val customerService: CustomerService,
    private val vendorService: VendorService,
    private val jwtUtility: JwtUtility,
    private val userSessionService: UserSessionService,
    private val refreshTokenService: RefreshTokenService
) {

    /**
     * Delegate signup to customer service
     */
    fun handleCustomerSignup(signupDto: SignupDto): Customer {
        return customerService.signupCustomer(signupDto)
    }

    /**
     * Login flow for customers:
     * - delegate auth to customerService (which returns token pair)
     * - extract tokenIdentifier and expiry from access token
     * - create a server-side session so logout is trivial
     * @param LoginDto
     * @return [TokenPairResponse]
     */
    fun handleCustomerLogin(loginDto: LoginDto): TokenPairResponse {
        val tokenPair = customerService.login(loginDto)

        // Extract tokenIdentifier and expiry from the access token
        val accessToken = tokenPair.accessToken
        val tokenIdentifier = jwtUtility.getTokenIdentifier(accessToken)
        val accessTokenExpiryEpochMillis = jwtUtility.getExpirationEpochMillis(accessToken)

        // Persist server-side session
        val customerId = jwtUtility.getUserId(accessToken)
        val userType = jwtUtility.getUserType(accessToken)

        val refreshTokenEntity = jwtUtility.parseToken(customerId, userType, tokenPair.refreshToken)
        refreshTokenService.storeRefreshToken(refreshTokenEntity)

        userSessionService.createSession(
            tokenIdentifier = tokenIdentifier,
            userId = customerId,
            userType = userType,
            expiresAtEpocMillis = accessTokenExpiryEpochMillis
        )

        return tokenPair
    }

    /**
     * Delegate signup to vendor service
     */
    fun handleVendorSignup(vendorSignupDto: VendorSignupDto): Vendor {
        return vendorService.signup(vendorSignupDto)
    }

    /**
     * Login flow for vendors:
     * - delegate auth to vendorService (which returns token pair)
     * - extract tokenIdentifier and expiry from access token
     * - create a server-side session so logout is trivial
     * @param LoginDto
     * @return [TokenPairResponse]
     */
    fun handleVendorLogin(loginDto: LoginDto): TokenPairResponse {
        val tokenPair = vendorService.login(loginDto)

        // Extract tokenIdentifier and expiry from the access token
        val accessToken = tokenPair.accessToken
        val tokenIdentifier = jwtUtility.getTokenIdentifier(accessToken)
        val accessTokenExpiryEpochMillis = jwtUtility.getExpirationEpochMillis(accessToken)

        val vendorId = jwtUtility.getUserId(accessToken)
        val userType = jwtUtility.getUserType(accessToken)

        // Persist server-side session
        val refreshTokenEntity = jwtUtility.parseToken(vendorId, userType, tokenPair.refreshToken)
        refreshTokenService.storeRefreshToken(refreshTokenEntity)

        userSessionService.createSession(
            tokenIdentifier = tokenIdentifier,
            userId = vendorId,
            userType = userType,
            expiresAtEpocMillis = accessTokenExpiryEpochMillis
        )

        return tokenPair
    }

    /**
     * Refresh flow:
     * - call jwtUtility.refresh (which validates refresh token and returns a new access token)
     * - persist a session for the new access token
     * - optionally revoke previous sessions if you track them
     * @param String
     * @return [AccessTokenResponse]
     */
    fun handleRefresh(rawRefreshToken: String): AccessTokenResponse {
        val accessTokenResponse = jwtUtility.refresh(rawRefreshToken)
        val newAccessToken = accessTokenResponse.accessToken

        val tokenIdentifier = jwtUtility.getTokenIdentifier(newAccessToken)
        val accessTokenExpiryEpochMillis = jwtUtility.getExpirationEpochMillis(newAccessToken)

        // Persist server-side session
        val userId = jwtUtility.getUserId(newAccessToken)
        val userType = jwtUtility.getUserType(newAccessToken)

        userSessionService.createSession(
            tokenIdentifier = tokenIdentifier,
            userId = userId,
            userType = userType,
            expiresAtEpocMillis = accessTokenExpiryEpochMillis
        )

        return accessTokenResponse
    }

    /**
     * Logout flow:
     * - If the client sent an access token, delete the corresponding session (immediate revoke)
     * - If the client sent a refresh token, delete the stored refresh token record
     */
    fun handleLogout(authorizationHeader: String?, logoutRequestDto: LogoutDto?) {

        // Delete the session if access token press
        authorizationHeader?.takeIf { it.startsWith("Bearer ") }?.let { header ->
            val rawAccessToken = header.substringAfter("Bearer ").trim()
            try {
                val tokenIdentifier = jwtUtility.getTokenIdentifier(rawAccessToken)
                userSessionService.deleteSession(tokenIdentifier)

            } catch (e: MalformedJwtException) {
                throw MalformedJwtException("Malformed token. ${e.message}")
            }
        }

        logoutRequestDto?.refreshToken?.let { it ->
            try {
                refreshTokenService.revokeByRefreshToken(it)

            } catch (e: Exception) {
                throw Exception("An error occurred during the logout process. Contact customer support. ${e.message}")
            }
        }
    }

}


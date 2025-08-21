package com.qinet.feastique.config

import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.service.UserDetailService
import com.qinet.feastique.service.UserSessionService
import com.qinet.feastique.utility.JwtUtility
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Filter that intercepts every HTTP request once per request cycle and
 * performs JWT-based authentication if a valid token is present.
 *
 * **Purpose**:
 * - Extract the JWT access token from the `Authorization` header.
 * - Validate the token using [JwtUtility].
 * - If valid, load the user details and set authentication in the
 *   [SecurityContextHolder] for the current request thread.
 *
 * **Flow**:
 * 1. Check if the `Authorization` header exists and starts with `"Bearer "`.
 * 2. Extract the token (string after `"Bearer "`).
 * 3. Validate the token's signature and expiration.
 * 4. Retrieve the username embedded in the token.
 * 5. Load the user details from the database or memory.
 * 6. Create an [UsernamePasswordAuthenticationToken] with the loaded authorities.
 * 7. Store it in the Spring Security context to mark the request as authenticated.
 *
 * This filter is executed before Spring Security's authorization filters,
 * ensuring authentication is done before any protected endpoint is accessed.
 *
 * @param jwtUtility Utility class for creating, parsing, and validating JWT tokens.
 * @param userDetailService Service for loading user details from a data source.
 * @param userSessionService Service for verifying user session existence.
 */
@Component
class JwtAuthenticationFilter(
    private val jwtUtility: JwtUtility,
    private val userDetailService: UserDetailService,
    private val userSessionService: UserSessionService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {

        val path = request.requestURI

        // Skip heavy token/session work for logout endpoint(s)
        if (path == "/api/auth/logout" || path.startsWith("/api/auth/logout/")) {
            filterChain.doFilter(request, response)
            return
        }

        val authorizationHeader = request.getHeader("Authorization")

        if (authorizationHeader?.startsWith("Bearer ") == true) {
            val rawAccessToken = authorizationHeader.substringAfter("Bearer ").trim()
            try {
                if (jwtUtility.validateAccessToken(rawAccessToken)) {
                    val tokenIdentifier = jwtUtility.getTokenIdentifier(rawAccessToken)

                    userSessionService.getSession(tokenIdentifier)
                        ?.takeIf { it.expiresAtEpochMillis > System.currentTimeMillis() }
                        ?.also { session ->
                            val username = jwtUtility.getUsername(rawAccessToken)

                            /**
                             * Get user details as a [UserSecurity] object from the
                             * security authentication object to get access to the id.
                             * `as UserDetails` also works, but you will not be able
                             * to access the id.
                             */
                            val loadedUserDetails = userDetailService.loadUserByUsername(username) as UserSecurity

                            // Prepare the authorities exposed by the UserDetails
                            val authorities = if (loadedUserDetails.authorities.isEmpty()) {

                                // fallback: build from stored userType (ensure prefix)
                                val normalizedUserType = session.userType.uppercase().removePrefix("ROLE_")
                                listOf(SimpleGrantedAuthority("ROLE_$normalizedUserType"))
                            } else {
                                loadedUserDetails.authorities.toList()
                            }

                            val authentication = UsernamePasswordAuthenticationToken(
                                loadedUserDetails,
                                null,
                                authorities
                            )

                            SecurityContextHolder.getContext().authentication = authentication

                        } ?: run {
                        // session missing or expired -> unauthenticated
                        filterChain.doFilter(request, response)
                        return
                    }
                }
            } catch (e: Exception) {
                filterChain.doFilter(request, response)
                throw Exception("An error occurred while logging in. ${e.message}")
            }
        }
        filterChain.doFilter(request, response)
    }
}


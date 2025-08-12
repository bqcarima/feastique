package com.qinet.feastique.config

import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.service.UserDetailService
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
 */
@Component
class JwtAuthenticationFilter(
    private val jwtUtility: JwtUtility,
    private val userDetailService: UserDetailService
) : OncePerRequestFilter() {

    /**
     * Performs JWT extraction and validation, and sets authentication context.
     *
     * @param request Incoming HTTP request.
     * @param response HTTP response object.
     * @param filterChain The remaining filter chain to be executed.
     */
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // Extract Authorization header
        val header = request.getHeader("Authorization")

        // Proceed only if token is present and starts with "Bearer "
        if (header != null && header.startsWith("Bearer ")) {
            val token = header.substring(7) // Remove "Bearer " prefix

            // Validate token's integrity and expiration
            if (jwtUtility.validateAccessToken(token)) {

                // Extract username from the token
                val username = try {
                    jwtUtility.getUsername(token)

                } catch (e: Exception) {
                    throw IllegalArgumentException("Unable to get username from token. $e")
                }

                // Load user details (authorities, credentials, etc.)
                val userDetail = userDetailService.loadUserByUsername(username!!) as UserSecurity

                // Extract user type from token
                val userType = try {
                    jwtUtility.getUserType(token)
                } catch (e: Exception) {
                    throw IllegalArgumentException("Unable to get account type from token. $e")
                }

                val normalizedUserType = userType.uppercase().removePrefix("ROLE_")
                val authorities = listOf(SimpleGrantedAuthority("ROLE_$normalizedUserType"))

                // Create authentication object
                val authentication = UsernamePasswordAuthenticationToken(
                    userDetail,
                    null,
                    userDetail.authorities)

                // Set authentication in security context
                SecurityContextHolder.getContext().authentication = authentication

            }
        }

        // Continue with the remaining filters
        filterChain.doFilter(request, response)
    }
}

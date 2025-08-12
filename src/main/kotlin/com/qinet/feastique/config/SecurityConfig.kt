package com.qinet.feastique.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

/**
 * Spring Security configuration class.
 *
 * Sets up HTTP security, session management, endpoint access rules, password encoding,
 * and registers the JWT authentication filter into the Spring Security filter chain.
 *
 * @param jwtAuthFilter The JWT authentication filter that validates JWT tokens on incoming requests.
 */
@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthFilter: JwtAuthenticationFilter

) {
    /**
     * Exposes the AuthenticationManager bean.
     *
     * Used to authenticate users programmatically (e.g., in login endpoints).
     *
     * @param authConfig The AuthenticationConfiguration provided by Spring Security.
     * @return The configured AuthenticationManager.
     */

    @Bean
    fun authenticationManager(authConfig: AuthenticationConfiguration): AuthenticationManager {
        return authConfig.authenticationManager
    }

    /**
     * Configures the HTTP security for the application.
     *
     * - Disables CSRF since it is a stateless REST API.
     * - Sets session management to stateless.
     * - Defines authorization rules.
     *   - Public access to "/api/auth/.."
     *   - Customers can access "/api/customer/.."
     *   - Vendors can access "/api/vendor/.."
     *   - All other endpoints require authentication
     *
     * - Adds the JWT authentication filter before the username/password filter.
     *
     * @param httpSecurity The HttpSecurity object to configure.
     * @return The SecurityFilterChain after configuration.
     */
    @Bean
    fun filterChain(httpSecurity: HttpSecurity): SecurityFilterChain {
        httpSecurity
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .csrf { it.disable() }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/api/customer/**").hasRole("CUSTOMER")
                    .requestMatchers("/api/vendor/**").hasRole("VENDOR")
                    .anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)

        return httpSecurity.build()
    }

    /**
     * Password encoder bean that uses BCrypt hashing algorithm.
     *
     * Used to encode and verify user passwords securely.
     *
     * @return BCryptPasswordEncoder instance.
     */
    @Bean
    fun bCryptPasswordEncoder(): BCryptPasswordEncoder {
        return BCryptPasswordEncoder()
    }

}


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
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource


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

    @Bean
    fun authenticationManager(authConfig: AuthenticationConfiguration): AuthenticationManager {
        return authConfig.authenticationManager
    }

    /**
     * Configures HTTP security for the application.
     *
     * - Disables CSRF (stateless REST API).
     * - Sets session management to stateless.
     * - Defines authorization rules:
     *   - Public access to explicit auth endpoints only.
     *     - Customers can access "/api/v1/customer/.."
     *     - Vendors can access "/api/v1/vendor/.."
     *     - All other requests require authentication.
     *   - Adds JWT filter before the username/password filter.
     *   - Configures security response headers.
     */
    @Bean
    fun filterChain(httpSecurity: HttpSecurity): SecurityFilterChain {
        httpSecurity
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .headers { headers ->
                headers.frameOptions { it.deny() }
                headers.contentTypeOptions { }
                headers.httpStrictTransportSecurity {
                    it.includeSubDomains(true).maxAgeInSeconds(31536000)
                }
            }

            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(
                        "/api/v1/auth/login",
                        "/api/v1/auth/vendors/login",
                        "/api/v1/auth/signup",
                        "/api/v1/auth/vendors/signup",
                        "/api/v1/auth/refresh"
                    ).permitAll()
                    .requestMatchers("/api/v1/customer/**").hasRole("CUSTOMER")
                    .requestMatchers("/api/v1/vendor/**").hasRole("VENDOR")
                    .anyRequest().authenticated()
            }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)

        return httpSecurity.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {

            allowedOrigins = listOf("http://localhost:8080")
            allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
            allowedHeaders = listOf("Authorization", "Content-Type")
            allowCredentials = true
        }
        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", configuration)
        }
    }

    @Bean
    fun bCryptPasswordEncoder(): BCryptPasswordEncoder {
        return BCryptPasswordEncoder()
    }

}


package com.qinet.feastique.config

import com.qinet.feastique.service.UserDetailService
import com.qinet.feastique.utility.JwtUtility
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtUtility: JwtUtility,
    private val userDetailService: UserDetailService

) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val header = request.getHeader("Authorization")
        if(header != null && header.startsWith("Bearer ")) {
            val token = header.substring(7)
            if(jwtUtility.validateAccessToken(token)) {
                val username = jwtUtility.getUsername(token)
                val userDetail = userDetailService.loadUserByUsername(username)

                val authentication = UsernamePasswordAuthenticationToken(
                    userDetail,
                    null,
                    userDetail?.authorities
                )

                SecurityContextHolder.getContext().authentication = authentication
            }
        }

        filterChain.doFilter(request, response)
    }
}

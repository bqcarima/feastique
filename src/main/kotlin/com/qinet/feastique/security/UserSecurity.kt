package com.qinet.feastique.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class UserSecurity(
    val id: Long,
    private val username: String,
    private val password: String,
    private val userAuthorities: MutableCollection<GrantedAuthority>

) : UserDetails {
    override fun getAuthorities() = userAuthorities
    override fun getPassword() = password
    override fun getUsername() = username
    override fun isAccountNonExpired() = true
    override fun isAccountNonLocked() = true
    override fun isCredentialsNonExpired() = true
    override fun isEnabled() = true
}
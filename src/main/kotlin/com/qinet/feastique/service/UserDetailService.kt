package com.qinet.feastique.service

import com.qinet.feastique.repository.customer.CustomerRepository
import com.qinet.feastique.repository.vendor.VendorRepository
import com.qinet.feastique.security.UserSecurity
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import java.util.Collections

@Service
class UserDetailService(
    private val customerRepository: CustomerRepository,
    private val vendorRepository: VendorRepository
) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails? {

        val customer = customerRepository.findFirstByUsername(username)
        if (customer != null) {
            return UserSecurity(
                id = customer.id!!,
                username = customer.username,
                password = customer.password!!,
                Collections.singleton(SimpleGrantedAuthority("ROLE_CUSTOMER"))
            )
        }

        val vendor = vendorRepository.findFirstByUsername(username)
        if(vendor != null) {
            return UserSecurity(
                id = vendor.id!!,
                username = vendor.username,
                password = vendor.password!!,
                Collections.singleton(SimpleGrantedAuthority("ROLE_VENDOR"))
            )
        }

        throw UsernameNotFoundException("User not found with username: $username")
    }
}
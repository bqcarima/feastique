package com.qinet.feastique.utility

import com.qinet.feastique.exception.MalformedUrlException
import com.qinet.feastique.security.UserSecurity
import org.springframework.security.core.GrantedAuthority
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class SecurityUtility {

    /**
     * Returns the first role of the user without the "ROLE_" prefix.
     * Example: "ROLE_VENDOR" -> "VENDOR"
     * @param [UserSecurity]
     *
     * @return [String] role as uppercase String (e.g., "VENDOR" or "CUSTOMER"),
     *         or null if no role is found.
     *
     * @author Bassey Otudor
     */
    fun getRole(user: UserSecurity): String? {
        return user.authorities
            .firstOrNull()
            ?.authority
            ?.removePrefix("ROLE_")
            ?.uppercase()
    }

    /**
     * Ensures the user has exactly one role.
     * Throws an exception if no role or multiple roles are found.
     *
     * @param [UserSecurity]
     * @return [String]
     * @throws IllegalArgumentException
     */
    fun getSingleRole(user: UserSecurity): String {
        val roles = user.authorities
            .map(GrantedAuthority::getAuthority)
            .map { it.removePrefix("ROLE_").uppercase() }

        if (roles.size != 1) {
            throw IllegalStateException("Expected exactly 1 role, but found: $roles")
        }

        return roles.first()
    }

    /**
     * Ensures the user is accessing a path applicable to them.
     * Throws an exception if found to be false.
     * This also doubles as a security check.
     *
     * @param userId
     * @param [UserSecurity]
     * @throws MalformedUrlException
     */
    fun validatePath(userId: UUID, userDetails: UserSecurity) {
        if(userId != userDetails.id) {
            throw MalformedUrlException()
        }
    }
}
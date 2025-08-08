package com.qinet.feastique.service.vendor

import com.qinet.feastique.model.dto.LoginDto
import com.qinet.feastique.model.dto.VendorSignupDto
import com.qinet.feastique.model.entity.Vendor
import com.qinet.feastique.model.entity.address.VendorAddress
import com.qinet.feastique.model.enums.AccountType
import com.qinet.feastique.repository.vendor.VendorRepository
import com.qinet.feastique.response.TokenPair
import com.qinet.feastique.security.PasswordEncoder
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.service.RefreshTokenService
import com.qinet.feastique.utility.JwtUtility
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class VendorService(
    private val authManager: AuthenticationManager,
    private val vendorRepository: VendorRepository,
    private val vendorAddressService: VendorAddressService,
    private val refreshTokenService: RefreshTokenService,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtility: JwtUtility
) {

    @Transactional(readOnly = true)
    fun getVendorById(vendorId: Long): Optional<Vendor> {
        return vendorRepository.findById(vendorId)
    }

    @Transactional(readOnly = true)
    fun getVendorByUsername(username: String): Vendor? {
        return vendorRepository.findByUsername(username)
    }

    @Transactional(readOnly = true)
    fun getVendorByPhoneNumber(phoneNumber: String): Vendor? {
        return vendorRepository.findByDefaultPhoneNumber(phoneNumber)
    }

    @Transactional
    fun saveVendor(vendor: Vendor) {
        vendorRepository.save(vendor)
    }

    @Transactional
    fun signup(vendorSignupDTO: VendorSignupDto) {
        if(vendorSignupDTO.username?.let { getVendorByUsername(it) } == null) {
            if(vendorSignupDTO.defaultPhoneNumber?.let {getVendorByPhoneNumber(it)} == null) {
                // Information meant for the vendor table
                val vendor = Vendor()
                vendor.username = vendorSignupDTO.username ?: throw IllegalArgumentException("Please enter a username")
                vendor.firstName = vendorSignupDTO.firstName ?: throw IllegalArgumentException("Please enter a first name.")
                vendor.lastName = vendorSignupDTO.lastName ?: throw IllegalArgumentException("Please enter a last name.")
                vendor.username = vendorSignupDTO.username ?: throw IllegalArgumentException("Please enter a username.")
                vendor.defaultPhoneNumber = vendorSignupDTO.defaultPhoneNumber ?: throw IllegalArgumentException("Please enter a phone number.")
                vendor.chefName = vendorSignupDTO.chefName ?: throw IllegalArgumentException("Please enter your a restaurant name.")
                vendor.restaurantName = vendorSignupDTO.restaurantName ?: throw IllegalArgumentException("Please enter your a chef name.")
                vendor.password = passwordEncoder.encode(vendorSignupDTO.password!!)
                vendor.accountType = AccountType.VENDOR
                vendor.firstName = vendorSignupDTO.firstName ?: throw IllegalArgumentException("Please enter a first name.")

                saveVendor(vendor)

                // Information meant for the address table
                val address = VendorAddress()
                address.country = "Cameroon"
                address.region = vendorSignupDTO.region ?: throw java.lang.IllegalArgumentException("Please select a region.")
                address.city = vendorSignupDTO.city ?: throw IllegalArgumentException("Please enter a username.")
                address.neighbourhood = vendorSignupDTO.neighbourhood ?: throw IllegalArgumentException("Please enter a neighbourhood.")
                address.streetName = vendorSignupDTO.streetName
                address.directions = vendorSignupDTO.directions ?: throw IllegalArgumentException("Please enter a neighbourhood.")
                address.longitude = vendorSignupDTO.longitude
                address.latitude = vendorSignupDTO.latitude
                address.vendor = vendor

                vendorAddressService.saveAddress(address)

                // Update the vendor with a foreign key reference in the address table
                saveVendor(vendor)

            } else {
                throw Exception("Phone number is already taken.")
            }
        } else {
            throw Exception("Error creating account.")
        }
    }

    @Transactional
    fun login(loginDTO: LoginDto): TokenPair {
        // Create authentication token
        val authenticationToken = UsernamePasswordAuthenticationToken(
            loginDTO.username,
            loginDTO.password
        )

        // Authenticate vendor
        val authentication = authManager.authenticate(authenticationToken)
        SecurityContextHolder.getContext().authentication = authentication

        /*
        Get user details as a UserSecurity object from the
        security authentication object to get access to the id.
        "as UserDetails" also works, but you will not be able
        to access the vendor id.
        */
        val userDetails = authentication.principal as UserSecurity
        val vendorId = userDetails.id

        if(refreshTokenService.getTokenByVendorId(vendorId) != null) {
            refreshTokenService.deleteTokenByVendorId(vendorId)
        }

        // Generate and return token pair
        return jwtUtility.generateTokenPair(vendorId, userDetails.username, AccountType.VENDOR.name)
    }

}


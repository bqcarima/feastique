package com.qinet.feastique.service.vendor

import com.qinet.feastique.exception.DuplicateFoundException
import com.qinet.feastique.exception.PhoneNumberUnavailableException
import com.qinet.feastique.exception.RequestedEntityNotFoundException
import com.qinet.feastique.exception.UserNotFoundException
import com.qinet.feastique.exception.UsernameUnavailableException
import com.qinet.feastique.model.dto.PasswordDto
import com.qinet.feastique.model.dto.customer.LoginDto
import com.qinet.feastique.model.dto.vendor.VendorSignupDto
import com.qinet.feastique.model.dto.vendor.VendorUpdateDto
import com.qinet.feastique.model.entity.Vendor
import com.qinet.feastique.model.entity.address.VendorAddress
import com.qinet.feastique.model.entity.phoneNumber.VendorPhoneNumber
import com.qinet.feastique.model.enums.AccountType
import com.qinet.feastique.repository.phoneNumber.VendorPhoneNumberRepository
import com.qinet.feastique.repository.vendor.VendorAddressRepository
import com.qinet.feastique.repository.vendor.VendorRepository
import com.qinet.feastique.response.token.TokenPairResponse
import com.qinet.feastique.security.PasswordEncoder
import com.qinet.feastique.security.SessionManager
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.service.RefreshTokenService
import com.qinet.feastique.service.UserSessionService
import com.qinet.feastique.utility.JwtUtility
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class VendorService(
    private val authManager: AuthenticationManager,
    private val vendorRepository: VendorRepository,
    private val vendorAddressRepository: VendorAddressRepository,
    private val vendorPhoneNumberRepository: VendorPhoneNumberRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtility: JwtUtility,
    private val userSessionService: UserSessionService,
    private val refreshTokenService: RefreshTokenService,
    private val sessionManager: SessionManager
) {

    @Transactional(readOnly = true)
    fun getVendorById(vendorId: Long): Vendor {
        return vendorRepository.findById(vendorId)
            .orElseThrow {
                UserNotFoundException("Vendor not found.")
            }
    }

    @Transactional(readOnly = true)
    fun getVendorByIdWithAddressAndPhoneNumber(vendorDetails: UserSecurity): Vendor {
        val vendor = vendorRepository.findVendorByIdWithAddressAndPhoneNumber(vendorDetails.id)
        if (vendor == null) {
            throw RequestedEntityNotFoundException("Vendor not found.")
        }
        return vendor
    }

    @Transactional(readOnly = true)
    fun isDuplicateFound(username: String? = null, phoneNumber: String? = null): Boolean {
        return when {
            username != null -> vendorRepository.findFirstByUsernameIgnoreCase(username) != null
            phoneNumber != null -> vendorPhoneNumberRepository.findFirstByPhoneNumber(phoneNumber) != null
            else -> throw IllegalArgumentException("Either username or phone must be provided")
        }
    }


    @Transactional
    fun saveVendor(vendor: Vendor): Vendor {
        return vendorRepository.save(vendor)
    }

    @Transactional
    fun signup(vendorSignupDto: VendorSignupDto): Vendor {
        if (!isDuplicateFound(username = vendorSignupDto.username!!)) {
            if (!isDuplicateFound(phoneNumber = vendorSignupDto.phoneNumber)) {
                // Information meant for the vendor table
                val vendor = Vendor()
                vendor.username = vendorSignupDto.username ?: throw IllegalArgumentException("Please enter a username")
                vendor.firstName =
                    vendorSignupDto.firstName ?: throw IllegalArgumentException("Please enter a first name.")
                vendor.lastName =
                    vendorSignupDto.lastName ?: throw IllegalArgumentException("Please enter a last name.")
                vendor.username = vendorSignupDto.username ?: throw IllegalArgumentException("Please enter a username.")
                vendor.chefName =
                    vendorSignupDto.chefName ?: throw IllegalArgumentException("Please enter your a restaurant name.")
                vendor.restaurantName =
                    vendorSignupDto.restaurantName ?: throw IllegalArgumentException("Please enter your a chef name.")
                vendor.password = passwordEncoder.encode(vendorSignupDto.password!!)
                vendor.accountType = AccountType.VENDOR.type
                var savedVendor = saveVendor(vendor)

                // Information meant for the vendor phone number table
                val vendorPhoneNumber = VendorPhoneNumber()
                vendorPhoneNumber.phoneNumber = vendorSignupDto.phoneNumber
                vendorPhoneNumber.vendor = savedVendor
                vendorPhoneNumber.default = true
                vendorPhoneNumberRepository.save(vendorPhoneNumber)


                // Information meant for the address table
                val address = VendorAddress()
                address.country = "Cameroon"
                address.region =
                    vendorSignupDto.region ?: throw java.lang.IllegalArgumentException("Please select a region.")
                address.city = vendorSignupDto.city ?: throw IllegalArgumentException("Please enter a username.")
                address.neighbourhood =
                    vendorSignupDto.neighbourhood ?: throw IllegalArgumentException("Please enter a neighbourhood.")
                address.streetName = vendorSignupDto.streetName
                address.directions =
                    vendorSignupDto.directions ?: throw IllegalArgumentException("Please enter a neighbourhood.")
                address.longitude = vendorSignupDto.longitude
                address.latitude = vendorSignupDto.latitude
                address.vendor = savedVendor

                val savedAddress = vendorAddressRepository.save(address)
                savedVendor.address = savedAddress

                // Update the vendor with a foreign key reference in the address table
                savedVendor = saveVendor(vendor)
                return savedVendor
            } else {
                throw PhoneNumberUnavailableException("Phone number: ${vendorSignupDto.password} is already taken.")
            }
        } else {
            throw UsernameUnavailableException("Error creating, username: ${vendorSignupDto.username} is unavailable.")
        }
    }

    @Transactional
    fun login(loginDto: LoginDto): TokenPairResponse {
        val authenticationToken = UsernamePasswordAuthenticationToken(
            loginDto.username,
            loginDto.password
        )

        val authentication = try {
            authManager.authenticate(authenticationToken)
        } catch (e: Exception) {
            throw Exception("Authentication failed. ${e.message}")
        }

        val userDetails = authentication.principal as? UserSecurity
            ?: throw IllegalStateException("Unexpected principal type after authentication")

        val tokenPair = jwtUtility.generateTokenPair(userDetails.id, userDetails.username, AccountType.VENDOR.name)
        return tokenPair
    }

    @Transactional
    fun updateVendor(vendorUpdateDto: VendorUpdateDto, vendorDetails: UserSecurity): Any? {
        val vendor = getVendorById(vendorDetails.id)
        val oldUsername = vendor.username
        if (vendor.username != vendorUpdateDto.username) {
            if (isDuplicateFound(username = vendorUpdateDto.username)) {
                throw DuplicateFoundException("Username ${vendorUpdateDto.username} is unavailable.")
            }
            vendor.username = vendorUpdateDto.username ?: throw IllegalArgumentException("Please enter a username.")
        }

        vendor.firstName = vendorUpdateDto.firstName ?: throw IllegalArgumentException("Please enter a first name.")
        vendor.lastName = vendorUpdateDto.lastName ?: throw IllegalArgumentException("Please enter a last name.")
        vendor.chefName = vendorUpdateDto.chefName ?: throw IllegalArgumentException("Please enter a chef name.")
        vendor.restaurantName =
            vendorUpdateDto.restaurantName ?: throw IllegalArgumentException("Please enter a restaurant name.")
        vendor.image = vendorUpdateDto.image ?: throw IllegalArgumentException("Please enter am image url.")
        val savedVendor = saveVendor(vendor)

        if (oldUsername != savedVendor.username) {

            // delete old refresh token and old session
            sessionManager.resetSessions(savedVendor.id!!, savedVendor.accountType.toString())

            // Generate a new token par.
            val newTokenPair = jwtUtility.generateTokenPair(
                savedVendor.id!!,
                savedVendor.username,
                savedVendor.accountType ?: AccountType.VENDOR.name
            )

            // Extract tokenIdentifier and expiry from the access token
            val accessToken = newTokenPair.accessToken
            val tokenIdentifier = jwtUtility.getTokenIdentifier(accessToken)
            val accessTokenExpiryEpochMillis = jwtUtility.getExpirationEpochMillis(accessToken)

            val vendorId = jwtUtility.getUserId(accessToken)
            val userType = jwtUtility.getUserType(accessToken)

            // Persist server-side session
            val refreshToken = jwtUtility.parseToken(vendorId, userType, newTokenPair.refreshToken)
            refreshTokenService.storeRefreshToken(refreshToken)

            userSessionService.createSession(
                tokenIdentifier = tokenIdentifier,
                userId = vendorId,
                userType = userType,
                expiresAtEpocMillis = accessTokenExpiryEpochMillis
            )
            return newTokenPair
        } else {
            return savedVendor
        }
    }

    @Transactional
    fun changePassword(passwordDto: PasswordDto, vendorDetails: UserSecurity) {
        val vendor = getVendorById(vendorDetails.id)
        if (!passwordEncoder.matches(passwordDto.currentPassword, vendor.password!!))
            throw IllegalArgumentException("Invalid password.")

        if (passwordDto.newPassword != passwordDto.confirmedNewPassword)
            throw IllegalArgumentException("Passwords do not match.")

        vendor.password = passwordEncoder.encode(passwordDto.confirmedNewPassword)
        saveVendor(vendor)
    }
}


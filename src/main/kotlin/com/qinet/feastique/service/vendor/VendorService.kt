package com.qinet.feastique.service.vendor

import com.qinet.feastique.exception.*
import com.qinet.feastique.model.dto.LoginDto
import com.qinet.feastique.model.dto.user.PasswordChangeDto
import com.qinet.feastique.model.dto.user.VendorSignupDto
import com.qinet.feastique.model.dto.user.VendorUpdateDto
import com.qinet.feastique.model.entity.address.VendorAddress
import com.qinet.feastique.model.entity.contact.VendorPhoneNumber
import com.qinet.feastique.model.entity.user.Vendor
import com.qinet.feastique.model.enums.AccountType
import com.qinet.feastique.model.enums.Region
import com.qinet.feastique.model.enums.RegionCode
import com.qinet.feastique.repository.address.VendorAddressRepository
import com.qinet.feastique.repository.contact.CustomerPhoneNumberRepository
import com.qinet.feastique.repository.contact.VendorPhoneNumberRepository
import com.qinet.feastique.repository.user.VendorRepository
import com.qinet.feastique.response.token.TokenPairResponse
import com.qinet.feastique.security.PasswordEncoder
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.service.RefreshTokenService
import com.qinet.feastique.service.user.UserSessionService
import com.qinet.feastique.utility.JwtUtility
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class VendorService(
    private val authManager: AuthenticationManager,
    private val vendorRepository: VendorRepository,
    private val vendorAddressRepository: VendorAddressRepository,
    private val vendorPhoneNumberRepository: VendorPhoneNumberRepository,
    private val customerPhoneNumberRepository: CustomerPhoneNumberRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtility: JwtUtility,
    private val userSessionService: UserSessionService,
    private val refreshTokenService: RefreshTokenService
) {

    @Transactional(readOnly = true)
    fun getVendorById(vendorId: UUID): Vendor {
        return vendorRepository.findById(vendorId)
            .orElseThrow {
                UserNotFoundException("Vendor not found.")
            }
    }

    @Transactional(readOnly = true)
    fun getVendorByIdWithAddressAndPhoneNumber(vendorDetails: UserSecurity): Vendor {
        val vendor = vendorRepository.findVendorByIdWithAddressAndPhoneNumber(vendorDetails.id)
            ?: throw RequestedEntityNotFoundException("Vendor not found.")
        return vendor
    }

    @Transactional(readOnly = true)
    fun isDuplicateFound(username: String? = null, phoneNumber: String? = null): Boolean {
        return when {
            username != null -> vendorRepository.existsByUsernameIgnoreCase(username)
            phoneNumber != null -> (vendorPhoneNumberRepository.existsByPhoneNumber(phoneNumber) && customerPhoneNumberRepository.existsByPhoneNumber(
                phoneNumber
            ))

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
                val vendor = Vendor().apply {
                    this.username = requireNotNull(vendorSignupDto.username) { "Please enter a username." }
                    this.firstName = requireNotNull(vendorSignupDto.firstName) { "Please enter a first name." }
                    this.lastName = requireNotNull(vendorSignupDto.lastName) { "Please enter a last name." }
                    this.chefName = requireNotNull(vendorSignupDto.chefName) { "Please enter a chef name." }

                    this.restaurantName = vendorSignupDto.restaurantName

                    this.password = passwordEncoder.encode(vendorSignupDto.password!!)
                    this.accountType = AccountType.VENDOR
                    this.openingTime = requireNotNull(vendorSignupDto.openingTime) { "Please specify an opening time." }
                    this.closingTime = requireNotNull(vendorSignupDto.closingTime) { "Please specify a closing time." }
                }

                val regionAsString = requireNotNull(vendorSignupDto.region) { "Please select a region." }
                val regionAsEnum = Region.fromString(regionAsString)

                vendor.region = regionAsEnum

                val regionCode = RegionCode.fromString(regionAsString)
                val lastVendor = vendorRepository.findTopByRegionOrderByVendorCodeDescWithLock(regionAsEnum)
                    .firstOrNull()

                val nextNumber = if (lastVendor != null) {
                    val lastNumericSequence = lastVendor.vendorCode!!.takeLast(4).toInt()
                    lastNumericSequence + 1
                } else {
                    1
                }

                vendor.vendorCode = "%s%04d".format(regionCode.type, nextNumber)
                var savedVendor = saveVendor(vendor)

                // Information meant for the vendor phone number table
                val vendorPhoneNumber = VendorPhoneNumber()
                vendorPhoneNumber.phoneNumber = vendorSignupDto.phoneNumber
                vendorPhoneNumber.vendor = savedVendor
                vendorPhoneNumber.default = true
                val saveVendorPhoneNumber = vendorPhoneNumberRepository.save(vendorPhoneNumber)
                savedVendor.vendorPhoneNumber.add(saveVendorPhoneNumber)

                // Information meant for the address table
                val address = VendorAddress().apply {
                    this.country = "Cameroon"
                    this.region = regionAsEnum
                    this.city = requireNotNull(vendorSignupDto.city) { "Please enter a city." }
                    this.neighbourhood =
                        requireNotNull(vendorSignupDto.neighbourhood) { "Please enter a neighbourhood." }
                    this.streetName = vendorSignupDto.streetName
                    this.directions =
                        requireNotNull(vendorSignupDto.directions) { "Please enter directions to you location" }
                    this.longitude = vendorSignupDto.longitude
                    this.latitude = vendorSignupDto.latitude
                    this.vendor = savedVendor
                }

                val savedAddress = vendorAddressRepository.save(address)
                savedVendor.address = savedAddress

                // Update the vendor with a foreign key reference in the address table
                savedVendor = saveVendor(savedVendor)
                return savedVendor
            } else {
                throw PhoneNumberUnavailableException()
            }
        } else {
            throw UsernameUnavailableException()
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

        val tokenPair =
            jwtUtility.generateTokenPair(userDetails.id, userDetails.username, AccountType.valueOf("VENDOR"))
        return tokenPair
    }

    @Transactional
    fun updateVendor(vendorUpdateDto: VendorUpdateDto, vendorDetails: UserSecurity): TokenPairResponse {
        val vendor = getVendorById(vendorDetails.id)
        vendor.username
        if (vendor.username != vendorUpdateDto.username) {
            if (isDuplicateFound(username = vendorUpdateDto.username)) {
                throw DuplicateFoundException("Username ${vendorUpdateDto.username} is unavailable.")
            }
            vendor.username = requireNotNull(vendorUpdateDto.username) { "Please enter a username." }
        }

        vendor.firstName = requireNotNull(vendorUpdateDto.firstName) { "Please enter a first name." }
        vendor.lastName = requireNotNull(vendorUpdateDto.lastName) { "Please enter a last name." }
        vendor.chefName = requireNotNull(vendorUpdateDto.chefName) { "Please enter a chef name." }
        vendor.restaurantName = requireNotNull(vendorUpdateDto.restaurantName) { "Please enter a restaurant name." }
        vendor.image = requireNotNull(vendorUpdateDto.image) { "Please select an image." }
        val savedVendor = saveVendor(vendor)


        // delete old refresh token and old session
        userSessionService.resetSessions(savedVendor.id, savedVendor.accountType.toString())

        // Generate a new token par.
        val newTokenPair = jwtUtility.generateTokenPair(
            savedVendor.id,
            savedVendor.username,
            savedVendor.accountType ?: AccountType.VENDOR
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

    }

    @Transactional
    fun changePassword(passwordChangeDto: PasswordChangeDto, vendorDetails: UserSecurity) {
        val vendor = getVendorById(vendorDetails.id)
        if (!passwordEncoder.matches(passwordChangeDto.currentPassword, vendor.password!!))
            throw IllegalArgumentException("Invalid password.")

        if (passwordChangeDto.newPassword != passwordChangeDto.confirmedNewPassword) {
            throw IllegalArgumentException("Passwords do not match.")
        }

        vendor.password = passwordEncoder.encode(passwordChangeDto.confirmedNewPassword)
        vendor.accountUpdated = LocalDateTime.now()
        saveVendor(vendor)
    }
}


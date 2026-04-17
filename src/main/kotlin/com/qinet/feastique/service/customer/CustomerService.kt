package com.qinet.feastique.service.customer

import com.qinet.feastique.exception.DuplicateFoundException
import com.qinet.feastique.exception.PhoneNumberUnavailableException
import com.qinet.feastique.exception.UserNotFoundException
import com.qinet.feastique.exception.UsernameUnavailableException
import com.qinet.feastique.model.dto.LoginDto
import com.qinet.feastique.model.dto.user.CustomerSignupDto
import com.qinet.feastique.model.dto.user.CustomerUpdateDto
import com.qinet.feastique.model.dto.user.PasswordChangeDto
import com.qinet.feastique.model.entity.address.CustomerAddress
import com.qinet.feastique.model.entity.contact.CustomerPhoneNumber
import com.qinet.feastique.model.entity.user.Customer
import com.qinet.feastique.model.enums.AccountType
import com.qinet.feastique.model.enums.Region
import com.qinet.feastique.repository.contact.CustomerPhoneNumberRepository
import com.qinet.feastique.repository.contact.VendorPhoneNumberRepository
import com.qinet.feastique.repository.user.CustomerRepository
import com.qinet.feastique.response.token.TokenPairResponse
import com.qinet.feastique.security.PasswordEncoder
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.service.authentication.RefreshTokenService
import com.qinet.feastique.service.user.UserSessionService
import com.qinet.feastique.utility.JwtUtility
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CustomerService(
    private val authManager: AuthenticationManager,
    private val customerRepository: CustomerRepository,
    private val customerAddressService: CustomerAddressService,
    private val customerPhoneNumberRepository: CustomerPhoneNumberRepository,
    private val vendorPhoneNumberRepository: VendorPhoneNumberRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtility: JwtUtility,
    private val userSessionService: UserSessionService,
    private val refreshTokenService: RefreshTokenService
) {

    @Transactional(readOnly = true)
    fun getCustomerById(customerDetails: UserSecurity): Customer {
        val customer = customerRepository.findById(customerDetails.id)
            .orElseThrow {
                throw UserNotFoundException("Username not found.")
            }
        return customer
    }

    @Transactional(readOnly = true)
    fun getCustomerWithPhoneNumberAndAddress(customerDetails: UserSecurity): Customer {
        val customer = customerRepository.findByCustomerByIdWithPhoneNumberAndAddress(customerDetails.id)
            ?: throw UserNotFoundException("Customer not found.")
        return customer
    }

    @Transactional(readOnly = true)
    fun isDuplicateFound(username: String? = null, phoneNumber: String? = null): Boolean {
        return when {
            username != null -> customerRepository.existsByUsernameIgnoreCase(username)
            phoneNumber != null -> (customerPhoneNumberRepository.existsByPhoneNumber(phoneNumber) || vendorPhoneNumberRepository.existsByPhoneNumber(
                phoneNumber
            ))

            else -> throw IllegalArgumentException("Either username or phone must be provided")
        }
    }

    @Transactional
    fun saveCustomer(customer: Customer): Customer {
        return customerRepository.save(customer)
    }

    @Transactional
    fun signupCustomer(customerSignupDto: CustomerSignupDto): Customer {
        if (!isDuplicateFound(username = customerSignupDto.username)) {
            if (!isDuplicateFound(phoneNumber = customerSignupDto.phoneNumber)) {

                // Information meant for the customer table
                val customer = Customer().apply {
                    firstName = requireNotNull(customerSignupDto.firstName) { "Please enter your first name." }
                    lastName = requireNotNull(customerSignupDto.lastName) { "Please enter your last name." }
                    username = requireNotNull(customerSignupDto.username) { "Please enter a username." }
                    accountType = AccountType.CUSTOMER
                    anniversary = customerSignupDto.anniversary
                    password = passwordEncoder.encode(customerSignupDto.password)
                }

                var savedCustomer = saveCustomer(customer)

                // Information meant for the address table
                val address = CustomerAddress().apply {
                    country = "Cameroon"
                    region = Region.fromString(customerSignupDto.region)
                    city = requireNotNull(customerSignupDto.city) { "Please enter a city." }
                    neighbourhood = requireNotNull(customerSignupDto.neighbourhood) { "Please enter a neighbourhood." }
                    streetName = customerSignupDto.streetName
                    directions =
                        requireNotNull(customerSignupDto.directions) { "Please enter directions to exact location." }
                    longitude = customerSignupDto.longitude
                    latitude = customerSignupDto.latitude
                    default = true
                    this.customer = savedCustomer
                }
                customerAddressService.saveAddress(address)
                savedCustomer.address.add(address)

                // Information meant for the customer phone number table
                val phoneNumber = CustomerPhoneNumber().apply {
                    this.phoneNumber = requireNotNull(customerSignupDto.phoneNumber) { "Please enter a phone number." }
                    this.default = true
                    this.customer = savedCustomer
                }
                customerPhoneNumberRepository.save(phoneNumber)
                savedCustomer.phoneNumber.add(phoneNumber)

                // Update the customer with a foreign key reference in the address table
                savedCustomer = saveCustomer(savedCustomer)
                return savedCustomer

            } else {
                throw PhoneNumberUnavailableException()
            }

        } else {
            throw UsernameUnavailableException()
        }
    }

    @Transactional
    fun login(loginDto: LoginDto): TokenPairResponse {
        // Create authentication token
        val authenticationToken = UsernamePasswordAuthenticationToken(
            loginDto.username,
            loginDto.password
        )

        // Authenticate the customer
        val authentication = try {
            authManager.authenticate(authenticationToken)
        } catch (e: BadCredentialsException) {
            throw BadCredentialsException("Authentication failed. ${e.message}")
        }


        /**
         * Get user details as a [UserSecurity] object from the
         * security authentication object to get access to the id.
         * "as [UserDetails]" also works, but you will not be able
         * to access the customer id.
         */
        val userDetails = authentication.principal as? UserSecurity
            ?: throw IllegalArgumentException("Unexpected principal type after authentication.")
        val tokenPair = jwtUtility.generateTokenPair(userDetails.id, userDetails.username, AccountType.CUSTOMER)

        // Generate and return token pair
        return tokenPair
    }

    @Transactional
    fun updateCustomer(customerUpdateDto: CustomerUpdateDto, customerDetails: UserSecurity): TokenPairResponse {
        val customer = getCustomerById(customerDetails)
        customerDetails.username
        if (customer.username != customerUpdateDto.username) {
            if (isDuplicateFound(username = customerUpdateDto.username)) {
                throw DuplicateFoundException("Username ${customerUpdateDto.username} is unavailable.")
            }
            customer.username = requireNotNull(customerUpdateDto.username) { "Please enter a username." }
        }

        customer.firstName = requireNotNull(customerUpdateDto.firstName) { "Please enter your first name." }
        customer.lastName = requireNotNull(customerUpdateDto.lastName) { "Please enter your last name." }
        customer.dob = requireNotNull(customerUpdateDto.dob) { "Please enter a date of birth." }
        customer.anniversary = customerUpdateDto.anniversary
        customer.displayPicture = customerUpdateDto.image

        val savedCustomer = saveCustomer(customer)

        // delete old refresh token and old session
        userSessionService.resetSessions(savedCustomer.id, savedCustomer.accountType?.name ?: AccountType.CUSTOMER.name)

        // Generate a new token pair
        val newTokenPair = jwtUtility.generateTokenPair(
            savedCustomer.id,
            savedCustomer.username,
            AccountType.CUSTOMER
        )

        // Extract token identifier and expiry from the access token
        val accessToken = newTokenPair.accessToken
        val tokenIdentifier = jwtUtility.getTokenIdentifier(accessToken)
        val accessTokenExpiryEpochMillis = jwtUtility.getExpirationEpochMillis(accessToken)

        val customerId = jwtUtility.getUserId(accessToken)
        val userType = jwtUtility.getUserType(accessToken)

        // Persist server-side session
        val refreshToken = jwtUtility.parseToken(customerId, userType, newTokenPair.refreshToken)
        refreshTokenService.storeRefreshToken(refreshToken)

        userSessionService.createSession(
            tokenIdentifier = tokenIdentifier,
            userId = customerId,
            userType = userType,
            expiresAtEpocMillis = accessTokenExpiryEpochMillis,
        )
        return newTokenPair

    }

    @Transactional
    fun changePassword(passwordChangeDto: PasswordChangeDto, customerDetails: UserSecurity) {
        val customer = getCustomerById(customerDetails)
        if (!passwordEncoder.matches(passwordChangeDto.currentPassword, customer.password!!))
            throw IllegalArgumentException("Invalid password.")

        if (passwordChangeDto.newPassword != passwordChangeDto.confirmedNewPassword) {
            throw IllegalArgumentException("Passwords do not match.")
        }
        customer.password = passwordEncoder.encode(passwordChangeDto.confirmedNewPassword)
        saveCustomer(customer)
    }

}


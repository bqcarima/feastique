package com.qinet.feastique.service.customer

import com.qinet.feastique.exception.DuplicateFoundException
import com.qinet.feastique.exception.PhoneNumberUnavailableException
import com.qinet.feastique.exception.UserNotFoundException
import com.qinet.feastique.exception.UsernameUnavailableException
import com.qinet.feastique.model.dto.LoginDto
import com.qinet.feastique.model.dto.PasswordDto
import com.qinet.feastique.model.dto.customer.SignupDto
import com.qinet.feastique.model.dto.customer.UpdateDto
import com.qinet.feastique.model.entity.user.Customer
import com.qinet.feastique.model.entity.address.CustomerAddress
import com.qinet.feastique.model.entity.phoneNumber.CustomerPhoneNumber
import com.qinet.feastique.model.enums.AccountType
import com.qinet.feastique.repository.customer.CustomerPhoneNumberRepository
import com.qinet.feastique.repository.customer.CustomerRepository
import com.qinet.feastique.repository.phoneNumber.VendorPhoneNumberRepository
import com.qinet.feastique.response.token.TokenPairResponse
import com.qinet.feastique.security.PasswordEncoder
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.service.RefreshTokenService
import com.qinet.feastique.service.user.UserSessionService
import com.qinet.feastique.utility.JwtUtility
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.security.core.userdetails.UserDetails
import java.time.LocalDateTime

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
            phoneNumber != null -> (customerPhoneNumberRepository.existsByPhoneNumber(phoneNumber) && vendorPhoneNumberRepository.existsByPhoneNumber(phoneNumber))
            else -> throw IllegalArgumentException("Either username or phone must be provided")
        }
    }

    @Transactional
    fun saveCustomer(customer: Customer): Customer {
        return customerRepository.save(customer)
    }

    @Transactional
    fun signupCustomer(signupDto: SignupDto): Customer {
        if(!isDuplicateFound(username = signupDto.username)) {
            if(!isDuplicateFound(phoneNumber = signupDto.phoneNumber)) {

                // Information meant for the customer table
                val customer = Customer().apply {
                    firstName = signupDto.firstName
                    lastName = signupDto.lastName
                    username = signupDto.username
                    dob = signupDto.dob ?: throw IllegalArgumentException("Please enter a date of birth.")
                    accountType = AccountType.CUSTOMER
                    anniversary = signupDto.anniversary
                    password = passwordEncoder.encode(signupDto.password)
                }

                var savedCustomer = saveCustomer(customer)

                // Information meant for the address table
                val address = CustomerAddress().apply {
                    country = "Cameroon"
                    region = signupDto.region
                    city = signupDto.city
                    neighbourhood = signupDto.neighbourhood
                    streetName = signupDto.streetName
                    directions = signupDto.directions
                    longitude = signupDto.longitude
                    latitude = signupDto.latitude
                    default = true
                    this.customer = savedCustomer
                }
                customerAddressService.saveAddress(address)
                savedCustomer.address.add(address)

                // Information meant for the customer phone number table
                val phoneNumber = CustomerPhoneNumber().apply {
                    this.phoneNumber = signupDto.phoneNumber
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
    fun updateCustomer(updateDto: UpdateDto, customerDetails: UserSecurity): Any? {
        val customer = getCustomerById(customerDetails)
        val oldUsername = customerDetails.username
        if (customer.username != updateDto.username) {
            if (isDuplicateFound(username = updateDto.username)) {
                throw DuplicateFoundException("Username ${updateDto.username} is unavailable.")
            }
            customer.username = updateDto.username
        }

        customer.firstName = updateDto.firstName
        customer.lastName = updateDto.lastName
        customer.dob = updateDto.dob
        customer.anniversary = updateDto.anniversary
        customer.image = updateDto.image
        customer.accountUpdated = LocalDateTime.now()
        val savedCustomer = saveCustomer(customer)

        if (oldUsername != savedCustomer.username) {

            // delete old refresh token and old session
            userSessionService.resetSessions(savedCustomer.id!!, savedCustomer.accountType?.name ?: AccountType.CUSTOMER.name)

            // Generate a new token pair
            val newTokenPair = jwtUtility.generateTokenPair(
                savedCustomer.id!!,
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
            val refreshToken = jwtUtility.parseToken(customerId, userType,newTokenPair.refreshToken)
            refreshTokenService.storeRefreshToken(refreshToken)

            userSessionService.createSession(
                tokenIdentifier = tokenIdentifier,
                userId = customerId,
                userType = userType,
                expiresAtEpocMillis = accessTokenExpiryEpochMillis,
            )
            return newTokenPair
        } else {
            return getCustomerWithPhoneNumberAndAddress(customerDetails)
        }
    }

    @Transactional
    fun changePassword(passwordDto: PasswordDto, customerDetails: UserSecurity) {
        val customer = getCustomerById(customerDetails)
        if (!passwordEncoder.matches(passwordDto.currentPassword, customer.password!!))
            throw IllegalArgumentException("Invalid password.")

        if (passwordDto.newPassword != passwordDto.confirmedNewPassword) {
            throw IllegalArgumentException("Passwords do not match.")
        }
        customer.accountUpdated = LocalDateTime.now()
        customer.password = passwordEncoder.encode(passwordDto.confirmedNewPassword)
        saveCustomer(customer)
    }
}


package com.qinet.feastique.service.customer

import com.qinet.feastique.model.dto.customer.LoginDto
import com.qinet.feastique.model.dto.customer.SignupDto
import com.qinet.feastique.model.entity.Customer
import com.qinet.feastique.model.entity.address.CustomerAddress
import com.qinet.feastique.model.enums.AccountType
import com.qinet.feastique.repository.customer.CustomerRepository
import com.qinet.feastique.response.token.TokenPairResponse
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
import kotlin.jvm.optionals.getOrNull

@Service
class CustomerService(
    private val authManager: AuthenticationManager,
    private val customerRepository: CustomerRepository,
    private val customerAddressService: CustomerAddressService,
    private val refreshTokenService: RefreshTokenService,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtility: JwtUtility
) {

    @Transactional(readOnly = true)
    fun getCustomerById(customerId: Long): Optional<Customer> {
        return customerRepository.findById(customerId)
    }

    @Transactional(readOnly = true)
    fun getCustomerByUsername(username: String): Optional<Customer> {
        return customerRepository.findFirstByUsername(username)
    }

    @Transactional(readOnly = true)
    fun getCustomerByPhoneNumber(phoneNumber: String): Optional<Customer> {
        return customerRepository.findFirstByDefaultPhoneNumber(phoneNumber)
    }

    @Transactional
    fun saveCustomer(customer: Customer) {
        customerRepository.save(customer)
    }

    @Transactional
    fun signupCustomer(signupDto: SignupDto) {
        if(signupDto.username?.let {getCustomerByUsername(it).getOrNull()} == null) {

            if(signupDto.defaultPhoneNumber?.let { getCustomerByPhoneNumber(it).getOrNull() } == null) {


                // Information meant for the customer table
                val customer = Customer()
                customer.firstName = signupDto.firstName ?: throw IllegalArgumentException("Please enter a first name.")
                customer.lastName = signupDto.lastName ?: throw IllegalArgumentException("Please enter a last name.")
                customer.username = signupDto.username ?: throw IllegalArgumentException("Please enter a username.")
                customer.defaultPhoneNumber = signupDto.defaultPhoneNumber ?: throw IllegalArgumentException("Please enter a phone number.")
                customer.password = passwordEncoder.encode(signupDto.password!!)
                customer.accountType = AccountType.CUSTOMER
                customer.firstName = signupDto.firstName ?: throw IllegalArgumentException("Please enter a first name.")

                saveCustomer(customer)

                // Information meant for the address table
                val address = CustomerAddress()
                address.country = "Cameroon"
                address.region = signupDto.region ?: throw java.lang.IllegalArgumentException("Please select a region.")
                address.city = signupDto.city ?: throw IllegalArgumentException("Please enter a username.")
                address.neighbourhood = signupDto.neighbourhood ?: throw IllegalArgumentException("Please enter a neighbourhood.")
                address.streetName = signupDto.streetName
                address.directions = signupDto.directions ?: throw IllegalArgumentException("Please enter a neighbourhood.")
                address.longitude = signupDto.longitude
                address.latitude = signupDto.latitude
                address.customer = customer

                customerAddressService.saveAddress(address)

                // Update the customer with a foreign key reference in the address table
                saveCustomer(customer)

            } else {
                throw Exception("Phone number is already taken.")
            }

        } else {
            throw Exception("Username is already taken.")
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
        val authentication = authManager.authenticate(authenticationToken)
        SecurityContextHolder.getContext().authentication = authentication

        /*
        Get user details as a UserSecurity object from the
        security authentication object to get access to the id.
        "as UserDetails" also works, but you will not be able
        to access the customer id.
        */
        val userDetails = authentication.principal as UserSecurity
        val customerId = userDetails.id
        val sessionTokenIdentifier = UUID.randomUUID().toString()

        if(refreshTokenService.getTokenByCustomerId(customerId) != null) {
            refreshTokenService.deleteTokenByCustomerId(customerId)
        }

        // Generate and return token pair
        return jwtUtility.generateTokenPair(customerId, userDetails.username, AccountType.CUSTOMER.name)
    }

}
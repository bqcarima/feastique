package com.qinet.feastique.service.customer

import com.qinet.feastique.model.dto.LoginDto
import com.qinet.feastique.model.dto.SignupDto
import com.qinet.feastique.model.entity.Customer
import com.qinet.feastique.model.entity.address.CustomerAddress
import com.qinet.feastique.model.enums.AccountType
import com.qinet.feastique.repository.customer.CustomerRepository
import com.qinet.feastique.response.TokenPairResponse
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
        return customerRepository.findByUsername(username)
    }

    @Transactional(readOnly = true)
    fun getCustomerByPhoneNumber(phoneNumber: String): Optional<Customer> {
        return customerRepository.findByDefaultPhoneNumber(phoneNumber)
    }

    @Transactional
    fun saveCustomer(customer: Customer) {
        customerRepository.save(customer)
    }

    @Transactional
    fun signupCustomer(signupDTO: SignupDto) {
        if(signupDTO.username?.let {getCustomerByUsername(it).getOrNull()} == null) {

            if(signupDTO.defaultPhoneNumber?.let { getCustomerByPhoneNumber(it).getOrNull() } == null) {


                // Information meant for the customer table
                val customer = Customer()
                customer.firstName = signupDTO.firstName ?: throw IllegalArgumentException("Please enter a first name.")
                customer.lastName = signupDTO.lastName ?: throw IllegalArgumentException("Please enter a last name.")
                customer.username = signupDTO.username ?: throw IllegalArgumentException("Please enter a username.")
                customer.defaultPhoneNumber = signupDTO.defaultPhoneNumber ?: throw IllegalArgumentException("Please enter a phone number.")
                customer.password = passwordEncoder.encode(signupDTO.password!!)
                customer.accountType = AccountType.CUSTOMER
                customer.firstName = signupDTO.firstName ?: throw IllegalArgumentException("Please enter a first name.")

                saveCustomer(customer)

                // Information meant for the address table
                val address = CustomerAddress()
                address.country = "Cameroon"
                address.region = signupDTO.region ?: throw java.lang.IllegalArgumentException("Please select a region.")
                address.city = signupDTO.city ?: throw IllegalArgumentException("Please enter a username.")
                address.neighbourhood = signupDTO.neighbourhood ?: throw IllegalArgumentException("Please enter a neighbourhood.")
                address.streetName = signupDTO.streetName
                address.directions = signupDTO.directions ?: throw IllegalArgumentException("Please enter a neighbourhood.")
                address.longitude = signupDTO.longitude
                address.latitude = signupDTO.latitude
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
    fun login(loginDTO: LoginDto): TokenPairResponse {
        // Create authentication token
        val authenticationToken = UsernamePasswordAuthenticationToken(
            loginDTO.username,
            loginDTO.password
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

        if(refreshTokenService.getTokenByCustomerId(customerId) != null) {
            refreshTokenService.deleteTokenByCustomerId(customerId)
        }

        // Generate and return token pair
        return jwtUtility.generateTokenPair(customerId, userDetails.username, AccountType.CUSTOMER.name)
    }

}
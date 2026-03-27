package com.qinet.feastique.service.user

import com.qinet.feastique.model.entity.authentication.RefreshToken
import com.qinet.feastique.model.entity.authentication.UserSession
import com.qinet.feastique.model.entity.user.Customer
import com.qinet.feastique.model.entity.user.Vendor
import com.qinet.feastique.model.enums.AccountType
import com.qinet.feastique.model.enums.Region
import com.qinet.feastique.repository.authentication.UserSessionRepository
import com.qinet.feastique.repository.user.CustomerRepository
import com.qinet.feastique.repository.user.VendorRepository
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.service.authentication.RefreshTokenService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import org.springframework.security.core.userdetails.UsernameNotFoundException
import java.time.LocalTime
import java.util.*

// Shared fixtures

private val UDS_CUSTOMER_ID: UUID = UUID.randomUUID()
private val UDS_VENDOR_ID: UUID = UUID.randomUUID()

private const val UDS_TOKEN_IDENTIFIER = "uds-tok-id-abc123"
private const val UDS_VENDOR_TOKEN_IDENTIFIER = "uds-vendor-tok-id-xyz789"
private val UDS_TOKEN_EXPIRY = System.currentTimeMillis() + 900_000L

private fun udsJaneCustomer(): Customer = Customer().apply {
    id = UDS_CUSTOMER_ID
    username = "jane_doe"
    password = "hashed_passWord123"
    firstName = "Jane"
    lastName = "Doe"
    accountType = AccountType.CUSTOMER
}

private fun udsSabiVendor(): Vendor = Vendor().apply {
    id = UDS_VENDOR_ID
    username = "sabi_chef"
    password = "hashed_sabiChef98"
    firstName = "Ambe"
    lastName = "Chancie"
    chefName = "Sabi Chef"
    restaurantName = "Sabi Foods"
    accountType = AccountType.VENDOR
    region = Region.CENTRE
    vendorCode = "CM020001"
    openingTime = LocalTime.of(8, 30)
    closingTime = LocalTime.of(18, 30)
}

private fun customerSession(): UserSession = UserSession(
    tokenIdentifier = UDS_TOKEN_IDENTIFIER,
    userId = UDS_CUSTOMER_ID,
    userType = "CUSTOMER",
    expiresAtEpochMillis = UDS_TOKEN_EXPIRY
)

private fun vendorSession(): UserSession = UserSession(
    tokenIdentifier = UDS_VENDOR_TOKEN_IDENTIFIER,
    userId = UDS_VENDOR_ID,
    userType = "VENDOR",
    expiresAtEpochMillis = UDS_TOKEN_EXPIRY
)

private fun customerRefreshToken(): RefreshToken = RefreshToken(
    customerId = UDS_CUSTOMER_ID,
    vendorId = null,
    expiresAt = Date(System.currentTimeMillis() + 2_592_000_000L),
    hashedToken = "hashed_jane_refresh"
)

private fun vendorRefreshToken(): RefreshToken = RefreshToken(
    customerId = null,
    vendorId = UDS_VENDOR_ID,
    expiresAt = Date(System.currentTimeMillis() + 2_592_000_000L),
    hashedToken = "hashed_sabi_refresh"
)


// UserDetailService

class UserDetailServiceTest {

    private lateinit var customerRepository: CustomerRepository
    private lateinit var vendorRepository: VendorRepository
    private lateinit var userDetailService: UserDetailService

    @BeforeEach
    fun setUp() {
        customerRepository = mock()
        vendorRepository = mock()
        userDetailService = UserDetailService(customerRepository, vendorRepository)
    }

    @Nested
    inner class LoadCustomerByUsername {

        @Test
        fun `returns UserSecurity with ROLE_CUSTOMER when customer is found`() {
            whenever(customerRepository.findFirstByUsername("jane_doe")).thenReturn(udsJaneCustomer())

            val result = userDetailService.loadUserByUsername("jane_doe") as UserSecurity

            assertEquals(UDS_CUSTOMER_ID, result.id)
            assertEquals("jane_doe", result.username)
            assertTrue(result.authorities.any { it.authority == "ROLE_CUSTOMER" })
        }

        @Test
        fun `returns correct password hash for customer`() {
            whenever(customerRepository.findFirstByUsername("jane_doe")).thenReturn(udsJaneCustomer())

            val result = userDetailService.loadUserByUsername("jane_doe")

            assertEquals("hashed_passWord123", result.password)
        }

        @Test
        fun `does not check vendor repository when customer is found`() {
            whenever(customerRepository.findFirstByUsername("jane_doe")).thenReturn(udsJaneCustomer())

            userDetailService.loadUserByUsername("jane_doe")

            verify(vendorRepository, never()).findFirstByUsername(any())
        }

        @Test
        fun `returned customer principal has exactly one authority`() {
            whenever(customerRepository.findFirstByUsername("jane_doe")).thenReturn(udsJaneCustomer())

            val result = userDetailService.loadUserByUsername("jane_doe") as UserSecurity

            assertEquals(1, result.authorities.size)
        }

        @Test
        fun `account is non-expired and non-locked for customer`() {
            whenever(customerRepository.findFirstByUsername("jane_doe")).thenReturn(udsJaneCustomer())

            val result = userDetailService.loadUserByUsername("jane_doe")

            assertTrue(result.isAccountNonExpired)
            assertTrue(result.isAccountNonLocked)
            assertTrue(result.isCredentialsNonExpired)
            assertTrue(result.isEnabled)
        }
    }

    @Nested
    inner class LoadVendorByUsername {

        @BeforeEach
        fun stubCustomerNotFound() {
            whenever(customerRepository.findFirstByUsername("sabi_chef")).thenReturn(null)
        }

        @Test
        fun `returns UserSecurity with ROLE_VENDOR when vendor is found and customer is not`() {
            whenever(vendorRepository.findFirstByUsername("sabi_chef")).thenReturn(udsSabiVendor())

            val result = userDetailService.loadUserByUsername("sabi_chef") as UserSecurity

            assertEquals(UDS_VENDOR_ID, result.id)
            assertEquals("sabi_chef", result.username)
            assertTrue(result.authorities.any { it.authority == "ROLE_VENDOR" })
        }

        @Test
        fun `returns correct password hash for vendor`() {
            whenever(vendorRepository.findFirstByUsername("sabi_chef")).thenReturn(udsSabiVendor())

            val result = userDetailService.loadUserByUsername("sabi_chef")

            assertEquals("hashed_sabiChef98", result.password)
        }

        @Test
        fun `checks vendor repository only after customer lookup returns null`() {
            whenever(vendorRepository.findFirstByUsername("sabi_chef")).thenReturn(udsSabiVendor())

            userDetailService.loadUserByUsername("sabi_chef")

            val inOrder = inOrder(customerRepository, vendorRepository)
            inOrder.verify(customerRepository).findFirstByUsername("sabi_chef")
            inOrder.verify(vendorRepository).findFirstByUsername("sabi_chef")
        }

        @Test
        fun `returned vendor principal has exactly one authority`() {
            whenever(vendorRepository.findFirstByUsername("sabi_chef")).thenReturn(udsSabiVendor())

            val result = userDetailService.loadUserByUsername("sabi_chef") as UserSecurity

            assertEquals(1, result.authorities.size)
        }

        @Test
        fun `vendor authority does not include ROLE_CUSTOMER`() {
            whenever(vendorRepository.findFirstByUsername("sabi_chef")).thenReturn(udsSabiVendor())

            val result = userDetailService.loadUserByUsername("sabi_chef") as UserSecurity

            assertFalse(result.authorities.any { it.authority == "ROLE_CUSTOMER" })
        }

        @Test
        fun `customer authority does not include ROLE_VENDOR`() {
            whenever(customerRepository.findFirstByUsername("jane_doe")).thenReturn(udsJaneCustomer())

            val result = userDetailService.loadUserByUsername("jane_doe") as UserSecurity

            assertFalse(result.authorities.any { it.authority == "ROLE_VENDOR" })
        }
    }

    @Nested
    inner class UsernameNotFound {

        @Test
        fun `throws UsernameNotFoundException when neither customer nor vendor exists`() {
            whenever(customerRepository.findFirstByUsername("ghost_user")).thenReturn(null)
            whenever(vendorRepository.findFirstByUsername("ghost_user")).thenReturn(null)

            assertThrows<UsernameNotFoundException> {
                userDetailService.loadUserByUsername("ghost_user")
            }
        }

        @Test
        fun `exception message contains the missing username`() {
            val missingUsername = "nobody_here"
            whenever(customerRepository.findFirstByUsername(missingUsername)).thenReturn(null)
            whenever(vendorRepository.findFirstByUsername(missingUsername)).thenReturn(null)

            val ex = assertThrows<UsernameNotFoundException> {
                userDetailService.loadUserByUsername(missingUsername)
            }
            assertTrue(ex.message!!.contains(missingUsername))
        }

        @Test
        fun `both repositories are queried before throwing`() {
            whenever(customerRepository.findFirstByUsername("ghost_user")).thenReturn(null)
            whenever(vendorRepository.findFirstByUsername("ghost_user")).thenReturn(null)

            runCatching { userDetailService.loadUserByUsername("ghost_user") }

            verify(customerRepository).findFirstByUsername("ghost_user")
            verify(vendorRepository).findFirstByUsername("ghost_user")
        }
    }
}


// UserSessionService

class UserSessionServiceTest {

    private lateinit var userSessionRepository: UserSessionRepository
    private lateinit var refreshTokenService: RefreshTokenService
    private lateinit var userSessionService: UserSessionService

    @BeforeEach
    fun setUp() {
        userSessionRepository = mock()
        refreshTokenService = mock()
        userSessionService = UserSessionService(userSessionRepository, refreshTokenService)
    }

    @Nested
    inner class CreateSession {

        @Test
        fun `creates and returns a customer session with correct fields`() {
            whenever(userSessionRepository.save(any())).thenReturn(customerSession())

            val result = userSessionService.createSession(
                tokenIdentifier = UDS_TOKEN_IDENTIFIER,
                userId = UDS_CUSTOMER_ID,
                userType = "CUSTOMER",
                expiresAtEpocMillis = UDS_TOKEN_EXPIRY
            )

            assertEquals(UDS_TOKEN_IDENTIFIER, result.tokenIdentifier)
            assertEquals(UDS_CUSTOMER_ID, result.userId)
            assertEquals("CUSTOMER", result.userType)
            assertEquals(UDS_TOKEN_EXPIRY, result.expiresAtEpochMillis)
        }

        @Test
        fun `creates and returns a vendor session with correct fields`() {
            whenever(userSessionRepository.save(any())).thenReturn(vendorSession())

            val result = userSessionService.createSession(
                tokenIdentifier = UDS_VENDOR_TOKEN_IDENTIFIER,
                userId = UDS_VENDOR_ID,
                userType = "VENDOR",
                expiresAtEpocMillis = UDS_TOKEN_EXPIRY
            )

            assertEquals(UDS_VENDOR_TOKEN_IDENTIFIER, result.tokenIdentifier)
            assertEquals(UDS_VENDOR_ID, result.userId)
            assertEquals("VENDOR", result.userType)
        }

        @Test
        fun `delegates to repository save exactly once`() {
            whenever(userSessionRepository.save(any())).thenReturn(customerSession())

            userSessionService.createSession(UDS_TOKEN_IDENTIFIER, UDS_CUSTOMER_ID, "CUSTOMER", UDS_TOKEN_EXPIRY)

            verify(userSessionRepository, times(1)).save(any())
        }

        @Test
        fun `persists session with the provided token identifier`() {
            whenever(userSessionRepository.save(any())).thenReturn(customerSession())

            userSessionService.createSession(UDS_TOKEN_IDENTIFIER, UDS_CUSTOMER_ID, "CUSTOMER", UDS_TOKEN_EXPIRY)

            verify(userSessionRepository).save(argThat {
                tokenIdentifier == UDS_TOKEN_IDENTIFIER
            })
        }
    }

    @Nested
    inner class GetSession {

        @Test
        fun `returns customer session when token identifier matches`() {
            val session = customerSession()
            whenever(userSessionRepository.findByTokenIdentifier(UDS_TOKEN_IDENTIFIER)).thenReturn(session)

            val result = userSessionService.getSession(UDS_TOKEN_IDENTIFIER)

            assertEquals(session, result)
            assertEquals(UDS_CUSTOMER_ID, result?.userId)
        }

        @Test
        fun `returns vendor session when token identifier matches`() {
            val session = vendorSession()
            whenever(userSessionRepository.findByTokenIdentifier(UDS_VENDOR_TOKEN_IDENTIFIER)).thenReturn(session)

            val result = userSessionService.getSession(UDS_VENDOR_TOKEN_IDENTIFIER)

            assertEquals(session, result)
            assertEquals(UDS_VENDOR_ID, result?.userId)
        }

        @Test
        fun `returns null when no session exists for the identifier`() {
            whenever(userSessionRepository.findByTokenIdentifier("unknown-id")).thenReturn(null)

            assertNull(userSessionService.getSession("unknown-id"))
        }

        @Test
        fun `delegates to findByTokenIdentifier once`() {
            whenever(userSessionRepository.findByTokenIdentifier(UDS_TOKEN_IDENTIFIER)).thenReturn(customerSession())

            userSessionService.getSession(UDS_TOKEN_IDENTIFIER)

            verify(userSessionRepository).findByTokenIdentifier(UDS_TOKEN_IDENTIFIER)
        }
    }

    @Nested
    inner class DeleteSession {

        @Test
        fun `deletes customer session when it exists`() {
            whenever(userSessionRepository.existsByTokenIdentifier(UDS_TOKEN_IDENTIFIER)).thenReturn(true)

            userSessionService.deleteSession(UDS_TOKEN_IDENTIFIER)

            verify(userSessionRepository).deleteByTokenIdentifier(UDS_TOKEN_IDENTIFIER)
        }

        @Test
        fun `deletes vendor session when it exists`() {
            whenever(userSessionRepository.existsByTokenIdentifier(UDS_VENDOR_TOKEN_IDENTIFIER)).thenReturn(true)

            userSessionService.deleteSession(UDS_VENDOR_TOKEN_IDENTIFIER)

            verify(userSessionRepository).deleteByTokenIdentifier(UDS_VENDOR_TOKEN_IDENTIFIER)
        }

        @Test
        fun `is a no-op when session does not exist`() {
            whenever(userSessionRepository.existsByTokenIdentifier("ghost-token")).thenReturn(false)

            userSessionService.deleteSession("ghost-token")

            verify(userSessionRepository, never()).deleteByTokenIdentifier(any())
        }

        @Test
        fun `checks existence before attempting deletion`() {
            whenever(userSessionRepository.existsByTokenIdentifier(UDS_TOKEN_IDENTIFIER)).thenReturn(true)

            userSessionService.deleteSession(UDS_TOKEN_IDENTIFIER)

            val inOrder = inOrder(userSessionRepository)
            inOrder.verify(userSessionRepository).existsByTokenIdentifier(UDS_TOKEN_IDENTIFIER)
            inOrder.verify(userSessionRepository).deleteByTokenIdentifier(UDS_TOKEN_IDENTIFIER)
        }
    }

    @Nested
    inner class DeleteAllSessionsForUser {

        @Test
        fun `delegates to deleteByUserIdAndUserType with correct arguments for customer`() {
            userSessionService.deleteAllSessionsForUser(UDS_CUSTOMER_ID, "CUSTOMER")

            verify(userSessionRepository).deleteByUserIdAndUserType(UDS_CUSTOMER_ID, "CUSTOMER")
        }

        @Test
        fun `delegates to deleteByUserIdAndUserType with correct arguments for vendor`() {
            userSessionService.deleteAllSessionsForUser(UDS_VENDOR_ID, "VENDOR")

            verify(userSessionRepository).deleteByUserIdAndUserType(UDS_VENDOR_ID, "VENDOR")
        }
    }

    @Nested
    inner class ResetSessions {

        @Test
        fun `resets customer sessions by deleting refresh token and all sessions`() {
            userSessionService.resetSessions(UDS_CUSTOMER_ID, "CUSTOMER")

            verify(refreshTokenService).deleteTokenByCustomerId(UDS_CUSTOMER_ID)
            verify(userSessionRepository).deleteByUserIdAndUserType(UDS_CUSTOMER_ID, "CUSTOMER")
        }

        @Test
        fun `resets vendor sessions by deleting refresh token and all sessions`() {
            userSessionService.resetSessions(UDS_VENDOR_ID, "VENDOR")

            verify(refreshTokenService).deleteTokenByVendorId(UDS_VENDOR_ID)
            verify(userSessionRepository).deleteByUserIdAndUserType(UDS_VENDOR_ID, "VENDOR")
        }

        @Test
        fun `resetting customer sessions does not delete vendor refresh tokens`() {
            userSessionService.resetSessions(UDS_CUSTOMER_ID, "CUSTOMER")

            verify(refreshTokenService, never()).deleteTokenByVendorId(any())
        }

        @Test
        fun `resetting vendor sessions does not delete customer refresh tokens`() {
            userSessionService.resetSessions(UDS_VENDOR_ID, "VENDOR")

            verify(refreshTokenService, never()).deleteTokenByCustomerId(any())
        }

        @Test
        fun `deletes refresh token before clearing sessions`() {
            userSessionService.resetSessions(UDS_CUSTOMER_ID, "CUSTOMER")

            val inOrder = inOrder(refreshTokenService, userSessionRepository)
            inOrder.verify(refreshTokenService).deleteTokenByCustomerId(UDS_CUSTOMER_ID)
            inOrder.verify(userSessionRepository).deleteByUserIdAndUserType(UDS_CUSTOMER_ID, "CUSTOMER")
        }
    }

    @Nested
    inner class CleanupExpiredSessions {

        @Test
        fun `delegates to deleteByExpiresAtEpochMillisLessThan with the provided threshold`() {
            val threshold = System.currentTimeMillis()

            userSessionService.cleanupExpiredSessions(threshold)

            verify(userSessionRepository).deleteByExpiresAtEpochMillisLessThan(threshold)
        }

        @Test
        fun `passes zero as threshold to delete all sessions`() {
            userSessionService.cleanupExpiredSessions(0L)

            verify(userSessionRepository).deleteByExpiresAtEpochMillisLessThan(0L)
        }
    }

    @Nested
    inner class SessionExpiryHelper {

        @Test
        fun `isExpired returns true when expiry is in the past`() {
            val expired = customerSession().copy(expiresAtEpochMillis = System.currentTimeMillis() - 1_000L)

            assertTrue(expired.isExpired())
        }

        @Test
        fun `isExpired returns false when expiry is in the future`() {
            val active = customerSession().copy(expiresAtEpochMillis = System.currentTimeMillis() + 900_000L)

            assertFalse(active.isExpired())
        }

        @Test
        fun `isExpired accepts a custom reference time that is before the expiry`() {
            val session = customerSession().copy(expiresAtEpochMillis = 1_000L)

            assertFalse(session.isExpired(currentEpochMillis = 500L))
        }

        @Test
        fun `isExpired accepts a custom reference time that is after the expiry`() {
            val session = customerSession().copy(expiresAtEpochMillis = 1_000L)

            assertTrue(session.isExpired(currentEpochMillis = 1_500L))
        }

        @Test
        fun `isExpired returns true when expiry equals the reference time`() {
            val session = customerSession().copy(expiresAtEpochMillis = 1_000L)

            // expiresAtEpochMillis <= currentEpochMillis -> expired
            assertTrue(session.isExpired(currentEpochMillis = 1_000L))
        }

        @Test
        fun `vendor session expiry check behaves the same as customer`() {
            val expired = vendorSession().copy(expiresAtEpochMillis = System.currentTimeMillis() - 1_000L)
            val active = vendorSession().copy(expiresAtEpochMillis = System.currentTimeMillis() + 900_000L)

            assertTrue(expired.isExpired())
            assertFalse(active.isExpired())
        }
    }
}


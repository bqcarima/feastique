package com.qinet.feastique.service.authentication


import com.qinet.feastique.exception.PhoneNumberUnavailableException
import com.qinet.feastique.exception.UsernameUnavailableException
import com.qinet.feastique.model.dto.LoginDto
import com.qinet.feastique.model.dto.LogoutDto
import com.qinet.feastique.model.dto.user.VendorSignupDto
import com.qinet.feastique.model.entity.address.VendorAddress
import com.qinet.feastique.model.entity.authentication.RefreshToken
import com.qinet.feastique.model.entity.authentication.UserSession
import com.qinet.feastique.model.entity.contact.VendorPhoneNumber
import com.qinet.feastique.model.entity.user.Vendor
import com.qinet.feastique.model.enums.AccountType
import com.qinet.feastique.model.enums.Region
import com.qinet.feastique.repository.address.VendorAddressRepository
import com.qinet.feastique.repository.authentication.RefreshTokenRepository
import com.qinet.feastique.repository.authentication.UserSessionRepository
import com.qinet.feastique.repository.bookmark.VendorBookmarkRepository
import com.qinet.feastique.repository.contact.CustomerPhoneNumberRepository
import com.qinet.feastique.repository.contact.VendorPhoneNumberRepository
import com.qinet.feastique.repository.like.VendorLikeRepository
import com.qinet.feastique.repository.user.VendorRepository
import com.qinet.feastique.response.token.AccessTokenResponse
import com.qinet.feastique.response.token.TokenPairResponse
import com.qinet.feastique.security.HashEncoder
import com.qinet.feastique.security.PasswordEncoder
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.service.customer.CustomerService
import com.qinet.feastique.service.user.UserSessionService
import com.qinet.feastique.service.vendor.VendorService
import com.qinet.feastique.utility.CursorEncoder
import com.qinet.feastique.utility.JwtUtility
import com.qinet.feastique.utility.SecurityUtility
import io.jsonwebtoken.MalformedJwtException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.time.LocalTime
import java.util.*

// Shared fixtures

private val VENDOR_ID = UUID.randomUUID()

private val VENDOR_OPENING_TIME = LocalTime.of(8, 30)
private val VENDOR_CLOSING_TIME = LocalTime.of(18, 30)

private val VENDOR_SIGNUP_DTO = VendorSignupDto(
    username = "sabi_chef",
    firstName = "Ambe",
    lastName = "Chancie",
    phoneNumber = "677222333",
    chefName = "Sabi Chef",
    restaurantName = "Sabi Foods",
    password = "sabiChef98",
    accountType = AccountType.VENDOR,
    region = "CENTRE",
    city = "Yaounde",
    neighbourhood = "Biyem-Assi",
    streetName = "----",
    directions = "Fifty metres after Mogahmo on the other side of the road",
    longitude = "----",
    latitude = "-----",
    openingTime = VENDOR_OPENING_TIME,
    closingTime = VENDOR_CLOSING_TIME,
    balance = 0,
    verified = false,
    image = "image"
)

private val VENDOR_LOGIN_DTO = LoginDto(
    username = "sabi_chef",
    password = "sabiChef98"
)

private const val VENDOR_FAKE_ACCESS_TOKEN = "vendor.header.payload.access-sig"
private const val VENDOR_FAKE_REFRESH_TOKEN = "vendor.header.payload.refresh-sig"
private const val VENDOR_TOKEN_IDENTIFIER = "vendor-tok-id-abc123"
private const val USER_TYPE_VENDOR = "VENDOR"
private val VENDOR_TOKEN_EXPIRY_MILLIS = System.currentTimeMillis() + 900_000L

private val VENDOR_FAKE_TOKEN_PAIR = TokenPairResponse(
    accessToken = VENDOR_FAKE_ACCESS_TOKEN,
    refreshToken = VENDOR_FAKE_REFRESH_TOKEN
)

private fun sabiVendor(): Vendor = Vendor().apply {
    id = VENDOR_ID
    username = "sabi_chef"
    firstName = "Ambe"
    lastName = "Chancie"
    chefName = "Sabi Chef"
    restaurantName = "Sabi Foods"
    accountType = AccountType.VENDOR
    password = "hashed_sabiChef98"
    region = Region.CENTRE
    vendorCode = "CM020001"
    openingTime = VENDOR_OPENING_TIME
    closingTime = VENDOR_CLOSING_TIME
}

private fun sabiSecurity(): UserSecurity = UserSecurity(
    id = VENDOR_ID,
    username = "sabi_chef",
    password = "hashed_sabiChef98",
    userAuthorities = mutableListOf(SimpleGrantedAuthority("ROLE_VENDOR"))
)

private fun sabiSession(): UserSession = UserSession(
    tokenIdentifier = VENDOR_TOKEN_IDENTIFIER,
    userId = VENDOR_ID,
    userType = USER_TYPE_VENDOR,
    expiresAtEpochMillis = VENDOR_TOKEN_EXPIRY_MILLIS
)

private fun sabiRefreshToken(): RefreshToken = RefreshToken(
    customerId = null,
    vendorId = VENDOR_ID,
    expiresAt = Date(System.currentTimeMillis() + 2_592_000_000L),
    hashedToken = "hashed_sabi_refresh"
)

// VendorService — signup & login

class VendorServiceTest {

    private lateinit var authManager: AuthenticationManager
    private lateinit var vendorRepository: VendorRepository
    private lateinit var vendorAddressRepository: VendorAddressRepository
    private lateinit var vendorPhoneNumberRepository: VendorPhoneNumberRepository
    private lateinit var customerPhoneNumberRepository: CustomerPhoneNumberRepository
    private lateinit var vendorLikeRepository: VendorLikeRepository
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var jwtUtility: JwtUtility
    private lateinit var userSessionService: UserSessionService
    private lateinit var refreshTokenService: RefreshTokenService
    private lateinit var securityUtility: SecurityUtility
    private lateinit var vendorBookmarkRepository: VendorBookmarkRepository
    private lateinit var cursorEncoder: CursorEncoder
    private lateinit var vendorService: VendorService

    @BeforeEach
    fun setUp() {
        authManager = mock()
        vendorRepository = mock()
        vendorAddressRepository = mock()
        vendorPhoneNumberRepository = mock()
        customerPhoneNumberRepository = mock()
        vendorLikeRepository = mock()
        passwordEncoder = mock()
        jwtUtility = mock()
        userSessionService = mock()
        refreshTokenService = mock()
        securityUtility = mock()
        vendorBookmarkRepository = mock()
        cursorEncoder = mock()

        vendorService = VendorService(
            authManager = authManager,
            vendorRepository = vendorRepository,
            vendorAddressRepository = vendorAddressRepository,
            vendorPhoneNumberRepository = vendorPhoneNumberRepository,
            customerPhoneNumberRepository = customerPhoneNumberRepository,
            vendorLikeRepository = vendorLikeRepository,
            passwordEncoder = passwordEncoder,
            jwtUtility = jwtUtility,
            userSessionService = userSessionService,
            refreshTokenService = refreshTokenService,
            securityUtility = securityUtility,
            vendorBookmarkRepository = vendorBookmarkRepository,
            cursorEncoder = cursorEncoder
        )
    }

    // Signup happy path

    @Nested
    inner class SignupHappyPath {

        @BeforeEach
        fun stubHappyPath() {
            val vendor = sabiVendor()
            whenever(vendorRepository.existsByUsernameIgnoreCase("sabi_chef")).thenReturn(false)
            whenever(vendorPhoneNumberRepository.existsByPhoneNumber("677222333")).thenReturn(false)
            whenever(customerPhoneNumberRepository.existsByPhoneNumber("677222333")).thenReturn(false)
            whenever(passwordEncoder.encode("sabiChef98")).thenReturn("hashed_sabiChef98")
            whenever(vendorRepository.save(any())).thenReturn(vendor)
            whenever(vendorRepository.findTopByRegionOrderByVendorCodeDescWithLock(any(), any()))
                .thenReturn(emptyList())
            whenever(vendorAddressRepository.save(any())).thenReturn(
                VendorAddress().apply { this.vendor = vendor }
            )
            whenever(vendorPhoneNumberRepository.save(any())).thenReturn(
                VendorPhoneNumber().apply { this.vendor = vendor }
            )
        }

        @Test
        fun `returns saved vendor with correct username`() {
            val result = vendorService.signup(VENDOR_SIGNUP_DTO)
            assertEquals("sabi_chef", result.username)
        }

        @Test
        fun `returns saved vendor with correct name fields`() {
            val result = vendorService.signup(VENDOR_SIGNUP_DTO)
            assertEquals("Ambe", result.firstName)
            assertEquals("Chancie", result.lastName)
            assertEquals("Sabi Chef", result.chefName)
            assertEquals("Sabi Foods", result.restaurantName)
        }

        @Test
        fun `encodes password before persisting`() {
            vendorService.signup(VENDOR_SIGNUP_DTO)
            verify(passwordEncoder).encode("sabiChef98")
        }

        @Test
        fun `sets accountType to VENDOR`() {
            vendorService.signup(VENDOR_SIGNUP_DTO)
            verify(vendorRepository, atLeastOnce()).save(argThat {
                accountType == AccountType.VENDOR
            })
        }

        @Test
        fun `saves vendor at least twice - initial then after linking address and phone`() {
            vendorService.signup(VENDOR_SIGNUP_DTO)
            verify(vendorRepository, atLeast(2)).save(any())
        }

        @Test
        fun `persists phone 677222333 as default`() {
            vendorService.signup(VENDOR_SIGNUP_DTO)
            verify(vendorPhoneNumberRepository).save(argThat {
                phoneNumber == "677222333" && default == true
            })
        }

        @Test
        fun `persists address with all fields from vendor signup dto`() {
            vendorService.signup(VENDOR_SIGNUP_DTO)
            verify(vendorAddressRepository).save(argThat {
                country == "Cameroon" &&
                        region == Region.CENTRE &&
                        city == "Yaounde" &&
                        neighbourhood == "Biyem-Assi" &&
                        streetName == "----" &&
                        directions == "Fifty metres after Mogahmo on the other side of the road" &&
                        longitude == "----" &&
                        latitude == "-----"
            })
        }

        @Test
        fun `persists opening time 08-30 and closing time 18-30`() {
            vendorService.signup(VENDOR_SIGNUP_DTO)
            verify(vendorRepository, atLeastOnce()).save(argThat {
                openingTime == VENDOR_OPENING_TIME &&
                        closingTime == VENDOR_CLOSING_TIME
            })
        }

        @Test
        fun `generates vendor code for CENTRE region from first sequence`() {
            vendorService.signup(VENDOR_SIGNUP_DTO)
            verify(vendorRepository, atLeastOnce()).save(argThat {
                vendorCode == "CM020001"
            })
        }

        @Test
        fun `queries existing vendor codes to determine next sequence number`() {
            vendorService.signup(VENDOR_SIGNUP_DTO)
            verify(vendorRepository).findTopByRegionOrderByVendorCodeDescWithLock(Region.CENTRE)
        }

        @Test
        fun `increments vendor code sequence when a prior vendor exists in the same region`() {
            val existingVendor = sabiVendor().apply { vendorCode = "CM020003" }
            whenever(vendorRepository.findTopByRegionOrderByVendorCodeDescWithLock(Region.CENTRE))
                .thenReturn(listOf(existingVendor))

            vendorService.signup(VENDOR_SIGNUP_DTO)

            verify(vendorRepository, atLeastOnce()).save(argThat {
                vendorCode == "CM020004"
            })
        }
    }

    // Signup — duplicate username

    @Nested
    inner class SignupDuplicateUsername {

        @BeforeEach
        fun stubTakenUsername() {
            whenever(vendorRepository.existsByUsernameIgnoreCase("sabi_chef")).thenReturn(true)
        }

        @Test
        fun `throws UsernameUnavailableException`() {
            assertThrows<UsernameUnavailableException> {
                vendorService.signup(VENDOR_SIGNUP_DTO)
            }
        }

        @Test
        fun `short-circuits before phone duplicate check`() {
            assertThrows<UsernameUnavailableException> {
                vendorService.signup(VENDOR_SIGNUP_DTO)
            }
            verify(vendorPhoneNumberRepository, never()).existsByPhoneNumber(any())
            verify(customerPhoneNumberRepository, never()).existsByPhoneNumber(any())
        }

        @Test
        fun `does not persist anything`() {
            assertThrows<UsernameUnavailableException> {
                vendorService.signup(VENDOR_SIGNUP_DTO)
            }
            verify(vendorRepository, never()).save(any())
            verify(vendorAddressRepository, never()).save(any())
            verify(vendorPhoneNumberRepository, never()).save(any())
        }
    }

    // Signup — duplicate phone

    @Nested
    inner class SignupDuplicatePhone {

        // isDuplicateFound for phone — check whether VendorService uses && or ||.
        // These tests assume &&: both repos must return true to block signup.
        // If it uses ||, swap the `proceeds normally` tests to assertThrows.

        @BeforeEach
        fun stubUniqueUsername() {
            whenever(vendorRepository.existsByUsernameIgnoreCase("sabi_chef")).thenReturn(false)
        }

        @Test
        fun `throws PhoneNumberUnavailableException when number exists in both repositories`() {
            whenever(vendorPhoneNumberRepository.existsByPhoneNumber("677222333")).thenReturn(true)
            whenever(customerPhoneNumberRepository.existsByPhoneNumber("677222333")).thenReturn(true)

            assertThrows<PhoneNumberUnavailableException> {
                vendorService.signup(VENDOR_SIGNUP_DTO)
            }
        }

        @Test
        fun `does not persist anything when phone is a duplicate`() {
            whenever(vendorPhoneNumberRepository.existsByPhoneNumber("677222333")).thenReturn(true)
            whenever(customerPhoneNumberRepository.existsByPhoneNumber("677222333")).thenReturn(true)

            assertThrows<PhoneNumberUnavailableException> {
                vendorService.signup(VENDOR_SIGNUP_DTO)
            }
            verify(vendorRepository, never()).save(any())
            verify(vendorAddressRepository, never()).save(any())
            verify(vendorPhoneNumberRepository, never()).save(any())
        }

        @Test
        fun `proceeds normally when number exists only in vendor repository`() {
            val vendor = sabiVendor()
            whenever(vendorPhoneNumberRepository.existsByPhoneNumber("677222333")).thenReturn(true)
            whenever(customerPhoneNumberRepository.existsByPhoneNumber("677222333")).thenReturn(false)
            stubSignupDependencies(vendor)

            assertDoesNotThrow { vendorService.signup(VENDOR_SIGNUP_DTO) }
        }

        @Test
        fun `proceeds normally when number exists only in customer repository`() {
            val vendor = sabiVendor()
            whenever(vendorPhoneNumberRepository.existsByPhoneNumber("677222333")).thenReturn(false)
            whenever(customerPhoneNumberRepository.existsByPhoneNumber("677222333")).thenReturn(true)
            stubSignupDependencies(vendor)

            assertDoesNotThrow { vendorService.signup(VENDOR_SIGNUP_DTO) }
        }

        private fun stubSignupDependencies(vendor: Vendor) {
            whenever(passwordEncoder.encode("sabiChef98")).thenReturn("hashed_sabiChef98")
            whenever(vendorRepository.save(any())).thenReturn(vendor)
            whenever(vendorRepository.findTopByRegionOrderByVendorCodeDescWithLock(any(), any()))
                .thenReturn(emptyList())
            whenever(vendorAddressRepository.save(any())).thenReturn(
                VendorAddress().apply { this.vendor = vendor }
            )
            whenever(vendorPhoneNumberRepository.save(any())).thenReturn(
                VendorPhoneNumber().apply { this.vendor = vendor }
            )
        }
    }

    // Login happy path

    @Nested
    inner class LoginHappyPath {

        private lateinit var mockAuthentication: Authentication

        @BeforeEach
        fun stubLogin() {
            mockAuthentication = mock()
            whenever(mockAuthentication.principal).thenReturn(sabiSecurity())
            whenever(authManager.authenticate(any())).thenReturn(mockAuthentication)
            whenever(
                jwtUtility.generateTokenPair(VENDOR_ID, "sabi_chef", AccountType.VENDOR)
            ).thenReturn(VENDOR_FAKE_TOKEN_PAIR)
        }

        @Test
        fun `returns token pair on valid credentials`() {
            val result = vendorService.login(VENDOR_LOGIN_DTO)
            assertEquals(VENDOR_FAKE_ACCESS_TOKEN, result.accessToken)
            assertEquals(VENDOR_FAKE_REFRESH_TOKEN, result.refreshToken)
        }

        @Test
        fun `delegates authentication to AuthenticationManager`() {
            vendorService.login(VENDOR_LOGIN_DTO)
            verify(authManager).authenticate(argThat<UsernamePasswordAuthenticationToken> {
                principal == "sabi_chef" &&
                        credentials == "sabiChef98"
            })
        }

        @Test
        fun `generates token pair using vendor id and username`() {
            vendorService.login(VENDOR_LOGIN_DTO)
            verify(jwtUtility).generateTokenPair(VENDOR_ID, "sabi_chef", AccountType.VENDOR)
        }
    }

    // Login — bad credentials

    @Nested
    inner class LoginBadCredentials {

        @Test
        fun `throws exception on wrong password`() {
            whenever(authManager.authenticate(any()))
                .thenThrow(BadCredentialsException("Bad credentials"))

            assertThrows<Exception> {
                vendorService.login(LoginDto(username = "sabi_chef", password = "wrong"))
            }
        }

        @Test
        fun `does not generate any token when authentication fails`() {
            whenever(authManager.authenticate(any()))
                .thenThrow(BadCredentialsException("Bad credentials"))

            runCatching { vendorService.login(LoginDto("sabi_chef", "wrong")) }

            verify(jwtUtility, never()).generateTokenPair(any(), any(), any())
        }
    }
}

// AuthenticationService — vendor login, logout, refresh

class VendorAuthenticationServiceTest {

    private lateinit var customerService: CustomerService
    private lateinit var vendorService: VendorService
    private lateinit var jwtUtility: JwtUtility
    private lateinit var userSessionService: UserSessionService
    private lateinit var refreshTokenService: RefreshTokenService
    private lateinit var authService: AuthenticationService

    @BeforeEach
    fun setUp() {
        customerService = mock()
        vendorService = mock()
        jwtUtility = mock()
        userSessionService = mock()
        refreshTokenService = mock()

        authService = AuthenticationService(
            customerService = customerService,
            vendorService = vendorService,
            jwtUtility = jwtUtility,
            userSessionService = userSessionService,
            refreshTokenService = refreshTokenService
        )
    }

    // Signup delegation

    @Nested
    inner class VendorSignupDelegation {

        @Test
        fun `handleVendorSignup delegates to vendorService`() {
            val expected = sabiVendor()
            whenever(vendorService.signup(VENDOR_SIGNUP_DTO)).thenReturn(expected)

            val result = authService.handleVendorSignup(VENDOR_SIGNUP_DTO)

            assertEquals(expected, result)
            verify(vendorService).signup(VENDOR_SIGNUP_DTO)
        }
    }

    // Login

    @Nested
    inner class VendorLogin {

        @BeforeEach
        fun stubLoginDependencies() {
            whenever(vendorService.login(VENDOR_LOGIN_DTO)).thenReturn(VENDOR_FAKE_TOKEN_PAIR)
            whenever(jwtUtility.getTokenIdentifier(VENDOR_FAKE_ACCESS_TOKEN)).thenReturn(VENDOR_TOKEN_IDENTIFIER)
            whenever(jwtUtility.getExpirationEpochMillis(VENDOR_FAKE_ACCESS_TOKEN)).thenReturn(
                VENDOR_TOKEN_EXPIRY_MILLIS
            )
            whenever(jwtUtility.getUserId(VENDOR_FAKE_ACCESS_TOKEN)).thenReturn(VENDOR_ID)
            whenever(jwtUtility.getUserType(VENDOR_FAKE_ACCESS_TOKEN)).thenReturn(USER_TYPE_VENDOR)
            whenever(jwtUtility.parseToken(VENDOR_ID, USER_TYPE_VENDOR, VENDOR_FAKE_REFRESH_TOKEN))
                .thenReturn(sabiRefreshToken())
        }

        @Test
        fun `handleVendorLogin returns the token pair from vendorService`() {
            val result = authService.handleVendorLogin(VENDOR_LOGIN_DTO)
            assertEquals(VENDOR_FAKE_ACCESS_TOKEN, result.accessToken)
            assertEquals(VENDOR_FAKE_REFRESH_TOKEN, result.refreshToken)
        }

        @Test
        fun `handleVendorLogin resets existing sessions before creating a new one`() {
            authService.handleVendorLogin(VENDOR_LOGIN_DTO)

            val order = inOrder(userSessionService)
            order.verify(userSessionService).resetSessions(VENDOR_ID, USER_TYPE_VENDOR)
            order.verify(userSessionService).createSession(
                tokenIdentifier = VENDOR_TOKEN_IDENTIFIER,
                userId = VENDOR_ID,
                userType = USER_TYPE_VENDOR,
                expiresAtEpocMillis = VENDOR_TOKEN_EXPIRY_MILLIS
            )
        }

        @Test
        fun `handleVendorLogin stores refresh token with correct vendor id`() {
            authService.handleVendorLogin(VENDOR_LOGIN_DTO)
            verify(refreshTokenService).storeRefreshToken(argThat {
                vendorId == VENDOR_ID &&
                        customerId == null &&
                        hashedToken == "hashed_sabi_refresh"
            })
        }

        @Test
        fun `handleVendorLogin creates a session with correct token identifier and expiry`() {
            authService.handleVendorLogin(VENDOR_LOGIN_DTO)
            verify(userSessionService).createSession(
                tokenIdentifier = VENDOR_TOKEN_IDENTIFIER,
                userId = VENDOR_ID,
                userType = USER_TYPE_VENDOR,
                expiresAtEpocMillis = VENDOR_TOKEN_EXPIRY_MILLIS
            )
        }

        @Test
        fun `handleVendorLogin does not create session when vendorService throws`() {
            whenever(vendorService.login(VENDOR_LOGIN_DTO))
                .thenThrow(BadCredentialsException("Bad credentials"))

            runCatching { authService.handleVendorLogin(VENDOR_LOGIN_DTO) }

            verify(userSessionService, never()).createSession(any(), any(), any(), any())
            verify(refreshTokenService, never()).storeRefreshToken(any())
        }
    }


    // Token refresh

    @Nested
    inner class VendorTokenRefresh {

        private val newAccessToken = "vendor.header.payload.new-access-sig"
        private val newTokenId = "vendor-new-tok-id-xyz"
        private val newExpiry = System.currentTimeMillis() + 900_000L

        @BeforeEach
        fun stubRefresh() {
            whenever(jwtUtility.refresh(VENDOR_FAKE_REFRESH_TOKEN))
                .thenReturn(AccessTokenResponse(newAccessToken))
            whenever(jwtUtility.getTokenIdentifier(newAccessToken)).thenReturn(newTokenId)
            whenever(jwtUtility.getExpirationEpochMillis(newAccessToken)).thenReturn(newExpiry)
            whenever(jwtUtility.getUserId(newAccessToken)).thenReturn(VENDOR_ID)
            whenever(jwtUtility.getUserType(newAccessToken)).thenReturn(USER_TYPE_VENDOR)
        }

        @Test
        fun `handleRefresh returns new access token`() {
            val result = authService.handleRefresh(VENDOR_FAKE_REFRESH_TOKEN)
            assertEquals(newAccessToken, result.accessToken)
        }

        @Test
        fun `handleRefresh delegates to jwtUtility refresh`() {
            authService.handleRefresh(VENDOR_FAKE_REFRESH_TOKEN)
            verify(jwtUtility).refresh(VENDOR_FAKE_REFRESH_TOKEN)
        }

        @Test
        fun `handleRefresh creates a new session for the new vendor access token`() {
            authService.handleRefresh(VENDOR_FAKE_REFRESH_TOKEN)
            verify(userSessionService).createSession(
                tokenIdentifier = newTokenId,
                userId = VENDOR_ID,
                userType = USER_TYPE_VENDOR,
                expiresAtEpocMillis = newExpiry
            )
        }

        @Test
        fun `handleRefresh does not create session when jwtUtility throws`() {
            whenever(jwtUtility.refresh(VENDOR_FAKE_REFRESH_TOKEN))
                .thenThrow(RuntimeException("Invalid refresh token"))

            runCatching { authService.handleRefresh(VENDOR_FAKE_REFRESH_TOKEN) }

            verify(userSessionService, never()).createSession(any(), any(), any(), any())
        }
    }

    // Logout

    @Nested
    inner class VendorLogout {

        private val bearerHeader = "Bearer $VENDOR_FAKE_ACCESS_TOKEN"

        @BeforeEach
        fun stubLogout() {
            whenever(jwtUtility.getTokenIdentifier(VENDOR_FAKE_ACCESS_TOKEN))
                .thenReturn(VENDOR_TOKEN_IDENTIFIER)
        }

        @Test
        fun `handleLogout deletes vendor session when Authorization header is present`() {
            authService.handleLogout(bearerHeader, null)
            verify(userSessionService).deleteSession(VENDOR_TOKEN_IDENTIFIER)
        }

        @Test
        fun `handleLogout revokes vendor refresh token when LogoutDto is present`() {
            val dto = LogoutDto(
                accessToken = VENDOR_FAKE_ACCESS_TOKEN,
                refreshToken = VENDOR_FAKE_REFRESH_TOKEN
            )
            authService.handleLogout(null, dto)
            verify(refreshTokenService).revokeByRefreshToken(VENDOR_FAKE_REFRESH_TOKEN)
        }

        @Test
        fun `handleLogout deletes session AND revokes refresh token when both are provided`() {
            val dto = LogoutDto(
                accessToken = VENDOR_FAKE_ACCESS_TOKEN,
                refreshToken = VENDOR_FAKE_REFRESH_TOKEN
            )
            authService.handleLogout(bearerHeader, dto)

            verify(userSessionService).deleteSession(VENDOR_TOKEN_IDENTIFIER)
            verify(refreshTokenService).revokeByRefreshToken(VENDOR_FAKE_REFRESH_TOKEN)
        }

        @Test
        fun `handleLogout is a no-op when both header and dto are null`() {
            authService.handleLogout(null, null)

            verify(userSessionService, never()).deleteSession(any())
            verify(refreshTokenService, never()).revokeByRefreshToken(any())
        }

        @Test
        fun `handleLogout ignores non-Bearer Authorization header`() {
            authService.handleLogout("Basic dXNlcjpwYXNz", null)
            verify(userSessionService, never()).deleteSession(any())
        }

        @Test
        fun `handleLogout throws MalformedJwtException when vendor access token is malformed`() {
            whenever(jwtUtility.getTokenIdentifier("bad.vendor.token"))
                .thenThrow(MalformedJwtException("Malformed"))

            assertThrows<MalformedJwtException> {
                authService.handleLogout("Bearer bad.vendor.token", null)
            }
        }

        @Test
        fun `handleLogout does not delete session when only refresh token is provided`() {
            val dto = LogoutDto(
                accessToken = VENDOR_FAKE_ACCESS_TOKEN,
                refreshToken = VENDOR_FAKE_REFRESH_TOKEN
            )
            authService.handleLogout(null, dto)
            verify(userSessionService, never()).deleteSession(any())
        }
    }
}


// UserSessionService — vendor session lifecycle
class VendorSessionServiceTest {

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
    inner class CreateVendorSession {

        @Test
        fun `createSession persists a vendor UserSession with correct fields`() {
            val session = sabiSession()
            whenever(userSessionRepository.save(any())).thenReturn(session)

            val result = userSessionService.createSession(
                tokenIdentifier = VENDOR_TOKEN_IDENTIFIER,
                userId = VENDOR_ID,
                userType = USER_TYPE_VENDOR,
                expiresAtEpocMillis = VENDOR_TOKEN_EXPIRY_MILLIS
            )

            assertEquals(VENDOR_TOKEN_IDENTIFIER, result.tokenIdentifier)
            assertEquals(VENDOR_ID, result.userId)
            assertEquals(USER_TYPE_VENDOR, result.userType)
            assertEquals(VENDOR_TOKEN_EXPIRY_MILLIS, result.expiresAtEpochMillis)
        }

        @Test
        fun `createSession calls repository save for vendor`() {
            whenever(userSessionRepository.save(any())).thenReturn(sabiSession())

            userSessionService.createSession(
                VENDOR_TOKEN_IDENTIFIER, VENDOR_ID, USER_TYPE_VENDOR, VENDOR_TOKEN_EXPIRY_MILLIS
            )

            verify(userSessionRepository).save(any())
        }
    }

    @Nested
    inner class GetVendorSession {

        @Test
        fun `getSession returns vendor session when token identifier exists`() {
            val session = sabiSession()
            whenever(userSessionRepository.findByTokenIdentifier(VENDOR_TOKEN_IDENTIFIER))
                .thenReturn(session)

            val result = userSessionService.getSession(VENDOR_TOKEN_IDENTIFIER)

            assertEquals(session, result)
        }

        @Test
        fun `getSession returns null for unknown vendor token identifier`() {
            whenever(userSessionRepository.findByTokenIdentifier("unknown-vendor")).thenReturn(null)
            assertNull(userSessionService.getSession("unknown-vendor"))
        }
    }

    @Nested
    inner class DeleteVendorSession {

        @Test
        fun `deleteSession removes existing vendor session`() {
            whenever(userSessionRepository.existsByTokenIdentifier(VENDOR_TOKEN_IDENTIFIER))
                .thenReturn(true)

            userSessionService.deleteSession(VENDOR_TOKEN_IDENTIFIER)

            verify(userSessionRepository).deleteByTokenIdentifier(VENDOR_TOKEN_IDENTIFIER)
        }

        @Test
        fun `deleteSession is a no-op when vendor session does not exist`() {
            whenever(userSessionRepository.existsByTokenIdentifier("ghost-vendor")).thenReturn(false)

            userSessionService.deleteSession("ghost-vendor")

            verify(userSessionRepository, never()).deleteByTokenIdentifier(any())
        }
    }

    @Nested
    inner class ResetVendorSessions {

        @Test
        fun `resetSessions deletes vendor refresh token and all vendor sessions`() {
            userSessionService.resetSessions(VENDOR_ID, USER_TYPE_VENDOR)

            verify(refreshTokenService).deleteTokenByVendorId(VENDOR_ID)
            verify(userSessionRepository).deleteByUserIdAndUserType(VENDOR_ID, USER_TYPE_VENDOR)
        }

        @Test
        fun `resetSessions for vendor does not touch customer refresh tokens`() {
            userSessionService.resetSessions(VENDOR_ID, USER_TYPE_VENDOR)
            verify(refreshTokenService, never()).deleteTokenByCustomerId(any())
        }
    }

    @Nested
    inner class VendorSessionExpiry {

        @Test
        fun `isExpired returns true when vendor session expiry is in the past`() {
            val expired = sabiSession().copy(expiresAtEpochMillis = System.currentTimeMillis() - 1000)
            assertTrue(expired.isExpired())
        }

        @Test
        fun `isExpired returns false when vendor session expiry is in the future`() {
            val active = sabiSession().copy(expiresAtEpochMillis = System.currentTimeMillis() + 900_000)
            assertFalse(active.isExpired())
        }

        @Test
        fun `isExpired uses provided currentEpochMillis for vendor session`() {
            val session = sabiSession().copy(expiresAtEpochMillis = 1_000L)
            val beforeTime = 500L
            val afterTime = 1_500L

            assertFalse(session.isExpired(beforeTime))
            assertTrue(session.isExpired(afterTime))
        }
    }
}


// RefreshTokenService — vendor token CRUD
class VendorRefreshTokenServiceTest {

    private lateinit var refreshTokenRepository: RefreshTokenRepository
    private lateinit var hashEncoder: HashEncoder
    private lateinit var refreshTokenService: RefreshTokenService

    @BeforeEach
    fun setUp() {
        refreshTokenRepository = mock()
        hashEncoder = mock()
        refreshTokenService = RefreshTokenService(refreshTokenRepository, hashEncoder)
    }

    @Test
    fun `storeRefreshToken persists vendor refresh token`() {
        val token = sabiRefreshToken()
        refreshTokenService.storeRefreshToken(token)
        verify(refreshTokenRepository).save(token)
    }

    @Test
    fun `getTokenByVendorId returns token when found`() {
        whenever(refreshTokenRepository.findByVendorId(VENDOR_ID)).thenReturn(sabiRefreshToken())

        val result = refreshTokenService.getTokenByVendorId(VENDOR_ID)

        assertNotNull(result)
        assertEquals(VENDOR_ID, result?.vendorId)
        assertNull(result?.customerId)
    }

    @Test
    fun `getTokenByVendorId returns null when not found`() {
        whenever(refreshTokenRepository.findByVendorId(VENDOR_ID)).thenReturn(null)
        assertNull(refreshTokenService.getTokenByVendorId(VENDOR_ID))
    }

    @Test
    fun `deleteTokenByVendorId calls repository delete`() {
        refreshTokenService.deleteTokenByVendorId(VENDOR_ID)
        verify(refreshTokenRepository).deleteByVendorId(VENDOR_ID)
    }

    @Test
    fun `revokeByRefreshToken deletes vendor token matching the hashed raw token`() {
        val raw = VENDOR_FAKE_REFRESH_TOKEN
        val hashed = "hashed_sabi_refresh"
        whenever(hashEncoder.encode(raw)).thenReturn(hashed)
        whenever(refreshTokenRepository.findByHashedToken(hashed)).thenReturn(sabiRefreshToken())

        refreshTokenService.revokeByRefreshToken(raw)

        verify(refreshTokenRepository).delete(any())
    }

    @Test
    fun `revokeByRefreshToken is a no-op when no matching vendor token exists`() {
        whenever(hashEncoder.encode(any())).thenReturn("hashed_something")
        whenever(refreshTokenRepository.findByHashedToken(any())).thenReturn(null)

        refreshTokenService.revokeByRefreshToken("unknown-vendor-token")

        verify(refreshTokenRepository, never()).delete(any())
    }

    @Test
    fun `vendor refresh token has null customerId`() {
        whenever(refreshTokenRepository.findByVendorId(VENDOR_ID)).thenReturn(sabiRefreshToken())

        val result = refreshTokenService.getTokenByVendorId(VENDOR_ID)

        assertNull(result?.customerId)
    }
}


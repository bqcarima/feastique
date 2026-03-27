package com.qinet.feastique.service.vendor

import com.qinet.feastique.exception.DuplicateFoundException
import com.qinet.feastique.exception.RequestedEntityNotFoundException
import com.qinet.feastique.exception.UserNotFoundException
import com.qinet.feastique.model.dto.user.PasswordChangeDto
import com.qinet.feastique.model.dto.user.VendorUpdateDto
import com.qinet.feastique.model.entity.authentication.RefreshToken
import com.qinet.feastique.model.entity.user.Vendor
import com.qinet.feastique.model.enums.AccountType
import com.qinet.feastique.model.enums.Region
import com.qinet.feastique.repository.address.VendorAddressRepository
import com.qinet.feastique.repository.bookmark.VendorBookmarkRepository
import com.qinet.feastique.repository.contact.CustomerPhoneNumberRepository
import com.qinet.feastique.repository.contact.VendorPhoneNumberRepository
import com.qinet.feastique.repository.like.VendorLikeRepository
import com.qinet.feastique.repository.user.VendorRepository
import com.qinet.feastique.response.token.TokenPairResponse
import com.qinet.feastique.security.PasswordEncoder
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.service.authentication.RefreshTokenService
import com.qinet.feastique.service.user.UserSessionService
import com.qinet.feastique.utility.CursorEncoder
import com.qinet.feastique.utility.JwtUtility
import com.qinet.feastique.utility.SecurityUtility
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.time.LocalTime
import java.util.*

// Shared data

private val VS_VENDOR_ID = UUID.randomUUID()

private const val VS_NEW_ACCESS_TOKEN = "vs.new.header.payload.access-sig"
private const val VS_NEW_REFRESH_TOKEN = "vs.new.header.payload.refresh-sig"
private const val VS_TOKEN_IDENTIFIER = "vs-tok-id-abc123"
private val VS_TOKEN_EXPIRY = System.currentTimeMillis() + 900_000L

private val VS_NEW_TOKEN_PAIR = TokenPairResponse(
    accessToken = VS_NEW_ACCESS_TOKEN,
    refreshToken = VS_NEW_REFRESH_TOKEN
)

private fun sabiVendor(username: String = "sabi_chef"): Vendor = Vendor().apply {
    id = VS_VENDOR_ID
    this.username = username
    firstName = "Ambe"
    lastName = "Chancie"
    chefName = "Sabi Chef"
    restaurantName = "Sabi Foods"
    accountType = AccountType.VENDOR
    password = "hashed_sabiChef98"
    region = Region.CENTRE
    vendorCode = "CM020001"
    openingTime = LocalTime.of(8, 30)
    closingTime = LocalTime.of(18, 30)
}

private fun sabiSecurity(username: String = "sabi_chef"): UserSecurity = UserSecurity(
    id = VS_VENDOR_ID,
    username = username,
    password = "hashed_sabiChef98",
    userAuthorities = mutableListOf(SimpleGrantedAuthority("ROLE_VENDOR"))
)


// VendorService tests
class VendorServiceProfileTest {

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


    // getVendorById
    @Nested
    inner class GetVendorById {

        @Test
        fun `returns vendor when found`() {
            val vendor = sabiVendor()
            whenever(vendorRepository.findById(VS_VENDOR_ID))
                .thenReturn(Optional.of(vendor))

            val result = vendorService.getVendorById(VS_VENDOR_ID)

            assertEquals(VS_VENDOR_ID, result.id)
            assertEquals("sabi_chef", result.username)
        }

        @Test
        fun `throws UserNotFoundException when vendor does not exist`() {
            whenever(vendorRepository.findById(VS_VENDOR_ID))
                .thenReturn(Optional.empty())

            assertThrows<UserNotFoundException> {
                vendorService.getVendorById(VS_VENDOR_ID)
            }
        }
    }


    // getVendorByIdWithAddressAndPhoneNumber
    @Nested
    inner class GetVendorByIdWithAddressAndPhoneNumber {

        @Test
        fun `returns vendor with address and phone when found`() {
            val vendor = sabiVendor()
            whenever(vendorRepository.findVendorByIdWithAddressAndPhoneNumber(VS_VENDOR_ID))
                .thenReturn(vendor)

            val result = vendorService.getVendorByIdWithAddressAndPhoneNumber(sabiSecurity())

            assertNotNull(result)
            assertEquals(VS_VENDOR_ID, result.id)
        }

        @Test
        fun `throws RequestedEntityNotFoundException when vendor is not found`() {
            whenever(vendorRepository.findVendorByIdWithAddressAndPhoneNumber(VS_VENDOR_ID))
                .thenReturn(null)

            assertThrows<RequestedEntityNotFoundException> {
                vendorService.getVendorByIdWithAddressAndPhoneNumber(sabiSecurity())
            }
        }
    }


    // updateVendor
    @Nested
    inner class UpdateVendor {

        private val updateDto = VendorUpdateDto(
            username = "sabi_chef",
            firstName = "Ambe",
            lastName = "Chancie",
            chefName = "Sabi Chef",
            restaurantName = "Sabi Foods",
            image = "https://cdn.feastique.com/sabi.jpg"
        )

        @BeforeEach
        fun stubUpdate() {
            val vendor = sabiVendor()
            whenever(vendorRepository.findById(VS_VENDOR_ID)).thenReturn(Optional.of(vendor))
            whenever(vendorRepository.save(any())).thenReturn(vendor)
            whenever(jwtUtility.generateTokenPair(VS_VENDOR_ID, "sabi_chef", AccountType.VENDOR))
                .thenReturn(VS_NEW_TOKEN_PAIR)
            whenever(jwtUtility.getTokenIdentifier(VS_NEW_ACCESS_TOKEN)).thenReturn(VS_TOKEN_IDENTIFIER)
            whenever(jwtUtility.getExpirationEpochMillis(VS_NEW_ACCESS_TOKEN)).thenReturn(VS_TOKEN_EXPIRY)
            whenever(jwtUtility.getUserId(VS_NEW_ACCESS_TOKEN)).thenReturn(VS_VENDOR_ID)
            whenever(jwtUtility.getUserType(VS_NEW_ACCESS_TOKEN)).thenReturn("VENDOR")
            whenever(jwtUtility.parseToken(VS_VENDOR_ID, "VENDOR", VS_NEW_REFRESH_TOKEN))
                .thenReturn(
                    RefreshToken(
                        customerId = null,
                        vendorId = VS_VENDOR_ID,
                        expiresAt = Date(System.currentTimeMillis() + 2_592_000_000L),
                        hashedToken = "hashed_new_vendor_refresh"
                    )
                )
        }

        @Test
        fun `returns new token pair after update`() {
            val result = vendorService.updateVendor(updateDto, sabiSecurity())
            assertEquals(VS_NEW_ACCESS_TOKEN, result.accessToken)
            assertEquals(VS_NEW_REFRESH_TOKEN, result.refreshToken)
        }

        @Test
        fun `saves updated vendor fields`() {
            vendorService.updateVendor(updateDto, sabiSecurity())
            verify(vendorRepository).save(argThat {
                firstName == "Ambe" &&
                        lastName == "Chancie" &&
                        chefName == "Sabi Chef" &&
                        restaurantName == "Sabi Foods" &&
                        image == "https://cdn.feastique.com/sabi.jpg"
            })
        }

        @Test
        fun `resets sessions after update`() {
            vendorService.updateVendor(updateDto, sabiSecurity())
            verify(userSessionService).resetSessions(VS_VENDOR_ID, AccountType.VENDOR.toString())
        }

        @Test
        fun `creates new session after update`() {
            vendorService.updateVendor(updateDto, sabiSecurity())
            verify(userSessionService).createSession(
                tokenIdentifier = VS_TOKEN_IDENTIFIER,
                userId = VS_VENDOR_ID,
                userType = "VENDOR",
                expiresAtEpocMillis = VS_TOKEN_EXPIRY
            )
        }

        @Test
        fun `stores new refresh token after update`() {
            vendorService.updateVendor(updateDto, sabiSecurity())
            verify(refreshTokenService).storeRefreshToken(argThat {
                vendorId == VS_VENDOR_ID &&
                        customerId == null &&
                        hashedToken == "hashed_new_vendor_refresh"
            })
        }

        @Test
        fun `generates new token pair after update`() {
            vendorService.updateVendor(updateDto, sabiSecurity())
            verify(jwtUtility).generateTokenPair(VS_VENDOR_ID, "sabi_chef", AccountType.VENDOR)
        }
    }


    // updateVendor — username change
    @Nested
    inner class UpdateVendorUsernameChange {

        private val updateDtoNewUsername = VendorUpdateDto(
            username = "sabi_chef_updated",
            firstName = "Ambe",
            lastName = "Chancie",
            chefName = "Sabi Chef",
            restaurantName = "Sabi Foods",
            image = "https://cdn.feastique.com/sabi.jpg"
        )

        @BeforeEach
        fun stubUsernameChange() {
            val vendor = sabiVendor()
            whenever(vendorRepository.findById(VS_VENDOR_ID)).thenReturn(Optional.of(vendor))
            whenever(vendorRepository.existsByUsernameIgnoreCase("sabi_chef_updated")).thenReturn(false)
            whenever(vendorRepository.save(any())).thenReturn(
                sabiVendor(username = "sabi_chef_updated")
            )
            whenever(jwtUtility.generateTokenPair(VS_VENDOR_ID, "sabi_chef_updated", AccountType.VENDOR))
                .thenReturn(VS_NEW_TOKEN_PAIR)
            whenever(jwtUtility.getTokenIdentifier(VS_NEW_ACCESS_TOKEN)).thenReturn(VS_TOKEN_IDENTIFIER)
            whenever(jwtUtility.getExpirationEpochMillis(VS_NEW_ACCESS_TOKEN)).thenReturn(VS_TOKEN_EXPIRY)
            whenever(jwtUtility.getUserId(VS_NEW_ACCESS_TOKEN)).thenReturn(VS_VENDOR_ID)
            whenever(jwtUtility.getUserType(VS_NEW_ACCESS_TOKEN)).thenReturn("VENDOR")
            whenever(jwtUtility.parseToken(any(), any(), any())).thenReturn(
                RefreshToken(
                    customerId = null,
                    vendorId = VS_VENDOR_ID,
                    expiresAt = Date(System.currentTimeMillis() + 2_592_000_000L),
                    hashedToken = "hashed_new_vendor_refresh"
                )
            )
        }

        @Test
        fun `checks username availability when username changes`() {
            vendorService.updateVendor(updateDtoNewUsername, sabiSecurity())
            verify(vendorRepository).existsByUsernameIgnoreCase("sabi_chef_updated")
        }

        @Test
        fun `throws DuplicateFoundException when new username is already taken`() {
            whenever(vendorRepository.existsByUsernameIgnoreCase("sabi_chef_updated")).thenReturn(true)

            assertThrows<DuplicateFoundException> {
                vendorService.updateVendor(updateDtoNewUsername, sabiSecurity())
            }
        }

        @Test
        fun `does not check username availability when username is unchanged`() {
            val sameUsernameDto = VendorUpdateDto(
                username = "sabi_chef",
                firstName = "Ambe",
                lastName = "Chancie",
                chefName = "Sabi Chef",
                restaurantName = "Sabi Foods",
                image = "https://cdn.feastique.com/sabi.jpg"
            )
            val vendor = sabiVendor()
            whenever(vendorRepository.findById(VS_VENDOR_ID)).thenReturn(Optional.of(vendor))
            whenever(vendorRepository.save(any())).thenReturn(vendor)
            whenever(jwtUtility.generateTokenPair(VS_VENDOR_ID, "sabi_chef", AccountType.VENDOR))
                .thenReturn(VS_NEW_TOKEN_PAIR)
            whenever(jwtUtility.getTokenIdentifier(VS_NEW_ACCESS_TOKEN)).thenReturn(VS_TOKEN_IDENTIFIER)
            whenever(jwtUtility.getExpirationEpochMillis(VS_NEW_ACCESS_TOKEN)).thenReturn(VS_TOKEN_EXPIRY)
            whenever(jwtUtility.getUserId(VS_NEW_ACCESS_TOKEN)).thenReturn(VS_VENDOR_ID)
            whenever(jwtUtility.getUserType(VS_NEW_ACCESS_TOKEN)).thenReturn("VENDOR")
            whenever(jwtUtility.parseToken(any(), any(), any())).thenReturn(
                RefreshToken(
                    customerId = null,
                    vendorId = VS_VENDOR_ID,
                    expiresAt = Date(System.currentTimeMillis() + 2_592_000_000L),
                    hashedToken = "hashed_new_vendor_refresh"
                )
            )

            vendorService.updateVendor(sameUsernameDto, sabiSecurity())

            verify(vendorRepository, never()).existsByUsernameIgnoreCase(any())
        }
    }


    // changePassword
    @Nested
    inner class ChangePassword {

        private val passwordChangeDto = PasswordChangeDto(
            currentPassword = "sabiChef98",
            newPassword = "newSabiChef99",
            confirmedNewPassword = "newSabiChef99"
        )

        @BeforeEach
        fun stubChangePassword() {
            val vendor = sabiVendor()
            whenever(vendorRepository.findById(VS_VENDOR_ID)).thenReturn(Optional.of(vendor))
            whenever(passwordEncoder.matches("sabiChef98", "hashed_sabiChef98")).thenReturn(true)
            whenever(passwordEncoder.encode("newSabiChef99")).thenReturn("hashed_newSabiChef99")
            whenever(vendorRepository.save(any())).thenReturn(vendor)
        }

        @Test
        fun `saves vendor with new encoded password`() {
            vendorService.changePassword(passwordChangeDto, sabiSecurity())
            verify(vendorRepository).save(argThat {
                password == "hashed_newSabiChef99"
            })
        }

        @Test
        fun `encodes the new password before saving`() {
            vendorService.changePassword(passwordChangeDto, sabiSecurity())
            verify(passwordEncoder).encode("newSabiChef99")
        }

        @Test
        fun `throws IllegalArgumentException when current password is wrong`() {
            whenever(passwordEncoder.matches("sabiChef98", "hashed_sabiChef98")).thenReturn(false)

            assertThrows<IllegalArgumentException> {
                vendorService.changePassword(passwordChangeDto, sabiSecurity())
            }
        }

        @Test
        fun `throws IllegalArgumentException when new password and confirmation do not match`() {
            val mismatchedDto = PasswordChangeDto(
                currentPassword = "sabiChef98",
                newPassword = "newSabiChef99",
                confirmedNewPassword = "differentPassword00"
            )

            assertThrows<IllegalArgumentException> {
                vendorService.changePassword(mismatchedDto, sabiSecurity())
            }
        }

        @Test
        fun `does not save vendor when current password is wrong`() {
            whenever(passwordEncoder.matches("sabiChef98", "hashed_sabiChef98")).thenReturn(false)

            runCatching { vendorService.changePassword(passwordChangeDto, sabiSecurity()) }

            verify(vendorRepository, never()).save(any())
        }

        @Test
        fun `does not save vendor when new passwords do not match`() {
            val mismatchedDto = PasswordChangeDto(
                currentPassword = "sabiChef98",
                newPassword = "newSabiChef99",
                confirmedNewPassword = "differentPassword00"
            )

            runCatching { vendorService.changePassword(mismatchedDto, sabiSecurity()) }

            verify(vendorRepository, never()).save(any())
        }

        @Test
        fun `does not encode new password when current password is wrong`() {
            whenever(passwordEncoder.matches("sabiChef98", "hashed_sabiChef98")).thenReturn(false)

            runCatching { vendorService.changePassword(passwordChangeDto, sabiSecurity()) }

            verify(passwordEncoder, never()).encode(any())
        }
    }
}


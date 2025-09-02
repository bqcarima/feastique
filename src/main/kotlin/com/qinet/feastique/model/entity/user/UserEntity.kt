package com.qinet.feastique.model.entity.user

import com.fasterxml.jackson.annotation.JsonFormat
import com.qinet.feastique.model.enums.AccountType
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import java.util.*

@MappedSuperclass
abstract class UserEntity {
    @Id
    @GeneratedValue
    var id: Long? = null

    @NotBlank(message = "Username cannot be null.")
    @NotEmpty(message = "Username cannot be empty.")
    var username: String = UUID.randomUUID().toString()

    @Column(name = "first_name")
    @NotBlank(message = "First name cannot be null.")
    @NotEmpty(message = "First name cannot be empty.")
    var firstName: String? = ""

    @Column(name = "last_name")
    @NotBlank(message = "Last name cannot be null.")
    @NotEmpty(message = "Last name cannot be empty.")
    var lastName: String? = ""

    @NotBlank(message = "Password cannot be null.")
    @NotEmpty(message = "Password cannot be empty.")
    var password: String? = ""

    var image: String? = ""

    @Column(name = "registration_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH-mm-ss-dd-MM-yyyy")
    @CreationTimestamp
    var registrationDate: Date? = null

    @Column(name = "account_type")
    @Enumerated(EnumType.STRING)
    var accountType: AccountType? = null

    var verified: Boolean? = false

    @Column(name = "account_updated_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH-mm-ss-dd-MM-yyyy")
    @UpdateTimestamp
    var accountUpdated: LocalDateTime? = null
}


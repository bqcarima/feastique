package com.qinet.feastique.model.entity.phoneNumber

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

@MappedSuperclass
abstract class PhoneNumber {
    @Id
    @GeneratedValue
    var id: Long? = null

    @Column(name = "phone_number")
    @NotBlank(message = "Phone number cannot be null.")
    @NotEmpty(message = "Phone number cannot be empty.")
    var phoneNumber: String? = ""

    @Column(name = "is_default")
    var default: Boolean? = false

}
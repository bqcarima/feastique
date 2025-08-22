package com.qinet.feastique.model.entity.phoneNumber

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

@Entity
@Table(name = "phone_number")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
class PhoneNumber {
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
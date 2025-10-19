package com.qinet.feastique.model.entity.phoneNumber

import com.github.f4b6a3.uuid.UuidCreator
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import java.util.UUID

@MappedSuperclass
abstract class PhoneNumber {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    var id: UUID = UuidCreator.getTimeOrdered()

    @Column(name = "phone_number")
    @NotBlank(message = "Phone number cannot be null.")
    @NotEmpty(message = "Phone number cannot be empty.")
    var phoneNumber: String? = ""

    @Column(name = "is_default")
    var default: Boolean? = false

}
package com.qinet.feastique.model.entity.complement

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.f4b6a3.uuid.UuidCreator
import com.qinet.feastique.model.entity.user.Vendor
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import java.util.*

@Entity
@Table(name = "complements")
class Complement {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    var id: UUID = UuidCreator.getTimeOrdered()

    @Column(name = "complement_name")
    @NotBlank(message = "Complement name cannot be null.")
    @NotEmpty(message = "Complement cannot be empty.")
    var complementName: String? = ""

    var price: Long? = 0

    @JsonBackReference // prevent infinite recursion for extra protection
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    @JsonIgnore
    lateinit var vendor: Vendor
}


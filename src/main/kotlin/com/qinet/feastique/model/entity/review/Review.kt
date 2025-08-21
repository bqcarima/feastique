package com.qinet.feastique.model.entity.review

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.qinet.feastique.model.entity.Customer
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.util.*

/*
@Entity
@Table(name = "review")
class Review {

    @Id
    @GeneratedValue
    var id: Long? = null

    @NotBlank(message = "Review must not be blank")
    @NotEmpty(message = "Review must not be empty")
    var review: String? = ""

    @NotBlank(message = "Review must not be blank")
    @NotEmpty(message = "Review must not be empty")
    var rating: Float? = 0.0F

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    var createdAt: Date? = null

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    var updatedAt: Date? = null

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnore
    lateinit var customer: Customer
}
*/


package com.qinet.feastique.model.entity.post

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.qinet.feastique.model.entity.Customer
import jakarta.persistence.*

@Entity
@Table(name = "post_like")
class PostLike {

    @Id
    @GeneratedValue
    var id: Long? = null

    @JsonBackReference // prevent infinite recursion for extra protection
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    @JsonIgnore
    lateinit var post: Post

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnore
    lateinit var customer: Customer
}
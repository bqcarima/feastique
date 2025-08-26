package com.qinet.feastique.model.entity.post

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonManagedReference
import com.qinet.feastique.model.entity.user.Vendor
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.Formula
import java.util.Date

@Entity
@Table(name = "post")
class Post {

    @Id
    @GeneratedValue
    var id: Long? = null

    @NotBlank(message = "Title cannot be blank")
    @NotEmpty(message = "Title cannot be blank")
    var title: String? = null

    var body: String? = null

    @NotBlank(message = "Image cannot be blank")
    @NotEmpty(message = "Image cannot be blank")
    var image: String? = null

    @Column(name = "created_at")
    @CreationTimestamp
    var createdAt: Date? = null

    @Column(name = "updated_at")
    var updatedAt: Date? = null

    @JsonBackReference // prevent infinite recursion for extra protection
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    @JsonIgnore
    lateinit var vendor: Vendor

    // Fetch number of likeCount directly when retrieving post without initialising likes
    @Formula("(SELECT COUNT pl.id) FROM post_like WHERE pl.post.id = :id")
    var likeCount: Long = 0

    @JsonManagedReference
    @OneToMany(
        mappedBy = "post",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var likes: MutableList<PostLike> = mutableListOf()
}


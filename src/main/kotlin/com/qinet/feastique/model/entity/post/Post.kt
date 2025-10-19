package com.qinet.feastique.model.entity.post

import com.fasterxml.jackson.annotation.JsonManagedReference
import com.github.f4b6a3.uuid.UuidCreator
import com.qinet.feastique.model.entity.user.Vendor
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.Formula
import java.util.*

@Entity
@Table(name = "posts")
class Post {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    var id: UUID = UuidCreator.getTimeOrdered()

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

    @ManyToOne
    @JoinColumn(name = "vendor_id", nullable = false)
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


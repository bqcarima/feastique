package com.qinet.feastique.model.entity.post

import com.fasterxml.jackson.annotation.JsonBackReference
import com.github.f4b6a3.uuid.UuidCreator
import com.qinet.feastique.model.entity.image.PostImage
import com.qinet.feastique.model.entity.user.Vendor
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.Formula
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "posts")
class Post {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    var id: UUID = UuidCreator.getTimeOrdered()

    @NotBlank(message = "Title cannot be blank")
    var title: String? = null

    var body: String? = null

    @JsonBackReference
    @OneToMany(
        mappedBy = "post",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    var postImages: MutableSet<PostImage> = mutableSetOf()

    @Column(name = "created_at")
    @CreationTimestamp
    var createdAt: LocalDateTime? = null

    @Column(name = "updated_at")
    @UpdateTimestamp
    var updatedAt: LocalDateTime? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    lateinit var vendor: Vendor

    @Formula("(SELECT COUNT(pl.id) FROM post_likes pl WHERE pl.post_id = id)")
    var likeCount: Long = 0
}


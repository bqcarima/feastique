package com.qinet.feastique.repository.post

import com.qinet.feastique.model.entity.post.Post
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface PostRepository : JpaRepository<Post, UUID> {
    fun findAllByVendorId(vendorId: UUID): List<Post>

}
package com.qinet.feastique.repository.post

import com.qinet.feastique.model.entity.post.Post
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PostRepository : JpaRepository<Post, Long> {
    fun findAllByVendorId(vendorId: Long): List<Post>

}
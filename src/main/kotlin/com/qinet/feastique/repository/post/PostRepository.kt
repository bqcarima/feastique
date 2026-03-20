package com.qinet.feastique.repository.post

import com.qinet.feastique.model.entity.post.Post
import org.springframework.data.domain.Limit
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.ScrollPosition
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Window
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface PostRepository : JpaRepository<Post, UUID> {
    fun findAllByVendorId(vendorId: UUID): List<Post>

    fun findByIdAndVendorId(id: UUID, vendorId: UUID): Post?
    fun findAllByVendorId(vendorId: UUID, pageable: Pageable): Page<Post>
    fun findAllByVendorId(vendorId: UUID, scrollPosition: ScrollPosition, sort: Sort, limit: Limit): Window<Post>

}


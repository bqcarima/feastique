package com.qinet.feastique.service

import com.qinet.feastique.exception.PermissionDeniedException
import com.qinet.feastique.exception.RequestedEntityNotFoundException
import com.qinet.feastique.exception.UserNotFoundException
import com.qinet.feastique.model.dto.PostDto
import com.qinet.feastique.model.entity.post.Post
import com.qinet.feastique.repository.post.PostRepository
import com.qinet.feastique.repository.vendor.VendorRepository
import com.qinet.feastique.security.UserSecurity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import kotlin.collections.any

@Service
class PostService (
    private val postRepository: PostRepository,
    private val vendorRepository: VendorRepository,
) {

    @Transactional(readOnly = true)
    fun getPostById(id: UUID, vendorDetails: UserSecurity): Post {
        val post = postRepository.findById(id)
            .orElseThrow { RequestedEntityNotFoundException("No posts found with id $id") }
            .also {
                if (it.vendor.id != vendorDetails.id) {
                    throw PermissionDeniedException("You do not have permission to access this post.")
                }
            }
        return post
    }

    @Transactional(readOnly = true)
    fun getAllPosts(vendorDetails: UserSecurity): List<Post> {
        val posts = postRepository.findAllByVendorId(vendorDetails.id)
            .takeIf { it.isNotEmpty() }
            ?: throw RequestedEntityNotFoundException("No post found for the vendor: ${vendorDetails.id}")

        posts.also { list ->
            if (list.any { it.vendor.id != vendorDetails.id}) {
                throw PermissionDeniedException("You do not have the permission to access these foods.")
            }
        }
        return posts
    }

    @Transactional
    fun deletePost(id: UUID, vendorDetails: UserSecurity) {
        val post = getPostById(id, vendorDetails)
        postRepository.delete(post)
    }

    @Transactional
    fun savePost(post: Post): Post {
        return postRepository.save(post)
    }

    @Transactional
    fun addOrUpdatePost(postDto: PostDto, vendorDetails: UserSecurity): Post {
        val vendor = vendorRepository.findById(vendorDetails.id)
            .orElseThrow { UserNotFoundException("Vendor not found.") }

        var post: Post = if (postDto.id != null) {
            getPostById(postDto.id, vendorDetails)
        } else {
            Post().apply {
                this.vendor = vendor
            }
        }

        post.title = requireNotNull(postDto.title) { "Please enter a title." }
        post.body = postDto.body
        post.image = requireNotNull(postDto.image) { "Please select at least one image." }
        post = savePost(post)
        vendorRepository.save(vendor)

        return post
    }
}


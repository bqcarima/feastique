package com.qinet.feastique.service.post

import com.qinet.feastique.common.mapper.toResponse
import com.qinet.feastique.exception.RequestedEntityNotFoundException
import com.qinet.feastique.exception.UserNotFoundException
import com.qinet.feastique.model.dto.PostDto
import com.qinet.feastique.model.entity.image.PostImage
import com.qinet.feastique.model.entity.post.Post
import com.qinet.feastique.model.enums.Constants
import com.qinet.feastique.repository.like.PostLikeRepository
import com.qinet.feastique.repository.post.PostRepository
import com.qinet.feastique.repository.user.VendorRepository
import com.qinet.feastique.response.pagination.WindowResponse
import com.qinet.feastique.response.post.PostResponse
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.utility.CursorEncoder
import com.qinet.feastique.utility.SecurityUtility
import org.slf4j.LoggerFactory
import org.springframework.data.domain.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import kotlin.jvm.optionals.getOrElse

@Service
class PostService (
    private val postRepository: PostRepository,
    private val postLikeRepository: PostLikeRepository,
    private val vendorRepository: VendorRepository,
    private val cursorEncoder: CursorEncoder,
    private val securityUtility: SecurityUtility
) {

    @Suppress("unused")
    private val logger = LoggerFactory.getLogger(PostService::class.java)
    @Transactional(readOnly = true)
    fun getPost(id: UUID, userDetails: UserSecurity): PostResponse {
        val role = securityUtility.getSingleRole(userDetails)
        var liked = false

        val post = when (role) {
            "CUSTOMER" -> {
                liked = postLikeRepository.existsByPostIdAndCustomerId(id, userDetails.id)
                postRepository.findById(id)
                    .getOrElse { throw RequestedEntityNotFoundException() }
            }
            "VENDOR" -> postRepository.findByIdAndVendorId(id, userDetails.id)
                ?: throw RequestedEntityNotFoundException()

            else -> throw IllegalArgumentException("Unsupported role: $role")
        }

        return post.toResponse(liked)
    }

    @Transactional(readOnly = true)
    fun getPostById(id: UUID, vendorDetails: UserSecurity): Post {
        return postRepository.findByIdAndVendorId(id, vendorDetails.id)
            ?: throw RequestedEntityNotFoundException()
    }

    @Transactional(readOnly = true)
    fun getAllPosts(vendorDetails: UserSecurity, page: Int, size: Int): Page<PostResponse> {
        val pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())
        return postRepository.findAllByVendorId(vendorDetails.id, pageable).map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun scrollPosts(
        vendorId: UUID,
        cursor: String?,
        size: Int = Constants.DEFAULT_PAGE_SIZE.type,
        userDetails: UserSecurity
    ): WindowResponse<PostResponse> {
        val currentOffset: Long = cursor?.toLongOrNull() ?: 0L
        val scrollPosition = if (currentOffset == 0L) ScrollPosition.offset() else ScrollPosition.offset(currentOffset)
        val sort = Sort.by("createdAt").descending()

        val window = postRepository.findAllByVendorId(vendorId, scrollPosition, sort, Limit.of(size))

        val likedPostIds: Set<UUID> = if (securityUtility.getSingleRole(userDetails) == "CUSTOMER") {
            val postIds = window.toList().map { it.id }
            postLikeRepository.findAllByCustomerIdAndPostIdIn(userDetails.id, postIds)
                .map { it.post.id }
                .toHashSet()
        } else {
            emptySet()
        }

        return window.map { it.toResponse(it.id in likedPostIds) }
            .toResponse(currentOffset) { cursorEncoder.encodeOffset(it) }
    }

    @Transactional
    fun deletePost(id: UUID, vendorDetails: UserSecurity) {
        postRepository.delete(getPostById(id, vendorDetails))
    }

    @Transactional
    fun savePost(post: Post): Post {
        return postRepository.saveAndFlush(post)
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
        post.postImages = preparePostImages(postDto, post)
        post = savePost(post)

        return post
    }

    private fun preparePostImages(postDto: PostDto, post: Post): MutableSet<PostImage> {
        if (postDto.postImages.isEmpty() ) {
            throw IllegalArgumentException("Please add at least 1 image for the post")
        }

        val existingPostImages = post.postImages.associateBy { it.id }

        val updatedImages = postDto.postImages.map { dto ->
            val image = existingPostImages[dto.id] ?: PostImage().apply { this.post = post }
            image.imageUrl = dto.imageUrl
            image
        }

        post.postImages.removeIf { existing -> updatedImages.none { it.id == existing.id } }
        updatedImages.forEach { updated ->
            if (post.postImages.none { it.id == updated.id }) post.postImages.add(updated)
        }

        return post.postImages
    }
}


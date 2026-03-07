package com.qinet.feastique.controller

import com.qinet.feastique.common.mapper.toResponse
import com.qinet.feastique.model.dto.PostDto
import com.qinet.feastique.response.PostResponse
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.service.PostService
import com.qinet.feastique.utility.SecurityUtility
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/vendors/{vendorId}/posts")
class PostController(
    private val postService: PostService,
    private val securityUtility: SecurityUtility
) {

    @PutMapping
    fun addOrUpdatePost(
        @PathVariable("vendorId") vendorId: UUID,
        @RequestBody @Valid postDto: PostDto,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ) : ResponseEntity<PostResponse> {
        securityUtility.validatePath(vendorId, vendorDetails)
        val post = postService.addOrUpdatePost(postDto, vendorDetails)
        return ResponseEntity(post.toResponse(), HttpStatus.CREATED)
    }

    @DeleteMapping("/{id}")
    fun deletePost(
        @PathVariable("id") id: UUID,
        @PathVariable("vendorId") vendorId: UUID,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ) : ResponseEntity<String> {
        securityUtility.validatePath(vendorId, vendorDetails)
        postService.deletePost(id, vendorDetails)
        return ResponseEntity("Post deleted successfully.",HttpStatus.NO_CONTENT)
    }

    @GetMapping("/{id}")
    fun getPost(
        @PathVariable("id") id: UUID,
        @PathVariable("vendorId") vendorId: UUID,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ) : ResponseEntity<PostResponse> {
        securityUtility.validatePath(vendorId, vendorDetails)
        val post = postService.getPostById(id, vendorDetails)
        return ResponseEntity(post.toResponse(), HttpStatus.OK)
    }

    @GetMapping
    fun getAllPosts(
        @PathVariable("vendorId") vendorId: UUID,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ) : ResponseEntity<List<PostResponse>> {
        securityUtility.validatePath(vendorId, vendorDetails)
        val post = postService.getAllPosts(vendorDetails)
        return ResponseEntity(post.map { it.toResponse() }, HttpStatus.OK)
    }
}
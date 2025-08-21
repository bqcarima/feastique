package com.qinet.feastique.controller

import com.qinet.feastique.common.mapper.toResponse
import com.qinet.feastique.model.dto.PostDto
import com.qinet.feastique.response.PostResponse
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.service.PostService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/vendors/{vendorId}/posts")
class PostController(
    private val postService: PostService
) {

    @PutMapping
    fun addOrUpdatePost(
        @PathVariable("vendorId") vendorId: Long,
        @RequestBody @Valid postDto: PostDto,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ) : ResponseEntity<PostResponse> {
        val post = postService.addOrUpdatePost(postDto, vendorDetails)
        return ResponseEntity(post.toResponse(), HttpStatus.CREATED)
    }

    @DeleteMapping("/{id}")
    fun deletePost(
        @PathVariable("id") id: Long,
        @PathVariable("vendorId") vendorId: Long,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ) : ResponseEntity<String> {
        postService.deletePost(id, vendorDetails)
        return ResponseEntity("Post deleted successfully.",HttpStatus.NO_CONTENT)
    }

    @GetMapping("/{id}")
    fun getPost(
        @PathVariable("id") id: Long,
        @PathVariable("vendorId") vendorId: Long,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ) : ResponseEntity<PostResponse> {
        val post = postService.getPostById(id, vendorDetails)
        return ResponseEntity(post.toResponse(), HttpStatus.OK)
    }

    @GetMapping
    fun getAllPosts(
        @PathVariable("vendorId") vendorId: Long,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ) : ResponseEntity<List<PostResponse>> {
        val post = postService.getAllPosts(vendorDetails)
        return ResponseEntity(post.map { it.toResponse() }, HttpStatus.OK)
    }
}
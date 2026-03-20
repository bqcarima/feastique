package com.qinet.feastique.controller.post

import com.qinet.feastique.common.mapper.toResponse
import com.qinet.feastique.model.dto.PostDto
import com.qinet.feastique.response.pagination.PageResponse
import com.qinet.feastique.response.pagination.WindowResponse
import com.qinet.feastique.response.post.PostResponse
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.service.post.PostService
import com.qinet.feastique.utility.SecurityUtility
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1")
class PostController(
    private val postService: PostService,
    private val securityUtility: SecurityUtility
) {

    @PutMapping("/vendors/{vendorId}/posts")
    fun addOrUpdatePost(
        @PathVariable vendorId: UUID,
        @RequestBody @Valid postDto: PostDto,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ) : ResponseEntity<PostResponse> {
        securityUtility.validatePath(vendorId, vendorDetails)
        val post = postService.addOrUpdatePost(postDto, vendorDetails)
        return ResponseEntity(post.toResponse(), HttpStatus.CREATED)
    }

    @DeleteMapping("/vendors/{vendorId}/posts/delete/{id}")
    fun deletePost(
        @PathVariable id: UUID,
        @PathVariable vendorId: UUID,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ) : ResponseEntity<String> {
        securityUtility.validatePath(vendorId, vendorDetails)
        postService.deletePost(id, vendorDetails)
        return ResponseEntity("Post deleted successfully.", HttpStatus.OK)
    }

    @GetMapping(
        path = [
            "/customers/{customerId}/vendors/{vendorId}/posts/{id}",
            "/vendors/{vendorId}/posts/{id}"
        ]
    )
    fun getPost(
        @PathVariable id: UUID,
        @PathVariable customerId: UUID?,
        @PathVariable vendorId: UUID,
        @AuthenticationPrincipal userDetails: UserSecurity

    ) : ResponseEntity<PostResponse> {
        val pathId = customerId ?: vendorId
        securityUtility.validatePath(pathId, userDetails)
        val post = postService.getPost(id, userDetails)
        return ResponseEntity(post, HttpStatus.OK)
    }

    @GetMapping("/vendors/{vendorId}/posts")
    fun getAllPosts(
        @PathVariable vendorId: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ) : ResponseEntity<PageResponse<PostResponse>> {
        securityUtility.validatePath(vendorId, vendorDetails)
        val page = postService.getAllPosts(vendorDetails, page, size)
        return ResponseEntity(page.toResponse(), HttpStatus.OK)
    }

    @GetMapping(
        path = [
            "/customers/{customerId}/vendors/{vendorId}/posts/scroll",
            "/vendors/{vendorId}/posts/scroll"
        ]
    )
    fun scrollPosts(
        @PathVariable(required = false) customerId: UUID?,
        @PathVariable vendorId: UUID,
        @RequestParam(required = false) cursor: String?,
        @RequestParam(defaultValue = "10") size: Int,
        @AuthenticationPrincipal userDetails: UserSecurity

    ) : ResponseEntity<WindowResponse<PostResponse>> {
        val pathId = customerId ?: vendorId
        securityUtility.validatePath(pathId, userDetails)
        val window = postService.scrollPosts(vendorId, cursor, size, userDetails)
        return ResponseEntity(window, HttpStatus.OK)
    }
}


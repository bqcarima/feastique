package com.qinet.feastique.service.post

import com.qinet.feastique.exception.RequestedEntityNotFoundException
import com.qinet.feastique.exception.UserNotFoundException
import com.qinet.feastique.model.dto.ImageDto
import com.qinet.feastique.model.dto.PostDto
import com.qinet.feastique.model.entity.image.PostImage
import com.qinet.feastique.model.entity.post.Post
import com.qinet.feastique.model.entity.user.Vendor
import com.qinet.feastique.model.enums.AccountType
import com.qinet.feastique.model.enums.Region
import com.qinet.feastique.repository.like.PostLikeRepository
import com.qinet.feastique.repository.post.PostRepository
import com.qinet.feastique.repository.user.VendorRepository
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.utility.CursorEncoder
import com.qinet.feastique.utility.SecurityUtility
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import org.springframework.data.domain.*
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.util.*

// Shared fixtures

private val VENDOR_ID = UUID.randomUUID()
private val CUSTOMER_ID = UUID.randomUUID()
private val POST_ID_1 = UUID.randomUUID()

private fun sabiVendor(): Vendor = Vendor().apply {
    id = VENDOR_ID
    username = "sabi_chef"
    firstName = "Ambe"
    lastName = "Chancie"
    chefName = "Sabi Chef"
    restaurantName = "Sabi Foods"
    accountType = AccountType.VENDOR
    password = "hashed_sabiChef98"
    region = Region.CENTRE
    vendorCode = "CM020001"
}

private fun vendorSecurity(): UserSecurity = UserSecurity(
    id = VENDOR_ID,
    username = "sabi_chef",
    password = "hashed_sabiChef98",
    userAuthorities = mutableListOf(SimpleGrantedAuthority("ROLE_VENDOR"))
)

private fun customerSecurity(): UserSecurity = UserSecurity(
    id = CUSTOMER_ID,
    username = "jane_doe",
    password = "hashed_passWord123",
    userAuthorities = mutableListOf(SimpleGrantedAuthority("ROLE_CUSTOMER"))
)

private fun postImage(post: Post, url: String = "https://cdn.feastique.com/img1.png"): PostImage =
    PostImage().apply {
        this.post = post
        imageUrl = url
    }

private fun singlePost(id: UUID = POST_ID_1, vendor: Vendor = sabiVendor()): Post = Post().apply {
    this.id = id
    this.vendor = vendor
    title = "Chef Special"
    body = "Today's special is jollof rice"
    postImages = mutableSetOf()
}

private fun postDtoCreate(
    title: String = "Chef Special",
    body: String? = "Today's special",
    images: Set<ImageDto> = setOf(ImageDto(id = null, imageUrl = "https://cdn.feastique.com/img1.png"))
): PostDto = PostDto(
    id = null,
    title = title,
    body = body,
    postImages = images
)

private fun postDtoUpdate(
    id: UUID,
    title: String = "Updated Special",
    body: String? = "Updated body",
    images: Set<ImageDto> = setOf(ImageDto(id = null, imageUrl = "https://cdn.feastique.com/img2.png"))
): PostDto = PostDto(
    id = id,
    title = title,
    body = body,
    postImages = images
)


class PostServiceTest {

    private lateinit var postRepository: PostRepository
    private lateinit var postLikeRepository: PostLikeRepository
    private lateinit var vendorRepository: VendorRepository
    private lateinit var cursorEncoder: CursorEncoder
    private lateinit var securityUtility: SecurityUtility
    private lateinit var postService: PostService

    @BeforeEach
    fun setUp() {
        postRepository = mock()
        postLikeRepository = mock()
        vendorRepository = mock()
        cursorEncoder = mock()
        securityUtility = mock()

        postService = PostService(
            postRepository = postRepository,
            postLikeRepository = postLikeRepository,
            vendorRepository = vendorRepository,
            cursorEncoder = cursorEncoder,
            securityUtility = securityUtility
        )
    }

    // getPost
    @Nested
    inner class GetPost {

        @Test
        fun `vendor gets own post by id`() {
            val post = singlePost()
            whenever(securityUtility.getSingleRole(any())).thenReturn("VENDOR")
            whenever(postRepository.findByIdAndVendorId(POST_ID_1, VENDOR_ID)).thenReturn(post)

            val result = postService.getPost(POST_ID_1, vendorSecurity())

            assertEquals("Chef Special", result.title)
        }

        @Test
        fun `customer gets a post by id and liked flag is true when customer liked it`() {
            val post = singlePost()
            whenever(securityUtility.getSingleRole(any())).thenReturn("CUSTOMER")
            whenever(postLikeRepository.existsByPostIdAndCustomerId(POST_ID_1, CUSTOMER_ID)).thenReturn(true)
            whenever(postRepository.findById(POST_ID_1)).thenReturn(Optional.of(post))

            val result = postService.getPost(POST_ID_1, customerSecurity())

            assertTrue(result.likedByCurrentUser)
        }

        @Test
        fun `customer gets a post and liked flag is false when customer has not liked it`() {
            val post = singlePost()
            whenever(securityUtility.getSingleRole(any())).thenReturn("CUSTOMER")
            whenever(postLikeRepository.existsByPostIdAndCustomerId(POST_ID_1, CUSTOMER_ID)).thenReturn(false)
            whenever(postRepository.findById(POST_ID_1)).thenReturn(Optional.of(post))

            val result = postService.getPost(POST_ID_1, customerSecurity())

            assertFalse(result.likedByCurrentUser)
        }

        @Test
        fun `vendor gets post and like check is not performed`() {
            val post = singlePost()
            whenever(securityUtility.getSingleRole(any())).thenReturn("VENDOR")
            whenever(postRepository.findByIdAndVendorId(POST_ID_1, VENDOR_ID)).thenReturn(post)

            postService.getPost(POST_ID_1, vendorSecurity())

            verify(postLikeRepository, never()).existsByPostIdAndCustomerId(any(), any())
        }

        @Test
        fun `vendor throws RequestedEntityNotFoundException when post does not belong to them`() {
            whenever(securityUtility.getSingleRole(any())).thenReturn("VENDOR")
            whenever(postRepository.findByIdAndVendorId(POST_ID_1, VENDOR_ID)).thenReturn(null)

            assertThrows<RequestedEntityNotFoundException> {
                postService.getPost(POST_ID_1, vendorSecurity())
            }
        }

        @Test
        fun `customer throws RequestedEntityNotFoundException when post does not exist`() {
            whenever(securityUtility.getSingleRole(any())).thenReturn("CUSTOMER")
            whenever(postLikeRepository.existsByPostIdAndCustomerId(POST_ID_1, CUSTOMER_ID)).thenReturn(false)
            whenever(postRepository.findById(POST_ID_1)).thenReturn(Optional.empty())

            assertThrows<RequestedEntityNotFoundException> {
                postService.getPost(POST_ID_1, customerSecurity())
            }
        }

        @Test
        fun `throws IllegalArgumentException for unsupported role`() {
            val unknownSecurity = UserSecurity(
                id = VENDOR_ID,
                username = "admin",
                password = "pw",
                userAuthorities = mutableListOf(SimpleGrantedAuthority("ROLE_ADMIN"))
            )
            whenever(securityUtility.getSingleRole(unknownSecurity)).thenReturn("ADMIN")

            assertThrows<IllegalArgumentException> {
                postService.getPost(POST_ID_1, unknownSecurity)
            }
        }
    }

    // getPostById
    @Nested
    inner class GetPostById {

        @Test
        fun `returns post when it belongs to the vendor`() {
            val post = singlePost()
            whenever(postRepository.findByIdAndVendorId(POST_ID_1, VENDOR_ID)).thenReturn(post)

            val result = postService.getPostById(POST_ID_1, vendorSecurity())

            assertEquals(POST_ID_1, result.id)
        }

        @Test
        fun `throws RequestedEntityNotFoundException when post is not found`() {
            whenever(postRepository.findByIdAndVendorId(POST_ID_1, VENDOR_ID)).thenReturn(null)

            assertThrows<RequestedEntityNotFoundException> {
                postService.getPostById(POST_ID_1, vendorSecurity())
            }
        }

        @Test
        fun `throws RequestedEntityNotFoundException when post belongs to a different vendor`() {
            whenever(postRepository.findByIdAndVendorId(POST_ID_1, VENDOR_ID)).thenReturn(null)

            assertThrows<RequestedEntityNotFoundException> {
                postService.getPostById(POST_ID_1, vendorSecurity())
            }
        }
    }

    // getAllPosts
    @Nested
    inner class GetAllPosts {

        @Test
        fun `returns page of posts for the vendor`() {
            val post = singlePost()
            val page = PageImpl(listOf(post))
            whenever(postRepository.findAllByVendorId(eq(VENDOR_ID), any<Pageable>())).thenReturn(page)

            val result = postService.getAllPosts(vendorSecurity(), 0, 10)

            assertEquals(1, result.totalElements)
        }

        @Test
        fun `returns empty page when vendor has no posts`() {
            whenever(postRepository.findAllByVendorId(eq(VENDOR_ID), any<Pageable>())).thenReturn(PageImpl(emptyList()))

            val result = postService.getAllPosts(vendorSecurity(), 0, 10)

            assertEquals(0, result.totalElements)
        }

        @Test
        fun `uses descending sort by createdAt`() {
            whenever(postRepository.findAllByVendorId(eq(VENDOR_ID), any<Pageable>())).thenReturn(PageImpl(emptyList()))

            postService.getAllPosts(vendorSecurity(), 0, 10)

            verify(postRepository).findAllByVendorId(eq(VENDOR_ID), argThat<Pageable> {
                sort.getOrderFor("createdAt")?.isDescending == true
            })
        }

        @Test
        fun `maps posts to response correctly`() {
            val post = singlePost().also {
                it.postImages = mutableSetOf(postImage(it))
            }
            whenever(postRepository.findAllByVendorId(eq(VENDOR_ID), any<Pageable>())).thenReturn(PageImpl(listOf(post)))

            val result = postService.getAllPosts(vendorSecurity(), 0, 10)

            assertEquals("Chef Special", result.content.first().title)
        }

        @Test
        fun `passes correct page number and size to repository`() {
            whenever(postRepository.findAllByVendorId(eq(VENDOR_ID), any<Pageable>())).thenReturn(PageImpl(emptyList()))

            postService.getAllPosts(vendorSecurity(), 2, 5)

            verify(postRepository).findAllByVendorId(eq(VENDOR_ID), argThat<Pageable> {
                pageNumber == 2 && pageSize == 5
            })
        }
    }

    // scrollPosts
    @Nested
    inner class ScrollPosts {

        private fun buildWindow(posts: List<Post>): Window<Post> {
            val positions = posts.map { ScrollPosition.offset() }
            return Window.from(posts, { ScrollPosition.offset() }, false)
        }

        @Test
        fun `returns window response for vendor role without checking likes`() {
            val post = singlePost()
            val window = buildWindow(listOf(post))
            whenever(securityUtility.getSingleRole(vendorSecurity())).thenReturn("VENDOR")
            whenever(postRepository.findAllByVendorId(eq(VENDOR_ID), any<ScrollPosition>(), any<Sort>(), any<Limit>()))
                .thenReturn(window)
            whenever(cursorEncoder.encodeOffset(any())).thenReturn("cursor123")

            val result = postService.scrollPosts(VENDOR_ID, null, 10, vendorSecurity())

            assertNotNull(result)
            verify(postLikeRepository, never()).findAllByCustomerIdAndPostIdIn(any(), any())
        }

        @Test
        fun `customer scroll checks likes for all posts in the window`() {
            val post = singlePost()
            val window = buildWindow(listOf(post))
            whenever(securityUtility.getSingleRole(any())).thenReturn("CUSTOMER")
            whenever(postRepository.findAllByVendorId(eq(VENDOR_ID), any<ScrollPosition>(), any<Sort>(), any<Limit>()))
                .thenReturn(window)
            whenever(postLikeRepository.findAllByCustomerIdAndPostIdIn(eq(CUSTOMER_ID), any()))
                .thenReturn(emptyList())
            whenever(cursorEncoder.encodeOffset(any())).thenReturn("cursor123")

            postService.scrollPosts(VENDOR_ID, null, 10, customerSecurity())

            verify(postLikeRepository).findAllByCustomerIdAndPostIdIn(eq(CUSTOMER_ID), argThat {
                contains(POST_ID_1)
            })
        }

        @Test
        fun `uses offset zero for first page when cursor is null`() {
            val window = buildWindow(emptyList())
            whenever(securityUtility.getSingleRole(vendorSecurity())).thenReturn("VENDOR")
            whenever(postRepository.findAllByVendorId(eq(VENDOR_ID), any<ScrollPosition>(), any<Sort>(), any<Limit>()))
                .thenReturn(window)
            whenever(cursorEncoder.encodeOffset(any())).thenReturn("cursor0")

            postService.scrollPosts(VENDOR_ID, null, 10, vendorSecurity())

            verify(postRepository).findAllByVendorId(eq(VENDOR_ID), any(), any(), argThat<Limit> {
                max() == 10
            })
        }

        @Test
        fun `hasNext is false when window has no next page`() {
            val window = buildWindow(emptyList())
            whenever(securityUtility.getSingleRole(vendorSecurity())).thenReturn("VENDOR")
            whenever(postRepository.findAllByVendorId(eq(VENDOR_ID), any<ScrollPosition>(), any<Sort>(), any<Limit>()))
                .thenReturn(window)
            whenever(cursorEncoder.encodeOffset(any())).thenReturn("cursor0")

            val result = postService.scrollPosts(VENDOR_ID, null, 10, vendorSecurity())

            assertFalse(result.hasNext)
        }
    }

    // deletePost
    @Nested
    inner class DeletePost {

        @Test
        fun `deletes post when vendor owns it`() {
            val post = singlePost()
            whenever(postRepository.findByIdAndVendorId(POST_ID_1, VENDOR_ID)).thenReturn(post)

            postService.deletePost(POST_ID_1, vendorSecurity())

            verify(postRepository).delete(post)
        }

        @Test
        fun `throws RequestedEntityNotFoundException when post does not exist`() {
            whenever(postRepository.findByIdAndVendorId(POST_ID_1, VENDOR_ID)).thenReturn(null)

            assertThrows<RequestedEntityNotFoundException> {
                postService.deletePost(POST_ID_1, vendorSecurity())
            }
        }

        @Test
        fun `does not call delete when post is not found`() {
            whenever(postRepository.findByIdAndVendorId(POST_ID_1, VENDOR_ID)).thenReturn(null)

            runCatching { postService.deletePost(POST_ID_1, vendorSecurity()) }

            verify(postRepository, never()).delete(any<Post>())
        }

        @Test
        fun `throws RequestedEntityNotFoundException when post belongs to a different vendor`() {
            whenever(postRepository.findByIdAndVendorId(POST_ID_1, VENDOR_ID)).thenReturn(null)

            assertThrows<RequestedEntityNotFoundException> {
                postService.deletePost(POST_ID_1, vendorSecurity())
            }
        }
    }

    // savePost
    @Nested
    inner class SavePost {

        @Test
        fun `saves and returns the post`() {
            val post = singlePost()
            whenever(postRepository.saveAndFlush(post)).thenReturn(post)

            val result = postService.savePost(post)

            assertEquals(post, result)
            verify(postRepository).saveAndFlush(post)
        }
    }

    // addOrUpdatePost — create
    @Nested
    inner class AddOrUpdatePostCreate {

        @BeforeEach
        fun stubCreate() {
            val vendor = sabiVendor()
            val post = singlePost()
            whenever(vendorRepository.findById(VENDOR_ID)).thenReturn(Optional.of(vendor))
            whenever(postRepository.saveAndFlush(any())).thenReturn(post)
        }

        @Test
        fun `creates post with correct title when id is null`() {
            postService.addOrUpdatePost(postDtoCreate(), vendorSecurity())

            verify(postRepository).saveAndFlush(argThat {
                title == "Chef Special"
            })
        }

        @Test
        fun `creates post with correct body`() {
            postService.addOrUpdatePost(postDtoCreate(body = "Jollof special tonight"), vendorSecurity())

            verify(postRepository).saveAndFlush(argThat {
                body == "Jollof special tonight"
            })
        }

        @Test
        fun `creates post with null body when body is not provided`() {
            postService.addOrUpdatePost(postDtoCreate(body = null), vendorSecurity())

            verify(postRepository).saveAndFlush(argThat {
                body == null
            })
        }

        @Test
        fun `assigns vendor to new post`() {
            postService.addOrUpdatePost(postDtoCreate(), vendorSecurity())

            verify(postRepository).saveAndFlush(argThat {
                vendor.id == VENDOR_ID
            })
        }

        @Test
        fun `saves post images`() {
            val images = setOf(
                ImageDto(id = null, imageUrl = "https://cdn.feastique.com/img1.png"),
                ImageDto(id = null, imageUrl = "https://cdn.feastique.com/img2.png")
            )
            postService.addOrUpdatePost(postDtoCreate(images = images), vendorSecurity())

            verify(postRepository).saveAndFlush(argThat {
                postImages.size == 2
            })
        }

        @Test
        fun `throws UserNotFoundException when vendor does not exist`() {
            whenever(vendorRepository.findById(VENDOR_ID)).thenReturn(Optional.empty())

            assertThrows<UserNotFoundException> {
                postService.addOrUpdatePost(postDtoCreate(), vendorSecurity())
            }
        }

        @Test
        fun `throws IllegalArgumentException when post images set is empty`() {
            whenever(vendorRepository.findById(VENDOR_ID)).thenReturn(Optional.of(sabiVendor()))

            assertThrows<IllegalArgumentException> {
                postService.addOrUpdatePost(postDtoCreate(images = emptySet()), vendorSecurity())
            }
        }

        @Test
        fun `does not persist post when images list is empty`() {
            whenever(vendorRepository.findById(VENDOR_ID)).thenReturn(Optional.of(sabiVendor()))

            runCatching { postService.addOrUpdatePost(postDtoCreate(images = emptySet()), vendorSecurity()) }

            verify(postRepository, never()).saveAndFlush(any())
        }

        @Test
        fun `does not invoke getPostById when creating a new post`() {
            postService.addOrUpdatePost(postDtoCreate(), vendorSecurity())

            verify(postRepository, never()).findByIdAndVendorId(any(), any())
        }

        @Test
        fun `title is required and stored from dto`() {
            postService.addOrUpdatePost(postDtoCreate(title = "Weekend Specials"), vendorSecurity())

            verify(postRepository).saveAndFlush(argThat {
                title == "Weekend Specials"
            })
        }
    }

    // addOrUpdatePost — update
    @Nested
    inner class AddOrUpdatePostUpdate {

        @BeforeEach
        fun stubUpdate() {
            val vendor = sabiVendor()
            val existing = singlePost().also {
                it.postImages = mutableSetOf(postImage(it))
            }
            whenever(vendorRepository.findById(VENDOR_ID)).thenReturn(Optional.of(vendor))
            whenever(postRepository.findByIdAndVendorId(POST_ID_1, VENDOR_ID)).thenReturn(existing)
            whenever(postRepository.saveAndFlush(any())).thenReturn(existing)
        }

        @Test
        fun `fetches existing post by id when id is provided`() {
            postService.addOrUpdatePost(postDtoUpdate(POST_ID_1), vendorSecurity())

            verify(postRepository).findByIdAndVendorId(POST_ID_1, VENDOR_ID)
        }

        @Test
        fun `updates title on existing post`() {
            postService.addOrUpdatePost(postDtoUpdate(POST_ID_1, title = "New Title"), vendorSecurity())

            verify(postRepository).saveAndFlush(argThat {
                title == "New Title"
            })
        }

        @Test
        fun `updates body on existing post`() {
            postService.addOrUpdatePost(postDtoUpdate(POST_ID_1, body = "Updated body text"), vendorSecurity())

            verify(postRepository).saveAndFlush(argThat {
                body == "Updated body text"
            })
        }

        @Test
        fun `replaces images when new images are provided`() {
            val newImages = setOf(
                ImageDto(id = null, imageUrl = "https://cdn.feastique.com/new-img.png")
            )
            postService.addOrUpdatePost(postDtoUpdate(POST_ID_1, images = newImages), vendorSecurity())

            verify(postRepository).saveAndFlush(argThat {
                postImages.any { it.imageUrl == "https://cdn.feastique.com/new-img.png" }
            })
        }

        @Test
        fun `throws RequestedEntityNotFoundException when existing post is not found`() {
            whenever(postRepository.findByIdAndVendorId(POST_ID_1, VENDOR_ID)).thenReturn(null)

            assertThrows<RequestedEntityNotFoundException> {
                postService.addOrUpdatePost(postDtoUpdate(POST_ID_1), vendorSecurity())
            }
        }

        @Test
        fun `throws IllegalArgumentException when updated images list is empty`() {
            assertThrows<IllegalArgumentException> {
                postService.addOrUpdatePost(postDtoUpdate(POST_ID_1, images = emptySet()), vendorSecurity())
            }
        }
    }

    // preparePostImages — edge cases via addOrUpdatePost
    @Nested
    inner class PreparePostImages {

        @BeforeEach
        fun stubBase() {
            val vendor = sabiVendor()
            val post = singlePost()
            whenever(vendorRepository.findById(VENDOR_ID)).thenReturn(Optional.of(vendor))
            whenever(postRepository.saveAndFlush(any())).thenReturn(post)
        }

        @Test
        fun `single image is accepted`() {
            val images = setOf(ImageDto(id = null, imageUrl = "https://cdn.feastique.com/only.png"))

            postService.addOrUpdatePost(postDtoCreate(images = images), vendorSecurity())

            verify(postRepository).saveAndFlush(argThat {
                postImages.any { it.imageUrl == "https://cdn.feastique.com/only.png" }
            })
        }

        @Test
        fun `multiple images are all persisted on the post`() {
            val images = setOf(
                ImageDto(id = null, imageUrl = "https://cdn.feastique.com/a.png"),
                ImageDto(id = null, imageUrl = "https://cdn.feastique.com/b.png"),
                ImageDto(id = null, imageUrl = "https://cdn.feastique.com/c.png"),
            )

            postService.addOrUpdatePost(postDtoCreate(images = images), vendorSecurity())

            verify(postRepository).saveAndFlush(argThat {
                postImages.size == 3
            })
        }

        @Test
        fun `existing images with matching id are reused on update`() {
            val imageId = UUID.randomUUID()
            val existingImage = PostImage().apply {
                id = imageId
                imageUrl = "https://cdn.feastique.com/old.png"
                post = singlePost()
            }
            val existingPost = singlePost().also {
                it.postImages = mutableSetOf(existingImage)
            }
            whenever(vendorRepository.findById(VENDOR_ID)).thenReturn(Optional.of(sabiVendor()))
            whenever(postRepository.findByIdAndVendorId(POST_ID_1, VENDOR_ID)).thenReturn(existingPost)
            whenever(postRepository.saveAndFlush(any())).thenReturn(existingPost)

            val updatedImages = setOf(ImageDto(id = imageId, imageUrl = "https://cdn.feastique.com/updated.png"))
            postService.addOrUpdatePost(postDtoUpdate(POST_ID_1, images = updatedImages), vendorSecurity())

            verify(postRepository).saveAndFlush(argThat {
                postImages.any { it.id == imageId && it.imageUrl == "https://cdn.feastique.com/updated.png" }
            })
        }

        @Test
        fun `images absent from dto are removed from the post on update`() {
            val keptImageId = UUID.randomUUID()
            val removedImageId = UUID.randomUUID()
            val existingPost = singlePost().also { p ->
                p.postImages = mutableSetOf(
                    PostImage().apply { id = keptImageId; imageUrl = "keep.png"; post = p },
                    PostImage().apply { id = removedImageId; imageUrl = "remove.png"; post = p }
                )
            }
            whenever(vendorRepository.findById(VENDOR_ID)).thenReturn(Optional.of(sabiVendor()))
            whenever(postRepository.findByIdAndVendorId(POST_ID_1, VENDOR_ID)).thenReturn(existingPost)
            whenever(postRepository.saveAndFlush(any())).thenReturn(existingPost)

            val incomingImages = setOf(ImageDto(id = keptImageId, imageUrl = "keep.png"))
            postService.addOrUpdatePost(postDtoUpdate(POST_ID_1, images = incomingImages), vendorSecurity())

            verify(postRepository).saveAndFlush(argThat {
                postImages.none { it.id == removedImageId }
            })
        }
    }
}


package com.qinet.feastique.service.like

import com.qinet.feastique.exception.RequestedEntityNotFoundException
import com.qinet.feastique.model.entity.like.BeverageLike
import com.qinet.feastique.model.entity.like.DessertLike
import com.qinet.feastique.model.entity.like.FoodLike
import com.qinet.feastique.model.entity.like.HandheldLike
import com.qinet.feastique.model.entity.like.PostLike
import com.qinet.feastique.model.entity.like.VendorLike
import com.qinet.feastique.repository.consumables.beverage.BeverageRepository
import com.qinet.feastique.repository.consumables.dessert.DessertRepository
import com.qinet.feastique.repository.consumables.food.FoodRepository
import com.qinet.feastique.repository.consumables.handheld.HandheldRepository
import com.qinet.feastique.repository.like.BeverageLikeRepository
import com.qinet.feastique.repository.like.DessertLikeRepository
import com.qinet.feastique.repository.like.FoodLikeRepository
import com.qinet.feastique.repository.like.HandheldLikeRepository
import com.qinet.feastique.repository.like.PostLikeRepository
import com.qinet.feastique.repository.like.VendorLikeRepository
import com.qinet.feastique.repository.post.PostRepository
import com.qinet.feastique.repository.user.CustomerRepository
import com.qinet.feastique.repository.user.VendorRepository
import com.qinet.feastique.security.UserSecurity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import kotlin.jvm.optionals.getOrElse

@Service
class LikeService(
    private val postRepository: PostRepository,
    private val beverageRepository: BeverageRepository,
    private val foodRepository: FoodRepository,
    private val dessertRepository: DessertRepository,
    private val handheldRepository: HandheldRepository,
    private val vendorRepository: VendorRepository,
    private val customerRepository: CustomerRepository,
    private val postLikeRepository: PostLikeRepository,
    private val beverageLikeRepository: BeverageLikeRepository,
    private val foodLikeRepository: FoodLikeRepository,
    private val dessertLikeRepository: DessertLikeRepository,
    private val handheldLikeRepository: HandheldLikeRepository,
    private val vendorLikeRepository: VendorLikeRepository,
) {

    @Transactional
    fun likeOrUnlikePost(postId: UUID, customerDetails: UserSecurity) {
        val existingLike = postLikeRepository.findByPostIdAndCustomerId(postId, customerDetails.id)
        if (existingLike != null) {
            postLikeRepository.delete(existingLike)
        } else {
            val post = postRepository.findById(postId)
                .getOrElse { throw RequestedEntityNotFoundException("Post not found.") }
            val like = PostLike()
            like.post = post
            like.customer = customerRepository.getReferenceById(customerDetails.id)
            postLikeRepository.save(like)
        }
    }

    @Transactional
    fun likeOrUnlikeBeverage(beverageId: UUID, customerDetails: UserSecurity) {
        val existingLike = beverageLikeRepository.findByBeverageIdAndCustomerId(beverageId, customerDetails.id)
        if (existingLike != null) {
            beverageLikeRepository.delete(existingLike)
        } else {
            val beverage = beverageRepository.findById(beverageId)
                .getOrElse { throw RequestedEntityNotFoundException("Beverage not found.") }
            val like = BeverageLike()
            like.beverage = beverage
            like.customer = customerRepository.getReferenceById(customerDetails.id)
            beverageLikeRepository.save(like)
        }
    }

    @Transactional
    fun likeOrUnlikeFood(foodId: UUID, customerDetails: UserSecurity) {
        val existingLike = foodLikeRepository.findByFoodIdAndCustomerId(foodId, customerDetails.id)
        if (existingLike != null) {
            foodLikeRepository.delete(existingLike)
        } else {
            val food = foodRepository.findById(foodId)
                .getOrElse { throw RequestedEntityNotFoundException("Food not found.") }
            val like = FoodLike()
            like.food = food
            like.customer = customerRepository.getReferenceById(customerDetails.id)
            foodLikeRepository.save(like)
        }
    }

    @Transactional
    fun likeOrUnlikeDessert(dessertId: UUID, customerDetails: UserSecurity) {
        val existingLike = dessertLikeRepository.findByDessertIdAndCustomerId(dessertId, customerDetails.id)
        if (existingLike != null) {
            dessertLikeRepository.delete(existingLike)
        } else {
            val dessert = dessertRepository.findById(dessertId)
                .getOrElse { throw RequestedEntityNotFoundException("Dessert not found.") }
            val like = DessertLike()
            like.dessert = dessert
            like.customer = customerRepository.getReferenceById(customerDetails.id)
            dessertLikeRepository.save(like)
        }
    }

    @Transactional
    fun likeOrUnlikeHandheld(handheldId: UUID, customerDetails: UserSecurity) {
        val existingLike = handheldLikeRepository.findByHandheldIdAndCustomerId(handheldId, customerDetails.id)
        if (existingLike != null) {
            handheldLikeRepository.delete(existingLike)
        } else {
            val handheld = handheldRepository.findById(handheldId)
                .getOrElse { throw RequestedEntityNotFoundException("Handheld not found.") }
            val like = HandheldLike()
            like.handheld = handheld
            like.customer = customerRepository.getReferenceById(customerDetails.id)
            handheldLikeRepository.save(like)
        }
    }

    @Transactional
    fun likeOrUnlikeVendor(vendorId: UUID, customerDetails: UserSecurity) {
        val existingLike = vendorLikeRepository.findByVendorIdAndCustomerId(vendorId, customerDetails.id)
        if (existingLike != null) {
            vendorLikeRepository.delete(existingLike)
        } else {
            val vendor = vendorRepository.findById(vendorId)
                .getOrElse { throw RequestedEntityNotFoundException("Vendor not found.") }
            val like = VendorLike()
            like.vendor = vendor
            like.customer = customerRepository.getReferenceById(customerDetails.id)
            vendorLikeRepository.save(like)
        }
    }
}


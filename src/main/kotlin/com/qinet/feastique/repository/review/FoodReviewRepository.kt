package com.qinet.feastique.repository.review

//import com.qinet.feastique.model.entity.review.FoodReview
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/*
@Repository
interface FoodReviewRepository : JpaRepository<FoodReview, Long> {

    @Query("SELECT fr FROM FoodReview fr JOIN FETCH fr.review r WHERE fr.food.id = :foodId")
    fun findByFoodId(@Param("foodId") foodId: Long): List<FoodReview>


}*/

package com.qinet.feastique.service.food

import com.qinet.feastique.model.entity.food.FoodImage
import com.qinet.feastique.repository.food.FoodImageRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FoodImageService(
    private val foodImageRepository: FoodImageRepository
) {

    @Transactional(readOnly = true)
    fun getAllFoodImages(foodId: Long): List<FoodImage> {
        return foodImageRepository.findAllByFoodId(foodId)
    }

    @Transactional(readOnly = true)
    fun getFoodImage(id: Long, foodId: Long): FoodImage? {
        return foodImageRepository.findByIdAndFoodId(id, foodId)
    }

    @Transactional(readOnly = true)
    fun deleteImage(foodImage: FoodImage) {
        foodImageRepository.delete(foodImage)
    }

    @Transactional
    fun deleteAllImages(foodId: Long) {
        foodImageRepository.deleteAllByFoodId(foodId)
    }

}


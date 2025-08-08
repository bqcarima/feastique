package com.qinet.feastique.service.complement

import com.qinet.feastique.model.entity.complement.FoodComplement
import com.qinet.feastique.repository.complement.FoodComplementRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Optional

@Service
class FoodComplementService(
    private val foodComplementRepository: FoodComplementRepository
) {

    @Transactional(readOnly = true)
    fun getFoodComplement(id: Long): Optional<FoodComplement> {
        return foodComplementRepository.findById(id)
    }

    @Transactional(readOnly = true)
    fun getAllFoodComplements(foodId: Long): List<FoodComplement> {
        return foodComplementRepository.findAllByFoodId(foodId)
    }

    @Transactional
    fun saveFoodComplement(foodComplement: FoodComplement) {
        foodComplementRepository.save(foodComplement)
    }

    @Transactional
    fun deleteFoodComplement(complementId: Long, foodId: Long) {
        foodComplementRepository.deleteByComplementIdAndFoodId(complementId, foodId)
    }

    @Transactional
    fun deleteAllFoodComplements(foodId: Long) {
        foodComplementRepository.deleteAllByFoodId(foodId)
    }
}
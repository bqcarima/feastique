package com.qinet.feastique.controller.addOn

import com.qinet.feastique.model.dto.AddOnDto
import com.qinet.feastique.model.entity.addOn.AddOn
import com.qinet.feastique.response.AddOnResponse
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.service.addOn.AddOnService
import com.qinet.feastique.service.addOn.FoodAddOnService
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/vendor/{vendorId}/add_on")
class AddOnController(
    private val addOnService: AddOnService,
    private val foodAddOnService: FoodAddOnService
) {

    @PostMapping("/add")
    fun addAddOn (
        @PathVariable vendorId: Long,
        @RequestBody
        @Valid addOnDto: AddOnDto,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ): AddOnResponse {
        val addOn = addOnService.addAddOn(addOnDto, vendorDetails)

        return AddOnResponse(
            id = addOn.id!!,
            addOnName = addOn.addOnName!!,
            price = addOn.price!!
        )
    }

    @DeleteMapping("/delete/{addOnId}")
    fun deleteAddOn(
        @PathVariable vendorId: Long,
        @PathVariable addOnId: Long,
        @AuthenticationPrincipal vendorDetails: UserSecurity
    ) {
        val addOn = addOnService.getAddOn(addOnId)
            .orElseThrow { IllegalArgumentException("Add-on not found.") }
            .also {
                if(it.vendor.id != vendorDetails.id) {
                    throw IllegalArgumentException("You do not have permission to delete add-on")
                }
            }

        foodAddOnService.deleteAllFoodAddOnsByAddOnId(addOn.id!!)
        addOnService.deleteAddOn(addOn)
    }

    @GetMapping("/all")
    fun getAllAddOns(
        @PathVariable vendorId: Long,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ): List<AddOn> {
        if(vendorId != vendorDetails.id) {
            throw IllegalArgumentException("You do not have permission to delete this add-on.")
        }
        return addOnService.getAllComplements(vendorDetails.id)
    }
}


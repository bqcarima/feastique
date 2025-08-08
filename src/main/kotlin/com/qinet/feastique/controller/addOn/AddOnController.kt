package com.qinet.feastique.controller.addOn

import com.qinet.feastique.model.dto.AddOnDto
import com.qinet.feastique.model.entity.addOn.AddOn
import com.qinet.feastique.response.AddOnResponse
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.service.addOn.AddOnService
import com.qinet.feastique.service.vendor.VendorService
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/vendor/{vendorId}/add_on")
class AddOnController(
    private val addOnService: AddOnService,
    private val vendorService: VendorService
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
        val addOn = addOnService.getAddOn(addOnId).orElseThrow {
            Exception("Add-on not found.")
        }

        val vendor = vendorService.getVendorById(vendorDetails.id).orElseThrow {
            Exception("An unexpected error occurred. Unable to delete add-on.")
        }

        if(vendor != addOn.vendor) {
            throw IllegalAccessError("You do not have permission to delete add-on")
        }
    }

    @GetMapping("/all")
    fun getAllAddOns(
        @PathVariable vendorId: Long,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ): List<AddOn> {
        if(vendorId != vendorDetails.id) throw IllegalAccessException("You do not have permission to delete this add-on.")
        return addOnService.getAllComplements(vendorDetails.id)
    }
}
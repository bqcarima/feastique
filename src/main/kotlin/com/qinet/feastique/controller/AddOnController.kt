package com.qinet.feastique.controller

import com.qinet.feastique.common.mapper.toResponse
import com.qinet.feastique.model.dto.AddOnDto
import com.qinet.feastique.response.AddOnResponse
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.service.AddOnService
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/vendor/{vendorId}/add_on")
class AddOnController(
    private val addOnService: AddOnService
) {

    @PostMapping("/add")
    fun addAddOn (
        @PathVariable vendorId: Long,
        @RequestBody
        @Valid addOnDto: AddOnDto,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ): AddOnResponse {
        val addOn = addOnService.addOrUpdateAddOn(addOnDto, vendorDetails)

        return AddOnResponse(
            id = addOn.id!!,
            addOnName = addOn.addOnName!!,
            price = addOn.price!!
        )
    }

    @DeleteMapping("/delete/{id}")
    fun deleteAddOn(
        @PathVariable id: Long,
        @PathVariable vendorId: Long,
        @AuthenticationPrincipal vendorDetails: UserSecurity
    ) {
        addOnService.deleteAddOn(id, vendorId, vendorDetails)
    }

    @GetMapping("/all")
    fun getAllAddOns(
        @PathVariable vendorId: Long,
        @AuthenticationPrincipal vendorDetails: UserSecurity

    ): List<AddOnResponse> {
        return addOnService.getAllAddOns(vendorId, vendorDetails).map { it.toResponse() }
    }
}
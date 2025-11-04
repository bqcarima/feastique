package com.qinet.feastique.model.entity.provisions.addOn

import com.fasterxml.jackson.annotation.JsonBackReference
import com.qinet.feastique.model.entity.provisions.BaseEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "add_ons")
class AddOn : BaseEntity(){

    @JsonBackReference
    @OneToMany(
        mappedBy = "addOn",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var foodAddOn: MutableList<FoodAddOn> = mutableListOf()
}


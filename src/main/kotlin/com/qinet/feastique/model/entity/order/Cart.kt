package com.qinet.feastique.model.entity.order

import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "cart")
class Cart : OrderEntity()
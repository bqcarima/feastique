package com.qinet.feastique.repository

import com.qinet.feastique.model.entity.Menu
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID


interface MenuRepository : JpaRepository<Menu, UUID> {
}


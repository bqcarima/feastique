package com.qinet.feastique.repository

import com.qinet.feastique.model.entity.Menu
import org.springframework.data.jpa.repository.JpaRepository


interface MenuRepository : JpaRepository<Menu, Long> {
}


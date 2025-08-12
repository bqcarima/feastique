package com.qinet.feastique.common.ordering


import jakarta.annotation.PostConstruct
import jakarta.persistence.EntityManagerFactory
import org.hibernate.event.service.spi.EventListenerRegistry
import org.hibernate.event.spi.PreInsertEventListener
import org.hibernate.event.spi.PreUpdateEventListener
import org.hibernate.internal.SessionFactoryImpl
import org.springframework.context.annotation.Configuration

/**
 * Spring Boot configuration that registers [GlobalOrderIndexListener]
 * as a global Hibernate event listener.
 *
 * This avoids using deprecated APIs and ensures the listener
 * applies to all OrderIndexed entities without the need
 * for `@EntityListeners` on each entity.
 */
@Configuration
class HibernateListenerConfig(
    private val entityManagerFactory: EntityManagerFactory
) {

    @PostConstruct
    fun registerListener() {
        val sessionFactory = entityManagerFactory.unwrap(SessionFactoryImpl::class.java)
        val registry = sessionFactory.serviceRegistry.getService(EventListenerRegistry::class.java)

        val adapter = HibernateOrderIndexEventAdapter()

        registry?.appendListeners(org.hibernate.event.spi.EventType.PRE_INSERT, adapter)
        registry?.appendListeners(org.hibernate.event.spi.EventType.PRE_UPDATE, adapter)
    }

    /**
     * Adapts the [GlobalOrderIndexListener] to Hibernate's event system.
     */
    private class HibernateOrderIndexEventAdapter :
        PreInsertEventListener,
        PreUpdateEventListener {

        override fun onPreInsert(event: org.hibernate.event.spi.PreInsertEvent): Boolean {
            GlobalOrderIndexListener.beforeSave(event.entity)
            return false // false = don't veto the insert
        }

        override fun onPreUpdate(event: org.hibernate.event.spi.PreUpdateEvent): Boolean {
            GlobalOrderIndexListener.beforeSave(event.entity)
            return false
        }
    }
}


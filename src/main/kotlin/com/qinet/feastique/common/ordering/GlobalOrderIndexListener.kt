package com.qinet.feastique.common.ordering

import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate

/**
 * JPA entity listener that automatically adjusts the `orderIndex`
 * of [OrderIndexed] entities inside lists before persistence.
 *
 * This ensures that:
 *  - The first element has index 0
 *  - Subsequent elements have sequential indices (1, 2, 3…)
 *  - No manual reordering is needed in the service layer
 *
 * The listener works for both **insert** and **update** operations.
 *
 * **Note:**
 * This listener assumes that the owning entity has a property
 * of type `List<OrderIndexed>` which Hibernate manages.
 *
 * @see OrderIndexed
 */
object GlobalOrderIndexListener {

    @PrePersist
    @PreUpdate
    fun beforeSave(entity: Any) {
        entity::class.members
            .filter { it.returnType.classifier == List::class }
            .mapNotNull { it.call(entity) as? MutableList<*> }
            .forEach { list ->
                if (list.all { it is OrderIndexed }) {
                    (list as MutableList<OrderIndexed>).forEachIndexed { index, item ->
                        item.orderIndex = index
                    }
                }
            }
    }
}


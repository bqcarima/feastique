package com.qinet.feastique.common.ordering

/**
 * Contract for entities that participate in ordered collections
 * where their position in the list is tracked by a numeric index.
 *
 * This interface is used by the [GlobalOrderIndexListener] to ensure
 * that all `orderIndex` values in a managed list match the actual
 * position of the entity in that list before Hibernate persists
 * or updates it.
 *
 * Implementing classes must provide a mutable [orderIndex] property.
 *
 * Typical use cases:
 *  - Ordered menu items in a restaurant app
 *  - Ordered images in a gallery
 *  - Ordered steps in a workflow
 *
 * **Important:**
 * The `orderIndex` should start at **0** for the first element and
 * increase sequentially with no gaps.
 *
 * @property orderIndex The zero-based position of this entity within its containing list.
 */
interface OrderIndexed {
    var orderIndex: Int
}


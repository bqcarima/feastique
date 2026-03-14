package com.qinet.feastique.utility

import org.springframework.stereotype.Component
import java.util.Base64

@Component
class CursorEncoder {

    fun encodeOffset(offset: Long): String =
        Base64.getEncoder().encodeToString("offset:$offset".toByteArray())

    fun decodeOffset(cursor: String): Long =
        Base64.getDecoder().decode(cursor)
            .toString(Charsets.UTF_8)
            .removePrefix("offset:")
            .toLong()
}


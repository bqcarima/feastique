package com.qinet.feastique.exception

class MalformedUrlException(message: String? = "Malformed URL, cannot process request.") : RuntimeException(message)
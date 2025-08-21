package com.qinet.feastique.exception

class UserNotFoundException(message: String? = "User not found.") : RuntimeException(message)
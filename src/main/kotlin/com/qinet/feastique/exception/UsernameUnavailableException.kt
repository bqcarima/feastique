package com.qinet.feastique.exception

class UsernameUnavailableException(message: String? = "The username is already taken.") : RuntimeException(message)
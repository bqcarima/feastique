package com.qinet.feastique.exception

class PermissionDeniedException(message: String? = "You do not have the required permission to perform operation.") : RuntimeException(message)
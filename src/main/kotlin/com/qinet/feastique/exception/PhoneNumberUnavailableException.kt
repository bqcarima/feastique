package com.qinet.feastique.exception

class PhoneNumberUnavailableException(message: String? = "The phone number is already associated with a different account. Contact customer support to claim.") : RuntimeException(message)
package com.qinet.feastique.exception

class DuplicateFoundException(message: String? = "Cannot add duplicates.") : RuntimeException(message)
class EntityNotDeliverableException(message: String? = "This is not available for delivery.") : RuntimeException(message)

class MalformedUrlException(message: String? = "Malformed URL, cannot process request.") : RuntimeException(message)

class MultipleRolesException(message: String? = "User has multiple roles.") : RuntimeException(message)

class NoRoleException(message: String? = "User has no role.") : RuntimeException(message)

class PermissionDeniedException(message: String? = "You do not have the required permission to perform operation.") : RuntimeException(message)

class PhoneNumberNotFoundException(message: String? = "Phone number not found.") : RuntimeException(message)

class PhoneNumberUnavailableException(message: String? = "The phone number is already associated with a different account. Contact customer support to claim.") : RuntimeException(message)

class RequestedEntityNotFoundException(message: String? = "Entity not found.") : RuntimeException(message)

class UsernameUnavailableException(message: String? = "The username is unavailable.") : RuntimeException(message)

class UserNotFoundException(message: String? = "User not found.") : RuntimeException(message)


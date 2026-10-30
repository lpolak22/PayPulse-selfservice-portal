package com.example.paypulse.domain.user

sealed class UserManagementException : Exception()

class EmailAlreadyExistsException : UserManagementException()

class LastActiveAdminException : UserManagementException()

class UserNotFoundException : UserManagementException()
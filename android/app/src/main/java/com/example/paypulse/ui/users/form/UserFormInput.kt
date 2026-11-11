package com.example.paypulse.ui.users.form

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.paypulse.domain.user.User
import com.example.paypulse.domain.user.UserDraft
import com.example.paypulse.domain.user.UserDraftValidator
import com.example.paypulse.domain.user.UserField
import com.example.paypulse.domain.user.UserRole

class UserFormInput {
    var firstName by mutableStateOf("")
        private set
    var lastName by mutableStateOf("")
        private set
    var email by mutableStateOf("")
        private set
    var phone by mutableStateOf("")
        private set

    val values: UserFormValues
        get() = UserFormValues(firstName, lastName, email, phone)

    fun update(field: UserField, value: String) {
        when (field) {
            UserField.FIRST_NAME -> firstName = value
            UserField.LAST_NAME -> lastName = value
            UserField.EMAIL -> email = value
            UserField.PHONE -> phone = value
        }
    }

    fun fill(user: User) {
        firstName = user.firstName
        lastName = user.lastName
        email = user.email
        phone = user.phone.orEmpty()
    }

    fun toDraft(role: UserRole) = UserDraft(
        firstName = firstName.trim(),
        lastName = lastName.trim(),
        email = email.trim(),
        phone = UserDraftValidator.normalizePhone(phone),
        role = role
    )
}
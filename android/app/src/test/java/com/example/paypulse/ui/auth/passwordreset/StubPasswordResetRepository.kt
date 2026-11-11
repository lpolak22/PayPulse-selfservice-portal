package com.example.paypulse.ui.auth.passwordreset

import com.example.paypulse.domain.passwordreset.PasswordResetRepository
import com.example.paypulse.domain.passwordreset.PasswordResetResult
import com.example.paypulse.domain.passwordreset.ResetTokenCheckResult
import com.example.paypulse.domain.passwordreset.ResetTokenStatus

class StubPasswordResetRepository : PasswordResetRepository {

    data class ResetRequest(val token: String, val password: String, val confirmPassword: String)

    var requestResult: PasswordResetResult = PasswordResetResult.Success
    var tokenCheckResult: ResetTokenCheckResult = ResetTokenCheckResult.Checked(ResetTokenStatus.VALID)
    var resetResult: PasswordResetResult = PasswordResetResult.Success

    val requestedEmails = mutableListOf<String>()
    val checkedTokens = mutableListOf<String>()
    val resetRequests = mutableListOf<ResetRequest>()

    override suspend fun requestReset(email: String): PasswordResetResult {
        requestedEmails += email
        return requestResult
    }

    override suspend fun checkToken(token: String): ResetTokenCheckResult {
        checkedTokens += token
        return tokenCheckResult
    }

    override suspend fun resetPassword(token: String, password: String, confirmPassword: String): PasswordResetResult {
        resetRequests += ResetRequest(token, password, confirmPassword)
        return resetResult
    }
}

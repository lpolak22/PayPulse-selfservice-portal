package com.example.paypulse.data.auth

import com.example.paypulse.data.remote.AuthApi
import com.example.paypulse.data.remote.ErrorResponseParser
import com.example.paypulse.data.remote.dto.LoginRequestDto
import com.example.paypulse.data.remote.mapper.loginErrorFromCode
import com.example.paypulse.domain.auth.AuthRepository
import com.example.paypulse.domain.auth.LoginError
import com.example.paypulse.domain.auth.LoginResult
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException

class ApiAuthRepository(
    private val authApi: AuthApi,
    private val errorResponseParser: ErrorResponseParser
) : AuthRepository {

    override suspend fun login(email: String, password: String): LoginResult = try {
        authApi.login(LoginRequestDto(email, password))
        LoginResult.Success
    } catch (exception: HttpException) {
        LoginResult.Failure(loginErrorFromCode(errorResponseParser.errorCode(exception)))
    } catch (exception: IOException) {
        LoginResult.Failure(LoginError.NETWORK)
    } catch (exception: SerializationException) {
        LoginResult.Failure(LoginError.UNKNOWN)
    }
}

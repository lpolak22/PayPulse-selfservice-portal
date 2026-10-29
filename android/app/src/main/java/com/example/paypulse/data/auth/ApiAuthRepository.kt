package com.example.paypulse.data.auth

import com.example.paypulse.data.remote.AuthApi
import com.example.paypulse.data.remote.ErrorResponseParser
import com.example.paypulse.data.remote.dto.LoginRequestDto
import com.example.paypulse.data.remote.mapper.loginErrorFromCode
import com.example.paypulse.data.remote.mapper.toAuthTokens
import com.example.paypulse.domain.auth.AuthRepository
import com.example.paypulse.domain.auth.LoginError
import com.example.paypulse.domain.auth.LoginResult
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import java.security.GeneralSecurityException

class ApiAuthRepository(
    private val authApi: AuthApi,
    private val errorResponseParser: ErrorResponseParser,
    private val tokenStorage: TokenStorage
) : AuthRepository {

    override suspend fun login(email: String, password: String): LoginResult = try {
        tokenStorage.save(authApi.login(LoginRequestDto(email, password)).toAuthTokens())
        LoginResult.Success
    } catch (exception: HttpException) {
        LoginResult.Failure(loginErrorFromCode(errorResponseParser.errorCode(exception)))
    } catch (exception: IOException) {
        LoginResult.Failure(LoginError.NETWORK)
    } catch (exception: SerializationException) {
        LoginResult.Failure(LoginError.UNKNOWN)
    } catch (exception: GeneralSecurityException) {
        LoginResult.Failure(LoginError.UNKNOWN)
    }
}

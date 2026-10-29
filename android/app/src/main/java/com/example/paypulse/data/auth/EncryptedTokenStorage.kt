package com.example.paypulse.data.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.paypulse.data.security.KeystoreCipher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.security.GeneralSecurityException

private val Context.authTokensDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "auth_tokens",
    corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() }
)
private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")

class EncryptedTokenStorage(
    context: Context,
    private val cipher: KeystoreCipher
) : TokenStorage {

    private val dataStore = context.authTokensDataStore
    private val mutex = Mutex()
    private var isLoaded = false
    private var cachedTokens: AuthTokens? = null

    override suspend fun read(): AuthTokens? = mutex.withLock {
        if (!isLoaded) remember(readFromDisk())
        cachedTokens
    }

    override suspend fun save(tokens: AuthTokens) {
        mutex.withLock {
            dataStore.edit { preferences ->
                preferences[ACCESS_TOKEN_KEY] = cipher.encrypt(tokens.accessToken)
                preferences[REFRESH_TOKEN_KEY] = cipher.encrypt(tokens.refreshToken)
            }
            remember(tokens)
        }
    }

    override suspend fun clear() {
        mutex.withLock {
            dataStore.edit { it.clear() }
            remember(null)
        }
    }

    private fun remember(tokens: AuthTokens?) {
        cachedTokens = tokens
        isLoaded = true
    }

    private suspend fun readFromDisk(): AuthTokens? {
        val preferences = dataStore.data.first()
        val encryptedAccessToken = preferences[ACCESS_TOKEN_KEY] ?: return null
        val encryptedRefreshToken = preferences[REFRESH_TOKEN_KEY] ?: return null
        val tokens = decryptOrNull(encryptedAccessToken, encryptedRefreshToken)
        if (tokens == null) dataStore.edit { it.clear() }
        return tokens
    }

    private fun decryptOrNull(encryptedAccessToken: String, encryptedRefreshToken: String): AuthTokens? = try {
        AuthTokens(cipher.decrypt(encryptedAccessToken), cipher.decrypt(encryptedRefreshToken))
    } catch (exception: GeneralSecurityException) {
        null
    } catch (exception: IllegalArgumentException) {
        null
    }
}

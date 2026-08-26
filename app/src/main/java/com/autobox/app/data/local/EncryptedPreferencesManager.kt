package com.autobox.app.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.autobox.app.data.models.SnipeSettings
import com.google.gson.Gson

class EncryptedPreferencesManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_FILENAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val gson = Gson()

    var email: String?
        get() = sharedPreferences.getString(KEY_EMAIL, null)
        set(value) = sharedPreferences.edit().putString(KEY_EMAIL, value).apply()

    var password: String?
        get() = sharedPreferences.getString(KEY_PASSWORD, null)
        set(value) = sharedPreferences.edit().putString(KEY_PASSWORD, value).apply()

    var authToken: String?
        get() = sharedPreferences.getString(KEY_AUTH_TOKEN, null)
        set(value) = sharedPreferences.edit().putString(KEY_AUTH_TOKEN, value).apply()

    var userId: Long
        get() = sharedPreferences.getLong(KEY_USER_ID, -1L)
        set(value) = sharedPreferences.edit().putLong(KEY_USER_ID, value).apply()

    var userName: String?
        get() = sharedPreferences.getString(KEY_USER_NAME, null)
        set(value) = sharedPreferences.edit().putString(KEY_USER_NAME, value).apply()

    var boxId: Long
        get() = sharedPreferences.getLong(KEY_BOX_ID, -1L)
        set(value) = sharedPreferences.edit().putLong(KEY_BOX_ID, value).apply()

    var membershipId: Long
        get() = sharedPreferences.getLong(KEY_MEMBERSHIP_ID, -1L)
        set(value) = sharedPreferences.edit().putLong(KEY_MEMBERSHIP_ID, value).apply()

    var membershipName: String?
        get() = sharedPreferences.getString(KEY_MEMBERSHIP_NAME, null)
        set(value) = sharedPreferences.edit().putString(KEY_MEMBERSHIP_NAME, value).apply()

    fun isLoggedIn(): Boolean {
        return !authToken.isNullOrBlank() && membershipId > 0
    }

    fun clearAuth() {
        sharedPreferences.edit()
            .remove(KEY_AUTH_TOKEN)
            .remove(KEY_USER_ID)
            .remove(KEY_USER_NAME)
            .remove(KEY_MEMBERSHIP_ID)
            .remove(KEY_MEMBERSHIP_NAME)
            .remove(KEY_BOX_ID)
            .apply()
    }

    fun saveSnipeSettings(settings: SnipeSettings) {
        val json = gson.toJson(settings)
        sharedPreferences.edit().putString(KEY_SNIPE_SETTINGS, json).apply()
    }

    fun getSnipeSettings(): SnipeSettings {
        val json = sharedPreferences.getString(KEY_SNIPE_SETTINGS, null) ?: return SnipeSettings()
        return try {
            gson.fromJson(json, SnipeSettings::class.java) ?: SnipeSettings()
        } catch (e: Exception) {
            SnipeSettings()
        }
    }

    companion object {
        private const val PREFS_FILENAME = "autobox_secure_prefs"
        private const val KEY_EMAIL = "key_email"
        private const val KEY_PASSWORD = "key_password"
        private const val KEY_AUTH_TOKEN = "key_auth_token"
        private const val KEY_USER_ID = "key_user_id"
        private const val KEY_USER_NAME = "key_user_name"
        private const val KEY_BOX_ID = "key_box_id"
        private const val KEY_MEMBERSHIP_ID = "key_membership_id"
        private const val KEY_MEMBERSHIP_NAME = "key_membership_name"
        private const val KEY_SNIPE_SETTINGS = "key_snipe_settings"
    }
}

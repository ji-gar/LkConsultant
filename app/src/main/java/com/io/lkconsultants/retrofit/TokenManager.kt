package com.room.roomy.retrofit
import android.content.Context
import android.content.SharedPreferences

object  TokenProvider {
    private const val PREFS_NAME = "my_prefs"
    private const val TOKEN_KEY = "token_key"
    private const val COUNT = "count"
    private const val USERID = "userid"
    private const val NAME = "name"

    private lateinit var sharedPreferences: SharedPreferences

    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun setToken(token: String) {
        sharedPreferences.edit().putString(TOKEN_KEY, token).apply()
    }

    fun getToken(): String {
        return sharedPreferences.getString(TOKEN_KEY, "") ?: ""
    }

    fun clear() {
        sharedPreferences.edit().clear().apply()
    }

    fun setUserId(userId: String) {
        sharedPreferences.edit().putString(USERID, userId).apply()
    }

    fun getUserId(): String {
        return sharedPreferences.getString(USERID, "") ?: ""
    }

    fun setName(name: String) {
        sharedPreferences.edit().putString(NAME, name).apply()
    }

    fun getName(): String {
        return sharedPreferences.getString(NAME, "") ?: ""
    }

    fun setCount(count: Int) {
        sharedPreferences.edit().putInt(COUNT, count).apply()
    }

    fun getCount(): Int {
        return sharedPreferences.getInt(COUNT, 0)
    }
}


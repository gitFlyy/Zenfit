package com.example.zenfit

object ApiConfig {
    private const val BASE_URL = "http://192.168.18.12/zenfit/"
    const val LOGIN_URL = "${BASE_URL}login.php"
    const val SIGNUP_URL = "${BASE_URL}login.php"
    const val FORGOT_PASSWORD_URL = "${BASE_URL}forgot_password.php"
    const val RESET_PASSWORD_URL = "${BASE_URL}reset_password.php"
    const val CHANGE_PASSWORD_URL = "${BASE_URL}change_password.php"
    const val GET_ACCOUNT_URL = "${BASE_URL}account.php"
    const val UPDATE_ACCOUNT_URL = "${BASE_URL}update_account.php"
    const val DELETE_ACCOUNT_URL = "${BASE_URL}delete_account.php"
    const val UPDATE_PROFILE_URL = "${BASE_URL}update_account.php"
}

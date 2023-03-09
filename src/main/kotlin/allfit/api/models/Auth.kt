package allfit.api.models

import kotlinx.serialization.Serializable

@Serializable
data class AuthJson(
    val email: String,
    val password: String,
)

@Serializable
data class AuthResponseJson(
    val access_token: String,
)

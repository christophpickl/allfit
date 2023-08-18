package allfit.api

data class Credentials(
    val email: String,
    val clearTextPassword: String,
) {
    override fun toString() = "Credentials[email=$email, clearTextPassword=***]"
}

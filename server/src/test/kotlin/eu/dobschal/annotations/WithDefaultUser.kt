import io.quarkus.test.security.TestSecurity

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@TestSecurity(user = "user1", roles = ["user"])
annotation class WithDefaultUser

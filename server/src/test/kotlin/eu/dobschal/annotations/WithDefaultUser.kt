import eu.dobschal.resource.BaseResourceTest.Companion.USER1
import eu.dobschal.utils.USER_ROLE
import io.quarkus.test.security.TestSecurity

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@TestSecurity(user = USER1, roles = [USER_ROLE])
annotation class WithDefaultUser

package capstone.kidlink.activity

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import capstone.kidlink.MainDispatcherRule
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.AuthResult
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@Suppress("DEPRECATION")
@RunWith(MockitoJUnitRunner::class)
class LoginActivityTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    private lateinit var auth: FirebaseAuth

    @Mock
    private lateinit var user: FirebaseUser

    private lateinit var loginActivity: LoginActivity

    @Before
    fun setUp() {
        loginActivity = LoginActivity()
        loginActivity.auth = auth
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `loginUser should login successfully`() = runBlockingTest {
        val email = "hokaganteng@gmail.com"
        val password = "1408Hoka"
        val authResult = Mockito.mock(AuthResult::class.java)
        val task = Tasks.forResult(authResult)

        `when`(auth.signInWithEmailAndPassword(email, password)).thenReturn(task)

        loginActivity.loginUser(email, password)

        Mockito.verify(auth).signInWithEmailAndPassword(email, password)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `loginUser should handle login failure`() = runBlockingTest {
        val email = "hokaganteng@gmail.com"
        val password = "1408Hoka"
        val task = Tasks.forException<AuthResult>(Exception("Login failed"))

        `when`(auth.signInWithEmailAndPassword(email, password)).thenReturn(task)

        loginActivity.loginUser(email, password)

        Mockito.verify(auth).signInWithEmailAndPassword(email, password)
    }
}

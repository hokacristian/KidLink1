package capstone.kidlink.activity

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import capstone.kidlink.MainDispatcherRule
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.storage.FirebaseStorage
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
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull

@Suppress("DEPRECATION")
@RunWith(MockitoJUnitRunner::class)
class SignupActivityTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    private lateinit var auth: FirebaseAuth

    @Mock
    private lateinit var firestore: FirebaseFirestore

    @Mock
    private lateinit var storage: FirebaseStorage

    @Mock
    private lateinit var user: FirebaseUser

    @Mock
    private lateinit var collectionReference: CollectionReference

    @Mock
    private lateinit var documentReference: DocumentReference

    private lateinit var signupActivity: SignupActivity

    @Before
    fun setUp() {
        signupActivity = SignupActivity()
        signupActivity.auth = auth
        signupActivity.db = firestore
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `registerUser should register successfully`() = runBlockingTest {
        val email = "test@example.com"
        val password = "password"
        val name = "Test User"
        val ortuEmail = "ortu@example.com"
        val authResult = Mockito.mock(AuthResult::class.java)
        val task = Tasks.forResult(authResult)

        `when`(auth.createUserWithEmailAndPassword(email, password)).thenReturn(task)

        signupActivity.registerUser(name, email, password, ortuEmail)

        Mockito.verify(auth).createUserWithEmailAndPassword(email, password)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `registerUser should handle registration failure`() = runBlockingTest {
        val email = "test@example.com"
        val password = "password"
        val name = "Test User"
        val ortuEmail = "ortu@example.com"
        val task = Tasks.forException<AuthResult>(Exception("Registration failed"))

        `when`(auth.createUserWithEmailAndPassword(email, password)).thenReturn(task)

        signupActivity.registerUser(name, email, password, ortuEmail)

        Mockito.verify(auth).createUserWithEmailAndPassword(email, password)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `saveUserToFirestore should save user data successfully`() = runBlockingTest {
        val userId = "userId"
        val name = "Test User"
        val email = "test@example.com"
        val ortuEmail = "ortu@example.com"
        val profileImageUrl = "http://example.com/image.jpg"

        `when`(firestore.collection(any())).thenReturn(collectionReference)
        `when`(collectionReference.document(any())).thenReturn(documentReference)
        `when`(documentReference.set(anyOrNull())).thenReturn(Tasks.forResult(null))

        signupActivity.saveUserToFirestore(userId, name, email, ortuEmail, profileImageUrl)

        Mockito.verify(documentReference).set(anyOrNull())
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `saveUserToFirestore should handle failure`() = runBlockingTest {
        val userId = "userId"
        val name = "Test User"
        val email = "test@example.com"
        val ortuEmail = "ortu@example.com"
        val profileImageUrl = "http://example.com/image.jpg"
        val task = Tasks.forException<Void>(Exception("Failed to save user data"))

        `when`(firestore.collection(any())).thenReturn(collectionReference)
        `when`(collectionReference.document(any())).thenReturn(documentReference)
        `when`(documentReference.set(anyOrNull())).thenReturn(task)

        signupActivity.saveUserToFirestore(userId, name, email, ortuEmail, profileImageUrl)

        Mockito.verify(documentReference).set(anyOrNull())
    }
}

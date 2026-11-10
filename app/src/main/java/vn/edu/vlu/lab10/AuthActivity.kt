package vn.edu.vlu.lab10

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import vn.edu.vlu.lab10.databinding.ActivityAuthBinding

/**
 * AuthActivity: Registration / Login screen using Email + Password via Firebase Authentication.
 * - Uses View Binding instead of findViewById().
 * - Automatically logs in if the previous session is still valid.
 */
class AuthActivity : AppCompatActivity() {

    // View Binding: _binding holds the reference to the views in activity_auth.xml
    private lateinit var binding: ActivityAuthBinding

    // FirebaseAuth object (singleton) managing the login session
    private lateinit var auth: FirebaseAuth

    // Flag determining current mode: true = Register, false = Login
    private var isRegisterMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate layout using View Binding
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // (1) Check auto-login status
        if (auth.currentUser != null) {
            goToMain()
            return // Stop onCreate to avoid setting up unnecessary UI
        }

        setupModeToggle()
        setupSubmitButton()
        setupForgotPassword()
    }

    /** Listen to Login <-> Register tab switch button and update UI accordingly. */
    private fun setupModeToggle() {
        binding.toggleAuthMode.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener // Ignore uncheck events

            isRegisterMode = (checkedId == binding.btnTabRegister.id)

            // Confirm password field only visible in Registration mode
            binding.tilConfirmPassword.visibility =
                if (isRegisterMode) View.VISIBLE else View.GONE

            // Challenge 1: Hide/show forgot password link
            binding.tvForgotPassword.visibility =
                if (isRegisterMode) View.GONE else View.VISIBLE

            // Change text on submit button
            binding.btnSubmit.text = getString(
                if (isRegisterMode) R.string.btn_register else R.string.btn_login
            )

            clearErrors() // Clear old error messages when changing modes
        }
    }

    /** Assign event for submit button: validate data then call Firebase. */
    private fun setupSubmitButton() {
        binding.btnSubmit.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            val confirm = binding.etConfirmPassword.text.toString()

            // Validate data on the client side before making network calls (saves calls, fast response)
            if (!validateInput(email, password, confirm)) return@setOnClickListener

            if (isRegisterMode) registerUser(email, password) else loginUser(email, password)
        }
    }

    /**
     * Challenge 1: Send password reset email to the address the user entered.
     */
    private fun setupForgotPassword() {
        binding.tvForgotPassword.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()

            // Must have a valid email to send
            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.tilEmail.error = "Enter a valid email to receive the password reset link"
                return@setOnClickListener
            }

            showLoading(true)

            auth.sendPasswordResetEmail(email).addOnCompleteListener(this) { task ->
                showLoading(false)
                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Sent a password reset link to $email. Please check your inbox (including Spam).",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    handleAuthError(task.exception)
                }
            }
        }
    }

    /**
     * Validate input data. Returns true if valid.
     * Catches errors: empty/invalid email, password < 6 characters, mismatched confirm password.
     */
    private fun validateInput(email: String, password: String, confirm: String): Boolean {
        clearErrors()
        var isValid = true

        // Email cannot be empty and must be correctly formatted
        if (email.isEmpty()) {
            binding.tilEmail.error = "Please enter email"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Invalid email format (e.g., name@gmail.com)"
            isValid = false
        }

        // Firebase requires passwords to be at least 6 characters
        if (password.length < 6) {
            binding.tilPassword.error = "Password must be at least 6 characters"
            isValid = false
        }

        // In Registration mode: confirm password must match
        if (isRegisterMode && password != confirm) {
            binding.tilConfirmPassword.error = "Passwords do not match"
            isValid = false
        }

        return isValid
    }

    /** Register a new account with email and password. */
    private fun registerUser(email: String, password: String) {
        showLoading(true)
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                showLoading(false)
                if (task.isSuccessful) {
                    // Registration successful: Firebase automatically logs in the new user
                    Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                    goToMain()
                } else {
                    handleAuthError(task.exception)
                }
            }
    }

    /** Log in with an existing account. */
    private fun loginUser(email: String, password: String) {
        showLoading(true)
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                showLoading(false)
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                    goToMain()
                } else {
                    handleAuthError(task.exception)
                }
            }
    }

    /**
     * Convert Firebase exceptions into friendly messages.
     * Each Exception type corresponds to a specific error cause.
     */
    private fun handleAuthError(exception: Exception?) {
        when (exception) {
            // Weak password (< 6 chars or too simple) - WEAK_PASSWORD error
            is FirebaseAuthWeakPasswordException ->
                binding.tilPassword.error = "Password is too weak, please use at least 6 characters"

            // Invalid email format or incorrect login credentials
            is FirebaseAuthInvalidCredentialsException ->
                if (exception.errorCode == "ERROR_INVALID_EMAIL") {
                    binding.tilEmail.error = "Invalid email format"
                } else {
                    Toast.makeText(this, "Incorrect email or password", Toast.LENGTH_LONG).show()
                }

            // Email is already registered
            is FirebaseAuthUserCollisionException ->
                binding.tilEmail.error = "This email is already registered, please switch to Login"

            // Account does not exist or has been disabled
            is FirebaseAuthInvalidUserException ->
                Toast.makeText(this, "Account does not exist or has been disabled", Toast.LENGTH_LONG).show()

            // Network connection lost
            is FirebaseNetworkException ->
                Toast.makeText(this, "No network connection, please check again", Toast.LENGTH_LONG).show()

            // Remaining errors: display original message for easy debugging
            else ->
                Toast.makeText(this, "Error: ${exception?.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    /** Toggle progress circle and lock submit button to avoid multiple clicks. */
    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSubmit.isEnabled = !isLoading
    }

    /** Clear all error messages on input fields. */
    private fun clearErrors() {
        binding.tilEmail.error = null
        binding.tilPassword.error = null
        binding.tilConfirmPassword.error = null
    }

    /** Navigate to MainActivity and close AuthActivity so the Back button doesn't return to the login screen. */
    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java)
        // Clear entire old back stack, start a new task
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}

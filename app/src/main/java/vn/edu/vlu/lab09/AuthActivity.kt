package vn.edu.vlu.lab09

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import vn.edu.vlu.lab09.databinding.ActivityAuthBinding
import vn.edu.vlu.lab09.databinding.DialogForgotPasswordBinding

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private lateinit var auth: FirebaseAuth

    private var isLoginMode: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth instance
        auth = FirebaseAuth.getInstance()

        // Auto-Login Check: If already authenticated, route directly to MainActivity
        if (auth.currentUser != null) {
            navigateToMain()
            return
        }

        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        // Default to login mode
        updateMode(isLogin = true)

        // Clear error highlights as user types
        binding.etEmail.doAfterTextChanged { binding.tilEmail.error = null }
        binding.etPassword.doAfterTextChanged { binding.tilPassword.error = null }
        binding.etConfirmPassword.doAfterTextChanged { binding.tilConfirmPassword.error = null }
    }

    private fun setupListeners() {
        // Toggle between Login and Register modes
        binding.toggleGroupAuth.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnToggleLogin -> updateMode(isLogin = true)
                    R.id.btnToggleRegister -> updateMode(isLogin = false)
                }
            }
        }

        // Action submit button
        binding.btnSubmit.setOnClickListener {
            if (validateInputs()) {
                val email = binding.etEmail.text?.toString()?.trim().orEmpty()
                val password = binding.etPassword.text?.toString()?.trim().orEmpty()

                if (isLoginMode) {
                    performLogin(email, password)
                } else {
                    performRegistration(email, password)
                }
            }
        }

        // Challenge 1 (+7.5%): Forgot Password action
        binding.btnForgotPassword.setOnClickListener {
            showForgotPasswordDialog()
        }
    }

    private fun updateMode(isLogin: Boolean) {
        isLoginMode = isLogin
        clearErrors()

        if (isLogin) {
            binding.tilConfirmPassword.visibility = View.GONE
            binding.btnForgotPassword.visibility = View.VISIBLE
            binding.btnSubmit.setText(R.string.login)
        } else {
            binding.tilConfirmPassword.visibility = View.VISIBLE
            binding.btnForgotPassword.visibility = View.GONE
            binding.btnSubmit.setText(R.string.register)
        }
    }

    /**
     * Client-side validation:
     * - Email matches Patterns.EMAIL_ADDRESS
     * - Password length >= 6
     * - In registration mode: confirmPassword == password
     */
    private fun validateInputs(): Boolean {
        var isValid = true
        val email = binding.etEmail.text?.toString()?.trim().orEmpty()
        val password = binding.etPassword.text?.toString()?.trim().orEmpty()
        val confirmPassword = binding.etConfirmPassword.text?.toString()?.trim().orEmpty()

        if (email.isEmpty()) {
            binding.tilEmail.error = getString(R.string.error_empty_email)
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = getString(R.string.error_invalid_email)
            isValid = false
        } else {
            binding.tilEmail.error = null
        }

        if (password.isEmpty() || password.length < 6) {
            binding.tilPassword.error = getString(R.string.error_short_password)
            isValid = false
        } else {
            binding.tilPassword.error = null
        }

        if (!isLoginMode) {
            if (confirmPassword != password) {
                binding.tilConfirmPassword.error = getString(R.string.error_password_not_match)
                isValid = false
            } else {
                binding.tilConfirmPassword.error = null
            }
        }

        return isValid
    }

    /**
     * Firebase Email/Password Sign-In
     */
    private fun performLogin(email: String, pass: String) {
        setLoadingState(true)

        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener(this) { task ->
                setLoadingState(false)
                if (task.isSuccessful) {
                    Toast.makeText(this, R.string.login_success, Toast.LENGTH_SHORT).show()
                    navigateToMain()
                } else {
                    handleFirebaseException(task.exception)
                }
            }
    }

    /**
     * Firebase Email/Password Registration
     */
    private fun performRegistration(email: String, pass: String) {
        setLoadingState(true)

        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener(this) { task ->
                setLoadingState(false)
                if (task.isSuccessful) {
                    Toast.makeText(this, R.string.account_created_success, Toast.LENGTH_SHORT).show()
                    navigateToMain()
                } else {
                    handleFirebaseException(task.exception)
                }
            }
    }

    /**
     * Robust Error Handling for Firebase Exceptions
     */
    private fun handleFirebaseException(exception: Exception?) {
        val errorMessage = when (exception) {
            is FirebaseAuthWeakPasswordException -> {
                binding.tilPassword.error = getString(R.string.error_weak_password)
                getString(R.string.error_weak_password)
            }
            is FirebaseAuthUserCollisionException -> {
                binding.tilEmail.error = getString(R.string.error_email_collision)
                getString(R.string.error_email_collision)
            }
            is FirebaseAuthInvalidCredentialsException -> {
                getString(R.string.error_invalid_credentials)
            }
            is FirebaseAuthInvalidUserException -> {
                getString(R.string.error_invalid_credentials)
            }
            is FirebaseNetworkException -> {
                getString(R.string.error_network)
            }
            else -> {
                exception?.localizedMessage ?: "Đã có lỗi xảy ra. Vui lòng thử lại!"
            }
        }

        Snackbar.make(binding.root, errorMessage, Snackbar.LENGTH_LONG).show()
    }

    /**
     * Challenge 1 (+7.5% Bonus):
     * Dialog for requesting password reset via auth.sendPasswordResetEmail(email)
     */
    private fun showForgotPasswordDialog() {
        val dialogBinding = DialogForgotPasswordBinding.inflate(layoutInflater)

        // Pre-fill with entered email if present
        val currentEmail = binding.etEmail.text?.toString()?.trim().orEmpty()
        if (currentEmail.isNotEmpty()) {
            dialogBinding.etResetEmail.setText(currentEmail)
        }

        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.send_reset_link, null)
            .setNegativeButton(R.string.cancel) { d, _ -> d.dismiss() }
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val resetEmail = dialogBinding.etResetEmail.text?.toString()?.trim().orEmpty()

                if (resetEmail.isEmpty()) {
                    dialogBinding.tilResetEmail.error = getString(R.string.error_empty_email)
                    return@setOnClickListener
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(resetEmail).matches()) {
                    dialogBinding.tilResetEmail.error = getString(R.string.error_invalid_email)
                    return@setOnClickListener
                }

                dialogBinding.tilResetEmail.error = null
                dialog.dismiss()
                sendPasswordReset(resetEmail)
            }
        }

        dialog.show()
    }

    private fun sendPasswordReset(email: String) {
        setLoadingState(true)
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener(this) { task ->
                setLoadingState(false)
                if (task.isSuccessful) {
                    Snackbar.make(binding.root, R.string.reset_email_sent, Snackbar.LENGTH_LONG).show()
                } else {
                    handleFirebaseException(task.exception)
                }
            }
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSubmit.isEnabled = !isLoading
        binding.btnToggleLogin.isEnabled = !isLoading
        binding.btnToggleRegister.isEnabled = !isLoading
        binding.btnForgotPassword.isEnabled = !isLoading
    }

    private fun clearErrors() {
        binding.tilEmail.error = null
        binding.tilPassword.error = null
        binding.tilConfirmPassword.error = null
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}

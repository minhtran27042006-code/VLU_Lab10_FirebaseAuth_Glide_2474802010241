package vn.edu.vlu.lab10

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.firebase.auth.FirebaseAuth
import vn.edu.vlu.lab10.databinding.ActivityMainBinding

/**
 * MainActivity: home page displaying image list (Glide) and Logout button.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Protection: if not logged in (session expired/logged out), return to Auth screen
        val user = auth.currentUser
        if (user == null) {
            navigateToAuth()
            return
        }

        // Display current user email
        binding.tvWelcome.text = "Hello, ${user.email}"

        setupRecyclerView()
        setupLogoutButton()

        // Challenge 2 (+7.5%): Circular Avatar using Glide circleCrop()
        loadAvatar(user.email)
    }

    /** Configure RecyclerView: vertical LayoutManager + Adapter with 10 mock photos. */
    private fun setupRecyclerView() {
        val photos = PhotoDataSource.getPhotos()
        binding.rvPhotos.layoutManager = LinearLayoutManager(this)
        binding.rvPhotos.adapter = PhotoAdapter(photos)
        // RecyclerView size doesn't change based on content -> optimize layout measurement performance
        binding.rvPhotos.setHasFixedSize(true)
    }

    /** Handle Logout button: clear Firebase session then return to AuthActivity. */
    private fun setupLogoutButton() {
        binding.btnLogout.setOnClickListener {
            auth.signOut() // Clear login session and local token
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
            navigateToAuth()
        }
    }

    /** Navigate to AuthActivity and clear the entire back stack so users can't Back into the home page. */
    private fun navigateToAuth() {
        val intent = Intent(this, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    /**
     * Challenge 2: Load avatar from URL and crop it into a circle using circleCrop().
     */
    private fun loadAvatar(email: String?) {
        // Use email as a "seed" so each account gets a stable, unique avatar
        val seed = email ?: "guest"
        val avatarUrl = "https://i.pravatar.cc/200?u=$seed"

        Glide.with(this)
            .load(avatarUrl)
            .placeholder(R.drawable.ic_placeholder)
            .error(R.drawable.ic_broken_image)
            .circleCrop() // Transform the image into a circle
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(binding.ivAvatar)
    }
}

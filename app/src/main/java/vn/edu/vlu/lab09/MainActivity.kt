package vn.edu.vlu.lab09

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import vn.edu.vlu.lab09.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Verify active authentication session
        val currentUser = auth.currentUser
        if (currentUser == null) {
            navigateToAuth()
            return
        }

        setupUserHeader(currentUser.email)
        setupRecyclerView()
        setupListeners()
    }

    /**
     * Challenge 2 (+7.5%): Display dynamic circular user avatar in the header
     * using Glide's circleCrop() transformation based on user email.
     */
    private fun setupUserHeader(email: String?) {
        val displayEmail = email ?: "user@vanlanguni.vn"
        binding.tvUserEmail.text = displayEmail

        // Dynamic avatar generation based on the registered email address
        val avatarUrl = "https://ui-avatars.com/api/?name=${Uri.encode(displayEmail)}&background=312E81&color=FFFFFF&size=256&bold=true"

        Glide.with(this)
            .load(avatarUrl)
            .circleCrop()
            .placeholder(R.drawable.ic_avatar_placeholder)
            .error(R.drawable.ic_avatar_placeholder)
            .into(binding.ivUserAvatar)
    }

    /**
     * Initialize RecyclerView with PhotoAdapter and at least 10 high-resolution HTTPS URLs.
     */
    private fun setupRecyclerView() {
        val photos = getSamplePhotos()
        val adapter = PhotoAdapter(photos)

        binding.rvPhotos.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            setHasFixedSize(true)
            this.adapter = adapter
        }
    }

    private fun setupListeners() {
        // Logout button: auth.signOut() and clear activity back stack
        binding.btnLogout.setOnClickListener {
            performSignOut()
        }
    }

    /**
     * Sign out action: Revoke Firebase session, clear activity back stack,
     * and route user back to AuthActivity.
     */
    private fun performSignOut() {
        auth.signOut()
        Toast.makeText(this, "Đã đăng xuất tài khoản", Toast.LENGTH_SHORT).show()
        navigateToAuth()
    }

    private fun navigateToAuth() {
        val intent = Intent(this, AuthActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    /**
     * Populates at least 10 high-resolution HTTPS image URLs.
     */
    private fun getSamplePhotos(): List<PhotoItem> {
        return listOf(
            PhotoItem(
                id = 1,
                title = "Forest Path & Serene Lake",
                author = "Paul Jarvis",
                imageUrl = "https://picsum.photos/id/10/800/600"
            ),
            PhotoItem(
                id = 2,
                title = "Study Desk with Notes & Coffee",
                author = "Aleks Dorohovich",
                imageUrl = "https://picsum.photos/id/20/800/600"
            ),
            PhotoItem(
                id = 3,
                title = "Misty Mountain Ridge & Pine Trees",
                author = "Jerry Adney",
                imageUrl = "https://picsum.photos/id/28/800/600"
            ),
            PhotoItem(
                id = 4,
                title = "Abandoned Railway in Countryside",
                author = "Gofore",
                imageUrl = "https://picsum.photos/id/29/800/600"
            ),
            PhotoItem(
                id = 5,
                title = "Morning Workstation & Latte",
                author = "Bartek Szewczyk",
                imageUrl = "https://picsum.photos/id/42/800/600"
            ),
            PhotoItem(
                id = 6,
                title = "Vintage Laptop on Wooden Desk",
                author = "Luke Chesser",
                imageUrl = "https://picsum.photos/id/48/800/600"
            ),
            PhotoItem(
                id = 7,
                title = "Architectural Geometric Facade",
                author = "Nicholas Swanson",
                imageUrl = "https://picsum.photos/id/54/800/600"
            ),
            PhotoItem(
                id = 8,
                title = "Portrait in Golden Hour Sunlight",
                author = "Alexander Shustov",
                imageUrl = "https://picsum.photos/id/64/800/600"
            ),
            PhotoItem(
                id = 9,
                title = "Coastal Waves Crashing on Rocks",
                author = "Clem Onojeghuo",
                imageUrl = "https://picsum.photos/id/76/800/600"
            ),
            PhotoItem(
                id = 10,
                title = "Lonely Pine Tree on Green Meadow",
                author = "Johnny Goerend",
                imageUrl = "https://picsum.photos/id/84/800/600"
            ),
            PhotoItem(
                id = 11,
                title = "Golden Sunset Reflections on Lake",
                author = "Kenneth Thewissen",
                imageUrl = "https://picsum.photos/id/110/800/600"
            ),
            PhotoItem(
                id = 12,
                title = "Macro Dewdrops on Woodland Moss",
                author = "Anton Repponen",
                imageUrl = "https://picsum.photos/id/116/800/600"
            )
        )
    }
}

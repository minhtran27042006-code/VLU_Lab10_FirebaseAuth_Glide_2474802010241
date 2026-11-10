package vn.edu.vlu.lab09

/**
 * Data model representing a photo item in the gallery.
 *
 * @param id Unique identifier of the photo
 * @param title Descriptive title of the photograph
 * @param author Name of the photographer / creator
 * @param imageUrl High-resolution HTTPS image source URL
 */
data class PhotoItem(
    val id: Int,
    val title: String,
    val author: String,
    val imageUrl: String
)

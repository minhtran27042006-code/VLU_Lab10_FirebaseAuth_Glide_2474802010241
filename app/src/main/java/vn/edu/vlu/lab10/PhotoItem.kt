package vn.edu.vlu.lab10

/**
 * PhotoItem: data model for an image in the list.
 * data class auto-generates equals(), hashCode(), toString(), copy().
 */
data class PhotoItem(
    val id: Int, // Image ID
    val title: String, // Display title
    val author: String, // Author name
    val imageUrl: String // Image path (REQUIRES https:// protocol)
)

/**
 * PhotoDataSource: mock data source consisting of 10 high-quality images from Picsum Photos.
 * URL Format: https://picsum.photos/id/{id}/{width}/{height}
 * Use object (singleton) to directly call PhotoDataSource.getPhotos().
 */
object PhotoDataSource {
    fun getPhotos(): List<PhotoItem> = listOf(
        PhotoItem(1, "Sunrise on the mountain", "Picsum Studio", "https://picsum.photos/id/10/800/600"),
        PhotoItem(2, "Foggy pine forest", "Picsum Studio", "https://picsum.photos/id/11/800/600"),
        PhotoItem(3, "Sunset beach", "Picsum Studio", "https://picsum.photos/id/12/800/600"),
        PhotoItem(4, "Autumn road", "Picsum Studio", "https://picsum.photos/id/14/800/600"),
        PhotoItem(5, "Peaceful lake", "Picsum Studio", "https://picsum.photos/id/15/800/600"),
        PhotoItem(6, "Deep blue ocean", "Picsum Studio", "https://picsum.photos/id/16/800/600"),
        PhotoItem(7, "Spring valley", "Picsum Studio", "https://picsum.photos/id/17/800/600"),
        PhotoItem(8, "Coastal cliff", "Picsum Studio", "https://picsum.photos/id/18/800/600"),
        PhotoItem(9, "Ripe rice field", "Picsum Studio", "https://picsum.photos/id/19/800/600"),
        PhotoItem(10, "Waterfall in the forest", "Picsum Studio", "https://picsum.photos/id/20/800/600")
    )
}

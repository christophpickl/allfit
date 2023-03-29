package allfit.presentation

import javafx.scene.image.Image

object StaticImageStorage {

    private val images = StaticImage.values().map {
        it to ImageReader.readFromClasspath("/images/${it.filename}", 30.0)
    }.toMap()

    fun get(image: StaticImage) = images[image] ?: error("Invalid image: $image")
}

enum class StaticImage(val filename: String) {
    FavoriteFull("favorite_full.png"),
    FavoriteOutline("favorite_outline.png"),
    WishlistFull("wishlist_full.png"),
    WishlistOutline("wishlist_outline.png"),
    Reserved("reserved.png"),
}

object ImageReader {

//    fun readFromBytes(bytes: ByteArray): Image =
//        Image(ByteArrayInputStream(bytes))
//
//    fun readFromFileWithSize(file: File, size: Dimension): Image =
//        Image(file.toURI().toString(), size.width.toDouble(), size.height.toDouble(), false, false)

    fun readFromClasspath(path: String, width: Double = 0.0): Image =
        Image(
            ImageReader::class.java.getResource(path)?.openStream()
                ?: error("Could not read image from classpath: $path"),
            width, 0.0, true, false
        )
}

//fun Image.scale(dimension: Dimension): Image {
//    val swingImage = SwingFXUtils.fromFXImage(this, null)
//    val scaledImage = swingImage.getScaledInstance(dimension.width, dimension.height, java.awt.Image.SCALE_DEFAULT)
//
//    val bufferedImage = BufferedImage(scaledImage.getWidth(null), scaledImage.getHeight(null), BufferedImage.TYPE_INT_ARGB)
//    val bufferedImageGraphics = bufferedImage.createGraphics()
//    bufferedImageGraphics.drawImage(scaledImage, 0, 0, null)
//    bufferedImageGraphics.dispose()
//
//    return SwingFXUtils.toFXImage(bufferedImage, null)
//}
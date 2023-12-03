package allfit.presentation.logic

import javafx.scene.image.Image

object StaticIconStorage {

    private const val WIDTH = 30.0

    private val images = StaticIcon.entries.associateWith {
        load(it)
    }

    private fun load(image: StaticIcon) =
        ImageReader.readFromClasspath("/images/icon/${image.filename}", WIDTH)

    fun get(image: StaticIcon) = images[image] ?: error("Invalid image: $image")

    fun forceReload(image: StaticIcon) = load(image)
}

enum class StaticIcon(val filename: String) {
    FavoriteFull("favorite_full.png"),
    FavoriteOutline("favorite_outline.png"),
    WishlistFull("wishlist_full.png"),
    WishlistOutline("wishlist_outline.png"),
    Reserved("reserved.png"),
    ReservedNot("reserved_not.png"),
    Visited("visited.png"),
    Hidden("hidden.png"),
    Empty("empty.png"), // 1x1 transparent pixel image
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
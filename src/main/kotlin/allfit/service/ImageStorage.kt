package allfit.service

import java.io.File

class ImageStorage(
    private val baseFolder: File
) {
    init {
        require(baseFolder.exists())
        require(baseFolder.isDirectory)
    }

    fun save() {

    }

    fun load() {

    }
}
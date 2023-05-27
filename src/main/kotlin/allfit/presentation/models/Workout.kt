package allfit.presentation.models

import allfit.service.Images
import allfit.service.ensureMaxLength
import java.time.ZonedDateTime
import javafx.beans.property.ObjectProperty
import javafx.scene.image.Image
import tornadofx.getProperty
import tornadofx.property

interface Workout {
    val id: Int
    fun idProperty(): ObjectProperty<Int>

    val name: String
    fun nameProperty(): ObjectProperty<String>

    val url: String
    fun urlProperty(): ObjectProperty<String>

    val about: String
    fun aboutProperty(): ObjectProperty<String>

    val specifics: String
    fun specificsProperty(): ObjectProperty<String>

    val date: DateRange
    fun dateProperty(): ObjectProperty<DateRange>

    val address: String
    fun addressProperty(): ObjectProperty<String>

    val image: Image
    fun imageProperty(): ObjectProperty<Image>

    val isReserved: Boolean
    fun isReservedProperty(): ObjectProperty<Boolean>

    val partnerId: Int
}

class SimpleWorkout(
    id: Int,
    override val partnerId: Int,
    name: String,
    about: String,
    specifics: String,
    address: String,
    date: DateRange,
    image: Image,
    url: String,
    isReserved: Boolean,
) : Workout {
    override var id: Int by property(id)
    override fun idProperty() = getProperty(SimpleWorkout::id)
    override var name: String by property(name)
    override fun nameProperty() = getProperty(SimpleWorkout::name)
    override var about: String by property(about)
    override fun aboutProperty() = getProperty(SimpleWorkout::about)
    override var specifics: String by property(specifics)
    override fun specificsProperty() = getProperty(SimpleWorkout::specifics)
    override var address: String by property(address)
    override fun addressProperty() = getProperty(SimpleWorkout::address)
    override var url: String by property(url)
    override fun urlProperty() = getProperty(SimpleWorkout::url)
    override var date: DateRange by property(date)
    override fun dateProperty() = getProperty(SimpleWorkout::date)
    override var image: Image by property(image)
    override fun imageProperty() = getProperty(SimpleWorkout::image)
    override var isReserved: Boolean by property(isReserved)
    override fun isReservedProperty() = getProperty(SimpleWorkout::isReserved)

    init {
        require(id >= 0) { "Invalid ID: $id" }
        require(partnerId >= 0) { "Invalid partner ID: $id" }
    }

    override fun toString() =
        "SimpleWorkout[id=$id, partnerId=$partnerId, name=$name, isReserved=$isReserved, date=$date, url=$url, " +
                "address=$address, about=${about.ensureMaxLength(10)}, " +
                "specifics=${specifics.ensureMaxLength(10)}, image]"

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (this === other) return true
        if (other !is SimpleWorkout) return false
        return id == other.id &&
                partnerId == other.partnerId &&
                name == other.name &&
                about == other.about &&
                specifics == other.specifics &&
                isReserved == other.isReserved &&
                url == other.url &&
                address == other.address &&
                date == other.date &&
                image == other.image
    }

    override fun hashCode() = id.hashCode()
}

data class FullWorkout(
    val simpleWorkout: SimpleWorkout,
    val partner: SimplePartner,
) : Workout by simpleWorkout {
    companion object {
        val prototype = FullWorkout(
            simpleWorkout = SimpleWorkout(
                id = 0,
                partnerId = 0,
                name = "Workout",
                about = "About",
                specifics = "Specifics",
                address = "Address",
                date = DateRange(start = ZonedDateTime.now(), end = ZonedDateTime.now()),
                image = Images.prototype,
                url = "",
                isReserved = false
            ),
            partner = FullPartner.prototype.simplePartner
        )
    }
}

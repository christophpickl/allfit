package allfit.presentation.models

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
}

class SimpleWorkout(
    id: Int,
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
}

data class FullWorkout(
    val simpleWorkout: SimpleWorkout,
    val partner: SimplePartner,
) : Workout by simpleWorkout

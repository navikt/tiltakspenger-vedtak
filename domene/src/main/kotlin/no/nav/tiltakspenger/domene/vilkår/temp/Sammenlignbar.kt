package no.nav.tiltakspenger.domene.vilkår.temp

interface Sammenlignbar<T> {
    fun sammenlignbareFelter(): Set<*>

    fun erLik(annen: Sammenlignbar<T>): Boolean =
        this.sammenlignbareFelter() == annen.sammenlignbareFelter()
}

class SammenlignbarWrapper<T>(val value: T) : Sammenlignbar<T> {
    override fun sammenlignbareFelter(): Set<*> = setOf(value)
}

fun String.wrap() = SammenlignbarWrapper(this)
fun Long.wrap() = SammenlignbarWrapper(this)
fun Int.wrap() = SammenlignbarWrapper(this)
fun Double.wrap() = SammenlignbarWrapper(this)

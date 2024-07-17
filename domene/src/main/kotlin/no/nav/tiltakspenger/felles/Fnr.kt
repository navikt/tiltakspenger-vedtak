package no.nav.tiltakspenger.felles

/**
 * Kommentar jah: Denne kan vel flyttes til libs-common eller noe lignende?
 */
data class Fnr(private val fnr: String) {

    private val fnrPattern = Regex("[0-9]{11}")

    init {
        validate(fnr)
    }

    override fun toString(): String = "***********"

    private fun validate(fnr: String) {
        if (!fnr.matches(fnrPattern)) throw UgyldigFnrException(fnr)
    }

    companion object {
        /**
         * @return null hvis fnr er ugyldig. Regel: [fnrPattern]
         */
        @Suppress("unused")
        fun tryCreate(fnr: String): Fnr? {
            return try {
                Fnr(fnr)
            } catch (e: UgyldigFnrException) {
                null
            }
        }
    }
}

class UgyldigFnrException(@Suppress("unused") val unparsed: String) : RuntimeException("Ugyldig fnr.")

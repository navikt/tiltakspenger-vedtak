package no.nav.tiltakspenger.felles

import com.github.guepardoapps.kulid.ULID

interface Ulid {

    fun prefixPart(): String
    fun ulidPart(): String
    override fun toString(): String
}

data class UlidBase(private val stringValue: String) : Ulid {

    companion object {
        fun random(prefix: String): UlidBase {
            require(prefix.isNotEmpty()) { "Prefiks er tom" }
            return UlidBase("${prefix}_${ULID.random()}")
        }

        fun fromDb(stringValue: String) = UlidBase(stringValue)
    }

    init {
        require(stringValue.contains("_")) { "Ikke gyldig Id, skal bestå av to deler skilt med _" }
        require(stringValue.split("_").size == 2) { "Ikke gyldig Id, skal bestå av prefiks + ulid" }
        require(stringValue.split("_").first().isNotEmpty()) { "Ikke gyldig Id, prefiks er tom" }
        require(ULID.isValid(stringValue.split("_").last())) { "Ikke gyldig Id, ulid er ugyldig" }
    }

    override fun prefixPart(): String = stringValue.split("_").first()
    override fun ulidPart(): String = ULID.fromString(stringValue.split("_").last())
    override fun toString() = stringValue
}

data class InnsendingId private constructor(private val ulid: UlidBase) : Ulid by ulid {
    companion object {
        private const val PREFIX = "ins"
        fun random() = InnsendingId(ulid = UlidBase("${PREFIX}_${ULID.random()}"))

        fun fromDb(stringValue: String) = InnsendingId(ulid = UlidBase(stringValue))
    }
}

data class SøknadId private constructor(private val ulid: UlidBase) : Ulid by ulid {
    companion object {
        private const val PREFIX = "soknad"
        fun random() = SøknadId(ulid = UlidBase("${PREFIX}_${ULID.random()}"))

        fun fromDb(stringValue: String) = SøknadId(ulid = UlidBase(stringValue))
    }
}

data class SøkerId private constructor(private val ulid: UlidBase) : Ulid by ulid {
    companion object {
        private const val PREFIX = "soker"
        fun random() = SøkerId(ulid = UlidBase("${PREFIX}_${ULID.random()}"))

        fun fromDb(stringValue: String) = SøkerId(ulid = UlidBase(stringValue))
    }
}

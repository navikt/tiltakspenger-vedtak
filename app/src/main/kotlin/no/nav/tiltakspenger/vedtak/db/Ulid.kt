package no.nav.tiltakspenger.vedtak.db

import com.github.guepardoapps.kulid.ULID

@JvmInline
value class Ulid private constructor(private val stringValue: String) {
    companion object {
        fun new(prefix: String): Ulid {
            require(prefix.isNotEmpty()) { "Prefiks er tom" }
            return Ulid("${prefix}_${ULID.random()}")
        }

        fun fromDb(stringValue: String): Ulid {
            require(stringValue.contains("_")) { "Ikke gyldig Id, skal bestå av to deler skilt med _" }
            require(stringValue.split("_").size == 2) { "Ikke gyldig Id, skal bestå av prefiks + ulid" }
            require(stringValue.split("_").first().isNotEmpty()) { "Ikke gyldig Id, prefiks er tom" }
            require(ULID.isValid(stringValue.split("_").last())) { "Ikke gyldig Id, ulid er ugyldig" }
            return Ulid(stringValue)
        }
    }

    fun prefixPart(): String = stringValue.split("_").first()
    fun ulidPart(): String = ULID.fromString(stringValue.split("_").last())
    fun asString() = stringValue
}

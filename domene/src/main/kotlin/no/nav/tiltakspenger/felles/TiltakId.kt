package no.nav.tiltakspenger.felles

import no.nav.tiltakspenger.libs.common.Ulid
import no.nav.tiltakspenger.libs.common.UlidBase
import no.nav.tiltakspenger.libs.common.uuidToUlid
import ulid.ULID
import java.util.UUID

data class TiltakId private constructor(private val ulid: UlidBase) : Ulid by ulid {
    companion object {
        private const val PREFIX = "takt"
        fun random() = TiltakId(ulid = UlidBase("${PREFIX}_${ULID.randomULID()}"))

        fun fromDb(stringValue: String) = TiltakId(ulid = UlidBase(stringValue))

        fun fromString(stringValue: String) = TiltakId(ulid = UlidBase(stringValue))

        fun fromUUID(uuid: UUID) = TiltakId(ulid = UlidBase("${PREFIX}_${uuidToUlid(uuid)}"))
    }
}

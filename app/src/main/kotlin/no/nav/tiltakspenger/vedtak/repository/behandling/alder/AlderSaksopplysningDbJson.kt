package no.nav.tiltakspenger.vedtak.repository.behandling.alder

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.alder.AlderSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.ÅrsakTilEndring
import no.nav.tiltakspenger.vedtak.repository.felles.SaksbehandlerDbJson
import no.nav.tiltakspenger.vedtak.repository.felles.toDbJson
import java.time.LocalDate
import java.time.LocalDateTime

internal data class AlderSaksopplysningDbJson(
    val fødselsdato: LocalDate,
    val årsakTilEndring: ÅrsakTilEndringDbJson?,
    val saksbehandler: SaksbehandlerDbJson?,
    val tidsstempel: String,
) {
    fun toDomain(): AlderSaksopplysning {
        return when {
            saksbehandler != null -> AlderSaksopplysning.Saksbehandler(
                fødselsdato = fødselsdato,
                årsakTilEndring = årsakTilEndring!!.toDomain(),
                saksbehandler = saksbehandler.toDomain(),
                tidsstempel = LocalDateTime.parse(tidsstempel),
            )

            else -> {
                require(årsakTilEndring == null) { "Støtter ikke årsak til endring for AlderSaksopplysning.Personopplysning." }
                AlderSaksopplysning.Personopplysning(
                    fødselsdato = fødselsdato,
                    tidsstempel = LocalDateTime.parse(tidsstempel),
                )
            }
        }
    }

    enum class ÅrsakTilEndringDbJson {
        FEIL_I_INNHENTET_DATA,
        ENDRING_ETTER_SØKNADSTIDSPUNKT,
        ;

        fun toDomain(): ÅrsakTilEndring {
            return when (this) {
                FEIL_I_INNHENTET_DATA -> ÅrsakTilEndring.FEIL_I_INNHENTET_DATA
                ENDRING_ETTER_SØKNADSTIDSPUNKT -> ÅrsakTilEndring.ENDRING_ETTER_SØKNADSTIDSPUNKT
            }
        }
    }
}

internal fun AlderSaksopplysning.toDbJson(): AlderSaksopplysningDbJson {
    return AlderSaksopplysningDbJson(
        fødselsdato = fødselsdato,
        årsakTilEndring = when (årsakTilEndring) {
            ÅrsakTilEndring.FEIL_I_INNHENTET_DATA -> AlderSaksopplysningDbJson.ÅrsakTilEndringDbJson.FEIL_I_INNHENTET_DATA
            ÅrsakTilEndring.ENDRING_ETTER_SØKNADSTIDSPUNKT -> AlderSaksopplysningDbJson.ÅrsakTilEndringDbJson.ENDRING_ETTER_SØKNADSTIDSPUNKT
            null -> null
        },
        saksbehandler = saksbehandler?.toDbJson(),
        tidsstempel = tidsstempel.toString(),
    )
}

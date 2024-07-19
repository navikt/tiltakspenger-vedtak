package no.nav.tiltakspenger.vedtak.repository.behandling.alder

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.alder.AlderSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.Deltagelse
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.ÅrsakTilEndring
import no.nav.tiltakspenger.vedtak.repository.felles.PeriodeDbJson
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
                require(årsakTilEndring == null) { "Støtter ikke årsak til endring for AlderSaksopplysning.Søknad." }
                AlderSaksopplysning.Søknad(
                    fødselsdato = fødselsdato,
                    tidsstempel = LocalDateTime.parse(tidsstempel),
                )
            }
        }
    }

    data class PeriodiseringAvDeltagelseDbJson(
        val periode: PeriodeDbJson,
        val deltar: DeltagelseDbJson,
    )

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

    enum class DeltagelseDbJson {
        ALDER_OK,
        FOR_UNG,
        ;

        fun toDomain(): Deltagelse {
            return when (this) {
                ALDER_OK -> Deltagelse.DELTAR
                FOR_UNG -> Deltagelse.DELTAR_IKKE
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

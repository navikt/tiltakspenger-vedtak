package no.nav.tiltakspenger.vedtak.repository.behandling.kravdato

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.ÅrsakTilEndring
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.kravdato.KravdatoSaksopplysning
import no.nav.tiltakspenger.vedtak.repository.felles.SaksbehandlerDbJson
import no.nav.tiltakspenger.vedtak.repository.felles.toDbJson
import java.time.LocalDateTime

internal data class KravdatoSaksopplysningDbJson(
    val kravdato: LocalDateTime,
    val årsakTilEndring: ÅrsakTilEndringDbJson?,
    val saksbehandler: SaksbehandlerDbJson?,
    val tidsstempel: String,
) {
    fun toDomain(): KravdatoSaksopplysning {
        return when {
            saksbehandler != null -> KravdatoSaksopplysning.Saksbehandler(
                kravdato = kravdato,
                årsakTilEndring = årsakTilEndring!!.toDomain(),
                saksbehandler = saksbehandler.toDomain(),
                tidsstempel = LocalDateTime.parse(tidsstempel),
            )

            else -> {
                require(årsakTilEndring == null) { "Støtter ikke årsak til endring for KravdatoSaksopplysning.Søknad." }
                KravdatoSaksopplysning.Søknad(
                    kravdato = kravdato,
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

internal fun KravdatoSaksopplysning.toDbJson(): KravdatoSaksopplysningDbJson {
    return KravdatoSaksopplysningDbJson(
        kravdato = kravdato,
        årsakTilEndring = when (årsakTilEndring) {
            ÅrsakTilEndring.FEIL_I_INNHENTET_DATA -> KravdatoSaksopplysningDbJson.ÅrsakTilEndringDbJson.FEIL_I_INNHENTET_DATA
            ÅrsakTilEndring.ENDRING_ETTER_SØKNADSTIDSPUNKT -> KravdatoSaksopplysningDbJson.ÅrsakTilEndringDbJson.ENDRING_ETTER_SØKNADSTIDSPUNKT
            null -> null
        },
        saksbehandler = saksbehandler?.toDbJson(),
        tidsstempel = tidsstempel.toString(),
    )
}

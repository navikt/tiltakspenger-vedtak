package no.nav.tiltakspenger.vedtak.repository.behandling.kravfrist

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.ÅrsakTilEndring
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.kravfrist.KravfristSaksopplysning
import no.nav.tiltakspenger.vedtak.repository.behandling.kravfrist.KravfristSaksopplysningDbJson.ÅrsakTilEndringDbJson
import no.nav.tiltakspenger.vedtak.repository.felles.SaksbehandlerDbJson
import no.nav.tiltakspenger.vedtak.repository.felles.toDbJson
import java.time.LocalDateTime

internal data class KravfristSaksopplysningDbJson(
    val kravdato: LocalDateTime,
    val årsakTilEndring: ÅrsakTilEndringDbJson?,
    val saksbehandler: SaksbehandlerDbJson?,
    val tidsstempel: String,
) {
    fun toDomain(): KravfristSaksopplysning =
        when {
            saksbehandler != null ->
                KravfristSaksopplysning.Saksbehandler(
                    kravdato = kravdato,
                    årsakTilEndring = årsakTilEndring!!.toDomain(),
                    saksbehandler = saksbehandler.toDomain(),
                    tidsstempel = LocalDateTime.parse(tidsstempel),
                )

            else -> {
                require(årsakTilEndring == null) { "Støtter ikke årsak til endring for KravfristSaksopplysning.Søknad." }
                KravfristSaksopplysning.Søknad(
                    kravdato = kravdato,
                    tidsstempel = LocalDateTime.parse(tidsstempel),
                )
            }
        }

    enum class ÅrsakTilEndringDbJson {
        FEIL_I_INNHENTET_DATA,
        ENDRING_ETTER_SØKNADSTIDSPUNKT,
        ;

        fun toDomain(): ÅrsakTilEndring =
            when (this) {
                FEIL_I_INNHENTET_DATA -> ÅrsakTilEndring.FEIL_I_INNHENTET_DATA
                ENDRING_ETTER_SØKNADSTIDSPUNKT -> ÅrsakTilEndring.ENDRING_ETTER_SØKNADSTIDSPUNKT
            }
    }
}

internal fun KravfristSaksopplysning.toDbJson(): KravfristSaksopplysningDbJson =
    KravfristSaksopplysningDbJson(
        kravdato = kravdato,
        årsakTilEndring =
        when (årsakTilEndring) {
            ÅrsakTilEndring.FEIL_I_INNHENTET_DATA -> ÅrsakTilEndringDbJson.FEIL_I_INNHENTET_DATA
            ÅrsakTilEndring.ENDRING_ETTER_SØKNADSTIDSPUNKT -> ÅrsakTilEndringDbJson.ENDRING_ETTER_SØKNADSTIDSPUNKT
            null -> null
        },
        saksbehandler = saksbehandler?.toDbJson(),
        tidsstempel = tidsstempel.toString(),
    )

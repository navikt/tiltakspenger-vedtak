package no.nav.tiltakspenger.vedtak.repository.behandling.livsopphold

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.ÅrsakTilEndring
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LivsoppholdSaksopplysning
import no.nav.tiltakspenger.vedtak.repository.behandling.felles.ÅrsakTilEndringDbType
import no.nav.tiltakspenger.vedtak.repository.felles.PeriodeDbJson
import no.nav.tiltakspenger.vedtak.repository.felles.SaksbehandlerDbJson
import no.nav.tiltakspenger.vedtak.repository.felles.toDbJson
import java.time.LocalDateTime

internal data class LivsoppholdSaksopplysningDbJson(
    val harLivsoppholdYtelser: Boolean,
    val årsakTilEndring: ÅrsakTilEndringDbType?,
    val saksbehandler: SaksbehandlerDbJson?,
    val periode: PeriodeDbJson,
    val tidsstempel: String,
) {
    fun toDomain(): LivsoppholdSaksopplysning =
        when {
            saksbehandler != null -> {
                LivsoppholdSaksopplysning.Saksbehandler(
                    harLivsoppholdYtelser = harLivsoppholdYtelser,
                    årsakTilEndring = årsakTilEndring?.toDomain(),
                    tidsstempel = LocalDateTime.parse(tidsstempel),
                    navIdent = saksbehandler.navIdent,
                    periode = periode.toDomain(),
                )
            }
            else -> {
                LivsoppholdSaksopplysning.Søknad(
                    harLivsoppholdYtelser = harLivsoppholdYtelser,
                    tidsstempel = LocalDateTime.parse(tidsstempel),
                    periode = periode.toDomain(),
                )
            }
        }
}

internal fun LivsoppholdSaksopplysning.toDbJson(): LivsoppholdSaksopplysningDbJson =
    LivsoppholdSaksopplysningDbJson(
        harLivsoppholdYtelser = harLivsoppholdYtelser,
        årsakTilEndring =
        when (årsakTilEndring) {
            ÅrsakTilEndring.ENDRING_ETTER_SØKNADSTIDSPUNKT -> {
                ÅrsakTilEndringDbType.ENDRING_ETTER_SØKNADSTIDSPUNKT
            }

            ÅrsakTilEndring.FEIL_I_INNHENTET_DATA -> {
                ÅrsakTilEndringDbType.FEIL_I_INNHENTET_DATA
            }

            null -> null
        },
        saksbehandler = navIdent?.let { SaksbehandlerDbJson(it) },
        periode = periode.toDbJson(),
        tidsstempel = tidsstempel.toString(),
    )

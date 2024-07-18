package no.nav.tiltakspenger.vedtak.repository.behandling.institusjonsopphold

import no.nav.tiltakspenger.libs.periodisering.PeriodeMedVerdi
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.ÅrsakTilEndring
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.institusjonsopphold.InstitusjonsoppholdSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.institusjonsopphold.Opphold
import no.nav.tiltakspenger.vedtak.repository.felles.PeriodeDbJson
import no.nav.tiltakspenger.vedtak.repository.felles.SaksbehandlerDbJson
import no.nav.tiltakspenger.vedtak.repository.felles.toDbJson
import java.time.LocalDateTime

internal data class InstitusjonsoppholdSaksopplysningDbJson(
    val oppholdForPeriode: List<PeriodiseringAvOppholdDbJson>,
    val årsakTilEndring: ÅrsakTilEndringDbJson?,
    val saksbehandler: SaksbehandlerDbJson?,
    val tidsstempel: String,
) {
    fun toDomain(): InstitusjonsoppholdSaksopplysning {
        return when {
            saksbehandler != null -> InstitusjonsoppholdSaksopplysning.Saksbehandler(
                opphold = Periodisering(
                    oppholdForPeriode.map {
                        PeriodeMedVerdi(
                            periode = it.periode.toDomain(),
                            verdi = it.opphold.toDomain(),
                        )
                    },
                ),
                årsakTilEndring = årsakTilEndring!!.toDomain(),
                saksbehandler = saksbehandler.toDomain(),
                tidsstempel = LocalDateTime.parse(tidsstempel),
            )

            else -> {
                require(årsakTilEndring == null) { "Støtter ikke årsak til endring for InstitusjonsoppholdSaksopplysning.Søknad." }
                InstitusjonsoppholdSaksopplysning.Søknad(
                    opphold = Periodisering(
                        oppholdForPeriode.map {
                            PeriodeMedVerdi(
                                periode = it.periode.toDomain(),
                                verdi = it.opphold.toDomain(),
                            )
                        },
                    ),
                    tidsstempel = LocalDateTime.parse(tidsstempel),
                )
            }
        }
    }

    data class PeriodiseringAvOppholdDbJson(
        val periode: PeriodeDbJson,
        val opphold: OppholdDbJson,
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

    enum class OppholdDbJson {
        OPPHOLD,
        IKKE_OPPHOLD,
        ;

        fun toDomain(): Opphold {
            return when (this) {
                OPPHOLD -> Opphold.OPPHOLD
                IKKE_OPPHOLD -> Opphold.IKKE_OPPHOLD
            }
        }
    }
}

internal fun InstitusjonsoppholdSaksopplysning.toDbJson(): InstitusjonsoppholdSaksopplysningDbJson {
    return InstitusjonsoppholdSaksopplysningDbJson(
        oppholdForPeriode = this.opphold.perioder().map {
            InstitusjonsoppholdSaksopplysningDbJson.PeriodiseringAvOppholdDbJson(
                periode = it.periode.toDbJson(),
                opphold = when (it.verdi) {
                    Opphold.OPPHOLD -> InstitusjonsoppholdSaksopplysningDbJson.OppholdDbJson.OPPHOLD
                    Opphold.IKKE_OPPHOLD -> InstitusjonsoppholdSaksopplysningDbJson.OppholdDbJson.IKKE_OPPHOLD
                },
            )
        },
        årsakTilEndring = when (årsakTilEndring) {
            ÅrsakTilEndring.FEIL_I_INNHENTET_DATA -> InstitusjonsoppholdSaksopplysningDbJson.ÅrsakTilEndringDbJson.FEIL_I_INNHENTET_DATA
            ÅrsakTilEndring.ENDRING_ETTER_SØKNADSTIDSPUNKT -> InstitusjonsoppholdSaksopplysningDbJson.ÅrsakTilEndringDbJson.ENDRING_ETTER_SØKNADSTIDSPUNKT
            null -> null
        },
        saksbehandler = saksbehandler?.toDbJson(),
        tidsstempel = tidsstempel.toString(),
    )
}

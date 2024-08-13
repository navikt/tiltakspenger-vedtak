package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.institusjonsopphold

import no.nav.tiltakspenger.libs.periodisering.PeriodeMedVerdi
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.ÅrsakTilEndring
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.institusjonsopphold.InstitusjonsoppholdSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.institusjonsopphold.Opphold
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.felles.ÅrsakTilEndringDTO

internal data class InstitusjonsoppholdSaksopplysningDTO(
    val periodeMedOpphold: PeriodeMedOppholdDTO,
    val årsakTilEndring: ÅrsakTilEndringDTO?,
    val kilde: KildeDTO,
)

internal fun InstitusjonsoppholdSaksopplysning.toDTO(kilde: KildeDTO): InstitusjonsoppholdSaksopplysningDTO =
    InstitusjonsoppholdSaksopplysningDTO(
        periodeMedOpphold =
        this.opphold
            .perioder()
            .tilEnkelPeriode()
            .toDTO(),
        årsakTilEndring =
        when (årsakTilEndring) {
            ÅrsakTilEndring.FEIL_I_INNHENTET_DATA -> ÅrsakTilEndringDTO.FEIL_I_INNHENTET_DATA
            ÅrsakTilEndring.ENDRING_ETTER_SØKNADSTIDSPUNKT -> ÅrsakTilEndringDTO.ENDRING_ETTER_SØKNADSTIDSPUNKT
            null -> null
        },
        kilde = kilde,
    )

internal fun List<PeriodeMedVerdi<Opphold>>.tilEnkelPeriode(): PeriodeMedVerdi<Opphold> {
    if (this.size > 1) {
        return this.single { it.verdi == Opphold.OPPHOLD }
    }
    return this.single()
}

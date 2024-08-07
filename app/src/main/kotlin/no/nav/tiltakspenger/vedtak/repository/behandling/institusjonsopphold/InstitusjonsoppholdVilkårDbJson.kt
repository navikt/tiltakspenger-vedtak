package no.nav.tiltakspenger.vedtak.repository.behandling.institusjonsopphold

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.institusjonsopphold.InstitusjonsoppholdSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.institusjonsopphold.InstitusjonsoppholdVilkår
import no.nav.tiltakspenger.vedtak.repository.behandling.felles.PeriodisertUtfallDbJson
import no.nav.tiltakspenger.vedtak.repository.behandling.felles.toDbJson
import no.nav.tiltakspenger.vedtak.repository.behandling.felles.toDomain

/**
 * Har ansvar for å serialisere/deserialisere InstitusjonsoppholdVilkår til og fra json for lagring i database.
 */
internal data class InstitusjonsoppholdVilkårDbJson(
    val søknadSaksopplysning: InstitusjonsoppholdSaksopplysningDbJson,
    val saksbehandlerSaksopplysning: InstitusjonsoppholdSaksopplysningDbJson?,
    val avklartSaksopplysning: InstitusjonsoppholdSaksopplysningDbJson,
    val utfallsperioder: List<PeriodisertUtfallDbJson>,
) {
    fun toDomain(vurderingsperiode: Periode): InstitusjonsoppholdVilkår =
        InstitusjonsoppholdVilkår.fromDb(
            vurderingsperiode = vurderingsperiode,
            søknadSaksopplysning = søknadSaksopplysning.toDomain() as InstitusjonsoppholdSaksopplysning.Søknad,
            saksbehandlerSaksopplysning = saksbehandlerSaksopplysning?.toDomain() as InstitusjonsoppholdSaksopplysning.Saksbehandler?,
            avklartSaksopplysning = avklartSaksopplysning.toDomain(),
            utfall = utfallsperioder.toDomain(),
        )
}

internal fun InstitusjonsoppholdVilkår.toDbJson(): InstitusjonsoppholdVilkårDbJson =
    InstitusjonsoppholdVilkårDbJson(
        søknadSaksopplysning = søknadSaksopplysning.toDbJson(),
        saksbehandlerSaksopplysning = saksbehandlerSaksopplysning?.toDbJson(),
        avklartSaksopplysning = avklartSaksopplysning.toDbJson(),
        utfallsperioder = utfall().toDbJson(),
    )

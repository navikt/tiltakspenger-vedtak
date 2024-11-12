package no.nav.tiltakspenger.saksbehandling.domene.vilkår.institusjonsopphold

import no.nav.tiltakspenger.felles.nå
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.PeriodeMedVerdi
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknad

fun Søknad.institusjonsoppholdSaksopplysning(vurderingsperiode: Periode): InstitusjonsoppholdSaksopplysning.Søknad =
    when (institusjon) {
        is Søknad.PeriodeSpm.Nei ->
            InstitusjonsoppholdSaksopplysning.Søknad(
                opphold =
                Periodisering(
                    listOf(PeriodeMedVerdi(Opphold.IKKE_OPPHOLD, vurderingsperiode)),
                ),
                tidsstempel = nå(),
            )

        is Søknad.PeriodeSpm.Ja -> {
            require(vurderingsperiode.inneholderHele(institusjon.periode)) {
                "Vurderingens periode ($vurderingsperiode) må inneholde hele perioden fra søknaden (${institusjon.periode})."
            }
            InstitusjonsoppholdSaksopplysning.Søknad(
                opphold =
                Periodisering(Opphold.IKKE_OPPHOLD, vurderingsperiode).setVerdiForDelPeriode(
                    Opphold.OPPHOLD,
                    institusjon.periode,
                ),
                tidsstempel = nå(),
            )
        }
    }

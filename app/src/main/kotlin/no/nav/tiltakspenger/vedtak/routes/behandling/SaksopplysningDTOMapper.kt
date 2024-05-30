package no.nav.tiltakspenger.vedtak.routes.behandling

import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.HarYtelse
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.LivoppholdSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår
import java.time.LocalDate

object SaksopplysningDTOMapper {

    fun lagSaksopplysningMedVilkår(
        saksbehandler: Saksbehandler,
        saksopplysning: SaksopplysningDTO,
    ): LivoppholdSaksopplysning {
        val vilkår = when (saksopplysning.vilkår) {
            "AAP" -> Vilkår.AAP
            "ALDER" -> Vilkår.ALDER
            "ALDERSPENSJON" -> Vilkår.ALDERSPENSJON
            "DAGPENGER" -> Vilkår.DAGPENGER
            "FORELDREPENGER" -> Vilkår.FORELDREPENGER
            "GJENLEVENDEPENSJON" -> Vilkår.GJENLEVENDEPENSJON
            "INSTITUSJONSOPPHOLD" -> Vilkår.INSTITUSJONSOPPHOLD
            "INTROPROGRAMMET" -> Vilkår.INTROPROGRAMMET
            "JOBBSJANSEN" -> Vilkår.JOBBSJANSEN
            "KVP" -> Vilkår.KVP
            "LØNNSINNTEKT" -> Vilkår.LØNNSINNTEKT
            "OMSORGSPENGER" -> Vilkår.OMSORGSPENGER
            "OPPLÆRINGSPENGER" -> Vilkår.OPPLÆRINGSPENGER
            "OVERGANGSSTØNAD" -> Vilkår.OVERGANGSSTØNAD
            "PENSJONSINNTEKT" -> Vilkår.PENSJONSINNTEKT
            "PLEIEPENGER_NÆRSTÅENDE" -> Vilkår.PLEIEPENGER_NÆRSTÅENDE
            "PLEIEPENGER_SYKT_BARN" -> Vilkår.PLEIEPENGER_SYKT_BARN
            "SUPPLERENDESTØNADALDER" -> Vilkår.SUPPLERENDESTØNADALDER
            "SUPPLERENDESTØNADFLYKTNING" -> Vilkår.SUPPLERENDESTØNADFLYKTNING
            "SVANGERSKAPSPENGER" -> Vilkår.SVANGERSKAPSPENGER
            "SYKEPENGER" -> Vilkår.SYKEPENGER
            "UFØRETRYGD" -> Vilkår.UFØRETRYGD
            "ETTERLØNN" -> Vilkår.ETTERLØNN
            else -> throw IllegalStateException("Kan ikke lage saksopplysning for vilkår ${saksopplysning.vilkår}")
        }

        return LivoppholdSaksopplysning(
            vilkår = vilkår,
            kilde = Kilde.SAKSB,
            detaljer = saksopplysning.begrunnelse, // TODO: Her blir detaljer brukt til begrunnelse, bør kanskje revurderes
            harYtelse = Periodisering<no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.HarYtelse?>(
                defaultVerdi = if (saksopplysning.harYtelse) HarYtelse.HAR_YTELSE else HarYtelse.HAR_IKKE_YTELSE,
                totalePeriode = Periode(LocalDate.parse(saksopplysning.fom), LocalDate.parse(saksopplysning.tom)),
            ),
            saksbehandler = saksbehandler.navIdent,
        )
    }
}

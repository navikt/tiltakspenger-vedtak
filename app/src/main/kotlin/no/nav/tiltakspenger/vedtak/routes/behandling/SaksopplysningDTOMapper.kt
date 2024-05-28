package no.nav.tiltakspenger.vedtak.routes.behandling

import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.HarYtelseSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår
import java.time.LocalDate

object SaksopplysningDTOMapper {

    fun lagSaksopplysningMedVilkår(
        saksbehandler: Saksbehandler,
        saksopplysning: SaksopplysningDTO,
    ): Saksopplysning {
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

        return Saksopplysning.lagSaksopplysningFraSBH(
            fom = LocalDate.parse(saksopplysning.fom),
            tom = LocalDate.parse(saksopplysning.tom),
            vilkår = vilkår,
            detaljer = saksopplysning.begrunnelse,
            harYtelseSaksopplysning = if (saksopplysning.harYtelse) HarYtelseSaksopplysning.HAR_YTELSE else HarYtelseSaksopplysning.HAR_IKKE_YTELSE,
            saksbehandler = saksbehandler.navIdent,
        )
    }
}

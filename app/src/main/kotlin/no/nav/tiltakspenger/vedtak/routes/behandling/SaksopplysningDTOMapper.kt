package no.nav.tiltakspenger.vedtak.routes.behandling

import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.HarYtelse
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.LivsoppholdYtelseSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.YtelseSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Inngangsvilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.LivsoppholdDelVilkår
import java.time.LocalDate

object SaksopplysningDTOMapper {

    fun lagYtelseSaksopplysningMedVilkår(
        saksbehandler: Saksbehandler,
        saksopplysning: SaksopplysningDTO,
    ): YtelseSaksopplysning {
        val vilkår = when (saksopplysning.vilkår) {
            "ALDER" -> Inngangsvilkår.ALDER
            "INSTITUSJONSOPPHOLD" -> Inngangsvilkår.INSTITUSJONSOPPHOLD
            "INTROPROGRAMMET" -> Inngangsvilkår.INTROPROGRAMMET
            "KVP" -> Inngangsvilkår.KVP
            else -> throw IllegalStateException("Kan ikke lage saksopplysning for vilkår ${saksopplysning.vilkår}")
        }

        return YtelseSaksopplysning(
            vilkår = vilkår,
            kilde = Kilde.SAKSB,
            detaljer = saksopplysning.begrunnelse, // TODO: Her blir detaljer brukt til begrunnelse, bør kanskje revurderes
            harYtelse = Periodisering(
                initiellVerdi = if (saksopplysning.harYtelse) HarYtelse.HAR_YTELSE else HarYtelse.HAR_IKKE_YTELSE,
                totalePeriode = Periode(LocalDate.parse(saksopplysning.fom), LocalDate.parse(saksopplysning.tom)),
            ),
            saksbehandler = saksbehandler.navIdent,
        )
    }

    fun lagLivsoppholdSaksopplysningMedVilkår(
        saksbehandler: Saksbehandler,
        saksopplysning: SaksopplysningDTO,
    ): LivsoppholdYtelseSaksopplysning {
        val vilkår = when (saksopplysning.vilkår) {
            "AAP" -> LivsoppholdDelVilkår.AAP
            "ALDERSPENSJON" -> LivsoppholdDelVilkår.ALDERSPENSJON
            "DAGPENGER" -> LivsoppholdDelVilkår.DAGPENGER
            "FORELDREPENGER" -> LivsoppholdDelVilkår.FORELDREPENGER
            "GJENLEVENDEPENSJON" -> LivsoppholdDelVilkår.GJENLEVENDEPENSJON
            "JOBBSJANSEN" -> LivsoppholdDelVilkår.JOBBSJANSEN
            "LØNNSINNTEKT" -> LivsoppholdDelVilkår.LØNNSINNTEKT
            "OMSORGSPENGER" -> LivsoppholdDelVilkår.OMSORGSPENGER
            "OPPLÆRINGSPENGER" -> LivsoppholdDelVilkår.OPPLÆRINGSPENGER
            "OVERGANGSSTØNAD" -> LivsoppholdDelVilkår.OVERGANGSSTØNAD
            "PENSJONSINNTEKT" -> LivsoppholdDelVilkår.PENSJONSINNTEKT
            "PLEIEPENGER_NÆRSTÅENDE" -> LivsoppholdDelVilkår.PLEIEPENGER_NÆRSTÅENDE
            "PLEIEPENGER_SYKT_BARN" -> LivsoppholdDelVilkår.PLEIEPENGER_SYKT_BARN
            "SUPPLERENDESTØNADALDER" -> LivsoppholdDelVilkår.SUPPLERENDESTØNADALDER
            "SUPPLERENDESTØNADFLYKTNING" -> LivsoppholdDelVilkår.SUPPLERENDESTØNADFLYKTNING
            "SVANGERSKAPSPENGER" -> LivsoppholdDelVilkår.SVANGERSKAPSPENGER
            "SYKEPENGER" -> LivsoppholdDelVilkår.SYKEPENGER
            "UFØRETRYGD" -> LivsoppholdDelVilkår.UFØRETRYGD
            "ETTERLØNN" -> LivsoppholdDelVilkår.ETTERLØNN
            else -> throw IllegalStateException("Kan ikke lage saksopplysning for vilkår ${saksopplysning.vilkår}")
        }

        return LivsoppholdYtelseSaksopplysning(
            vilkår = vilkår,
            kilde = Kilde.SAKSB,
            detaljer = saksopplysning.begrunnelse, // TODO: Her blir detaljer brukt til begrunnelse, bør kanskje revurderes
            harYtelse = Periodisering(
                initiellVerdi = if (saksopplysning.harYtelse) HarYtelse.HAR_YTELSE else HarYtelse.HAR_IKKE_YTELSE,
                totalePeriode = Periode(LocalDate.parse(saksopplysning.fom), LocalDate.parse(saksopplysning.tom)),
            ),
            saksbehandler = saksbehandler.navIdent,
        )
    }
}

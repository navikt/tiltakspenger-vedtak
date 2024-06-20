package no.nav.tiltakspenger.vedtak.routes.behandling

import no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak.AntallDagerDTO
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Lovreferanse
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall
import no.nav.tiltakspenger.saksbehandling.service.søker.PeriodeDTO
import no.nav.tiltakspenger.vedtak.clients.utbetaling.UtfallsperiodeDTO
import java.time.LocalDate
import java.time.LocalDateTime

data class SammenstillingForBehandlingDTO(
    val behandlingId: String,
    val saksbehandler: String?,
    val beslutter: String?,
    val vurderingsperiode: PeriodeDTO,
    val søknadsdato: LocalDate,
    val tiltaksdeltagelsesaksopplysninger: TiltaksdeltagelsesaksopplysningDTO,
    val stønadsdager: List<AntallDagerSaksopplysningerDTO>,
    val alderssaksopplysning: AlderssaksopplysningDTO,
    val ytelsessaksopplysninger: YtelsessaksopplysningerDTO,
    val personopplysninger: PersonopplysningerDTO,
    val behandlingsteg: String,
    val status: String,
    val endringslogg: List<EndringDTO>,
    val samletUtfall: String,
    val utfallsperioder: List<UtfallsperiodeDTO>,
    val kravdatoSaksopplysninger: KravdatoSaksopplysningerDTO,
) {
    data class EndringDTO(
        val type: String,
        val begrunnelse: String,
        val endretAv: String,
        val endretTidspunkt: LocalDateTime,
    )

    enum class EndringsType(val beskrivelse: String) {
        SENDT_TILBAKE("Sendt i retur"),
        GODKJENT("Godkjent"),
    }

    data class PersonopplysningerDTO(
        val ident: String,
        val fornavn: String,
        val etternavn: String,
        val skjerming: Boolean,
        val strengtFortrolig: Boolean,
        val fortrolig: Boolean,
    )

    // Bør sende med samletUtfall her også
    data class TiltaksdeltagelsesaksopplysningDTO(
        val vilkår: String,
        val vilkårLovreferanse: LovreferanseDTO,
        val saksopplysninger: List<RegistrertTiltakDTO>,
    )

    data class AntallDagerSaksopplysningerDTO(
        val tiltakId: String,
        val tiltak: String,
        val arrangør: String,
        val avklartAntallDager: List<AntallDagerDTO>,
        val antallDagerSaksopplysningerFraRegister: AntallDagerDTO,
    )

    data class RegistrertTiltakDTO(
        val id: String,
        val arrangør: String,
        val navn: String,
        val periode: PeriodeDTO,
        val status: String,
        val kilde: String,
        val girRett: Boolean,
        val harSøkt: Boolean,
        val deltagelseUtfall: Utfall,
        val begrunnelse: String,
    )

    data class AlderssaksopplysningDTO(
        val periode: PeriodeDTO,
        val kilde: String,
        val detaljer: String,
        val vilkår: String,
        val vilkårTittel: String,
        val utfall: String,
        val vilkårLovreferanse: List<LovreferanseDTO>,
        val grunnlag: LocalDate,
    )

    data class YtelsessaksopplysningerDTO(
        val vilkår: String,
        val saksopplysninger: List<SaksopplysningUtDTO>,
        val samletUtfall: String,
        val vilkårLovreferanse: LovreferanseDTO,
    )

    data class SaksopplysningUtDTO(
        val periode: PeriodeDTO,
        val kilde: String,
        val detaljer: String,
        val saksopplysning: String,
        val saksopplysningTittel: String,
        val utfall: String,
    )

    data class LovreferanseDTO(
        val lovverk: String,
        val paragraf: String,
        val beskrivelse: String,

    ) {
        companion object {
            fun fraLovreferanse(lovreferanse: Lovreferanse) = LovreferanseDTO(
                lovverk = lovreferanse.lovverk,
                paragraf = lovreferanse.paragraf,
                beskrivelse = lovreferanse.beskrivelse,
            )
        }
    }

    data class KravdatoSaksopplysningerDTO(
        val opprinneligKravdato: KravdatoSaksopplysningDTO,
        val kravdatoFraSaksbehandler: KravdatoSaksopplysningDTO? = null,
        val vurderinger: List<VurderingDTO>,
        val samletUtfall: String,
        val lovreferanse: LovreferanseDTO,
    )

    data class KravdatoSaksopplysningDTO(
        val kravdato: LocalDate,
        val kilde: String,
    )

    data class VurderingDTO(
        val periode: PeriodeDTO,
        val utfall: String,
    )
}

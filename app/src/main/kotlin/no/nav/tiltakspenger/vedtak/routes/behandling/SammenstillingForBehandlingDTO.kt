package no.nav.tiltakspenger.vedtak.routes.behandling

import no.nav.tiltakspenger.saksbehandling.service.søker.PeriodeDTO
import no.nav.tiltakspenger.vedtak.clients.utbetaling.UtfallsperiodeDTO
import java.time.LocalDate
import java.time.LocalDateTime

data class SammenstillingForBehandlingDTO(
    val behandlingId: String,
    val saksbehandler: String?,
    val beslutter: String?,
    val fom: LocalDate,
    val tom: LocalDate,
    val søknad: SøknadDTO,
    val registrerteTiltak: List<RegistrertTiltakDTO>,
    val saksopplysninger: List<KategoriserteSaksopplysningerDTO>,
    val personopplysninger: PersonopplysningerDTO,
    val tilstand: String,
    val status: String,
    val endringslogg: List<EndringDTO>,
    val samletUtfall: String,
    val utfallsperioder: List<UtfallsperiodeDTO>,
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

    data class RegistrertTiltakDTO(
        val arrangør: String,
        val dagerIUken: Int,
        val navn: String,
        val periode: PeriodeDTO,
        val prosent: Int,
        val status: String,
        val kilde: String,
        val girRett: Boolean,
        val harSøkt: Boolean,
        val vilkårOppfylt: Boolean,
    )

    data class SøknadDTO(
        val søknadsdato: LocalDate,
        val arrangoernavn: String,
        val tiltakstype: String,
        val deltakelseFom: LocalDate,
        val deltakelseTom: LocalDate,
    )

    data class SaksopplysningUtDTO(
        val fom: LocalDate,
        val tom: LocalDate,
        val kilde: String,
        val detaljer: String,
        val typeSaksopplysning: String,
        val vilkårTittel: String,
        val vilkårFlateTittel: String,
        val fakta: FaktaDTO,
        val utfall: String,
        val vilkårLovReferense: List<LovreferanseDTO>,
    )

    data class LovreferanseDTO(
        val lovverk: String,
        val paragraf: String,
        val beskrivelse: String,
    )

    data class KategoriserteSaksopplysningerDTO(
        val kategoriTittel: String,
        val saksopplysninger: List<SaksopplysningUtDTO>,
        val samletUtfall: String,
    )

    data class FaktaDTO(
        val harYtelse: String,
        val harIkkeYtelse: String,
    )
}

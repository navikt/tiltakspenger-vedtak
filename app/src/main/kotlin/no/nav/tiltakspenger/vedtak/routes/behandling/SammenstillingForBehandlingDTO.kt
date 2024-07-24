package no.nav.tiltakspenger.vedtak.routes.behandling

import no.nav.tiltakspenger.saksbehandling.domene.behandling.BehandlingTilstand
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.VilkårssettDTO
import no.nav.tiltakspenger.vedtak.routes.dto.PeriodeDTO
import java.time.LocalDateTime

internal data class SammenstillingForBehandlingDTO(
    val behandlingId: String,
    val saksbehandler: String?,
    val beslutter: String?,
    val vurderingsperiode: PeriodeDTO,
    val personopplysninger: PersonopplysningerDTO,
    val behandlingTilstand: BehandlingTilstand,
    val status: String,
    val endringslogg: List<EndringDTO>,
    val samletUtfall: String,
    val tiltaksdeltagelsesaksopplysninger: TiltaksdeltagelsesaksopplysningDTO,
    val stønadsdager: List<AntallDagerSaksopplysningerDTO>,
    val vilkårsett: VilkårssettDTO,
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
}

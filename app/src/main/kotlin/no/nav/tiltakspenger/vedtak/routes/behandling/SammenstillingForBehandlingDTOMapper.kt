package no.nav.tiltakspenger.vedtak.routes.behandling

import no.nav.tiltakspenger.saksbehandling.domene.attestering.Attestering
import no.nav.tiltakspenger.saksbehandling.domene.attestering.AttesteringStatus
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.Personopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.søkere
import no.nav.tiltakspenger.vedtak.routes.behandling.SammenstillingForBehandlingDTO.EndringDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.SammenstillingForBehandlingDTO.EndringsType
import no.nav.tiltakspenger.vedtak.routes.behandling.SammenstillingForBehandlingDTO.PersonopplysningerDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.stønadsdager.toDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.toDTO
import no.nav.tiltakspenger.vedtak.routes.dto.PeriodeDTO

internal object SammenstillingForBehandlingDTOMapper {
    fun mapSammenstillingDTO(
        behandling: Førstegangsbehandling,
        personopplysninger: List<Personopplysninger>,
        attesteringer: List<Attestering>,
    ): SammenstillingForBehandlingDTO =
        SammenstillingForBehandlingDTO(
            behandlingId = behandling.id.toString(),
            saksbehandler = behandling.saksbehandler,
            beslutter = behandling.beslutter,
            vurderingsperiode =
            PeriodeDTO(
                fraOgMed = behandling.vurderingsperiode.fraOgMed.toString(),
                tilOgMed = behandling.vurderingsperiode.tilOgMed.toString(),
            ),
            personopplysninger =
            personopplysninger
                .søkere()
                .map {
                    PersonopplysningerDTO(
                        ident = it.fnr.verdi,
                        fornavn = it.fornavn,
                        etternavn = it.etternavn,
                        skjerming = it.avklartSkjerming(),
                        strengtFortrolig = it.strengtFortrolig,
                        fortrolig = it.fortrolig,
                    )
                }.first(),
            status = behandling.status.toDTO().toString(),
            endringslogg =
            attesteringer.map { att ->
                EndringDTO(
                    type =
                    when (att.svar) {
                        AttesteringStatus.GODKJENT -> EndringsType.GODKJENT.beskrivelse
                        AttesteringStatus.SENDT_TILBAKE -> EndringsType.SENDT_TILBAKE.beskrivelse
                    },
                    begrunnelse = att.begrunnelse ?: "Godkjent av beslutter",
                    endretAv = att.beslutter,
                    endretTidspunkt = att.tidspunkt,
                )
            },
            vilkårsett = behandling.vilkårssett.toDTO(),
            stønadsdager = behandling.stønadsdager.toDTO(),
        )
}

package no.nav.tiltakspenger.vedtak.clients.meldekort

import no.nav.tiltakspenger.saksbehandling.domene.behandling.UtfallForPeriode
import no.nav.tiltakspenger.saksbehandling.domene.sak.SakDetaljer
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Vedtak
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.VedtaksType

object MeldekortGrunnlagDTOMapper {
    fun mapMeldekortGrunnlagDTO(sak: SakDetaljer, vedtak: Vedtak) =
        MeldekortGrunnlagDTO(
            vedtakId = vedtak.id.toString(),
            sakId = sak.saknummer.verdi,
            behandlingId = vedtak.behandling.id.toString(),
            status = when (vedtak.vedtaksType) {
                VedtaksType.AVSLAG -> StatusDTO.IKKE_AKTIV
                VedtaksType.INNVILGELSE -> StatusDTO.AKTIV
                VedtaksType.STANS -> StatusDTO.IKKE_AKTIV
                VedtaksType.FORLENGELSE -> StatusDTO.AKTIV
            },
            vurderingsperiode = PeriodeDTO(
                fra = vedtak.periode.fraOgMed,
                til = vedtak.periode.tilOgMed,
            ),
            tiltak = mapTiltakDTO(vedtak),
            personopplysninger = PersonopplysningerDTO(
                fornavn = vedtak.behandling.søknad().personopplysninger.fornavn,
                etternavn = vedtak.behandling.søknad().personopplysninger.etternavn,
                ident = vedtak.behandling.søknad().personopplysninger.ident,
            ),
            utfallsperioder = vedtak.utfallsperioder.map {
                UtfallsperiodeDTO(
                    fom = it.fom,
                    tom = it.tom,
                    antallBarn = it.antallBarn,
                    utfall = when (it.utfall) {
                        UtfallForPeriode.GIR_RETT_TILTAKSPENGER -> UtfallForPeriodeDTO.GIR_RETT_TILTAKSPENGER
                        UtfallForPeriode.GIR_IKKE_RETT_TILTAKSPENGER -> UtfallForPeriodeDTO.GIR_IKKE_RETT_TILTAKSPENGER
                        UtfallForPeriode.KREVER_MANUELL_VURDERING -> UtfallForPeriodeDTO.KREVER_MANUELL_VURDERING
                    },

                )
            },
        )

    // TODO: Denne tar bare med informasjon om tiltaket fra søknaden, her må vi ta med alle tiltakene som er OPPFYLT.
    // TODO: Og antall dager i uken må fylles ut korrekt.
    // TODO: Egentlig er ikke List<TiltakDTO> en god nok struktur for å fange informasjonen vi er ute etter
    // TODO: Det vil ikke håndtere to tiltak som begge er på tre dager i uka og hvor tiltakene er på de samme tre dagene
    fun mapTiltakDTO(vedtak: Vedtak): List<TiltakDTO> =
        vedtak.behandling.tiltak.tiltak
            .filter { it.eksternId == vedtak.behandling.søknad().tiltak.id }
            .map {
                TiltakDTO(
                    periodeDTO = PeriodeDTO(
                        fra = it.deltakelseFom,
                        til = it.deltakelseTom,
                    ),
                    typeBeskrivelse = it.gjennomføring.typeNavn,
                    typeKode = it.gjennomføring.typeKode,
                    antDagerIUken = 5f,
                )
            }
}

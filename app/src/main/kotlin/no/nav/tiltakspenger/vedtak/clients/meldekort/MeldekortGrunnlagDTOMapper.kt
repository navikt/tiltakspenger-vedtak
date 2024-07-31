package no.nav.tiltakspenger.vedtak.clients.meldekort

import no.nav.tiltakspenger.saksbehandling.domene.behandling.UtfallForPeriode
import no.nav.tiltakspenger.saksbehandling.domene.sak.SakDetaljer
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Vedtak
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.VedtaksType

object MeldekortGrunnlagDTOMapper {
    fun mapMeldekortGrunnlagDTO(sak: SakDetaljer, vedtak: Vedtak) =
        MeldekortGrunnlagDTO(
            vedtakId = vedtak.id.toString(),
            sakId = sak.saksnummer.verdi,
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
            // TODO KEB Her må vi fylle på riktig verdi.
            tiltak = listOf(
                TiltakDTO(
                    periodeDTO = PeriodeDTO(
                        fra = vedtak.periode.fraOgMed,
                        til = vedtak.periode.tilOgMed,
                    ),
                    typeBeskrivelse = "suavitate",
                    typeKode = "expetenda",
                    antDagerIUken = 2f,
                ),
            ),
            personopplysninger = PersonopplysningerDTO(
                fornavn = vedtak.behandling.søknad().personopplysninger.fornavn,
                etternavn = vedtak.behandling.søknad().personopplysninger.etternavn,
                ident = vedtak.behandling.søknad().personopplysninger.fnr.verdi,
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
}

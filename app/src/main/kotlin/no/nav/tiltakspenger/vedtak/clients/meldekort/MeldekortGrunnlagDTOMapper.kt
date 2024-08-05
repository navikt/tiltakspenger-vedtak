package no.nav.tiltakspenger.vedtak.clients.meldekort

import no.nav.tiltakspenger.saksbehandling.domene.sak.SakDetaljer
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Vedtak
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.VedtaksType
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.AvklartUtfallForPeriode

object MeldekortGrunnlagDTOMapper {
    fun mapMeldekortGrunnlagDTO(sak: SakDetaljer, vedtak: Vedtak): MeldekortGrunnlagDTO {
        val personopplysninger = vedtak.behandling.søknad.personopplysninger
        val vedtaksperiode = vedtak.periode
        return MeldekortGrunnlagDTO(
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
                fra = vedtaksperiode.fraOgMed,
                til = vedtaksperiode.tilOgMed,
            ),
            // TODO KEW Her må vi fylle på riktig verdi.
            tiltak = listOf(
                TiltakDTO(
                    periodeDTO = PeriodeDTO(
                        fra = vedtaksperiode.fraOgMed,
                        til = vedtaksperiode.tilOgMed,
                    ),
                    typeBeskrivelse = "suavitate",
                    typeKode = "expetenda",
                    antDagerIUken = 2f,
                ),
            ),
            personopplysninger = PersonopplysningerDTO(
                fornavn = personopplysninger.fornavn,
                etternavn = personopplysninger.etternavn,
                ident = personopplysninger.fnr.verdi,
            ),
            utfallsperioder = vedtak.utfallsperioder.perioder().map {
                UtfallsperiodeDTO(
                    fom = it.periode.fraOgMed,
                    tom = it.periode.tilOgMed,
                    utfall = when (it.verdi) {
                        AvklartUtfallForPeriode.OPPFYLT -> UtfallForPeriodeDTO.GIR_RETT_TILTAKSPENGER
                        AvklartUtfallForPeriode.IKKE_OPPFYLT -> UtfallForPeriodeDTO.GIR_IKKE_RETT_TILTAKSPENGER
                    },

                )
            },
        )
    }
}

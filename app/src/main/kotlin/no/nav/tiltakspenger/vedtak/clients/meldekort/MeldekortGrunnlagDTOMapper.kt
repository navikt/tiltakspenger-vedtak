package no.nav.tiltakspenger.vedtak.clients.meldekort

import no.nav.tiltakspenger.saksbehandling.domene.behandling.UtfallForPeriode
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Vedtak
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.VedtaksType

object MeldekortGrunnlagDTOMapper {
    fun mapMeldekortGrunnlagDTO(vedtak: Vedtak) =
        MeldekortGrunnlagDTO(
            vedtakId = vedtak.id.toString(),
            sakId = vedtak.sakId.toString(),
            behandlingId = vedtak.behandling.id.toString(),
            status = when (vedtak.vedtaksType) {
                VedtaksType.AVSLAG -> StatusDTO.IKKE_AKTIV
                VedtaksType.INNVILGELSE -> StatusDTO.AKTIV
                VedtaksType.STANS -> StatusDTO.IKKE_AKTIV
                VedtaksType.FORLENGELSE -> StatusDTO.AKTIV
            },
            vurderingsperiode = PeriodeDTO(
                fra = vedtak.periode.fra,
                til = vedtak.periode.til,
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

    fun mapTiltakDTO(vedtak: Vedtak) =
        vedtak.behandling.tiltak
            .filter { it.id == vedtak.behandling.søknad().tiltak.id }
            .map {
                TiltakDTO(
                    periodeDTO = PeriodeDTO(
                        fra = it.deltakelseFom,
                        til = it.deltakelseTom,
                    ),
                    typeBeskrivelse = it.gjennomføring.typeNavn,
                    typeKode = it.gjennomføring.typeKode,
                    antDagerIUken = it.deltakelseDagerUke
                        ?: if (it.deltakelseProsent == 100F) {
                            5F
                        } else {
                            throw IllegalStateException("Kan ikke beregne antall dager i uken for tiltak uten deltakelseDagerUke eller deltakelseProsent")
                        },
                )
            }
}

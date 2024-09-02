package no.nav.tiltakspenger.vedtak.clients.meldekort

import no.nav.tiltakspenger.libs.meldekort.MeldekortGrunnlagDTO
import no.nav.tiltakspenger.libs.meldekort.PeriodeDTO
import no.nav.tiltakspenger.libs.meldekort.PersonopplysningerDTO
import no.nav.tiltakspenger.libs.meldekort.StatusDTO
import no.nav.tiltakspenger.libs.meldekort.TiltakDTO
import no.nav.tiltakspenger.libs.meldekort.UtfallForPeriodeDTO
import no.nav.tiltakspenger.libs.meldekort.UtfallsperiodeDTO
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Rammevedtak
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.VedtaksType
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.AvklartUtfallForPeriode
import no.nav.tiltakspenger.vedtak.db.serialize

object MeldekortGrunnlagDTOMapper {
    fun toJson(vedtak: Rammevedtak): String {
        val behandling = vedtak.behandling
        val personopplysninger = behandling.søknad.personopplysninger
        val vedtaksperiode = vedtak.periode
        return MeldekortGrunnlagDTO(
            vedtakId = vedtak.id.toString(),
            sakId = vedtak.saksnummer.verdi,
            behandlingId = behandling.id.toString(),
            status =
            when (vedtak.vedtaksType) {
                VedtaksType.AVSLAG -> StatusDTO.IKKE_AKTIV
                VedtaksType.INNVILGELSE -> StatusDTO.AKTIV
                VedtaksType.STANS -> StatusDTO.IKKE_AKTIV
                VedtaksType.FORLENGELSE -> StatusDTO.AKTIV
            },
            vurderingsperiode =
            PeriodeDTO(
                fra = vedtaksperiode.fraOgMed,
                til = vedtaksperiode.tilOgMed,
            ),
            tiltak =
            listOf(
                TiltakDTO(
                    periodeDTO =
                    PeriodeDTO(
                        fra = vedtaksperiode.fraOgMed,
                        til = vedtaksperiode.tilOgMed,
                    ),
                    // TODO pre-mvp jah: Denne fila skal slettes. Vi flytter meldekort-api inn i vedtak.
                    typeKode = behandling.vilkårssett.tiltakDeltagelseVilkår.registerSaksopplysning.tiltakstype.name,
                    antDagerIUken = behandling.stønadsdager.registerSaksopplysning.antallDager,
                ),
            ),
            personopplysninger =
            PersonopplysningerDTO(
                fornavn = personopplysninger.fornavn,
                etternavn = personopplysninger.etternavn,
                ident = personopplysninger.fnr.verdi,
            ),
            utfallsperioder =
            vedtak.utfallsperioder.perioder().map {
                UtfallsperiodeDTO(
                    fom = it.periode.fraOgMed.toString(),
                    tom = it.periode.tilOgMed.toString(),
                    utfall =
                    when (it.verdi) {
                        AvklartUtfallForPeriode.OPPFYLT -> UtfallForPeriodeDTO.GIR_RETT_TILTAKSPENGER
                        AvklartUtfallForPeriode.IKKE_OPPFYLT -> UtfallForPeriodeDTO.GIR_IKKE_RETT_TILTAKSPENGER
                    },
                )
            },
        ).let { serialize(it) }
    }
}

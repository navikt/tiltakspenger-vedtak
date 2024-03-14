package no.nav.tiltakspenger.vedtak.clients.brevpublisher

import no.nav.tiltakspenger.domene.behandling.Søknad
import no.nav.tiltakspenger.domene.personopplysninger.PersonopplysningerSøker
import no.nav.tiltakspenger.domene.vedtak.Vedtak
import no.nav.tiltakspenger.libs.dokument.BrevDTO
import no.nav.tiltakspenger.libs.dokument.PersonaliaDTO
import no.nav.tiltakspenger.libs.dokument.TiltaksinfoDTO
import java.time.LocalDate

object VedtaksbrevMapper {
    fun mapVedtaksBrevDTO(vedtak: Vedtak, personopplysninger: PersonopplysningerSøker) =
        BrevDTO(
            personaliaDTO = mapPersonaliaDTO(vedtak, personopplysninger),
            tiltaksinfoDTO = mapTiltaksinfo(vedtak),
            fraDato = vedtak.periode.fra.toString(),
            tilDato = vedtak.periode.til.toString(),
            saksnummer = vedtak.sakId.toString(),
            barnetillegg = false,
            saksbehandler = vedtak.saksbehandler,
            kontor = "måkk",
            datoForUtsending = LocalDate.now(),
        )

    private fun mapPersonaliaDTO(vedtak: Vedtak, personopplysninger: PersonopplysningerSøker) =
        PersonaliaDTO(
            ident = personopplysninger.ident,
            fornavn = personopplysninger.fornavn,
            etternavn = personopplysninger.etternavn,
            // TODO Grøss:
            antallBarn = vedtak.behandling.søknad().barnetillegg.count { it.oppholderSegIEØS == Søknad.JaNeiSpm.Ja },
        )

    private fun mapTiltaksinfo(vedtak: Vedtak) =
        vedtak.behandling.tiltak
            .filter { it.id == vedtak.behandling.søknad().tiltak.id }
            .map {
                TiltaksinfoDTO(
                    tiltak = it.gjennomføring.typeNavn,
                    tiltaksnavn = it.gjennomføring.typeNavn,
                    tiltaksnummer = it.gjennomføring.typeKode,
                    arrangør = it.gjennomføring.arrangørnavn,
                )
            }.first()
}

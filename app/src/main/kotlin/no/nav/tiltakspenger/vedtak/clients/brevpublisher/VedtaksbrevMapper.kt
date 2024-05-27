package no.nav.tiltakspenger.vedtak.clients.brevpublisher

import no.nav.tiltakspenger.libs.dokument.BrevDTO
import no.nav.tiltakspenger.libs.dokument.PersonaliaDTO
import no.nav.tiltakspenger.libs.dokument.TiltaksinfoDTO
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknad
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.PersonopplysningerSøker
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Vedtak
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

val norskDatoFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d. MMMM yyyy", Locale.Builder().setLanguage("no").setRegion("NO").build())

object VedtaksbrevMapper {
    fun mapVedtaksBrevDTO(vedtak: Vedtak, personopplysninger: PersonopplysningerSøker) =
        BrevDTO(
            personalia = mapPersonaliaDTO(vedtak, personopplysninger),
            tiltaksinfo = mapTiltaksinfo(vedtak),
            fraDato = vedtak.periode.fra.format(norskDatoFormatter),
            tilDato = vedtak.periode.til.toString(),
            saksnummer = vedtak.sakId.toString(),
            barnetillegg = false,
            saksbehandler = vedtak.saksbehandler,
            beslutter = vedtak.beslutter,
            sats = 285, // TODO Disse satsene bor i utbetaling. Burde vi hente de derfra?
            satsBarn = 53,
            kontor = "måkk",
            datoForUtsending = LocalDate.now().format(norskDatoFormatter),
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

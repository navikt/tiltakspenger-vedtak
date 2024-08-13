package no.nav.tiltakspenger.vedtak.clients.brevpublisher

import no.nav.tiltakspenger.libs.dokument.BrevDTO
import no.nav.tiltakspenger.libs.dokument.PersonaliaDTO
import no.nav.tiltakspenger.libs.dokument.TiltaksinfoDTO
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.PersonopplysningerSøker
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saksnummer
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Vedtak
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

val norskDatoFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern(
        "d. MMMM yyyy",
        Locale
            .Builder()
            .setLanguage("no")
            .setRegion("NO")
            .build(),
    )

object VedtaksbrevMapper {
    fun mapVedtaksBrevDTO(
        saksnummer: Saksnummer,
        vedtak: Vedtak,
        personopplysninger: PersonopplysningerSøker,
    ) = BrevDTO(
        personalia = mapPersonaliaDTO(vedtak, personopplysninger),
        tiltaksinfo =
        TiltaksinfoDTO(
            // TODO() KEBH Denne må fikses, men ikke nå. Tar en runde på brev
            tiltak = "MåkK",
            tiltaksnavn = "mÅKk",
            tiltaksnummer = "MÅkk",
            arrangør = "måKK",
        ),
        fraDato = vedtak.periode.fraOgMed.format(norskDatoFormatter),
        tilDato = vedtak.periode.tilOgMed.format(norskDatoFormatter),
        saksnummer = saksnummer.verdi,
        // TODO jah: Er det vits at denne er her før vi implementerer barnetillegg?
        barnetillegg = false,
        saksbehandler = vedtak.saksbehandler,
        beslutter = vedtak.beslutter,
        // TODO Disse satsene bor i utbetaling. Burde vi hente de derfra?
        sats = 285,
        satsBarn = 53,
        kontor = "måkk",
        datoForUtsending = LocalDate.now().format(norskDatoFormatter),
    )

    private fun mapPersonaliaDTO(
        vedtak: Vedtak,
        personopplysninger: PersonopplysningerSøker,
    ) = PersonaliaDTO(
        ident = personopplysninger.fnr.verdi,
        fornavn = personopplysninger.fornavn,
        etternavn = personopplysninger.etternavn,
        // TODO brev jah: Ikke vits å ha denne her før vi har implementert barnetillegg
        antallBarn = 0,
    )
}

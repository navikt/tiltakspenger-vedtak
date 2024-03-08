package no.nav.tiltakspenger.objectmothers

import no.nav.tiltakspenger.innsending.Aktivitetslogg
import no.nav.tiltakspenger.innsending.Feil
import no.nav.tiltakspenger.innsending.ForeldrepengerVedtak
import no.nav.tiltakspenger.innsending.OvergangsstønadVedtak
import no.nav.tiltakspenger.innsending.Skjerming
import no.nav.tiltakspenger.innsending.UføreVedtak
import no.nav.tiltakspenger.innsending.YtelseSak
import no.nav.tiltakspenger.innsending.meldinger.FeilMottattHendelse
import no.nav.tiltakspenger.innsending.meldinger.ForeldrepengerMottattHendelse
import no.nav.tiltakspenger.innsending.meldinger.OvergangsstønadMottattHendelse
import no.nav.tiltakspenger.innsending.meldinger.PersonopplysningerMottattHendelse
import no.nav.tiltakspenger.innsending.meldinger.SkjermingMottattHendelse
import no.nav.tiltakspenger.innsending.meldinger.SøknadMottattHendelse
import no.nav.tiltakspenger.innsending.meldinger.TiltakMottattHendelse
import no.nav.tiltakspenger.innsending.meldinger.UføreMottattHendelse
import no.nav.tiltakspenger.innsending.meldinger.YtelserMottattHendelse
import no.nav.tiltakspenger.objectmothers.ObjectMother.nySøknad
import no.nav.tiltakspenger.objectmothers.ObjectMother.personopplysningKjedeligFyr
import no.nav.tiltakspenger.objectmothers.ObjectMother.skjermingFalse
import no.nav.tiltakspenger.objectmothers.ObjectMother.tiltak
import no.nav.tiltakspenger.objectmothers.ObjectMother.ytelseSak
import no.nav.tiltakspenger.saksbehandling.behandling.Søknad
import no.nav.tiltakspenger.saksbehandling.behandling.Tiltak
import no.nav.tiltakspenger.saksbehandling.personopplysninger.Personopplysninger
import no.nav.tiltakspenger.saksbehandling.personopplysninger.søkerOrNull
import java.time.LocalDateTime
import java.util.Random

interface HendelserMother {
    fun nySøknadMottattHendelse(
        journalpostId: String = Random().nextInt().toString(),
        ident: String = Random().nextInt().toString(),
        søknad: Søknad = nySøknad(
            personopplysninger = Søknad.Personopplysninger(
                ident = ident,
                fornavn = "Fornavn",
                etternavn = "Etternavn",
            ),
        ),
        aktivitetslogg: no.nav.tiltakspenger.innsending.Aktivitetslogg = no.nav.tiltakspenger.innsending.Aktivitetslogg(
            forelder = null,
        ),
    ): SøknadMottattHendelse {
        return SøknadMottattHendelse(
            aktivitetslogg = aktivitetslogg,
            journalpostId = journalpostId,
            søknad = søknad,
        )
    }

    fun nyPersonopplysningHendelse(
        journalpostId: String = Random().nextInt().toString(),
        personopplysninger: List<Personopplysninger> = listOf(personopplysningKjedeligFyr(strengtFortroligUtland = false)),
        aktivitetslogg: no.nav.tiltakspenger.innsending.Aktivitetslogg = no.nav.tiltakspenger.innsending.Aktivitetslogg(
            forelder = null,
        ),
        ident: String = personopplysninger.søkerOrNull()?.ident
            ?: Random().nextInt().toString(),
        tidsstempelPersonopplysningerInnhentet: LocalDateTime = LocalDateTime.now(),
    ): PersonopplysningerMottattHendelse {
        return PersonopplysningerMottattHendelse(
            aktivitetslogg = aktivitetslogg,
            journalpostId = journalpostId,
            ident = ident,
            personopplysninger = personopplysninger,
            tidsstempelPersonopplysningerInnhentet = tidsstempelPersonopplysningerInnhentet,
        )
    }

    fun nySkjermingHendelse(
        journalpostId: String = Random().nextInt().toString(),
        ident: String = Random().nextInt().toString(),
        skjerming: Skjerming = skjermingFalse(ident = journalpostId),
        aktivitetslogg: no.nav.tiltakspenger.innsending.Aktivitetslogg = no.nav.tiltakspenger.innsending.Aktivitetslogg(
            forelder = null,
        ),
        tidsstemplerSkjermingInnhentet: LocalDateTime = LocalDateTime.now(),
    ): SkjermingMottattHendelse {
        return SkjermingMottattHendelse(
            journalpostId = journalpostId,
            ident = ident,
            skjerming = skjerming,
            aktivitetslogg = aktivitetslogg,
            tidsstempelSkjermingInnhentet = tidsstemplerSkjermingInnhentet,
        )
    }

    fun nyTiltakHendelse(
        journalpostId: String = Random().nextInt().toString(),
        tiltak: List<Tiltak> = listOf(tiltak()),
        aktivitetslogg: no.nav.tiltakspenger.innsending.Aktivitetslogg = no.nav.tiltakspenger.innsending.Aktivitetslogg(
            forelder = null,
        ),
        tidsstempelTiltakInnhentet: LocalDateTime = LocalDateTime.now(),
    ): TiltakMottattHendelse {
        return TiltakMottattHendelse(
            journalpostId = journalpostId,
            tiltaks = tiltak,
            aktivitetslogg = aktivitetslogg,
            tidsstempelTiltakInnhentet = tidsstempelTiltakInnhentet,
        )
    }

    fun nyYtelseHendelse(
        journalpostId: String = Random().nextInt().toString(),
        ytelseSak: List<YtelseSak> = listOf(ytelseSak()),
        aktivitetslogg: no.nav.tiltakspenger.innsending.Aktivitetslogg = no.nav.tiltakspenger.innsending.Aktivitetslogg(
            forelder = null,
        ),
        tidsstempelYtelserInnhentet: LocalDateTime = LocalDateTime.now(),
    ): YtelserMottattHendelse {
        return YtelserMottattHendelse(
            journalpostId = journalpostId,
            ytelseSak = ytelseSak,
            aktivitetslogg = aktivitetslogg,
            tidsstempelYtelserInnhentet = tidsstempelYtelserInnhentet,
        )
    }

    fun nyForeldrepengerHendelse(
        ident: String = Random().nextInt().toString(),
        journalpostId: String = Random().nextInt().toString(),
        foreldrepengerVedtakListe: List<ForeldrepengerVedtak> = listOf(ObjectMother.foreldrepengerVedtak()),
        aktivitetslogg: no.nav.tiltakspenger.innsending.Aktivitetslogg = no.nav.tiltakspenger.innsending.Aktivitetslogg(
            forelder = null,
        ),
        tidsstempelForeldrepengerInnhentet: LocalDateTime = LocalDateTime.now(),
    ): ForeldrepengerMottattHendelse {
        return ForeldrepengerMottattHendelse(
            ident = ident,
            journalpostId = journalpostId,
            foreldrepengerVedtakListe = foreldrepengerVedtakListe,
            aktivitetslogg = aktivitetslogg,
            tidsstempelForeldrepengerVedtakInnhentet = tidsstempelForeldrepengerInnhentet,
        )
    }

    fun nyOvergangsstønadHendelse(
        ident: String = Random().nextInt().toString(),
        journalpostId: String = Random().nextInt().toString(),
        overgansstønader: List<OvergangsstønadVedtak> = listOf(ObjectMother.overgangsstønadVedtak()),
        aktivitetslogg: no.nav.tiltakspenger.innsending.Aktivitetslogg = no.nav.tiltakspenger.innsending.Aktivitetslogg(
            forelder = null,
        ),
        innhentet: LocalDateTime = LocalDateTime.now(),
    ): OvergangsstønadMottattHendelse {
        return OvergangsstønadMottattHendelse(
            ident = ident,
            journalpostId = journalpostId,
            overgangsstønadVedtakListe = overgansstønader,
            aktivitetslogg = aktivitetslogg,
            innhentet = innhentet,
        )
    }

    fun nyUføreHendelse(
        ident: String = Random().nextInt().toString(),
        journalpostId: String = Random().nextInt().toString(),
        uføreVedtak: UføreVedtak = ObjectMother.uføreVedtak(),
        aktivitetslogg: no.nav.tiltakspenger.innsending.Aktivitetslogg = no.nav.tiltakspenger.innsending.Aktivitetslogg(
            forelder = null,
        ),
        tidsstempelUføreVedtakInnhentet: LocalDateTime = LocalDateTime.now(),
    ): UføreMottattHendelse {
        return UføreMottattHendelse(
            ident = ident,
            journalpostId = journalpostId,
            uføreVedtak = uføreVedtak,
            aktivitetslogg = aktivitetslogg,
            tidsstempelUføreVedtakInnhentet = tidsstempelUføreVedtakInnhentet,
        )
    }

    fun nyFeilHendelse(
        journalpostId: String = Random().nextInt().toString(),
        aktivitetslogg: no.nav.tiltakspenger.innsending.Aktivitetslogg = no.nav.tiltakspenger.innsending.Aktivitetslogg(
            forelder = null,
        ),
        ident: String = Random().nextInt().toString(),
        feil: Feil = Feil.PersonIkkeFunnet,
    ) = FeilMottattHendelse(
        aktivitetslogg = aktivitetslogg,
        journalpostId = journalpostId,
        ident = ident,
        feil = feil,
    )
}

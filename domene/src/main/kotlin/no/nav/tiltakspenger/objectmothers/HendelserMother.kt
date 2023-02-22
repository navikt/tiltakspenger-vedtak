package no.nav.tiltakspenger.objectmothers

import no.nav.tiltakspenger.objectmothers.ObjectMother.nySøknadMedArenaTiltak
import no.nav.tiltakspenger.objectmothers.ObjectMother.personopplysningKjedeligFyr
import no.nav.tiltakspenger.objectmothers.ObjectMother.skjermingFalse
import no.nav.tiltakspenger.objectmothers.ObjectMother.tiltaksaktivitet
import no.nav.tiltakspenger.objectmothers.ObjectMother.ytelseSak
import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.Feil
import no.nav.tiltakspenger.vedtak.ForeldrepengerVedtak
import no.nav.tiltakspenger.vedtak.OvergangsstønadVedtak
import no.nav.tiltakspenger.vedtak.Personopplysninger
import no.nav.tiltakspenger.vedtak.Skjerming
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vedtak.Tiltaksaktivitet
import no.nav.tiltakspenger.vedtak.UføreVedtak
import no.nav.tiltakspenger.vedtak.YtelseSak
import no.nav.tiltakspenger.vedtak.meldinger.ArenaTiltakMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.FeilMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.ForeldrepengerMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.OvergangsstønadMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.PersonopplysningerMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.SkjermingMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.SøknadMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.UføreMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.YtelserMottattHendelse
import java.time.LocalDateTime
import java.util.Random

interface HendelserMother {
    fun nySøknadMottattHendelse(
        journalpostId: String = Random().nextInt().toString(),
        ident: String = Random().nextInt().toString(),
        søknad: Søknad = nySøknadMedArenaTiltak(ident = ident),
        aktivitetslogg: Aktivitetslogg = Aktivitetslogg(forelder = null),
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
        aktivitetslogg: Aktivitetslogg = Aktivitetslogg(forelder = null),
        ident: String = personopplysninger.filterIsInstance<Personopplysninger.Søker>().firstOrNull()?.ident
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
        aktivitetslogg: Aktivitetslogg = Aktivitetslogg(forelder = null),
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
        tiltaksaktivitet: List<Tiltaksaktivitet> = listOf(tiltaksaktivitet()),
        aktivitetslogg: Aktivitetslogg = Aktivitetslogg(forelder = null),
        tidsstempelTiltakInnhentet: LocalDateTime = LocalDateTime.now(),
    ): ArenaTiltakMottattHendelse {
        return ArenaTiltakMottattHendelse(
            journalpostId = journalpostId,
            tiltaksaktivitet = tiltaksaktivitet,
            aktivitetslogg = aktivitetslogg,
            tidsstempelTiltakInnhentet = tidsstempelTiltakInnhentet,
        )
    }

    fun nyYtelseHendelse(
        journalpostId: String = Random().nextInt().toString(),
        ytelseSak: List<YtelseSak> = listOf(ytelseSak()),
        aktivitetslogg: Aktivitetslogg = Aktivitetslogg(forelder = null),
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
        aktivitetslogg: Aktivitetslogg = Aktivitetslogg(forelder = null),
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
        aktivitetslogg: Aktivitetslogg = Aktivitetslogg(forelder = null),
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
        aktivitetslogg: Aktivitetslogg = Aktivitetslogg(forelder = null),
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
        aktivitetslogg: Aktivitetslogg = Aktivitetslogg(forelder = null),
        ident: String = Random().nextInt().toString(),
        feil: Feil = Feil.PersonIkkeFunnet,
    ) = FeilMottattHendelse(
        aktivitetslogg = aktivitetslogg,
        journalpostId = journalpostId,
        ident = ident,
        feil = feil,
    )
}

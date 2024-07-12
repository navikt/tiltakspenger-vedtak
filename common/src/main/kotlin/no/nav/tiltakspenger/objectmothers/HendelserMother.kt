package no.nav.tiltakspenger.objectmothers

import no.nav.tiltakspenger.innsending.domene.Aktivitetslogg
import no.nav.tiltakspenger.innsending.domene.Feil
import no.nav.tiltakspenger.innsending.domene.meldinger.FeilMottattHendelse
import no.nav.tiltakspenger.innsending.domene.meldinger.PersonopplysningerMottattHendelse
import no.nav.tiltakspenger.innsending.domene.meldinger.SkjermingMottattHendelse
import no.nav.tiltakspenger.innsending.domene.meldinger.SøknadMottattHendelse
import no.nav.tiltakspenger.innsending.domene.meldinger.TiltakMottattHendelse
import no.nav.tiltakspenger.objectmothers.ObjectMother.nySøknad
import no.nav.tiltakspenger.objectmothers.ObjectMother.personopplysningKjedeligFyr
import no.nav.tiltakspenger.objectmothers.ObjectMother.skjermingFalse
import no.nav.tiltakspenger.objectmothers.ObjectMother.tiltak
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknad
import no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak.Tiltak
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.Personopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.søkerOrNull
import no.nav.tiltakspenger.saksbehandling.domene.skjerming.Skjerming
import java.time.LocalDateTime
import java.util.Random

interface HendelserMother {

    companion object {
        private val random = Random()
    }

    fun nySøknadMottattHendelse(
        journalpostId: String = random.nextInt().toString(),
        ident: String = random.nextInt().toString(),
        søknad: Søknad = nySøknad(
            personopplysninger = Søknad.Personopplysninger(
                ident = ident,
                fornavn = "Fornavn",
                etternavn = "Etternavn",
            ),
        ),
        aktivitetslogg: Aktivitetslogg = Aktivitetslogg(
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
        journalpostId: String = random.nextInt().toString(),
        personopplysninger: List<Personopplysninger> = listOf(personopplysningKjedeligFyr(strengtFortroligUtland = false)),
        aktivitetslogg: Aktivitetslogg = Aktivitetslogg(
            forelder = null,
        ),
        ident: String = personopplysninger.søkerOrNull()?.ident
            ?: random.nextInt().toString(),
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
        journalpostId: String = random.nextInt().toString(),
        ident: String = random.nextInt().toString(),
        skjerming: Skjerming = skjermingFalse(ident = journalpostId),
        aktivitetslogg: Aktivitetslogg = Aktivitetslogg(
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
        journalpostId: String = random.nextInt().toString(),
        tiltak: List<Tiltak> = listOf(tiltak()),
        aktivitetslogg: Aktivitetslogg = Aktivitetslogg(
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

    fun nyFeilHendelse(
        journalpostId: String = random.nextInt().toString(),
        aktivitetslogg: Aktivitetslogg = Aktivitetslogg(
            forelder = null,
        ),
        ident: String = random.nextInt().toString(),
        feil: Feil = Feil.PersonIkkeFunnet,
    ) = FeilMottattHendelse(
        aktivitetslogg = aktivitetslogg,
        journalpostId = journalpostId,
        ident = ident,
        feil = feil,
    )
}

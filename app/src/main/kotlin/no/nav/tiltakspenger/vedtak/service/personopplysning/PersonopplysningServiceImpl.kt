package no.nav.tiltakspenger.vedtak.service.personopplysning

import no.nav.tiltakspenger.domene.personopplysninger.Personopplysninger
import no.nav.tiltakspenger.domene.personopplysninger.PersonopplysningerBarnMedIdent
import no.nav.tiltakspenger.domene.personopplysninger.PersonopplysningerBarnUtenIdent
import no.nav.tiltakspenger.domene.personopplysninger.PersonopplysningerSøker
import no.nav.tiltakspenger.domene.personopplysninger.barnMedIdent
import no.nav.tiltakspenger.domene.personopplysninger.erLik
import no.nav.tiltakspenger.domene.personopplysninger.søker
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.vedtak.innsending.Skjerming
import no.nav.tiltakspenger.vedtak.repository.sak.PersonopplysningerRepo
import no.nav.tiltakspenger.vedtak.repository.sak.SakRepo

class PersonopplysningServiceImpl(
    private val personopplysningerRepo: PersonopplysningerRepo,
    private val sakRepo: SakRepo,
) : PersonopplysningService {
    override fun hent(sakId: SakId): List<Personopplysninger> {
        return personopplysningerRepo.hent(sakId)
    }

    override fun mottaPersonopplysninger(journalpostId: String, personopplysninger: List<Personopplysninger>) {
        val sakId = sakRepo.hentSakDetaljerForJournalpostId(journalpostId)?.id
            ?: throw IllegalStateException("Fant ikke sak med journalpostId $journalpostId. Kunne ikke oppdatere personopplysninger")

        val personopplysningerFraDb = personopplysningerRepo.hent(sakId)

        // Metoden heter mottaPersonopplysninger, men her endrer vi bare på skjerming?!
        val personopplysningerMedSkjerming = personopplysningerFraDb.map {
            when (it) {
                is PersonopplysningerBarnMedIdent -> it.copy(skjermet = personopplysninger.barnMedIdent(it.ident)?.skjermet)
                is PersonopplysningerBarnUtenIdent -> it
                is PersonopplysningerSøker -> it.copy(skjermet = personopplysninger.søker(it.ident)?.skjermet)
            }
        }
        // Hvis personopplysninger ikke er endret trenger vi ikke oppdatere
        if (personopplysningerMedSkjerming.erLik(personopplysningerFraDb)) return

        personopplysningerRepo.lagre(sakId, personopplysningerMedSkjerming)
    }

    // Dette er egentlig akkurat det samme som det over!
    override fun mottaSkjerming(journalpostId: String, skjerming: Skjerming) {
        val sakId = sakRepo.hentSakDetaljerForJournalpostId(journalpostId)?.id
            ?: throw IllegalStateException("Fant ikke sak med journalpostId $journalpostId. Kunne ikke oppdatere personopplysninger")

        val personopplysninger = personopplysningerRepo.hent(sakId)

        val oppdatertePersonopplysninger = personopplysninger.map {
            when (it) {
                is PersonopplysningerBarnMedIdent -> it.copy(
                    skjermet = skjerming
                        .barn.firstOrNull { barn -> barn.ident == it.ident }?.skjerming,
                )

                is PersonopplysningerBarnUtenIdent -> it
                is PersonopplysningerSøker -> it.copy(
                    skjermet = skjerming.søker.skjerming,
                )
            }
        }
        personopplysningerRepo.lagre(sakId, oppdatertePersonopplysninger)
    }
}

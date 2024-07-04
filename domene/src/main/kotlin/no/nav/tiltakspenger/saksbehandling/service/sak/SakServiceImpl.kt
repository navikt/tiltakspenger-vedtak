package no.nav.tiltakspenger.saksbehandling.service.sak

import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.exceptions.IkkeFunnetException
import no.nav.tiltakspenger.felles.exceptions.TilgangException
import no.nav.tiltakspenger.innsending.domene.Aktivitetslogg
import no.nav.tiltakspenger.innsending.domene.meldinger.PersonopplysningerMottattHendelse
import no.nav.tiltakspenger.innsending.domene.tolkere.AlderTolker
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknad
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.SakPersonopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.sak.Sak
import no.nav.tiltakspenger.saksbehandling.domene.sak.SaksnummerGenerator
import no.nav.tiltakspenger.saksbehandling.domene.skjerming.Skjerming
import no.nav.tiltakspenger.saksbehandling.ports.BehandlingRepo
import no.nav.tiltakspenger.saksbehandling.ports.PersonGateway
import no.nav.tiltakspenger.saksbehandling.ports.SakRepo
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService
import no.nav.tiltakspenger.saksbehandling.service.søker.SøkerMediator
import java.time.LocalDateTime

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

class SakServiceImpl(
    val sakRepo: SakRepo,
    val behandlingRepo: BehandlingRepo,
    val behandlingService: BehandlingService,
    val personGateway: PersonGateway,
    val søkerMediator: SøkerMediator,
) : SakService {
    override fun motta(søknad: Søknad): Sak {
        val sak: Sak =
            (
                sakRepo.hentForIdentMedPeriode(
                    fnr = søknad.personopplysninger.ident,
                    periode = søknad.vurderingsperiode(),
                ).singleOrNull() ?: Sak.lagSak(
                    søknad = søknad,
                    saksnummer = SaksnummerGenerator().genererSaknummer(sakRepo.hentNesteLøpenr()),
                    sakPersonopplysninger = SakPersonopplysninger(
                        liste = runBlocking { personGateway.hentPerson(søknad.personopplysninger.ident) },
                    ),
                )
                ).håndter(søknad = søknad)

        return sakRepo.lagre(sak).also {
            // TODO jah: Skal slettes når vi tar ned RnR.
            søkerMediator.håndter(
                PersonopplysningerMottattHendelse(
                    aktivitetslogg = Aktivitetslogg(),
                    journalpostId = søknad.journalpostId,
                    ident = søknad.personopplysninger.ident,
                    personopplysninger = sak.personopplysninger.liste,
                    tidsstempelPersonopplysningerInnhentet = LocalDateTime.now(),
                ),
            )
            // TODO jah: Vil helst refaktorere dette, men beholder det inntil videre. Nå er hovedoppgaven av vi skal avvikle RnR.
            oppdaterAldersvilkår(
                sak = sak,
            )
        }
    }

    private fun oppdaterAldersvilkår(
        sak: Sak,
    ) {
        sak.behandlinger.filterIsInstance<Førstegangsbehandling>().forEach { behandling ->
            AlderTolker.tolkeData(sak.personopplysninger.søker().fødselsdato, sak.periode).forEach {
                behandlingService.leggTilSaksopplysning(behandling, it)
            }
        }
    }

    override fun mottaSkjerming(journalpostId: String, skjerming: Skjerming): Sak {
        val sak = sakRepo.hentForJournalpostId(journalpostId)
            ?: throw IllegalStateException("Fant ikke sak med journalpostId $journalpostId. Kunne ikke oppdatere skjerming")

        val oppdatertSak = sak.copy(
            personopplysninger = sak.personopplysninger.medSkjermingFra(lagMapAvSkjerming(skjerming)),
        )
        return sakRepo.lagre(oppdatertSak)
    }

    override fun hentMedBehandlingIdOrNull(behandlingId: BehandlingId): Sak? {
        val behandling = behandlingRepo.hentOrNull(behandlingId) ?: return null
        return sakRepo.hent(behandling.sakId)
    }

    override fun hentMedBehandlingId(behandlingId: BehandlingId, saksbehandler: Saksbehandler): Sak {
        val behandling = behandlingRepo.hent(behandlingId)
        val sak = sakRepo.hent(behandling.sakId) ?: throw IkkeFunnetException("Sak ikke funnet")
        if (!sak.personopplysninger.harTilgang(saksbehandler)) {
            throw TilgangException("Saksbehandler ${saksbehandler.navIdent} har ikke tilgang til sak ${sak.id}")
        }
        return sak
    }

    override fun hentForIdent(ident: String, saksbehandler: Saksbehandler): List<Sak> {
        val saker = sakRepo.hentForIdent(ident)
        saker.forEach { sak ->
            if (!sak.personopplysninger.harTilgang(saksbehandler)) {
                throw TilgangException("Saksbehandler ${saksbehandler.navIdent} har ikke tilgang til sak ${sak.id}")
            }
        }

        return saker
    }

    override fun hentForSaksnummer(saksnummer: String, saksbehandler: Saksbehandler): Sak {
        val sak = sakRepo.hentForSaksnummer(saksnummer)
            ?: throw IkkeFunnetException("Fant ikke sak med saksnummer $saksnummer")
        if (!sak.personopplysninger.harTilgang(saksbehandler)) {
            throw TilgangException("Saksbehandler ${saksbehandler.navIdent} har ikke tilgang til sak ${sak.id}")
        }
        return sak
    }

    override fun resettLøpenr() {
        sakRepo.resetLøpenummer()
    }

    private fun lagMapAvSkjerming(skjerming: Skjerming) =
        (skjerming.barn + skjerming.søker).associate { it.ident to it.skjerming }
}

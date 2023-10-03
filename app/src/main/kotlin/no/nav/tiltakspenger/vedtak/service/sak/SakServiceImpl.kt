package no.nav.tiltakspenger.vedtak.service.sak

import no.nav.tiltakspenger.domene.behandling.Søknadsbehandling
import no.nav.tiltakspenger.domene.sak.Sak
import no.nav.tiltakspenger.domene.sak.Saksnummer
import no.nav.tiltakspenger.domene.sak.SaksnummerGenerator
import no.nav.tiltakspenger.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.domene.saksopplysning.TypeSaksopplysning
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.felles.mars
import no.nav.tiltakspenger.objectmothers.ObjectMother
import no.nav.tiltakspenger.vedtak.Innsending
import no.nav.tiltakspenger.vedtak.Personopplysninger
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vedtak.Tiltaksaktivitet
import no.nav.tiltakspenger.vedtak.repository.sak.SakRepo
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår

class SakServiceImpl(
    val sakRepo: SakRepo,
) : SakService {
    override fun motta(søknad: Søknad): Sak {
        val sak: Sak =
            sakRepo.findByFnrAndPeriode(
                fnr = søknad.personopplysninger.ident,
                periode = søknad.vurderingsperiode(),
            ).singleOrNull() ?: Sak.lagSak(
                søknad = søknad,
                saksnummerGenerator = SaksnummerGenerator(),
            )

        val håndtertSak = sak.håndter(søknad = søknad)

        // TODO kanskje man her skal hente saksopplysninger via sak eller behandling?

        return sakRepo.save(håndtertSak)
    }

    override fun mottaPersonopplysninger(personopplysninger: List<Personopplysninger>): Sak {
        TODO("Not yet implemented")
    }

    // TODO Her må vi finne på noe lurt... Denne er midlertidig til vi finner ut av hvordan vi skal hente Saksopplysninger
    override fun mottaInnsending(innsending: Innsending): Sak {
        val sak = sakRepo.findByFnrAndPeriode(
            fnr = innsending.ident,
            periode = innsending.vurderingsperiodeForSøknad()!!,
        ).singleOrNull() ?: Sak.lagSak(
            søknad = innsending.søknad!!,
            saksnummerGenerator = SaksnummerGenerator(),
        )

        val sakMedSøknad = sak.håndter(innsending.søknad!!)
        val sakVilkårsvurdert = sakMedSøknad.mottaFakta(lagFaktaAvInnsending(innsending))

        return sakRepo.save(sakVilkårsvurdert)
    }

    override fun henteEllerOppretteSak(periode: Periode, fnr: String): Sak {
        TODO()
    }

    override fun henteMedBehandlingsId(behandlingId: BehandlingId): Sak {
        val behandling = Søknadsbehandling.Opprettet.opprettBehandling(
            søknad = ObjectMother.nySøknadMedTiltak(
                tiltak = ObjectMother.arenaTiltak(
                    arrangoernavn = "Art Vandeley",
                    tiltakskode = Tiltaksaktivitet.Tiltak.AMO,
                    opprinneligStartdato = 1.januar(2023),
                    opprinneligSluttdato = 31.mars(2023),
                    startdato = 1.januar(2023),
                    sluttdato = 31.mars(2023),
                ),
            ),
        )

        println("Vi lager behandling :")
        println("$behandling")
        return Sak(
            id = SakId.random(),
            saknummer = Saksnummer("123"),
            periode = Periode(fra = 1.januar(2023), til = 31.mars(2023)),
            behandlinger = listOf(
                behandling.vilkårsvurder(
                    listOf(
                        Saksopplysning.Aap(
                            fom = 1.januar(2023),
                            tom = 31.januar(2023),
                            vilkår = Vilkår.AAP,
                            kilde = Kilde.ARENA,
                            detaljer = "",
                            typeSaksopplysning = TypeSaksopplysning.HAR_YTELSE,
                        ),
                        Saksopplysning.Dagpenger.initSaksopplysning(
                            periode = Periode(fra = 1.januar(2023), til = 31.mars(2023)),
                        ),
                    ),
                ),
            ),
            personopplysninger = listOf(ObjectMother.personopplysningMaxFyr()),

        )
    }

    private fun lagFaktaAvInnsending(innsending: Innsending): List<Saksopplysning> {
        val saksopplysningDagpenger =
            Saksopplysning.Dagpenger.lagFakta(innsending.ytelser?.ytelserliste, innsending.filtreringsperiode())
        val saksopplysningAap =
            Saksopplysning.Aap.lagSaksopplysninger(innsending.ytelser?.ytelserliste, innsending.filtreringsperiode())
        return saksopplysningAap + saksopplysningDagpenger
    }
}

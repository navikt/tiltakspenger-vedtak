package no.nav.tiltakspenger.vedtak.routes.rivers

import io.kotest.matchers.shouldBe
import io.ktor.client.request.setBody
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import io.ktor.http.path
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import io.ktor.server.util.url
import io.mockk.every
import io.mockk.mockk
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.tiltakspenger.innsending.ports.InnsendingRepository
import no.nav.tiltakspenger.objectmothers.ObjectMother.innsendingRegistrert
import no.nav.tiltakspenger.objectmothers.ObjectMother.nySøker
import no.nav.tiltakspenger.saksbehandling.ports.BehandlingRepo
import no.nav.tiltakspenger.saksbehandling.ports.BrevPublisherGateway
import no.nav.tiltakspenger.saksbehandling.ports.MeldekortGrunnlagGateway
import no.nav.tiltakspenger.saksbehandling.ports.MultiRepo
import no.nav.tiltakspenger.saksbehandling.ports.PersonopplysningerRepo
import no.nav.tiltakspenger.saksbehandling.ports.SakRepo
import no.nav.tiltakspenger.saksbehandling.service.sak.SakServiceImpl
import no.nav.tiltakspenger.saksbehandling.service.utbetaling.UtbetalingService
import no.nav.tiltakspenger.saksbehandling.service.vedtak.VedtakServiceImpl
import no.nav.tiltakspenger.vedtak.InnsendingMediatorImpl
import no.nav.tiltakspenger.vedtak.SøkerMediator
import no.nav.tiltakspenger.vedtak.repository.attestering.AttesteringRepoImpl
import no.nav.tiltakspenger.vedtak.repository.søker.SøkerRepositoryImpl
import no.nav.tiltakspenger.vedtak.repository.vedtak.VedtakRepo
import no.nav.tiltakspenger.vedtak.routes.defaultRequest
import no.nav.tiltakspenger.vedtak.routes.jacksonSerialization
import no.nav.tiltakspenger.vedtak.routes.rivers.søknad.søknadRoutes
import no.nav.tiltakspenger.vedtak.routes.rivers.søknad.søknadpath
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class SøknadRoutesTest {
    private companion object {
        const val IDENT = "04927799109"
        const val JOURNALPOSTID = "foobar2"
    }

    private val innsendingRepository = mockk<InnsendingRepository>(relaxed = true)
    private val søkerRepository = mockk<SøkerRepositoryImpl>(relaxed = true)
    private val sakRepo = mockk<SakRepo>(relaxed = true)
    private val behandlingRepo = mockk<BehandlingRepo>(relaxed = true)
    private val vedtakRepo = mockk<VedtakRepo>(relaxed = true)
    private val vedtakService = mockk<VedtakServiceImpl>(relaxed = true)
    private val attesteringRepo = mockk<AttesteringRepoImpl>(relaxed = true)
    private val testRapid = TestRapid()
    private val innsendingMediator = InnsendingMediatorImpl(
        innsendingRepository = innsendingRepository,
        rapidsConnection = testRapid,
        observatører = listOf(),
    )
    private val søkerMediator = SøkerMediator(
        søkerRepository = søkerRepository,
        rapidsConnection = testRapid,
    )
    private val personopplysningRepo = mockk<PersonopplysningerRepo>(relaxed = true)
    private val utbetalingService = mockk<UtbetalingService>(relaxed = true)
    private val brevPublisherGateway = mockk<BrevPublisherGateway>(relaxed = true)
    private val meldekortGrunnlagGateway = mockk<MeldekortGrunnlagGateway>(relaxed = true)
    private val multiRepo = mockk<MultiRepo>(relaxed = true)

    private val behandlingService =
        no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingServiceImpl(
            behandlingRepo,
            vedtakRepo,
            personopplysningRepo,
            utbetalingService,
            brevPublisherGateway,
            meldekortGrunnlagGateway,
            multiRepo,
        )
    private val sakService = SakServiceImpl(sakRepo, behandlingRepo, behandlingService)

    @AfterEach
    fun reset() {
        testRapid.reset()
    }

    @Test
    fun `sjekk at kall til river søknad route sender ut et behov`() {
        every { innsendingRepository.hent(JOURNALPOSTID) } returns innsendingRegistrert(
            ident = IDENT,
            journalpostId = JOURNALPOSTID,
        )

        every { søkerRepository.findByIdent(IDENT) } returns nySøker(
            ident = IDENT,
        )

        every { sakRepo.hentForIdentMedPeriode(any(), any()) } returns emptyList()
        every { sakRepo.lagre(any()) } returnsArgument 0

        testApplication {
            application {
                // vedtakTestApi()
                jacksonSerialization()
                routing {
                    søknadRoutes(
                        innsendingMediator = innsendingMediator,
                        søkerMediator = søkerMediator,
                        sakService = sakService,
                    )
                }
            }
            defaultRequest(
                HttpMethod.Post,
                url {
                    protocol = URLProtocol.HTTPS
                    path(søknadpath)
                },
            ) {
                setBody(søknadBodyV3)
            }
                .apply {
                    status shouldBe HttpStatusCode.OK
                }
        }
        with(testRapid.inspektør) {
            Assertions.assertEquals(1, size)
            Assertions.assertEquals("behov", field(0, "@event_name").asText())
            Assertions.assertEquals("personopplysninger", field(0, "@behov")[0].asText())
            Assertions.assertEquals(IDENT, field(0, "ident").asText())
        }
    }

    private val søknadBodyV3 = """
        {
            "versjon": "3",
            "søknadId": "735ac33a-bf3b-43c0-a331-e9ac99bdd6f8",
            "dokInfo": {
              "journalpostId": "$JOURNALPOSTID",
              "dokumentInfoId": "987",
              "filnavn": "tiltakspengersoknad.json"
            },
            "personopplysninger": {
              "ident": "$IDENT",
              "fornavn": "NØDVENDIG",
              "etternavn": "HOFTE"
            },
            "tiltak": {
              "id": "123",
              "arrangør": "Testarrangør",
              "typeKode": "Annen utdanning",
              "typeNavn": "Annen utdanning",
              "deltakelseFom": "2025-04-01",
              "deltakelseTom": "2025-04-10"
            },
            "barnetilleggPdl": [
              {
                "fødselsdato": "2010-02-13",
                "fornavn": "INKLUDERENDE",
                "mellomnavn": null,
                "etternavn": "DIVA",
                "oppholderSegIEØS": {
                  "svar": "Ja"
                }
              }
            ],
            "barnetilleggManuelle": [],
            "vedlegg": [
              {
                "journalpostId": "123",
                "dokumentInfoId": "456",
                "filnavn": "tiltakspengersoknad.json"
              }
            ],
            "kvp": {
              "svar": "Nei",
              "fom": null,
              "tom": null
            },
            "intro": {
              "svar": "Nei",
              "fom": null,
              "tom": null
            },
            "institusjon": {
              "svar": "Nei",
              "fom": null,
              "tom": null
            },
            "etterlønn": {
              "svar": "Nei"
            },
            "gjenlevendepensjon": {
              "svar": "Nei",
              "fom": null,
              "tom": null
            },
            "alderspensjon": {
              "svar": "Nei",
              "fom": null
            },
            "sykepenger": {
              "svar": "Nei",
              "fom": null,
              "tom": null
            },
            "supplerendeStønadAlder": {
              "svar": "Nei",
              "fom": null,
              "tom": null
            },
            "supplerendeStønadFlyktning": {
              "svar": "Nei",
              "fom": null,
              "tom": null
            },
            "jobbsjansen": {
              "svar": "Nei",
              "fom": null,
              "tom": null
            },
            "trygdOgPensjon": {
              "svar": "Nei",
              "fom": null,
              "tom": null
            },
            "opprettet": "2023-06-14T21:12:08.447993177"
        }
    """.trimIndent()
}

package no.nav.tiltakspenger.vedtak.routes.kravfrist

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotest.matchers.shouldBe
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import io.ktor.http.path
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import io.ktor.server.util.url
import io.mockk.every
import io.mockk.mockk
import no.nav.tiltakspenger.felles.Rolle
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.random
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.objectmothers.ObjectMother
import no.nav.tiltakspenger.objectmothers.ObjectMother.nySøknad
import no.nav.tiltakspenger.objectmothers.ObjectMother.personopplysningFødselsdato
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saksnummer
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingServiceImpl
import no.nav.tiltakspenger.vedtak.clients.brevpublisher.BrevPublisherGatewayImpl
import no.nav.tiltakspenger.vedtak.clients.defaultObjectMapper
import no.nav.tiltakspenger.vedtak.clients.meldekort.MeldekortGrunnlagGatewayImpl
import no.nav.tiltakspenger.vedtak.clients.tiltak.TiltakGatewayImpl
import no.nav.tiltakspenger.vedtak.db.TestDataHelper
import no.nav.tiltakspenger.vedtak.db.persisterOpprettetFørstegangsbehandling
import no.nav.tiltakspenger.vedtak.db.withMigratedDb
import no.nav.tiltakspenger.vedtak.routes.behandling.behandlingPath
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.SamletUtfallDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.kravfrist.KravfristVilkårDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.kravfrist.kravfristRoutes
import no.nav.tiltakspenger.vedtak.routes.defaultRequest
import no.nav.tiltakspenger.vedtak.routes.jacksonSerialization
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

internal class KravfristRoutesTest {
    private val mockInnloggetSaksbehandlerProvider = mockk<InnloggetSaksbehandlerProvider>()
    private val mockBrevPublisherGateway = mockk<BrevPublisherGatewayImpl>()
    private val mockMeldekortGrunnlagGateway = mockk<MeldekortGrunnlagGatewayImpl>()
    private val mockTiltakGateway = mockk<TiltakGatewayImpl>()

    private val objectMapper: ObjectMapper = defaultObjectMapper()
    private val saksbehandler = Saksbehandler(
        "Q123456",
        "Superman",
        "a@b.c",
        listOf(Rolle.SAKSBEHANDLER, Rolle.SKJERMING, Rolle.STRENGT_FORTROLIG_ADRESSE),
    )

    @Test
    fun `test at endepunkt for henting av kravfrist fungerer og blir OPPFYLT`() {
        every { mockInnloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(any()) } returns saksbehandler

        val periodeFraOgMed = LocalDate.now().minusMonths(2)
        val periodeTilOgMed = LocalDate.now().minusMonths(1)

        val sakId = SakId.random()
        val saksnummer = Saksnummer("202301011001")
        val fnr = Fnr.random()
        val søknad = nySøknad(
            periode = Periode(fraOgMed = periodeFraOgMed, tilOgMed = periodeTilOgMed),
            tidsstempelHosOss = LocalDateTime.now(),
        )
        val saksbehandler = ObjectMother.saksbehandler()

        val registrerteTiltak = listOf(
            ObjectMother.tiltak(deltakelseTom = periodeTilOgMed, deltakelseFom = periodeFraOgMed),
        )
        val objectMotherSak = ObjectMother.sakMedOpprettetBehandling(
            sakId = sakId,
            saksnummer = saksnummer,
            fnr = fnr,
            søknad = søknad,
            fødselsdato = personopplysningFødselsdato(),
            saksbehandler = saksbehandler,
            registrerteTiltak = registrerteTiltak,
        )

        val behandlingId = objectMotherSak.behandlinger.first().id.toString()

        withMigratedDb { dataSource ->
            val testDataHelper = TestDataHelper(dataSource)

            testDataHelper.sessionFactory.withTransaction {
                testDataHelper.sakRepo.lagre(objectMotherSak)
            }

            val behandlingService = BehandlingServiceImpl(
                behandlingRepo = testDataHelper.behandlingRepo,
                vedtakRepo = testDataHelper.vedtakRepo,
                personopplysningRepo = testDataHelper.personopplysningerRepo,
                brevPublisherGateway = mockBrevPublisherGateway,
                meldekortGrunnlagGateway = mockMeldekortGrunnlagGateway,
                sakRepo = testDataHelper.sakRepo,
                attesteringRepo = testDataHelper.attesteringRepo,
                sessionFactory = testDataHelper.sessionFactory,
                saksoversiktRepo = testDataHelper.saksoversiktRepo,
            )

            testApplication {
                application {
                    jacksonSerialization()
                    routing {
                        kravfristRoutes(
                            innloggetSaksbehandlerProvider = mockInnloggetSaksbehandlerProvider,
                            behandlingService = behandlingService,
                        )
                    }
                }

                // Sjekk at man kan kjøre Get
                defaultRequest(
                    HttpMethod.Get,
                    url {
                        protocol = URLProtocol.HTTPS
                        path("$behandlingPath/$behandlingId/vilkar/kravfrist")
                    },
                ).apply {
                    status shouldBe HttpStatusCode.OK
                    val kravfristVilkår = objectMapper.readValue<KravfristVilkårDTO>(bodyAsText())
                    kravfristVilkår.samletUtfall shouldBe SamletUtfallDTO.OPPFYLT
                }
            }
        }
    }

    @Test
    fun `test at kravdato gir IKKE_OPPFYLT om det er søkt for lenge etter fristen`() {
        every { mockInnloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(any()) } returns saksbehandler

        withMigratedDb { dataSource ->
            val testDataHelper = TestDataHelper(dataSource)

            val (sak, _) = testDataHelper.persisterOpprettetFørstegangsbehandling(
                deltakelseFom = 1.januar(2021),
                deltakelseTom = 31.januar(2021),
            )
            val behandlingId = sak.førstegangsbehandling.id

            val behandlingService = BehandlingServiceImpl(
                behandlingRepo = testDataHelper.behandlingRepo,
                vedtakRepo = testDataHelper.vedtakRepo,
                personopplysningRepo = testDataHelper.personopplysningerRepo,
                brevPublisherGateway = mockBrevPublisherGateway,
                meldekortGrunnlagGateway = mockMeldekortGrunnlagGateway,
                sakRepo = testDataHelper.sakRepo,
                attesteringRepo = testDataHelper.attesteringRepo,
                sessionFactory = testDataHelper.sessionFactory,
                saksoversiktRepo = testDataHelper.saksoversiktRepo,
            )

            testApplication {
                application {
                    jacksonSerialization()
                    routing {
                        kravfristRoutes(
                            innloggetSaksbehandlerProvider = mockInnloggetSaksbehandlerProvider,
                            behandlingService = behandlingService,
                        )
                    }
                }

                // Sjekk at man kan kjøre Get
                defaultRequest(
                    HttpMethod.Get,
                    url {
                        protocol = URLProtocol.HTTPS
                        path("$behandlingPath/$behandlingId/vilkar/kravfrist")
                    },
                ).apply {
                    status shouldBe HttpStatusCode.OK
                    val kravfristVilkår = objectMapper.readValue<KravfristVilkårDTO>(bodyAsText())
                    kravfristVilkår.samletUtfall shouldBe SamletUtfallDTO.IKKE_OPPFYLT
                }
            }
        }
    }
}

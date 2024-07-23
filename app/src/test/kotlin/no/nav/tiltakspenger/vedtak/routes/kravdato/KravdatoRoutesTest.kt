package no.nav.tiltakspenger.vedtak.routes.kravdato

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
import kotliquery.sessionOf
import no.nav.tiltakspenger.felles.Rolle
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.objectmothers.ObjectMother
import no.nav.tiltakspenger.objectmothers.ObjectMother.nySøknad
import no.nav.tiltakspenger.objectmothers.ObjectMother.personopplysningFødselsdato
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingServiceImpl
import no.nav.tiltakspenger.saksbehandling.service.utbetaling.UtbetalingServiceImpl
import no.nav.tiltakspenger.vedtak.clients.brevpublisher.BrevPublisherGatewayImpl
import no.nav.tiltakspenger.vedtak.clients.defaultObjectMapper
import no.nav.tiltakspenger.vedtak.clients.meldekort.MeldekortGrunnlagGatewayImpl
import no.nav.tiltakspenger.vedtak.clients.tiltak.TiltakGatewayImpl
import no.nav.tiltakspenger.vedtak.db.DataSource
import no.nav.tiltakspenger.vedtak.db.PostgresTestcontainer
import no.nav.tiltakspenger.vedtak.db.flywayMigrate
import no.nav.tiltakspenger.vedtak.repository.attestering.AttesteringRepoImpl
import no.nav.tiltakspenger.vedtak.repository.behandling.PostgresBehandlingRepo
import no.nav.tiltakspenger.vedtak.repository.behandling.SaksopplysningRepo
import no.nav.tiltakspenger.vedtak.repository.behandling.TiltakDAO
import no.nav.tiltakspenger.vedtak.repository.behandling.UtfallsperiodeDAO
import no.nav.tiltakspenger.vedtak.repository.behandling.VurderingRepo
import no.nav.tiltakspenger.vedtak.repository.multi.MultiRepoImpl
import no.nav.tiltakspenger.vedtak.repository.sak.PersonopplysningerBarnMedIdentRepo
import no.nav.tiltakspenger.vedtak.repository.sak.PersonopplysningerBarnUtenIdentRepo
import no.nav.tiltakspenger.vedtak.repository.sak.PostgresPersonopplysningerRepo
import no.nav.tiltakspenger.vedtak.repository.sak.PostgresSakRepo
import no.nav.tiltakspenger.vedtak.repository.søknad.BarnetilleggDAO
import no.nav.tiltakspenger.vedtak.repository.søknad.SøknadDAO
import no.nav.tiltakspenger.vedtak.repository.søknad.SøknadTiltakDAO
import no.nav.tiltakspenger.vedtak.repository.søknad.VedleggDAO
import no.nav.tiltakspenger.vedtak.repository.vedtak.VedtakRepoImpl
import no.nav.tiltakspenger.vedtak.routes.behandling.behandlingPath
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.SamletUtfallDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.kravdato.KravdatoVilkårDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.kravdato.kravdatoRoutes
import no.nav.tiltakspenger.vedtak.routes.defaultRequest
import no.nav.tiltakspenger.vedtak.routes.jacksonSerialization
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDate
import java.time.LocalDateTime

@Testcontainers
class KravdatoRoutesTest {

    companion object {
        @Container
        val postgresContainer = PostgresTestcontainer
    }

    @BeforeEach
    fun setup() {
        flywayMigrate()
    }

    private val mockInnloggetSaksbehandlerProvider = mockk<InnloggetSaksbehandlerProvider>()
    private val mockedUtbetalingServiceImpl = mockk<UtbetalingServiceImpl>()
    private val mockBrevPublisherGateway = mockk<BrevPublisherGatewayImpl>()
    private val mockMeldekortGrunnlagGateway = mockk<MeldekortGrunnlagGatewayImpl>()
    private val mockTiltakGateway = mockk<TiltakGatewayImpl>()

    private val saksopplysningRepo = SaksopplysningRepo()
    private val behandlingRepo = PostgresBehandlingRepo(
        saksopplysningRepo = saksopplysningRepo,
        vurderingRepo = VurderingRepo(),
        søknadDAO = SøknadDAO(
            barnetilleggDAO = BarnetilleggDAO(),
            tiltakDAO = SøknadTiltakDAO(),
            vedleggDAO = VedleggDAO(),
        ),
        tiltakDAO = TiltakDAO(),
        utfallsperiodeDAO = UtfallsperiodeDAO(),
    )

    private val vedtakRepo = VedtakRepoImpl(behandlingRepo = behandlingRepo, utfallsperiodeDAO = UtfallsperiodeDAO())
    private val attesteringDAO = AttesteringRepoImpl()
    private val vedtakRepoImpl = VedtakRepoImpl()
    private val multiRepo =
        MultiRepoImpl(behandlingDao = behandlingRepo, attesteringDao = attesteringDAO, vedtakDao = vedtakRepoImpl)

    private val personopplysningerRepo = PostgresPersonopplysningerRepo(
        barnMedIdentDAO = PersonopplysningerBarnMedIdentRepo(),
        barnUtenIdentDAO = PersonopplysningerBarnUtenIdentRepo(),
    )

    private val sakRepo = PostgresSakRepo(
        behandlingRepo = behandlingRepo,
        personopplysningerRepo = PostgresPersonopplysningerRepo(
            barnMedIdentDAO = PersonopplysningerBarnMedIdentRepo(),
            barnUtenIdentDAO = PersonopplysningerBarnUtenIdentRepo(),
        ),
        vedtakRepo = vedtakRepo,
    )

    private val behandlingService = BehandlingServiceImpl(
        behandlingRepo = behandlingRepo,
        vedtakRepo = vedtakRepo,
        personopplysningRepo = personopplysningerRepo,
        utbetalingService = mockedUtbetalingServiceImpl,
        brevPublisherGateway = mockBrevPublisherGateway,
        meldekortGrunnlagGateway = mockMeldekortGrunnlagGateway,
        tiltakGateway = mockTiltakGateway,
        multiRepo = multiRepo,
        sakRepo = sakRepo,

    )

    private val objectMapper: ObjectMapper = defaultObjectMapper()
    private val mockSaksbehandler = Saksbehandler(
        "Q123456",
        "Superman",
        "a@b.c",
        listOf(Rolle.SAKSBEHANDLER, Rolle.SKJERMING, Rolle.STRENGT_FORTROLIG_ADRESSE),
    )

    @Test
    fun `test at endepunkt for henting av kravdato fungerer og blir OPPFYLT`() {
        every { mockInnloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(any()) } returns mockSaksbehandler

        val sakId = SakId.random()
        val søknad = nySøknad(
            periode = Periode(fraOgMed = LocalDate.now().minusMonths(2), tilOgMed = LocalDate.now().minusMonths(1)),
            tidsstempelHosOss = LocalDateTime.now(),
        )

        val objectMotherSak = ObjectMother.sakMedOpprettetBehandling(id = sakId, behandlinger = listOf(Førstegangsbehandling.opprettBehandling(sakId, søknad, personopplysningFødselsdato())))

        sessionOf(DataSource.hikariDataSource).use {
            sakRepo.lagre(objectMotherSak)
        }

        val behandlingId = objectMotherSak.behandlinger.first().id.toString()

        testApplication {
            application {
                jacksonSerialization()
                routing {
                    kravdatoRoutes(
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
                    path("$behandlingPath/$behandlingId/vilkar/kravdato")
                },
            ).apply {
                status shouldBe HttpStatusCode.OK
                val kravdatoVilkår = objectMapper.readValue<KravdatoVilkårDTO>(bodyAsText())
                kravdatoVilkår.samletUtfall shouldBe SamletUtfallDTO.OPPFYLT
            }
        }
    }

    @Test
    fun `test at kravdato gir IKKE_OPPFYLT om det er søkt for lenge etter fristen`() {
        every { mockInnloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(any()) } returns mockSaksbehandler

        val sakId = SakId.random()
        val søknad = nySøknad(
            periode = Periode(fraOgMed = LocalDate.now().minusMonths(2), tilOgMed = LocalDate.now().minusMonths(1)),
            tidsstempelHosOss = LocalDateTime.now().minusMonths(4),
        )

        val objectMotherSak = ObjectMother.sakMedOpprettetBehandling(id = sakId, behandlinger = listOf(Førstegangsbehandling.opprettBehandling(sakId, søknad, personopplysningFødselsdato())))

        sessionOf(DataSource.hikariDataSource).use {
            sakRepo.lagre(objectMotherSak)
        }

        val behandlingId = objectMotherSak.behandlinger.first().id.toString()

        testApplication {
            application {
                jacksonSerialization()
                routing {
                    kravdatoRoutes(
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
                    path("$behandlingPath/$behandlingId/vilkar/kravdato")
                },
            ).apply {
                status shouldBe HttpStatusCode.OK
                val kravdatoVilkår = objectMapper.readValue<KravdatoVilkårDTO>(bodyAsText())
                kravdatoVilkår.samletUtfall shouldBe SamletUtfallDTO.IKKE_OPPFYLT
            }
        }
    }
}

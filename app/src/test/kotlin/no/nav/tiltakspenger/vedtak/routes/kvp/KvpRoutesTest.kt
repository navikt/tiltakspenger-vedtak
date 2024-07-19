package no.nav.tiltakspenger.vedtak.routes.kvp

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import io.ktor.http.path
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import io.ktor.server.util.url
import io.mockk.every
import io.mockk.mockk
import kotliquery.sessionOf
import no.nav.tiltakspenger.felles.Rolle
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.objectmothers.ObjectMother
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingServiceImpl
import no.nav.tiltakspenger.saksbehandling.service.behandling.vilkår.kvp.KvpVilkårServiceImpl
import no.nav.tiltakspenger.saksbehandling.service.utbetaling.UtbetalingServiceImpl
import no.nav.tiltakspenger.vedtak.clients.brevpublisher.BrevPublisherGatewayImpl
import no.nav.tiltakspenger.vedtak.clients.defaultObjectMapper
import no.nav.tiltakspenger.vedtak.clients.meldekort.MeldekortGrunnlagGatewayImpl
import no.nav.tiltakspenger.vedtak.clients.tiltak.TiltakGatewayImpl
import no.nav.tiltakspenger.vedtak.db.DataSource
import no.nav.tiltakspenger.vedtak.db.PostgresTestcontainer
import no.nav.tiltakspenger.vedtak.db.flywayMigrate
import no.nav.tiltakspenger.vedtak.repository.attestering.AttesteringRepoImpl
import no.nav.tiltakspenger.vedtak.repository.behandling.KravdatoSaksopplysningRepo
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
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.kvp.KVPVilkårDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.kvp.KildeDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.kvp.KvpSaksopplysningDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.kvp.kvpRoutes
import no.nav.tiltakspenger.vedtak.routes.defaultRequest
import no.nav.tiltakspenger.vedtak.routes.dto.PeriodeDTO
import no.nav.tiltakspenger.vedtak.routes.jacksonSerialization
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
class KvpRoutesTest {

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
        kravdatoSaksopplysningRepo = KravdatoSaksopplysningRepo(),
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
    private val kvpVilkårService = KvpVilkårServiceImpl(behandlingRepo, behandlingService)

    private val objectMapper: ObjectMapper = defaultObjectMapper()
    private val periodeBrukerHarKvpEtterEndring = PeriodeDTO(fraOgMed = "2023-01-01", tilOgMed = "2023-01-03")
    private val mockSaksbehandler = Saksbehandler(
        "Q123456",
        "Superman",
        "a@b.c",
        listOf(Rolle.SAKSBEHANDLER, Rolle.SKJERMING, Rolle.STRENGT_FORTROLIG_ADRESSE),
    )

    @Test
    fun `test at endepunkt for henting og lagring av kvp fungerer`() {
        every { mockInnloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(any()) } returns mockSaksbehandler

        val objectMotherSak = ObjectMother.sakMedOpprettetBehandling(
            løpenummer = 1004,
        )

        sessionOf(DataSource.hikariDataSource).use {
            sakRepo.lagre(objectMotherSak)
        }

        val behandlingId = objectMotherSak.behandlinger.first().id.toString()

        testApplication {
            application {
                jacksonSerialization()
                routing {
                    kvpRoutes(
                        innloggetSaksbehandlerProvider = mockInnloggetSaksbehandlerProvider,
                        kvpVilkårService = kvpVilkårService,
                        behandlingService = behandlingService,
                    )
                }
            }

            // Sjekk at man kan kjøre Get
            defaultRequest(
                HttpMethod.Get,
                url {
                    protocol = URLProtocol.HTTPS
                    path("$behandlingPath/$behandlingId/vilkar/kvp")
                },
            ).apply {
                status shouldBe HttpStatusCode.OK
                val kvpVilkår = objectMapper.readValue<KVPVilkårDTO>(bodyAsText())
                kvpVilkår.avklartSaksopplysning.periodeMedDeltagelse.periode shouldNotBe periodeBrukerHarKvpEtterEndring
            }

            // Sjekk at man kan oppdatere data om kvp
            defaultRequest(
                HttpMethod.Post,
                url {
                    protocol = URLProtocol.HTTPS
                    path("$behandlingPath/$behandlingId/vilkar/kvp")
                },
            ) {
                setBody(bodyEndreKvp(periodeBrukerHarKvpEtterEndring, true))
            }.apply {
                status shouldBe HttpStatusCode.Created
            }

            // Hent data
            defaultRequest(
                HttpMethod.Get,
                url {
                    protocol = URLProtocol.HTTPS
                    path("$behandlingPath/$behandlingId/vilkar/kvp")
                },
            ).apply {
                status shouldBe HttpStatusCode.OK
                contentType() shouldBe ContentType.parse("application/json; charset=UTF-8")
                val kvpVilkår = objectMapper.readValue<KVPVilkårDTO>(bodyAsText())

                // sjekker at endringen har skjedd
                kvpVilkår.avklartSaksopplysning.kilde shouldBe KildeDTO.SAKSBEHANDLER
                kvpVilkår.avklartSaksopplysning.periodeMedDeltagelse.periode shouldBe periodeBrukerHarKvpEtterEndring
            }
        }
    }

    @Test
    fun `test at endring av kvp ikke endrer søknadsdata`() {
        every { mockInnloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(any()) } returns mockSaksbehandler

        val objectMotherSak = ObjectMother.sakMedOpprettetBehandling(
            løpenummer = 1003,
        )

        lateinit var originalDatoForKvpFraSøknaden: KvpSaksopplysningDTO

        sessionOf(DataSource.hikariDataSource).use {
            sakRepo.lagre(objectMotherSak)
        }

        val behandlingId = objectMotherSak.behandlinger.first().id.toString()

        testApplication {
            application {
                jacksonSerialization()
                routing {
                    kvpRoutes(
                        innloggetSaksbehandlerProvider = mockInnloggetSaksbehandlerProvider,
                        kvpVilkårService = kvpVilkårService,
                        behandlingService = behandlingService,
                    )
                }
            }

            defaultRequest(
                HttpMethod.Get,
                url {
                    protocol = URLProtocol.HTTPS
                    path("$behandlingPath/$behandlingId/vilkar/kvp")
                },
            ).apply {
                status shouldBe HttpStatusCode.OK
                val kvpVilkår = objectMapper.readValue<KVPVilkårDTO>(bodyAsText())
                originalDatoForKvpFraSøknaden = kvpVilkår.søknadSaksopplysning
            }

            defaultRequest(
                HttpMethod.Post,
                url {
                    protocol = URLProtocol.HTTPS
                    path("$behandlingPath/$behandlingId/vilkar/kvp")
                },
            ) {
                setBody(bodyEndreKvp(periodeBrukerHarKvpEtterEndring, true))
            }.apply {
                status shouldBe HttpStatusCode.Created
            }

            defaultRequest(
                HttpMethod.Get,
                url {
                    protocol = URLProtocol.HTTPS
                    path("$behandlingPath/$behandlingId/vilkar/kvp")
                },
            ).apply {
                status shouldBe HttpStatusCode.OK
                contentType() shouldBe ContentType.parse("application/json; charset=UTF-8")

                val kvpVilkår = objectMapper.readValue<KVPVilkårDTO>(bodyAsText())

                // sjekker at ikke originale
                kvpVilkår.søknadSaksopplysning shouldBe originalDatoForKvpFraSøknaden
            }
        }
    }

    @Test
    fun `test at samlet utfall for kvp blir IKKE_OPPFYLT om bruker går på kvp i vurderingsperioden`() {
        every { mockInnloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(any()) } returns mockSaksbehandler

        val objectMotherSak = ObjectMother.sakMedOpprettetBehandling(
            løpenummer = 1010,
        )

        lateinit var vurderingsPeriode: PeriodeDTO

        sessionOf(DataSource.hikariDataSource).use {
            sakRepo.lagre(objectMotherSak)
        }

        val behandlingId = objectMotherSak.behandlinger.first().id.toString()

        testApplication {
            application {
                jacksonSerialization()
                routing {
                    kvpRoutes(
                        innloggetSaksbehandlerProvider = mockInnloggetSaksbehandlerProvider,
                        kvpVilkårService = kvpVilkårService,
                        behandlingService = behandlingService,
                    )
                }
            }

            defaultRequest(
                HttpMethod.Get,
                url {
                    protocol = URLProtocol.HTTPS
                    path("$behandlingPath/$behandlingId/vilkar/kvp")
                },
            ).apply {
                status shouldBe HttpStatusCode.OK
                val kvpVilkår = objectMapper.readValue<KVPVilkårDTO>(bodyAsText())

                vurderingsPeriode = kvpVilkår.vurderingsperiode
            }

            val bodyKvpDeltarIHelePerioden = bodyEndreKvp(vurderingsPeriode, deltar = true)

            defaultRequest(
                HttpMethod.Post,
                url {
                    protocol = URLProtocol.HTTPS
                    path("$behandlingPath/$behandlingId/vilkar/kvp")
                },
            ) {
                setBody(bodyKvpDeltarIHelePerioden)
            }.apply {
                status shouldBe HttpStatusCode.Created
            }

            defaultRequest(
                HttpMethod.Get,
                url {
                    protocol = URLProtocol.HTTPS
                    path("$behandlingPath/$behandlingId/vilkar/kvp")
                },
            ).apply {
                status shouldBe HttpStatusCode.OK
                contentType() shouldBe ContentType.parse("application/json; charset=UTF-8")

                val kvpVilkår = objectMapper.readValue<KVPVilkårDTO>(bodyAsText())
                kvpVilkår.samletUtfall shouldBe SamletUtfallDTO.IKKE_OPPFYLT
            }
        }
    }

    private fun bodyEndreKvp(periodeDTO: PeriodeDTO, deltar: Boolean): String {
        val deltarString = if (deltar) "true" else "false"
        return """
        {
          "ytelseForPeriode": [
            {
              "periode": {
                "fraOgMed": "${periodeDTO.fraOgMed}",
                "tilOgMed": "${periodeDTO.tilOgMed}"
              },
              "deltar": $deltarString
            }
          ],
          "årsakTilEndring": "FEIL_I_INNHENTET_DATA"
        }
        """.trimIndent()
    }
}

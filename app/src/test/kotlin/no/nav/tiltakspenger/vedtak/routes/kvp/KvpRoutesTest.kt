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
import no.nav.tiltakspenger.felles.Rolle
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingServiceImpl
import no.nav.tiltakspenger.saksbehandling.service.behandling.vilkår.kvp.KvpVilkårServiceImpl
import no.nav.tiltakspenger.vedtak.clients.brevpublisher.BrevPublisherGatewayImpl
import no.nav.tiltakspenger.vedtak.clients.defaultObjectMapper
import no.nav.tiltakspenger.vedtak.clients.meldekort.MeldekortGrunnlagHttpClient
import no.nav.tiltakspenger.vedtak.clients.tiltak.TiltakGatewayImpl
import no.nav.tiltakspenger.vedtak.db.TestDataHelper
import no.nav.tiltakspenger.vedtak.db.persisterOpprettetFørstegangsbehandling
import no.nav.tiltakspenger.vedtak.db.withMigratedDb
import no.nav.tiltakspenger.vedtak.routes.behandling.BEHANDLING_PATH
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.SamletUtfallDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.kvp.KVPVilkårDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.kvp.KildeDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.kvp.KvpSaksopplysningDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.kvp.kvpRoutes
import no.nav.tiltakspenger.vedtak.routes.defaultRequest
import no.nav.tiltakspenger.vedtak.routes.dto.PeriodeDTO
import no.nav.tiltakspenger.vedtak.routes.dto.toDTO
import no.nav.tiltakspenger.vedtak.routes.jacksonSerialization
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider
import org.junit.jupiter.api.Test

class KvpRoutesTest {
    private val mockInnloggetSaksbehandlerProvider = mockk<InnloggetSaksbehandlerProvider>()
    private val mockBrevPublisherGateway = mockk<BrevPublisherGatewayImpl>()
    private val mockMeldekortGrunnlagGateway = mockk<MeldekortGrunnlagHttpClient>()
    private val mockTiltakGateway = mockk<TiltakGatewayImpl>()

    private val objectMapper: ObjectMapper = defaultObjectMapper()
    private val periodeBrukerHarKvpEtterEndring = PeriodeDTO(fraOgMed = "2023-01-01", tilOgMed = "2023-01-03")
    private val saksbehandler =
        Saksbehandler(
            "Q123456",
            "Superman",
            "a@b.c",
            listOf(Rolle.SAKSBEHANDLER, Rolle.SKJERMING, Rolle.STRENGT_FORTROLIG_ADRESSE),
        )

    @Test
    fun `test at endepunkt for henting og lagring av kvp fungerer`() {
        every { mockInnloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(any()) } returns saksbehandler

        withMigratedDb { dataSource ->
            val testDataHelper = TestDataHelper(dataSource)

            val (sak, _) = testDataHelper.persisterOpprettetFørstegangsbehandling(saksbehandler = saksbehandler)
            val behandlingId = sak.førstegangsbehandling.id

            val behandlingService =
                BehandlingServiceImpl(
                    behandlingRepo = testDataHelper.behandlingRepo,
                    vedtakRepo = testDataHelper.vedtakRepo,
                    personopplysningRepo = testDataHelper.personopplysningerRepo,
                    brevPublisherGateway = mockBrevPublisherGateway,
                    meldekortGrunnlagGateway = mockMeldekortGrunnlagGateway,
                    sakRepo = testDataHelper.sakRepo,
                    sessionFactory = testDataHelper.sessionFactory,
                    saksoversiktRepo = testDataHelper.saksoversiktRepo,
                    statistikkSakRepo = testDataHelper.statistikkSakRepo,
                    statistikkStønadRepo = testDataHelper.statistikkStønadRepo,
                )
            val kvpVilkårService = KvpVilkårServiceImpl(
                behandlingRepo = testDataHelper.behandlingRepo,
                behandlingService = behandlingService,
            )

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
                        path("$BEHANDLING_PATH/$behandlingId/vilkar/kvp")
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
                        path("$BEHANDLING_PATH/$behandlingId/vilkar/kvp")
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
                        path("$BEHANDLING_PATH/$behandlingId/vilkar/kvp")
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
    }

    @Test
    fun `test at endring av kvp ikke endrer søknadsdata`() {
        every { mockInnloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(any()) } returns saksbehandler

        lateinit var originalDatoForKvpFraSøknaden: KvpSaksopplysningDTO

        withMigratedDb { dataSource ->
            val testDataHelper = TestDataHelper(dataSource)

            val (sak, _) = testDataHelper.persisterOpprettetFørstegangsbehandling(saksbehandler = saksbehandler)
            val behandlingId = sak.førstegangsbehandling.id

            val behandlingService =
                BehandlingServiceImpl(
                    behandlingRepo = testDataHelper.behandlingRepo,
                    vedtakRepo = testDataHelper.vedtakRepo,
                    personopplysningRepo = testDataHelper.personopplysningerRepo,
                    brevPublisherGateway = mockBrevPublisherGateway,
                    meldekortGrunnlagGateway = mockMeldekortGrunnlagGateway,
                    sakRepo = testDataHelper.sakRepo,
                    sessionFactory = testDataHelper.sessionFactory,
                    saksoversiktRepo = testDataHelper.saksoversiktRepo,
                    statistikkSakRepo = testDataHelper.statistikkSakRepo,
                    statistikkStønadRepo = testDataHelper.statistikkStønadRepo,
                )
            val kvpVilkårService = KvpVilkårServiceImpl(
                behandlingRepo = testDataHelper.behandlingRepo,
                behandlingService = behandlingService,
            )

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
                        path("$BEHANDLING_PATH/$behandlingId/vilkar/kvp")
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
                        path("$BEHANDLING_PATH/$behandlingId/vilkar/kvp")
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
                        path("$BEHANDLING_PATH/$behandlingId/vilkar/kvp")
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
    }

    @Test
    fun `test at samlet utfall for kvp blir IKKE_OPPFYLT om bruker går på kvp i vurderingsperioden`() {
        every { mockInnloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(any()) } returns saksbehandler

        withMigratedDb { dataSource ->
            val testDataHelper = TestDataHelper(dataSource)

            val (sak, _) = testDataHelper.persisterOpprettetFørstegangsbehandling(saksbehandler = saksbehandler)
            val behandlingId = sak.førstegangsbehandling.id

            val behandlingService =
                BehandlingServiceImpl(
                    behandlingRepo = testDataHelper.behandlingRepo,
                    vedtakRepo = testDataHelper.vedtakRepo,
                    personopplysningRepo = testDataHelper.personopplysningerRepo,
                    brevPublisherGateway = mockBrevPublisherGateway,
                    meldekortGrunnlagGateway = mockMeldekortGrunnlagGateway,
                    sakRepo = testDataHelper.sakRepo,
                    sessionFactory = testDataHelper.sessionFactory,
                    saksoversiktRepo = testDataHelper.saksoversiktRepo,
                    statistikkSakRepo = testDataHelper.statistikkSakRepo,
                    statistikkStønadRepo = testDataHelper.statistikkStønadRepo,
                )
            val kvpVilkårService = KvpVilkårServiceImpl(
                behandlingRepo = testDataHelper.behandlingRepo,
                behandlingService = behandlingService,
            )

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

                val vurderingsperiodeDTO = sak.førstegangsbehandling.vurderingsperiode.toDTO()

                defaultRequest(
                    HttpMethod.Get,
                    url {
                        protocol = URLProtocol.HTTPS
                        path("$BEHANDLING_PATH/$behandlingId/vilkar/kvp")
                    },
                ).apply {
                    status shouldBe HttpStatusCode.OK
                    val kvpVilkår = objectMapper.readValue<KVPVilkårDTO>(bodyAsText())
                }

                val bodyKvpDeltarIHelePerioden = bodyEndreKvp(vurderingsperiodeDTO, deltar = true)

                defaultRequest(
                    HttpMethod.Post,
                    url {
                        protocol = URLProtocol.HTTPS
                        path("$BEHANDLING_PATH/$behandlingId/vilkar/kvp")
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
                        path("$BEHANDLING_PATH/$behandlingId/vilkar/kvp")
                    },
                ).apply {
                    status shouldBe HttpStatusCode.OK
                    contentType() shouldBe ContentType.parse("application/json; charset=UTF-8")

                    val kvpVilkår = objectMapper.readValue<KVPVilkårDTO>(bodyAsText())
                    kvpVilkår.samletUtfall shouldBe SamletUtfallDTO.IKKE_OPPFYLT
                }
            }
        }
    }

    private fun bodyEndreKvp(
        periodeDTO: PeriodeDTO,
        deltar: Boolean,
    ): String {
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

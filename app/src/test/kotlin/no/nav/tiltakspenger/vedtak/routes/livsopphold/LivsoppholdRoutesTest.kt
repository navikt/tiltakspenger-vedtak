package no.nav.tiltakspenger.vedtak.routes.livsopphold

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.ktor.client.request.setBody
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
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.felles.mars
import no.nav.tiltakspenger.felles.service.AuditService
import no.nav.tiltakspenger.libs.common.Rolle
import no.nav.tiltakspenger.libs.common.Roller
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.objectmothers.ObjectMother
import no.nav.tiltakspenger.objectmothers.ObjectMother.fraOgMedDatoJa
import no.nav.tiltakspenger.objectmothers.ObjectMother.ja
import no.nav.tiltakspenger.objectmothers.ObjectMother.nySøknad
import no.nav.tiltakspenger.objectmothers.ObjectMother.periodeJa
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknad
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingServiceImpl
import no.nav.tiltakspenger.saksbehandling.service.behandling.vilkår.livsopphold.LivsoppholdVilkårServiceImpl
import no.nav.tiltakspenger.vedtak.clients.defaultObjectMapper
import no.nav.tiltakspenger.vedtak.db.TestDataHelper
import no.nav.tiltakspenger.vedtak.db.persisterOpprettetFørstegangsbehandling
import no.nav.tiltakspenger.vedtak.db.withMigratedDb
import no.nav.tiltakspenger.vedtak.routes.behandling.BEHANDLING_PATH
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.SamletUtfallDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.livsopphold.LivsoppholdVilkårDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.livsopphold.livsoppholdRoutes
import no.nav.tiltakspenger.vedtak.routes.configureExceptions
import no.nav.tiltakspenger.vedtak.routes.defaultRequest
import no.nav.tiltakspenger.vedtak.routes.dto.PeriodeDTO
import no.nav.tiltakspenger.vedtak.routes.dto.toDTO
import no.nav.tiltakspenger.vedtak.routes.jacksonSerialization
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class LivsoppholdRoutesTest {
    private val mockInnloggetSaksbehandlerProvider = mockk<InnloggetSaksbehandlerProvider>()
    private val mockAuditService = mockk<AuditService>()

    private val objectMapper: ObjectMapper = defaultObjectMapper()

    private val saksbehandlerIdent = "Q123456"
    private val saksbehandler =
        Saksbehandler(
            saksbehandlerIdent,
            "Superman",
            "a@b.c",
            Roller(listOf(Rolle.SAKSBEHANDLER, Rolle.SKJERMING, Rolle.STRENGT_FORTROLIG_ADRESSE)),
        )

    @BeforeEach
    fun setup() {
        every { mockAuditService.logMedBehandlingId(any(), any(), any(), any()) } returns Unit
    }

    @Test
    fun `test at endepunkt for henting og lagring av livsopphold fungerer`() {
        every { mockInnloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(any()) } returns saksbehandler

        withMigratedDb { dataSource ->
            val testDataHelper = TestDataHelper(dataSource)

            val (sak, _) = testDataHelper.persisterOpprettetFørstegangsbehandling(saksbehandler = saksbehandler)
            val behandlingId = sak.førstegangsbehandling.id
            val vurderingsperiode = sak.førstegangsbehandling.vurderingsperiode

            val behandlingService =
                BehandlingServiceImpl(
                    behandlingRepo = testDataHelper.behandlingRepo,
                    vedtakRepo = testDataHelper.vedtakRepo,
                    personopplysningRepo = testDataHelper.personopplysningerRepo,
                    sakRepo = testDataHelper.sakRepo,
                    sessionFactory = testDataHelper.sessionFactory,
                    statistikkSakRepo = testDataHelper.statistikkSakRepo,
                    statistikkStønadRepo = testDataHelper.statistikkStønadRepo,
                    meldekortRepo = testDataHelper.meldekortRepo,
                )
            val livsoppholdVilkårService = LivsoppholdVilkårServiceImpl(
                behandlingRepo = testDataHelper.behandlingRepo,
                behandlingService = behandlingService,
            )
            testApplication {
                application {
                    jacksonSerialization()
                    routing {
                        livsoppholdRoutes(
                            innloggetSaksbehandlerProvider = mockInnloggetSaksbehandlerProvider,
                            livsoppholdVilkårService = livsoppholdVilkårService,
                            behandlingService = behandlingService,
                            auditService = mockAuditService,
                        )
                    }
                }
                // Sjekk at man kan kjøre Get
                defaultRequest(
                    HttpMethod.Get,
                    url {
                        protocol = URLProtocol.HTTPS
                        path("$BEHANDLING_PATH/$behandlingId/vilkar/livsopphold")
                    },
                ).apply {
                    status shouldBe HttpStatusCode.OK
                    val livsoppholdVilkår = objectMapper.readValue<LivsoppholdVilkårDTO>(bodyAsText())
                    livsoppholdVilkår.avklartSaksopplysning.shouldBeNull()
                }

                // Sjekk at man kan si at bruker ikke har livsoppholdytelser
                defaultRequest(
                    HttpMethod.Post,
                    url {
                        protocol = URLProtocol.HTTPS
                        path("$BEHANDLING_PATH/$behandlingId/vilkar/livsopphold")
                    },
                ) {
                    setBody(bodyLivsoppholdYtelse(vurderingsperiode.toDTO(), false))
                }.apply {
                    status shouldBe HttpStatusCode.Created
                }

                // Sjekk at dataene har blitt oppdatert
                defaultRequest(
                    HttpMethod.Get,
                    url {
                        protocol = URLProtocol.HTTPS
                        path("$BEHANDLING_PATH/$behandlingId/vilkar/livsopphold")
                    },
                ).apply {
                    status shouldBe HttpStatusCode.OK
                    val livsoppholdVilkår = objectMapper.readValue<LivsoppholdVilkårDTO>(bodyAsText())
                    livsoppholdVilkår.avklartSaksopplysning!!.harLivsoppholdYtelser.shouldBeFalse()
                    livsoppholdVilkår.avklartSaksopplysning.saksbehandler shouldNotBeNull { this.navIdent shouldBe saksbehandlerIdent }
                }
            }
        }
    }

    @Test
    fun `test at sbh ikke kan si at bruker har livsoppholdytelser`() {
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
                    sakRepo = testDataHelper.sakRepo,
                    sessionFactory = testDataHelper.sessionFactory,
                    statistikkSakRepo = testDataHelper.statistikkSakRepo,
                    statistikkStønadRepo = testDataHelper.statistikkStønadRepo,
                    meldekortRepo = testDataHelper.meldekortRepo,
                )
            val livsoppholdVilkårService = LivsoppholdVilkårServiceImpl(
                behandlingRepo = testDataHelper.behandlingRepo,
                behandlingService = behandlingService,
            )

            testApplication {
                application {
                    configureExceptions()
                    jacksonSerialization()
                    routing {
                        livsoppholdRoutes(
                            innloggetSaksbehandlerProvider = mockInnloggetSaksbehandlerProvider,
                            livsoppholdVilkårService = livsoppholdVilkårService,
                            behandlingService = behandlingService,
                            auditService = mockAuditService,
                        )
                    }
                }

                // Sjekk at man ikke kan si at bruker har livsoppholdytelser
                defaultRequest(
                    HttpMethod.Post,
                    url {
                        protocol = URLProtocol.HTTPS
                        path("$BEHANDLING_PATH/$behandlingId/vilkar/livsopphold")
                    },
                ) {
                    setBody(bodyLivsoppholdYtelse(sak.førstegangsbehandling.vurderingsperiode.toDTO(), true))
                }.apply {
                    status shouldBe HttpStatusCode.NotImplemented
                }
            }
        }
    }

    @Test
    fun `test at livsoppholdytelser blir uavklart om man bare har data fra søknaden`() {
        every { mockInnloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(any()) } returns saksbehandler

        withMigratedDb { dataSource ->
            val testDataHelper = TestDataHelper(dataSource)

            val (sak, _) = testDataHelper.persisterOpprettetFørstegangsbehandling(saksbehandler = saksbehandler)
            val behandlingId = sak.førstegangsbehandling.id
            val vurderingsperiode = sak.førstegangsbehandling.vurderingsperiode

            val behandlingService =
                BehandlingServiceImpl(
                    behandlingRepo = testDataHelper.behandlingRepo,
                    vedtakRepo = testDataHelper.vedtakRepo,
                    personopplysningRepo = testDataHelper.personopplysningerRepo,
                    sakRepo = testDataHelper.sakRepo,
                    sessionFactory = testDataHelper.sessionFactory,
                    statistikkSakRepo = testDataHelper.statistikkSakRepo,
                    statistikkStønadRepo = testDataHelper.statistikkStønadRepo,
                    meldekortRepo = testDataHelper.meldekortRepo,
                )
            val livsoppholdVilkårService =
                LivsoppholdVilkårServiceImpl(
                    behandlingRepo = testDataHelper.behandlingRepo,
                    behandlingService = behandlingService,
                )
            testApplication {
                application {
                    jacksonSerialization()
                    routing {
                        livsoppholdRoutes(
                            innloggetSaksbehandlerProvider = mockInnloggetSaksbehandlerProvider,
                            livsoppholdVilkårService = livsoppholdVilkårService,
                            behandlingService = behandlingService,
                            auditService = mockAuditService,
                        )
                    }
                }
                // Sjekk at man kan kjøre Get
                defaultRequest(
                    HttpMethod.Get,
                    url {
                        protocol = URLProtocol.HTTPS
                        path("$BEHANDLING_PATH/$behandlingId/vilkar/livsopphold")
                    },
                ).apply {
                    status shouldBe HttpStatusCode.OK
                    val livsoppholdVilkår = objectMapper.readValue<LivsoppholdVilkårDTO>(bodyAsText())
                    livsoppholdVilkår.samletUtfall shouldBe SamletUtfallDTO.UAVKLART
                    livsoppholdVilkår.avklartSaksopplysning.shouldBeNull()
                }

                // Sjekk at man kan si at bruker ikke har livsoppholdytelser
                defaultRequest(
                    HttpMethod.Post,
                    url {
                        protocol = URLProtocol.HTTPS
                        path("$BEHANDLING_PATH/$behandlingId/vilkar/livsopphold")
                    },
                ) {
                    setBody(bodyLivsoppholdYtelse(vurderingsperiode.toDTO(), false))
                }.apply {
                    status shouldBe HttpStatusCode.Created
                }

                // Sjekk at dataene har blitt oppdatert
                defaultRequest(
                    HttpMethod.Get,
                    url {
                        protocol = URLProtocol.HTTPS
                        path("$BEHANDLING_PATH/$behandlingId/vilkar/livsopphold")
                    },
                ).apply {
                    status shouldBe HttpStatusCode.OK
                    val livsoppholdVilkår = objectMapper.readValue<LivsoppholdVilkårDTO>(bodyAsText())
                    livsoppholdVilkår.avklartSaksopplysning!!.harLivsoppholdYtelser.shouldBeFalse()
                    livsoppholdVilkår.avklartSaksopplysning.saksbehandler shouldNotBeNull { this.navIdent shouldBe saksbehandlerIdent }
                }
            }
        }
    }

    @Test
    fun `test alle livsoppholdytelser stemmer overens med søknadsdata`() {
        every { mockInnloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(any()) } returns saksbehandler

        val sakId = SakId.random()
        val søknadMedSykepenger =
            nySøknad(
                sykepenger = periodeJa(fom = 1.januar(2023), tom = 31.mars(2023)),
            )

        val livsoppholdVilkårSykepenger = opprettSakOgKjørGetPåLivsopphold(sakId, søknadMedSykepenger, 1011)
        livsoppholdVilkårSykepenger.avklartSaksopplysning.shouldBeNull()
        livsoppholdVilkårSykepenger.samletUtfall shouldBe SamletUtfallDTO.UAVKLART

        val søknadMedEtterlønn =
            nySøknad(
                etterlønn = ja(),
            )
        val livsoppholdVilkårEtterlønn = opprettSakOgKjørGetPåLivsopphold(sakId, søknadMedEtterlønn, 1005)
        livsoppholdVilkårEtterlønn.avklartSaksopplysning.shouldBeNull()
        livsoppholdVilkårEtterlønn.samletUtfall shouldBe SamletUtfallDTO.UAVKLART

        val søknadMedGjenlevendepensjon =
            nySøknad(
                gjenlevendepensjon = periodeJa(fom = 1.januar(2023), tom = 31.mars(2023)),
            )
        val livsoppholdVilkårGjenlevendepensjon =
            opprettSakOgKjørGetPåLivsopphold(sakId, søknadMedGjenlevendepensjon, 1006)
        livsoppholdVilkårGjenlevendepensjon.avklartSaksopplysning.shouldBeNull()
        livsoppholdVilkårGjenlevendepensjon.samletUtfall shouldBe SamletUtfallDTO.UAVKLART

        val søknadMedSuAlder =
            nySøknad(
                supplerendeStønadAlder = periodeJa(fom = 1.januar(2023), tom = 31.mars(2023)),
            )
        val livsoppholdVilkårSuAlder = opprettSakOgKjørGetPåLivsopphold(sakId, søknadMedSuAlder, 1007)
        livsoppholdVilkårSuAlder.avklartSaksopplysning.shouldBeNull()
        livsoppholdVilkårSuAlder.samletUtfall shouldBe SamletUtfallDTO.UAVKLART

        val søknadMedSuflykning =
            nySøknad(
                supplerendeStønadFlyktning = periodeJa(fom = 1.januar(2023), tom = 31.mars(2023)),
            )
        val livsoppholdVilkårSuflykning = opprettSakOgKjørGetPåLivsopphold(sakId, søknadMedSuflykning, 1008)
        livsoppholdVilkårSuflykning.avklartSaksopplysning.shouldBeNull()
        livsoppholdVilkårSuflykning.samletUtfall shouldBe SamletUtfallDTO.UAVKLART

        val søknadMedJobbsjansen =
            nySøknad(
                jobbsjansen = periodeJa(fom = 1.januar(2023), tom = 31.mars(2023)),
            )
        val livsoppholdVilkårJobbsjansen = opprettSakOgKjørGetPåLivsopphold(sakId, søknadMedJobbsjansen, 1009)
        livsoppholdVilkårJobbsjansen.avklartSaksopplysning.shouldBeNull()
        livsoppholdVilkårJobbsjansen.samletUtfall shouldBe SamletUtfallDTO.UAVKLART

        val søknadMedPensjonsinntekt =
            nySøknad(
                trygdOgPensjon = periodeJa(fom = 1.januar(2023), tom = 31.mars(2023)),
            )
        val livsoppholdVilkårPensjonsinntekt = opprettSakOgKjørGetPåLivsopphold(sakId, søknadMedPensjonsinntekt, 1010)
        livsoppholdVilkårPensjonsinntekt.avklartSaksopplysning.shouldBeNull()
        livsoppholdVilkårPensjonsinntekt.samletUtfall shouldBe SamletUtfallDTO.UAVKLART

        val søknadMedAlderpensjon =
            nySøknad(
                alderspensjon = fraOgMedDatoJa(fom = 1.januar(2023)),
            )
        val livsoppholdVilkårAlderpensjon = opprettSakOgKjørGetPåLivsopphold(sakId, søknadMedAlderpensjon, 1011)
        livsoppholdVilkårAlderpensjon.avklartSaksopplysning.shouldBeNull()
        livsoppholdVilkårAlderpensjon.samletUtfall shouldBe SamletUtfallDTO.UAVKLART
    }

    private fun opprettSakOgKjørGetPåLivsopphold(
        sakId: SakId,
        søknad: Søknad,
        løpenummer: Int,
        saksbehandler: Saksbehandler = ObjectMother.saksbehandler(),
    ): LivsoppholdVilkårDTO {
        lateinit var livsoppholdVilkårDTO: LivsoppholdVilkårDTO

        withMigratedDb { dataSource ->
            val testDataHelper = TestDataHelper(dataSource)

            val (sak, _) =
                testDataHelper.persisterOpprettetFørstegangsbehandling(
                    sakId = sakId,
                    søknad = søknad,
                    løpenummer = løpenummer,
                    saksbehandler = saksbehandler,
                )
            val behandlingId = sak.førstegangsbehandling.id

            val behandlingService =
                BehandlingServiceImpl(
                    behandlingRepo = testDataHelper.behandlingRepo,
                    vedtakRepo = testDataHelper.vedtakRepo,
                    personopplysningRepo = testDataHelper.personopplysningerRepo,
                    sakRepo = testDataHelper.sakRepo,
                    sessionFactory = testDataHelper.sessionFactory,
                    statistikkSakRepo = testDataHelper.statistikkSakRepo,
                    statistikkStønadRepo = testDataHelper.statistikkStønadRepo,
                    meldekortRepo = testDataHelper.meldekortRepo,
                )
            val livsoppholdVilkårService = LivsoppholdVilkårServiceImpl(
                behandlingRepo = testDataHelper.behandlingRepo,
                behandlingService = behandlingService,
            )

            testApplication {
                application {
                    configureExceptions()
                    jacksonSerialization()
                    routing {
                        livsoppholdRoutes(
                            innloggetSaksbehandlerProvider = mockInnloggetSaksbehandlerProvider,
                            livsoppholdVilkårService = livsoppholdVilkårService,
                            behandlingService = behandlingService,
                            auditService = mockAuditService,
                        )
                    }
                }

                defaultRequest(
                    HttpMethod.Get,
                    url {
                        protocol = URLProtocol.HTTPS
                        path("$BEHANDLING_PATH/$behandlingId/vilkar/livsopphold")
                    },
                ).apply {
                    status shouldBe HttpStatusCode.OK
                    livsoppholdVilkårDTO = objectMapper.readValue<LivsoppholdVilkårDTO>(bodyAsText())
                }
            }
        }

        return livsoppholdVilkårDTO
    }

    private fun bodyLivsoppholdYtelse(
        periodeDTO: PeriodeDTO,
        harYtelse: Boolean,
    ): String {
        val harYtelseString = if (harYtelse) "true" else "false"
        return """
            {
              "ytelseForPeriode": 
                {
                  "periode": {
                    "fraOgMed": "${periodeDTO.fraOgMed}",
                    "tilOgMed": "${periodeDTO.tilOgMed}"
                  },
                  "harYtelse": $harYtelseString
                }
            }
        """.trimIndent()
    }
}

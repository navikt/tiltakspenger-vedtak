package no.nav.tiltakspenger.vedtak.routes.kvp

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
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
import kotliquery.sessionOf
import no.nav.tiltakspenger.felles.Rolle
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.objectmothers.ObjectMother
import no.nav.tiltakspenger.objectmothers.ObjectMother.fraOgMedDatoJa
import no.nav.tiltakspenger.objectmothers.ObjectMother.ja
import no.nav.tiltakspenger.objectmothers.ObjectMother.nySøknad
import no.nav.tiltakspenger.objectmothers.ObjectMother.periodeJa
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknad
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingServiceImpl
import no.nav.tiltakspenger.saksbehandling.service.behandling.vilkår.livsopphold.LivsoppholdVilkårServiceImpl
import no.nav.tiltakspenger.saksbehandling.service.utbetaling.UtbetalingServiceImpl
import no.nav.tiltakspenger.vedtak.clients.brevpublisher.BrevPublisherGatewayImpl
import no.nav.tiltakspenger.vedtak.clients.defaultObjectMapper
import no.nav.tiltakspenger.vedtak.clients.meldekort.MeldekortGrunnlagGatewayImpl
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
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.kvp.DeltagelseDTO
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
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
class LivsoppholdRoutesTest {

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
        multiRepo = multiRepo,
        sakRepo = sakRepo,

    )
    private val livsoppholdVilkårService = LivsoppholdVilkårServiceImpl(behandlingRepo, behandlingService)

    private val objectMapper: ObjectMapper = defaultObjectMapper()

    private val saksbehandlerIdent = "Q123456"
    private val mockSaksbehandler = Saksbehandler(
        saksbehandlerIdent,
        "Superman",
        "a@b.c",
        listOf(Rolle.SAKSBEHANDLER, Rolle.SKJERMING, Rolle.STRENGT_FORTROLIG_ADRESSE),
    )

    @Test
    fun `test at endepunkt for henting og lagring av livsopphold fungerer`() {
        every { mockInnloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(any()) } returns mockSaksbehandler

        val objectMotherSak = ObjectMother.sakMedOpprettetBehandling()

        sessionOf(DataSource.hikariDataSource).use {
            sakRepo.lagre(objectMotherSak)
        }

        val behandlingId = objectMotherSak.behandlinger.first().id.toString()
        val vurderingsPeriode = objectMotherSak.behandlinger.first().vurderingsperiode

        testApplication {
            application {
                jacksonSerialization()
                routing {
                    livsoppholdRoutes(
                        innloggetSaksbehandlerProvider = mockInnloggetSaksbehandlerProvider,
                        livsoppholdVilkårService = livsoppholdVilkårService,
                        behandlingService = behandlingService,
                    )
                }
            }

            // Sjekk at man kan kjøre Get
            defaultRequest(
                HttpMethod.Get,
                url {
                    protocol = URLProtocol.HTTPS
                    path("$behandlingPath/$behandlingId/vilkar/livsopphold")
                },
            ).apply {
                status shouldBe HttpStatusCode.OK
                val livsoppholdVilkår = objectMapper.readValue<LivsoppholdVilkårDTO>(bodyAsText())
                livsoppholdVilkår.avklartSaksopplysning.periodeMedDeltagelse.deltagelse shouldBe DeltagelseDTO.DELTAR_IKKE
                livsoppholdVilkår.avklartSaksopplysning.saksbehandler shouldBe null
            }

            // Sjekk at man kan si at bruker ikke har livsoppholdytelser
            defaultRequest(
                HttpMethod.Post,
                url {
                    protocol = URLProtocol.HTTPS
                    path("$behandlingPath/$behandlingId/vilkar/livsopphold")
                },
            ) {
                setBody(bodyLivsoppholdYtelse(vurderingsPeriode.toDTO(), false))
            }.apply {
                status shouldBe HttpStatusCode.Created
            }

            // Sjekk at dataene har blitt oppdatert
            defaultRequest(
                HttpMethod.Get,
                url {
                    protocol = URLProtocol.HTTPS
                    path("$behandlingPath/$behandlingId/vilkar/livsopphold")
                },
            ).apply {
                status shouldBe HttpStatusCode.OK
                val livsoppholdVilkår = objectMapper.readValue<LivsoppholdVilkårDTO>(bodyAsText())
                livsoppholdVilkår.avklartSaksopplysning.periodeMedDeltagelse.deltagelse shouldBe DeltagelseDTO.DELTAR_IKKE
                livsoppholdVilkår.avklartSaksopplysning.saksbehandler shouldNotBeNull { this.navIdent shouldBe saksbehandlerIdent }
            }
        }
    }

    @Test
    fun `test at sbh ikke kan si at bruker har livsoppholdytelser`() {
        every { mockInnloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(any()) } returns mockSaksbehandler

        val objectMotherSak = ObjectMother.sakMedOpprettetBehandling()

        sessionOf(DataSource.hikariDataSource).use {
            sakRepo.lagre(objectMotherSak)
        }

        val behandlingId = objectMotherSak.behandlinger.first().id.toString()
        val vurderingsPeriode = objectMotherSak.behandlinger.first().vurderingsperiode

        testApplication {
            application {
                configureExceptions()
                jacksonSerialization()
                routing {
                    livsoppholdRoutes(
                        innloggetSaksbehandlerProvider = mockInnloggetSaksbehandlerProvider,
                        livsoppholdVilkårService = livsoppholdVilkårService,
                        behandlingService = behandlingService,
                    )
                }
            }

            // Sjekk at man ikke kan si at bruker har livsoppholdytelser
            defaultRequest(
                HttpMethod.Post,
                url {
                    protocol = URLProtocol.HTTPS
                    path("$behandlingPath/$behandlingId/vilkar/livsopphold")
                },
            ) {
                setBody(bodyLivsoppholdYtelse(vurderingsPeriode.toDTO(), true))
            }.apply {
                status shouldBe HttpStatusCode.NotImplemented
            }
        }
    }

    @Test
    fun `test alle livsoppholdytelser stemmer overens med søknadsdata`() {
        every { mockInnloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(any()) } returns mockSaksbehandler

        val sakId = SakId.random()
        val søknadMedSykepenger = nySøknad(
            sykepenger = periodeJa(),
        )

        val livsoppholdVilkårSykepenger = opprettSakOgKjørGetPåLivsopphold(sakId, søknadMedSykepenger)
        livsoppholdVilkårSykepenger.avklartSaksopplysning.periodeMedDeltagelse
            .deltagelse shouldBe DeltagelseDTO.DELTAR
        livsoppholdVilkårSykepenger.samletUtfall shouldBe SamletUtfallDTO.IKKE_OPPFYLT

        val søknadMedEtterlønn = nySøknad(
            etterlønn = ja(),
        )
        val livsoppholdVilkårEtterlønn = opprettSakOgKjørGetPåLivsopphold(sakId, søknadMedEtterlønn)
        livsoppholdVilkårEtterlønn.avklartSaksopplysning.periodeMedDeltagelse
            .deltagelse shouldBe DeltagelseDTO.DELTAR
        livsoppholdVilkårEtterlønn.samletUtfall shouldBe SamletUtfallDTO.IKKE_OPPFYLT

        val søknadMedGjenlevendepensjon = nySøknad(
            gjenlevendepensjon = periodeJa(),
        )
        val livsoppholdVilkårGjenlevendepensjon = opprettSakOgKjørGetPåLivsopphold(sakId, søknadMedGjenlevendepensjon)
        livsoppholdVilkårGjenlevendepensjon.avklartSaksopplysning.periodeMedDeltagelse
            .deltagelse shouldBe DeltagelseDTO.DELTAR
        livsoppholdVilkårGjenlevendepensjon.samletUtfall shouldBe SamletUtfallDTO.IKKE_OPPFYLT

        val søknadMedSuAlder = nySøknad(
            supplerendeStønadAlder = periodeJa(),
        )
        val livsoppholdVilkårSuAlder = opprettSakOgKjørGetPåLivsopphold(sakId, søknadMedSuAlder)
        livsoppholdVilkårSuAlder.avklartSaksopplysning.periodeMedDeltagelse
            .deltagelse shouldBe DeltagelseDTO.DELTAR
        livsoppholdVilkårSuAlder.samletUtfall shouldBe SamletUtfallDTO.IKKE_OPPFYLT

        val søknadMedSuflykning = nySøknad(
            supplerendeStønadFlyktning = periodeJa(),
        )
        val livsoppholdVilkårSuflykning = opprettSakOgKjørGetPåLivsopphold(sakId, søknadMedSuflykning)
        livsoppholdVilkårSuflykning.avklartSaksopplysning.periodeMedDeltagelse
            .deltagelse shouldBe DeltagelseDTO.DELTAR
        livsoppholdVilkårSuflykning.samletUtfall shouldBe SamletUtfallDTO.IKKE_OPPFYLT

        val søknadMedJobbsjansen = nySøknad(
            jobbsjansen = periodeJa(),
        )
        val livsoppholdVilkårJobbsjansen = opprettSakOgKjørGetPåLivsopphold(sakId, søknadMedJobbsjansen)
        livsoppholdVilkårJobbsjansen.avklartSaksopplysning.periodeMedDeltagelse
            .deltagelse shouldBe DeltagelseDTO.DELTAR
        livsoppholdVilkårJobbsjansen.samletUtfall shouldBe SamletUtfallDTO.IKKE_OPPFYLT

        val søknadMedPensjonsinntekt = nySøknad(
            trygdOgPensjon = periodeJa(),
        )
        val livsoppholdVilkårPensjonsinntekt = opprettSakOgKjørGetPåLivsopphold(sakId, søknadMedPensjonsinntekt)
        livsoppholdVilkårPensjonsinntekt.avklartSaksopplysning.periodeMedDeltagelse
            .deltagelse shouldBe DeltagelseDTO.DELTAR
        livsoppholdVilkårPensjonsinntekt.samletUtfall shouldBe SamletUtfallDTO.IKKE_OPPFYLT

        val søknadMedAlderpensjon = nySøknad(
            alderspensjon = fraOgMedDatoJa(),
        )
        val livsoppholdVilkårAlderpensjon = opprettSakOgKjørGetPåLivsopphold(sakId, søknadMedAlderpensjon)
        livsoppholdVilkårAlderpensjon.avklartSaksopplysning.periodeMedDeltagelse
            .deltagelse shouldBe DeltagelseDTO.DELTAR
        livsoppholdVilkårAlderpensjon.samletUtfall shouldBe SamletUtfallDTO.IKKE_OPPFYLT
    }

    private fun opprettSakOgKjørGetPåLivsopphold(sakId: SakId, søknad: Søknad): LivsoppholdVilkårDTO {
        val objectMotherSak = ObjectMother.sakMedOpprettetBehandling(id = sakId, behandlinger = listOf(Førstegangsbehandling.opprettBehandling(sakId, søknad)))

        sessionOf(DataSource.hikariDataSource).use {
            sakRepo.lagre(objectMotherSak)
        }

        val behandlingId = objectMotherSak.behandlinger.first().id.toString()

        lateinit var livsoppholdVilkårDTO: LivsoppholdVilkårDTO

        testApplication {
            application {
                configureExceptions()
                jacksonSerialization()
                routing {
                    livsoppholdRoutes(
                        innloggetSaksbehandlerProvider = mockInnloggetSaksbehandlerProvider,
                        livsoppholdVilkårService = livsoppholdVilkårService,
                        behandlingService = behandlingService,
                    )
                }
            }

            defaultRequest(
                HttpMethod.Get,
                url {
                    protocol = URLProtocol.HTTPS
                    path("$behandlingPath/$behandlingId/vilkar/livsopphold")
                },
            ).apply {
                status shouldBe HttpStatusCode.OK
                livsoppholdVilkårDTO = objectMapper.readValue<LivsoppholdVilkårDTO>(bodyAsText())
            }
        }
        return livsoppholdVilkårDTO
    }

    private fun bodyLivsoppholdYtelse(periodeDTO: PeriodeDTO, deltar: Boolean): String {
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

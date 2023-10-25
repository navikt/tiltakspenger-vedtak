package no.nav.tiltakspenger.vedtak.service.søker

import mu.KotlinLogging
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.vedtak.Barnetillegg
import no.nav.tiltakspenger.vedtak.Innsending
import no.nav.tiltakspenger.vedtak.Personopplysninger
import no.nav.tiltakspenger.vedtak.Søker
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vedtak.Tiltak
import no.nav.tiltakspenger.vedtak.Tiltaksaktivitet
import no.nav.tiltakspenger.vedtak.Vedlegg
import no.nav.tiltakspenger.vilkårsvurdering.Anbefaling
import no.nav.tiltakspenger.vilkårsvurdering.Inngangsvilkårsvurderinger
import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering
import no.nav.tiltakspenger.vilkårsvurdering.anbefalingFor
import no.nav.tiltakspenger.vilkårsvurdering.kategori.AlderVilkårsvurderingKategori
import no.nav.tiltakspenger.vilkårsvurdering.kategori.InstitusjonVilkårsvurderingKategori
import no.nav.tiltakspenger.vilkårsvurdering.kategori.KommunaleYtelserVilkårsvurderingKategori
import no.nav.tiltakspenger.vilkårsvurdering.kategori.LønnsinntektVilkårsvurderingKategori
import no.nav.tiltakspenger.vilkårsvurdering.kategori.PensjonsinntektVilkårsvurderingKategori
import no.nav.tiltakspenger.vilkårsvurdering.kategori.StatligeYtelserVilkårsvurderingKategori
import no.nav.tiltakspenger.vilkårsvurdering.kategori.TiltakspengerVilkårsvurderingKategori
import no.nav.tiltakspenger.vilkårsvurdering.kategori.VilkårsvurderingKategori
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.AAPVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.AlderVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.AlderspensjonVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.DagpengerVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.EtterlønnVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.ForeldrepengerVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.GjenlevendepensjonVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.InstitusjonsoppholdVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.IntroProgrammetVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.KVPVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.LønnetArbeidVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.OmsorgspengerVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.OpplæringspengerVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.OvergangsstønadVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.PleiepengerNærståendeVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.PleiepengerSyktBarnVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.PrivatPensjonsinntektVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.SupplerendeStønadAlderVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.SupplerendeStønadFlyktningVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.SvangerskapspengerVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.SykepengerVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.TiltakspengerVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.UføreVilkarsvurdering

private val LOG = KotlinLogging.logger {}

class BehandlingMapper {

    fun mapSøkerOgInnsendinger(søker: Søker, innsendinger: List<Innsending>): SøkerDTO {
        return SøkerDTO(
            søkerId = søker.søkerId.toString(),
            ident = søker.ident,
            personopplysninger = søker.personopplysninger?.let { mapPersonopplysninger(it) },
            behandlinger = innsendinger.mapNotNull { mapInnsendingMedSøknad(it) },
        )
    }

    private fun mapKonklusjon(anbefaling: Anbefaling): KonklusjonDTO {
        return when (anbefaling) {
            is Anbefaling.Oppfylt -> KonklusjonDTO(oppfylt = mapOppfylt(anbefaling))
            is Anbefaling.IkkeOppfylt -> KonklusjonDTO(ikkeOppfylt = mapIkkeOppfylt(anbefaling))
            is Anbefaling.DelvisOppfylt -> KonklusjonDTO(delvisOppfylt = mapDelvisOppfylt(anbefaling))
            is Anbefaling.KreverManuellBehandling -> KonklusjonDTO(
                kreverManuellBehandling = mapKreverManuellBehandling(
                    anbefaling,
                ),
            )
        }
    }

    private fun mapDelvisOppfylt(anbefaling: Anbefaling.DelvisOppfylt): DelvisOppfyltDTO =
        DelvisOppfyltDTO(
            oppfylt = anbefaling.oppfylt.map { mapOppfylt(it) },
            ikkeOppfylt = anbefaling.ikkeOppfylt.map { mapIkkeOppfylt(it) },
        )

    private fun mapOppfylt(anbefaling: Anbefaling.Oppfylt): PeriodeMedVurderingerDTO =
        PeriodeMedVurderingerDTO(
            periode = mapPeriode(anbefaling.periodeMedVilkår.first),
            vurderinger = anbefaling.periodeMedVilkår.second.map {
                KonklusjonVurderingDTO(
                    vilkår = it.vilkår.tittel,
                    kilde = it.kilde,
                )
            },
        )

    private fun mapIkkeOppfylt(anbefaling: Anbefaling.IkkeOppfylt): PeriodeMedVurderingerDTO =
        PeriodeMedVurderingerDTO(
            periode = mapPeriode(anbefaling.periodeMedVilkår.first),
            vurderinger = anbefaling.periodeMedVilkår.second.map {
                KonklusjonVurderingDTO(
                    vilkår = it.vilkår.tittel,
                    kilde = it.kilde,
                )
            },
        )

    private fun mapKreverManuellBehandling(anbefaling: Anbefaling.KreverManuellBehandling): List<PeriodeMedVurderingerDTO> =
        anbefaling.perioderMedVilkår.map { entry ->
            PeriodeMedVurderingerDTO(
                periode = mapPeriode(entry.key),
                vurderinger = entry.value.map {
                    KonklusjonVurderingDTO(
                        vilkår = it.vilkår.tittel,
                        kilde = it.kilde,
                    )
                },
            )
        }

    private fun mapPeriode(periode: Periode): PeriodeDTO =
        PeriodeDTO(periode.fra, periode.til)

    fun mapInnsendingMedSøknad(innsending: Innsending): KlarEllerIkkeKlarForBehandlingDTO? {
        val søknaden = innsending.søknad ?: return null
        return søknaden.let { søknad ->
            if (!innsending.erFerdigstilt()) {
                IkkeKlarForBehandlingDTO(søknad = mapSøknad(søknad))
            } else {
                val vurderingsperiode =
                    innsending.vurderingsperiodeForSøknad()
                        ?: return IkkeKlarForBehandlingDTO(søknad = mapSøknad(søknad))
                            .also { LOG.warn("Fant ikke vurderingsperiode for innsending ${innsending.id}") }

                val vilkårsvurderinger: Inngangsvilkårsvurderinger =
                    vilkårsvurderinger(innsending, vurderingsperiode, søknad)
                KlarForBehandlingDTO(
                    søknad = mapSøknad(søknad),
                    registrerteTiltak = innsending.tiltak!!.tiltaksliste.map { mapTiltak(it) },
                    vurderingsperiode = mapVurderingsperiode(vurderingsperiode),
                    tiltakspengerYtelser = mapTiltakspenger(vilkårsvurderinger.tiltakspengerYtelser),
                    statligeYtelser = mapStatligeYtelser(vilkårsvurderinger.statligeYtelser),
                    kommunaleYtelser = mapKommunaleYtelser(vilkårsvurderinger.kommunaleYtelser),
                    pensjonsordninger = mapPensjonsordninger(vilkårsvurderinger.pensjonsordninger),
                    lønnsinntekt = mapLønnsinntekt(vilkårsvurderinger.lønnsinntekt),
                    institusjonsopphold = mapInstitusjonsopphold(vilkårsvurderinger.institusjonopphold),
                    barnetillegg = mapBarnetillegg(søknad.barnetillegg),
                    alderVilkårsvurdering = mapAlderVilkårsvurdering(vilkårsvurderinger.alder),
                    konklusjon = mapKonklusjon(
                        listOf(
                            vilkårsvurderinger.statligeYtelser.vurderinger(),
                            vilkårsvurderinger.alder.alderVilkårsvurdering.vurderinger(),
                            vilkårsvurderinger.lønnsinntekt.vurderinger(),
                            vilkårsvurderinger.kommunaleYtelser.vurderinger(),
                            vilkårsvurderinger.institusjonopphold.vurderinger(),
                            vilkårsvurderinger.pensjonsordninger.vurderinger(),
                            // Dropper denne inntil videre vilkårsvurderinger.tiltakspengerYtelser.vurderinger(),
                        ).flatten().anbefalingFor(vurderingsperiode),
                    ),
                    hash = innsending.endringsHash(),
                )
            }
        }
    }

    private fun mapPersonopplysninger(it: Personopplysninger.Søker) = PersonopplysningerDTO(
        fornavn = it.fornavn,
        etternavn = it.etternavn,
        ident = it.ident,
        fødselsdato = it.fødselsdato,
        barn = listOf(),
        fortrolig = it.fortrolig,
        strengtFortrolig = it.strengtFortrolig,
        skjermet = it.skjermet ?: false,
    )

    private fun mapVurderingsperiode(vurderingsperiode: Periode) = ÅpenPeriodeDTO(
        fra = vurderingsperiode.fra,
        til = vurderingsperiode.til,
    )

    private fun mapTiltak(it: Tiltaksaktivitet) = TiltakDTO(
        arrangør = it.arrangør,
        navn = it.tiltak.navn,
        periode = it.deltakelsePeriode.fom?.let { fom ->
            ÅpenPeriodeDTO(
                fra = fom,
                til = it.deltakelsePeriode.tom,
            )
        },
        prosent = it.deltakelseProsent,
        dagerIUken = it.antallDagerPerUke,
        status = it.deltakerStatus.tekst,
    )

    private fun mapSøknad(søknad: Søknad) = SøknadDTO(
        id = søknad.id.toString(),
        søknadId = søknad.søknadId,
        søknadsdato = (søknad.opprettet).toLocalDate(),
        arrangoernavn = søknad.tiltak?.arrangoernavn,
        tiltakskode = if (søknad.tiltak == null) {
            "Ukjent"
        } else {
            (søknad.tiltak as Tiltak).tiltakskode?.navn
                ?: "Annet"
        },
        beskrivelse = when (søknad.tiltak) {
            is Tiltak.ArenaTiltak -> null
            is Tiltak.BrukerregistrertTiltak -> (søknad.tiltak as Tiltak.BrukerregistrertTiltak).beskrivelse
            else -> null
        },
        startdato = søknad.tiltak?.startdato,
        sluttdato = søknad.tiltak?.sluttdato,
        antallDager = if (søknad.tiltak is Tiltak.BrukerregistrertTiltak) {
            (søknad.tiltak as Tiltak.BrukerregistrertTiltak).antallDager
        } else {
            null
        },
        fritekst = "", // TODO Må fjernes
        vedlegg = mapVedlegg(søknad.vedlegg),
    )

    private fun mapVedlegg(
        vedlegg: List<Vedlegg>,
    ): List<VedleggDTO> {
        return vedlegg.map {
            VedleggDTO(
                journalpostId = it.journalpostId,
                dokumentInfoId = it.dokumentInfoId,
                filnavn = it.filnavn,
            )
        }
    }

    private fun mapBarnetillegg(
        barnetillegg: List<Barnetillegg>,
    ): List<BarnetilleggDTO> {
        return barnetillegg.map {
            BarnetilleggDTO(
                navn = if (it.fornavn != null) it.fornavn + " " + it.etternavn else null,
                alder = 0, // TODO Må fjernes
                fødselsdato = if (it is Barnetillegg.Manuell) {
                    it.fødselsdato
                } else {
                    it.fødselsdato
                    // barnMedIdent.firstOrNull { b -> b.ident == (it as Barnetillegg.FraPdl).fødselsdato }?.fødselsdato
                },
                bosatt = "TODO", // TODO Må fjernes, var it.oppholdsland
                kilde = "Søknad",
                utfall = UtfallDTO.Oppfylt,
                søktBarnetillegg = true, // TODO Må endres, var it.søktBarnetillegg,
            )
        }
    }

    private fun mapTiltakspenger(vilkårsvurdering: VilkårsvurderingKategori): TiltakspengerDTO {
        val perioderMedTiltakspenger =
            vilkårsvurdering.vurderinger()
                .filter { it.vilkår is Vilkår.TILTAKSPENGER }
        return TiltakspengerDTO(
            samletUtfall = vilkårsvurdering.samletUtfall().mapToUtfallDTO(),
            perioder = perioderMedTiltakspenger.map { mapVurderingToVilkårsvurderingDTO(it) },
        )
    }

    private fun mapPensjonsordninger(vilkårsvurdering: VilkårsvurderingKategori): PensjonsordningerDTO {
        val perioderMedPensjonsordning =
            vilkårsvurdering.vurderinger()
                .filter { it.vilkår is Vilkår.PENSJONSINNTEKT }
        return PensjonsordningerDTO(
            samletUtfall = vilkårsvurdering.samletUtfall().mapToUtfallDTO(),
            perioder = perioderMedPensjonsordning.map { mapVurderingToVilkårsvurderingDTO(it) },
        )
    }

    private fun mapLønnsinntekt(vilkårsvurdering: VilkårsvurderingKategori): LønnsinntekterDTO {
        val perioderMedLønnsinntekter =
            vilkårsvurdering.vurderinger()
                .filter { it.vilkår is Vilkår.LØNNSINNTEKT }
        return LønnsinntekterDTO(
            samletUtfall = vilkårsvurdering.samletUtfall().mapToUtfallDTO(),
            perioder = perioderMedLønnsinntekter.map { mapVurderingToVilkårsvurderingDTO(it) },
        )
    }

    private fun mapInstitusjonsopphold(vilkårsvurdering: VilkårsvurderingKategori): InstitusjonsoppholdDTO {
        val perioderMedInstitusjonsopphold =
            vilkårsvurdering.vurderinger()
                .filter { it.vilkår is Vilkår.INSTITUSJONSOPPHOLD }
        return InstitusjonsoppholdDTO(
            samletUtfall = vilkårsvurdering.samletUtfall().mapToUtfallDTO(),
            perioder = perioderMedInstitusjonsopphold.map { mapVurderingToVilkårsvurderingDTO(it) },
        )
    }

    private fun mapAlderVilkårsvurdering(vilkårsvurdering: VilkårsvurderingKategori): AlderVilkårsvurderingDTO {
        val perioder =
            vilkårsvurdering.vurderinger()
                .filter { it.vilkår is Vilkår.ALDER }
        return AlderVilkårsvurderingDTO(
            samletUtfall = vilkårsvurdering.samletUtfall().mapToUtfallDTO(),
            perioder = perioder.map { mapVurderingToVilkårsvurderingDTO(it) },
        )
    }

    private fun mapStatligeYtelser(v: VilkårsvurderingKategori): StatligeYtelserDTO {
        val perioderMedDagpenger = v.vurderinger().filter { it.vilkår is Vilkår.DAGPENGER }
        val perioderMedAAP = v.vurderinger().filter { it.vilkår is Vilkår.AAP }
        val perioderMedUføre = v.vurderinger().filter { it.vilkår is Vilkår.UFØRETRYGD }
        val perioderMedSykepenger = v.vurderinger().filter { it.vilkår is Vilkår.SYKEPENGER }
        val perioderMedOvergangsstønad = v.vurderinger().filter { it.vilkår is Vilkår.OVERGANGSSTØNAD }
        val perioderMedPleiepengerNærstående = v.vurderinger().filter { it.vilkår is Vilkår.PLEIEPENGER_NÆRSTÅENDE }
        val perioderMedPleiepengerSyktBarn = v.vurderinger().filter { it.vilkår is Vilkår.PLEIEPENGER_SYKT_BARN }
        val perioderMedForeldrepenger = v.vurderinger().filter { it.vilkår is Vilkår.FORELDREPENGER }
        val perioderMedSvangerskapspenger = v.vurderinger().filter { it.vilkår is Vilkår.SVANGERSKAPSPENGER }
        val perioderMedOpplæringspenger = v.vurderinger().filter { it.vilkår is Vilkår.OPPLÆRINGSPENGER }
        val perioderMedOmsorgspenger = v.vurderinger().filter { it.vilkår is Vilkår.OMSORGSPENGER }
        val perioderMedGjenlevende = v.vurderinger().filter { it.vilkår is Vilkår.GJENLEVENDEPENSJON }
        val perioderMedSupplerendeStønadAlder = v.vurderinger().filter { it.vilkår is Vilkår.SUPPLERENDESTØNADALDER }
        val perioderMedSupplerendeStønadFlyktning =
            v.vurderinger().filter { it.vilkår is Vilkår.SUPPLERENDESTØNADFLYKTNING }
        val perioderMedAlderspensjon = v.vurderinger().filter { it.vilkår is Vilkår.ALDERSPENSJON }

        return StatligeYtelserDTO(
            samletUtfall = v.samletUtfall().mapToUtfallDTO(),
            aap = perioderMedAAP.map { mapVurderingToVilkårsvurderingDTO(it) },
            dagpenger = perioderMedDagpenger.map { mapVurderingToVilkårsvurderingDTO(it) },
            sykepenger = perioderMedSykepenger.map { mapVurderingToVilkårsvurderingDTO(it) },
            uføre = perioderMedUføre.map { mapVurderingToVilkårsvurderingDTO(it) },
            overgangsstønad = perioderMedOvergangsstønad.map { mapVurderingToVilkårsvurderingDTO(it) },
            pleiepengerNærstående = perioderMedPleiepengerNærstående.map { mapVurderingToVilkårsvurderingDTO(it) },
            pleiepengerSyktBarn = perioderMedPleiepengerSyktBarn.map { mapVurderingToVilkårsvurderingDTO(it) },
            foreldrepenger = perioderMedForeldrepenger.map { mapVurderingToVilkårsvurderingDTO(it) },
            svangerskapspenger = perioderMedSvangerskapspenger.map { mapVurderingToVilkårsvurderingDTO(it) },
            opplæringspenger = perioderMedOpplæringspenger.map { mapVurderingToVilkårsvurderingDTO(it) },
            omsorgspenger = perioderMedOmsorgspenger.map { mapVurderingToVilkårsvurderingDTO(it) },
            gjenlevendepensjon = perioderMedGjenlevende.map { mapVurderingToVilkårsvurderingDTO(it) },
            supplerendeStønad = perioderMedSupplerendeStønadAlder.map { mapVurderingToVilkårsvurderingDTO(it) },
            supplerendeStønadAlder = perioderMedSupplerendeStønadAlder.map { mapVurderingToVilkårsvurderingDTO(it) },
            supplerendeStønadFlyktning = perioderMedSupplerendeStønadFlyktning.map {
                mapVurderingToVilkårsvurderingDTO(
                    it,
                )
            },
            alderspensjon = perioderMedAlderspensjon.map { mapVurderingToVilkårsvurderingDTO(it) },
        )
    }

    private fun mapKommunaleYtelser(v: VilkårsvurderingKategori): KommunaleYtelserDTO {
        val perioderMedKVP = v.vurderinger().filter { it.vilkår is Vilkår.KVP }
        val perioderMedIntroprogrammet =
            v.vurderinger().filter { it.vilkår is Vilkår.INTROPROGRAMMET }
        return KommunaleYtelserDTO(
            samletUtfall = v.samletUtfall().mapToUtfallDTO(),
            kvp = perioderMedKVP.map { mapVurderingToVilkårsvurderingDTO(it) },
            introProgrammet = perioderMedIntroprogrammet.map { mapVurderingToVilkårsvurderingDTO(it) },
        )
    }

    private fun mapVurderingToVilkårsvurderingDTO(vurdering: Vurdering) =
        VilkårsvurderingDTO(
            periode = vurdering.fom?.let { fom ->
                ÅpenPeriodeDTO(
                    fra = fom,
                    til = vurdering.tom,
                )
            },
            kilde = vurdering.kilde,
            detaljer = vurdering.detaljer,
            kreverManuellVurdering = vurdering.utfall === Utfall.KREVER_MANUELL_VURDERING,
            utfall = vurdering.utfall.mapToUtfallDTO(),
        )

//    private fun mapBarn(innsending: Innsending) = listOf<BarnDTO>()
    /*
    søker.personopplysningerBarnMedIdent().map {
        BarnDTO(
            fornavn = it.fornavn,
            etternavn = it.etternavn,
            ident = it.ident,
            bosted = it.oppholdsland,
        )
    } + søker.personopplysningerBarnUtenIdent().map {
        BarnDTO(
            fornavn = it.fornavn!!,
            etternavn = it.etternavn!!,
            ident = null, // TODO
            bosted = null, // TODO
        )
    }
     */

    private fun vilkårsvurderinger(
        innsending: Innsending,
        vurderingsperiode: Periode,
        søknad: Søknad,
    ) = Inngangsvilkårsvurderinger(
        tiltakspengerYtelser = TiltakspengerVilkårsvurderingKategori(
            tiltakspengerVilkårsvurdering = TiltakspengerVilkårsvurdering(
                ytelser = innsending.ytelser!!.ytelserliste,
                vurderingsperiode = vurderingsperiode,
            ),
        ),
        statligeYtelser = StatligeYtelserVilkårsvurderingKategori(
            aap = AAPVilkårsvurdering(
                ytelser = innsending.ytelser!!.ytelserliste,
                vurderingsperiode = vurderingsperiode,
            ),
            dagpenger = DagpengerVilkårsvurdering(
                ytelser = innsending.ytelser!!.ytelserliste,
                vurderingsperiode = vurderingsperiode,
            ),
            foreldrepenger = ForeldrepengerVilkårsvurdering(
                ytelser = innsending.foreldrepengerVedtak!!.foreldrepengerVedtakliste,
                vurderingsperiode = vurderingsperiode,
            ),
            pleiepengerNærstående = PleiepengerNærståendeVilkårsvurdering(
                ytelser = innsending.foreldrepengerVedtak!!.foreldrepengerVedtakliste,
                vurderingsperiode = vurderingsperiode,
            ),
            pleiepengerSyktBarn = PleiepengerSyktBarnVilkårsvurdering(
                ytelser = innsending.foreldrepengerVedtak!!.foreldrepengerVedtakliste,
                vurderingsperiode = vurderingsperiode,
            ),
            omsorgspenger = OmsorgspengerVilkårsvurdering(
                ytelser = innsending.foreldrepengerVedtak!!.foreldrepengerVedtakliste,
                vurderingsperiode = vurderingsperiode,
            ),
            opplæringspenger = OpplæringspengerVilkårsvurdering(
                ytelser = innsending.foreldrepengerVedtak!!.foreldrepengerVedtakliste,
                vurderingsperiode = vurderingsperiode,
            ),
            svangerskapspenger = SvangerskapspengerVilkårsvurdering(
                ytelser = innsending.foreldrepengerVedtak!!.foreldrepengerVedtakliste,
                vurderingsperiode = vurderingsperiode,
            ),
            uføretrygd = UføreVilkarsvurdering(
                uføreVedtak = innsending.uføreVedtak?.uføreVedtak,
                vurderingsperiode = vurderingsperiode,
            ),
            overgangsstønad = OvergangsstønadVilkårsvurdering(
                overgangsstønadVedtak = innsending.overgangsstønadVedtak!!.overgangsstønadVedtak,
                vurderingsperiode = vurderingsperiode,
            ),
            sykepenger = SykepengerVilkårsvurdering(
                søknad = søknad,
                vurderingsperiode = vurderingsperiode,
            ),
            alderspensjon = AlderspensjonVilkårsvurdering(
                søknad = søknad,
                vurderingsperiode = vurderingsperiode,
            ),
            gjenlevendepensjon = GjenlevendepensjonVilkårsvurdering(
                søknad = søknad,
                vurderingsperiode = vurderingsperiode,
            ),
            supplerendeStønadFlyktning = SupplerendeStønadFlyktningVilkårsvurdering(
                søknad = søknad,
                vurderingsperiode = vurderingsperiode,
            ),
            supplerendeStønadAlder = SupplerendeStønadAlderVilkårsvurdering(
                søknad = søknad,
                vurderingsperiode = vurderingsperiode,
            ),
        ),
        kommunaleYtelser = KommunaleYtelserVilkårsvurderingKategori(
            intro = IntroProgrammetVilkårsvurdering(søknad = søknad, vurderingsperiode = vurderingsperiode),
            kvp = KVPVilkårsvurdering(søknad = søknad, vurderingsperiode = vurderingsperiode),
        ),
        pensjonsordninger = PensjonsinntektVilkårsvurderingKategori(
            privatPensjonsinntektVilkårsvurdering = PrivatPensjonsinntektVilkårsvurdering(
                søknad = søknad,
                vurderingsperiode = vurderingsperiode,
            ),
        ),
        lønnsinntekt = LønnsinntektVilkårsvurderingKategori(
            etterlønnVilkårsvurdering = EtterlønnVilkårsvurdering(
                søknad = søknad,
                vurderingsperiode = vurderingsperiode,
            ),
            lønnetArbeidVilkårsvurdering = LønnetArbeidVilkårsvurdering(
                søknad = søknad,
                vurderingsperiode = vurderingsperiode,
            ),
        ),
        institusjonopphold = InstitusjonVilkårsvurderingKategori(
            institusjonsoppholdVilkårsvurdering = InstitusjonsoppholdVilkårsvurdering(
                søknad = søknad,
                vurderingsperiode = vurderingsperiode,
                // institusjonsopphold = emptyList(),
            ),
        ),
        alder = AlderVilkårsvurderingKategori(
            alderVilkårsvurdering = AlderVilkårsvurdering(
                vurderingsperiode = vurderingsperiode,
                søkersFødselsdato = innsending.personopplysningerSøker()!!.fødselsdato,
            ),
        ),
    )

    private fun Utfall.mapToUtfallDTO(): UtfallDTO {
        return when (this) {
            Utfall.OPPFYLT -> UtfallDTO.Oppfylt
            Utfall.IKKE_OPPFYLT -> UtfallDTO.IkkeOppfylt
            Utfall.KREVER_MANUELL_VURDERING -> UtfallDTO.KreverManuellVurdering
        }
    }
}

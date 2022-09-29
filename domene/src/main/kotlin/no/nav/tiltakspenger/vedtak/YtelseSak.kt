package no.nav.tiltakspenger.vedtak

import java.time.LocalDate
import java.time.LocalDateTime

//Dokumentert her: https://confluence.adeo.no/display/ARENA/Arena+-+Tjeneste+Webservice+-+Ytelseskontrakt_v3#ArenaTjenesteWebserviceYtelseskontrakt_v3-HentYtelseskontraktListeResponse
data class YtelseSak(
    val fomGyldighetsperiode: LocalDateTime,
    val tomGyldighetsperiode: LocalDateTime,
    val datoKravMottatt: LocalDate?,
    val dataKravMottatt: String? = null,
    val fagsystemSakId: Int? = null,
    val status: YtelseSakStatus? = null,
    val ytelsestype: YtelseSakYtelsetype? = null,
    val vedtak: List<YtelseVedtak> = emptyList(),
    val antallDagerIgjen: Int? = null,
    val antallUkerIgjen: Int? = null,
    val innhentet: LocalDateTime,
) : Tidsstempler {
    override fun tidsstempelKilde(): LocalDateTime = innhentet
    override fun tidsstempelHosOss(): LocalDateTime = innhentet

    data class YtelseVedtak(
        val beslutningsDato: LocalDate? = null,
        val periodetypeForYtelse: YtelseVedtakPeriodeTypeForYtelse? = null,
        val vedtaksperiodeFom: LocalDate? = null,
        val vedtaksperiodeTom: LocalDate? = null,
        val vedtaksType: YtelseVedtakVedtakstype? = null,
        val status: YtelseVedtakStatus? = null,
    ) {
        enum class YtelseVedtakPeriodeTypeForYtelse(val navn: String) {
            E("Endring"),
            F("Forlenget ventetid"), // Gjelder ikke tiltakspenger
            G("Gjenopptak"),
            N("Annuller sanksjon"), // Gjelder ikke tiltakspenger
            O("Ny rettighet"),
            S("Stans"),
            T("Tidsbegrenset bortfall") // Gjelder ikke tiltakspenger
        }

        enum class YtelseVedtakVedtakstype(val navn: String, val ytelseSakYtelsetype: YtelseSakYtelsetype) {
            AAP("Arbeidsavklaringspenger", YtelseSakYtelsetype.AA),
            DAGO("Ordinære dagpenger", YtelseSakYtelsetype.DAGP),
            PERM("Dagpenger under permitteringer", YtelseSakYtelsetype.DAGP),
            FISK("Dagp. v/perm fra fiskeindustri", YtelseSakYtelsetype.DAGP),
            LONN("Lønnsgarantimidler - dagpenger", YtelseSakYtelsetype.DAGP),
            BASI("Tiltakspenger (basisytelse før 2014)", YtelseSakYtelsetype.INDIV),
            BTIL("Barnetillegg", YtelseSakYtelsetype.INDIV)
        }

        enum class YtelseVedtakStatus(val navn: String) {
            AVSLU("Avsluttet"),
            GODKJ("Godkjent"),
            INNST("Innstilt"),
            IVERK("Iverksatt"),
            MOTAT("Mottatt"),
            OPPRE("Opprettet"),
            REGIS("Registrert")
        }
    }

    enum class YtelseSakStatus(val navn: String) {
        AKTIV("Aktiv"),
        AVSLU("Lukket"),
        INAKT("Inaktiv")
    }

    enum class YtelseSakYtelsetype(val navn: String) {
        AA("Arbeidsavklaringspenger"),
        DAGP("Dagpenger"),
        INDIV("Individstønad"),
    }

}

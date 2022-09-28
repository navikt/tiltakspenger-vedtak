package no.nav.tiltakspenger.vedtak.rivers

import java.time.LocalDate
import java.time.LocalDateTime

data class YtelseSakDTO(
    val fomGyldighetsperiode: LocalDateTime,
    val tomGyldighetsperiode: LocalDateTime,
    val datoKravMottatt: LocalDate?,
    val dataKravMottatt: String? = null,
    val fagsystemSakId: Int? = null,
    val status: YtelseSakStatusEnum? = null,
    val ytelsestype: YtelseSakYtelsetypeEnum? = null,
    val vedtak: List<YtelseVedtakDTO> = emptyList(),
    val antallDagerIgjen: Int? = null,
    val antallUkerIgjen: Int? = null
) {

    data class YtelseVedtakDTO(
        val beslutningsDato: LocalDate? = null,
        val periodetypeForYtelse: YtelseVedtakPeriodeTypeForYtelseEnum? = null,
        val vedtaksperiodeFom: LocalDate? = null,
        val vedtaksperiodeTom: LocalDate? = null,
        val vedtaksType: YtelseVedtakVedtakstypeEnum? = null,
        val status: YtelseVedtakStatusEnum? = null,
    )
}


enum class YtelseSakStatusEnum(val navn: String) {
    AKTIV("Aktiv"),
    AVSLU("Lukket"),
    INAKT("Inaktiv")
}

enum class YtelseSakYtelsetypeEnum(val navn: String) {
    AA("Arbeidsavklaringspenger"),
    DAGP("Dagpenger"),
    INDIV("Individstønad"),
}

enum class YtelseVedtakPeriodeTypeForYtelseEnum(val navn: String) {
    E("Endring"),
    F("Forlenget ventetid"), // Gjelder ikke tiltakspenger
    G("Gjenopptak"),
    N("Annuller sanksjon"), // Gjelder ikke tiltakspenger
    O("Ny rettighet"),
    S("Stans"),
    T("Tidsbegrenset bortfall") // Gjelder ikke tiltakspenger
}

enum class YtelseVedtakVedtakstypeEnum(val navn: String, val ytelseSakYtelsetype: YtelseSakYtelsetypeEnum) {
    AAP("Arbeidsavklaringspenger", YtelseSakYtelsetypeEnum.AA),
    DAGO("Ordinære dagpenger", YtelseSakYtelsetypeEnum.DAGP),
    PERM("Dagpenger under permitteringer", YtelseSakYtelsetypeEnum.DAGP),
    FISK("Dagp. v/perm fra fiskeindustri", YtelseSakYtelsetypeEnum.DAGP),
    LONN("Lønnsgarantimidler - dagpenger", YtelseSakYtelsetypeEnum.DAGP),
    BASI("Tiltakspenger (basisytelse før 2014)", YtelseSakYtelsetypeEnum.INDIV)
}

enum class YtelseVedtakStatusEnum(val navn: String) {
    AVSLU("Avsluttet"),
    GODKJ("Godkjent"),
    INNST("Innstilt"),
    IVERK("Iverksatt"),
    MOTAT("Mottatt"),
    OPPRE("Opprettet"),
    REGIS("Registrert")
}

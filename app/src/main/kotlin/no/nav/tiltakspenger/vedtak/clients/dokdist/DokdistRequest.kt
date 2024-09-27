package no.nav.tiltakspenger.vedtak.clients.dokdist

import no.nav.tiltakspenger.felles.journalføring.JournalpostId
import no.nav.tiltakspenger.vedtak.routes.serialize

private data class DokdistRequest(
    val journalpostId: String,
    val bestillendeFagsystem: String = "IND",
    val dokumentProdApp: String = "Tiltakspenger",
    val distribusjonstype: DistribusjonsType = DistribusjonsType.VEDTAK,
    val distribusjonstidspunkt: Distribusjonstidspunkt = Distribusjonstidspunkt.KJERNETID,
) {
    enum class DistribusjonsType {
        VEDTAK,
        VIKTIG,
        ANNET,
    }

    enum class Distribusjonstidspunkt {
        UMIDDELBART,
        KJERNETID,
    }
}

fun JournalpostId.toDokdistRequest(): String {
    return DokdistRequest(
        journalpostId = this.toString(),
    ).let {
        serialize(it)
    }
}

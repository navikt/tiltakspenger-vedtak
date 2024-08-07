@file:Suppress("ktlint:standard:max-line-length")

package no.nav.tiltakspenger.vedtak.clients.tiltak

import no.nav.tiltakspenger.felles.TiltakId
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.tiltak.TiltakResponsDTO.DeltakerStatusDTO.AVBRUTT
import no.nav.tiltakspenger.libs.tiltak.TiltakResponsDTO.DeltakerStatusDTO.DELTAR
import no.nav.tiltakspenger.libs.tiltak.TiltakResponsDTO.DeltakerStatusDTO.FEILREGISTRERT
import no.nav.tiltakspenger.libs.tiltak.TiltakResponsDTO.DeltakerStatusDTO.FULLFORT
import no.nav.tiltakspenger.libs.tiltak.TiltakResponsDTO.DeltakerStatusDTO.HAR_SLUTTET
import no.nav.tiltakspenger.libs.tiltak.TiltakResponsDTO.DeltakerStatusDTO.IKKE_AKTUELL
import no.nav.tiltakspenger.libs.tiltak.TiltakResponsDTO.DeltakerStatusDTO.PABEGYNT_REGISTRERING
import no.nav.tiltakspenger.libs.tiltak.TiltakResponsDTO.DeltakerStatusDTO.SOKT_INN
import no.nav.tiltakspenger.libs.tiltak.TiltakResponsDTO.DeltakerStatusDTO.VENTELISTE
import no.nav.tiltakspenger.libs.tiltak.TiltakResponsDTO.DeltakerStatusDTO.VENTER_PA_OPPSTART
import no.nav.tiltakspenger.libs.tiltak.TiltakResponsDTO.DeltakerStatusDTO.VURDERES
import no.nav.tiltakspenger.libs.tiltak.TiltakResponsDTO.TiltakDTO
import no.nav.tiltakspenger.saksbehandling.domene.tiltak.Tiltak
import no.nav.tiltakspenger.saksbehandling.domene.tiltak.Tiltak.Gjennomføring
import no.nav.tiltakspenger.saksbehandling.domene.tiltak.TiltakDeltakerstatus.Avbrutt
import no.nav.tiltakspenger.saksbehandling.domene.tiltak.TiltakDeltakerstatus.Deltar
import no.nav.tiltakspenger.saksbehandling.domene.tiltak.TiltakDeltakerstatus.Feilregistrert
import no.nav.tiltakspenger.saksbehandling.domene.tiltak.TiltakDeltakerstatus.Fullført
import no.nav.tiltakspenger.saksbehandling.domene.tiltak.TiltakDeltakerstatus.HarSluttet
import no.nav.tiltakspenger.saksbehandling.domene.tiltak.TiltakDeltakerstatus.IkkeAktuell
import no.nav.tiltakspenger.saksbehandling.domene.tiltak.TiltakDeltakerstatus.PåbegyntRegistrering
import no.nav.tiltakspenger.saksbehandling.domene.tiltak.TiltakDeltakerstatus.SøktInn
import no.nav.tiltakspenger.saksbehandling.domene.tiltak.TiltakDeltakerstatus.Venteliste
import no.nav.tiltakspenger.saksbehandling.domene.tiltak.TiltakDeltakerstatus.VenterPåOppstart
import no.nav.tiltakspenger.saksbehandling.domene.tiltak.TiltakDeltakerstatus.Vurderes
import no.nav.tiltakspenger.saksbehandling.domene.tiltak.Tiltakskilde
import java.time.LocalDateTime

internal fun mapTiltak(
    tiltakDTO: List<TiltakDTO>,
    innhentet: LocalDateTime,
): List<Tiltak> =
    tiltakDTO
        .filterNot { it.deltakelseFom == null }
        .filterNot { it.deltakelseTom == null }
        .map {
            Tiltak(
                id = TiltakId.random(),
                eksternId = it.id,
                gjennomføring =
                Gjennomføring(
                    id = it.gjennomforing.id,
                    arrangørnavn = it.gjennomforing.arrangørnavn,
                    typeNavn = it.gjennomforing.typeNavn,
                    typeKode = it.gjennomforing.arenaKode.name,
                    rettPåTiltakspenger = it.gjennomforing.arenaKode.rettPåTiltakspenger,
                ),
                deltakelsesperiode = Periode(it.deltakelseFom!!, it.deltakelseTom!!),
                deltakelseStatus =
                when (it.deltakelseStatus) {
                    VURDERES -> Vurderes
                    VENTER_PA_OPPSTART -> VenterPåOppstart
                    DELTAR -> Deltar
                    HAR_SLUTTET -> HarSluttet
                    AVBRUTT -> Avbrutt
                    IKKE_AKTUELL -> IkkeAktuell
                    FEILREGISTRERT -> Feilregistrert
                    PABEGYNT_REGISTRERING -> PåbegyntRegistrering
                    SOKT_INN -> SøktInn
                    VENTELISTE -> Venteliste
                    FULLFORT -> Fullført
                },
                deltakelseProsent = it.deltakelseProsent,
                kilde =
                when {
                    it.kilde.lowercase().contains("komet") -> Tiltakskilde.Komet
                    it.kilde.lowercase().contains("arena") -> Tiltakskilde.Arena
                    else -> throw IllegalStateException(
                        "Kunne ikke parse tiltak fra tiltakspenger-tiltak. Ukjent kilde: ${it.kilde}. Forventet Arena eller Komet. Tiltaksid: ${it.id}",
                    )
                },
                registrertDato = it.registrertDato,
                innhentetTidspunkt = innhentet,
            )
        }

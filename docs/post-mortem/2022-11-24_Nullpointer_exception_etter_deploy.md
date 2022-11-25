## Hva skjedde?
Tiltakspenger-vedtak var nede i 90 min etter merge av en stor branch inn i main.

## Hvilken effekt hadde det?
Dette gjorde at Tiltakspenger-saksbehandler var utilgjengelig i dette tidsrommet og at innkomne søknader midlertidig ikke ble behandlet

## Hvordan oppdaget vi feilen
Kom varsel i tpts-varsel, men ikke alle fikk med seg denne

## Rotårsak til feilen
Vi lagret ikke identen fra søknaden. Den ble satt på Innsending, men update-kallet i Repositoryet oppdaterte bare tilstand. Testene fanget ikke opp dette, de var ikke gode nok. (Det var en endring der i at Innsending har både journalpostId og ident, mens Søker bare hadde ident, og det ekstra feltet som nå er med tok ikke testene (eller repoet) høyde for.

## Læring


## Hva gjorde vi bra
Vi fant feilen og rettet den?
Vi gikk fremover og rullet ikke tilbake

## Hva gjorde vi dårlig
Skrev for få tester

## Hvordan hindrer vi at noe lignende skjer igjen? (tiltak)
- (PJ) Vi burde ha en egen tpts-varsel for prod
  - tpts-varsel-dev
  - tpts-varsel-prod
  - tpts-byggefeil
- (PJ) lage rulleringsplan med ekstra ansvar for å følge med på varsel
- (EV) lage ende-til-ende tester
- huske å lage flere tester
- ikke så store brancher/kanskje ikke branch?
- dele tiltakspenger-vedtak i en api del og en kafka del?

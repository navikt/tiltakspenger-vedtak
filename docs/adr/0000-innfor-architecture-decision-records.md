# Innfør Architecture Decision Records

Status: Foreslått

## Kontekst

Beslutninger er vanskelige å forstå i fremtiden uten konteksten de ble tatt i. Vi trenger en måte å holde rede på konteksten rundt en beslutning. ADR virker være et godt verktøy for å holde denne oversikten.

## Beslutning

Innfør ADR - [Architecture Decision Records](https://adr.github.io/) - som et verktøy for å holde rede på beslutninger vi tar som har en viss grad av påvirkning på systemet. 

ADR'en skal skrives på norsk. 

En ADR skal inneholde følgende:

Tittel: en kort (mindre enn 50 tegn) beskrivende setning i [imperativ](https://no.wikipedia.org/wiki/Imperativ) form. F.eks. "Bruk norsk som domenespråk" eller "Innfør Architecture Decision Records".

Status: foreslått, akseptert, avslått, utdatert, erstattet, etc.

Kontekst: hva løser denne beslutningen eller endringen?

Beslutning: hva er det vi faktisk foreslår?

Konsekvenser: hva blir lettere eller vanskeligere av denne endringen?

### ADR filnavn

* Filnavnet starter med et løpenummer med 4 sifre. 0000 er den første ADR'en. Dette gjør det lettere å sortere på når endringen ble foreslått
* Filnavnet inneholder deretter tittelen på ADR'en. Dette gjør det lettere å lese filnavnet
* Filen skal være i `md`-format. Dette gjør det lettere å redigere dokumentene og gir alle dokumentene en helhetlig form.

## Konsekvenser

Alle beslutninger blir registrert som en ADR.

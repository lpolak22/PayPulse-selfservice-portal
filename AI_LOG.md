# AI_LOG: XP^AI radionica, tim Srećkovici

## Izjava o korištenju umjetne inteligencije

Pisano prema Okviru korištenja alata umjetne inteligencije FOI-ja i Smjernicama radionice.

1. **Alati.** Popis alata i modela je u tablici ispod. Ako ovdje nije drugačije navedeno, svaki dio koji je generirao AI naveden je u tablici Zapisi.
2. **Provjera.**
   - Nijedan rezultat AI-ja nije ušao u commit samo na riječ.
   - Kod je prošao testove prihvatljivosti i build, a par ga je pročitao prije commita.
   - Brojke koje AI tvrdi (npr. omjere kontrasta) provjerava test.
3. **Zaštita podataka.**
   - U upite nismo stavljali `.env`, API ključeve, lozinke, osobne podatke korisnika ni stvarne transakcije trgovaca.
   - Claude je repozitorij te Jiru i Confluence projekta samo čitao, preko alata koje je tim spojio.
4. **Odgovornost.** Za sav kod i tekst u ovoj grani odgovara tim. Svaki član zna objasniti dio na kojem je radio.

### Alati i razine

| Tko | Alat i model | Razina po Smjernicama |
|---|---|---|
| Svi, planiranje | Claude u aplikaciji (Claude Opus 5.5) | 3 |
| Par 1, Jan i Lucija | Claude Code (Claude Opus 5.5) i Claude u aplikaciji (Claude Opus 5.5) | 3 |
| Par 2, Ennio i Marija | upisati alat i model | 3 |
| Par 3, Ana | Claude Code (Claude Opus 5.5) + Claude chat za provjeru | 3 za XP-3, 4 za XP-6 |

XP-1 je razina 3, a ne 4:
- AI je predlagao testove, kod i boje.
- Redoslijed smo vodili mi: prvo testovi, pa kod.
- Odluke o bojama i opsegu donio je par. Primjer: rubove smo namjerno ostavili za XP-4.
- Svaki rezultat provjerili smo testom prije commita.

## Pravila

- Upit i odluku upisujemo odmah, a ne na kraju radionice.
- U upite ne lijepimo `.env`, API ključeve, lozinke ni stvarne podatke trgovaca.
- Ništa što AI napiše ne ide u commit dok test ne prođe i dok ga par ne pročita i ne zna objasniti.
- Kad AI pogriješi, upisujemo što je pogriješio i kako smo to otkrili.
- AI ne smije mijenjati testove. Testovi su ugovor, a AI piše samo produkcijski kod.
- Kad AI tvrdi neku brojku (npr. omjer kontrasta), ne vjerujemo na riječ: provjerava je test, a razliku upisujemo u log.

## Zapisi

| Vrijeme | Par | Priča | Razina | Upit (sažeto) | Što je AI dao | Što smo napravili s tim i zašto |
|---|---|---|---|---|---|---|
| 0 | Jan | Plan | 3 | Claude je dobio prezentaciju te predložiti raspodjelu posla | Ennio + Marija: xp-radionica/postavke-pristupacnosti, u 2. iteraciji xp-radionica/veci-tekst
Ana: xp-radionica/opisi-za-citac-zaslona, u 2. iteraciji xp-radionica/redak-transakcije-citac |Prihvatili smo podjelu posla i napravili kostur kako bi se moglo početi s radom |
| 39 | Ana | XP-3 | 3 | Pronađi ikone bez opisa, klikabilne bez teksta i naslove bez heading() u ui/users i ui/shell; ništa ne mijenjaj | Popis: 6 ukrasnih ikona (ispravno), SaveButton i UserRow bez oznake, 3 naslova bez heading(), 2 spinnera bez opisa | Prihvatili stavke 1–4, odbili opcionalne i bočne napomene (izvan priče). Sve nalaze ručno provjerili u kodu, točni su |
| 43 | Ana | XP-3 | 3 | Napiši samo 6 Compose UI testova iz kriterija, bez koda | UsersScreenReaderTest, 6 testova | Pokrenuli na emulatoru: 6/6 crveno na assertu (pravi razlog). Commit RED lokalno |
| 56 | Ana | XP-3 | 3 | GREEN: najmanja promjena za stavke 1–4, ne diraj testove | heading(), onClickLabel, contentDescription u 5 datoteka + 2 stringa | 6/6 zeleno, pregledali diff, push i PR |
| 13:00 | Jan | Plan | 3 | Claude je dobio prezentaciju radionice, repozitorij, Jiru i Confluence i trebao je predložiti funkcionalnost i raspodjelu posla koja se ne preklapa s planom projekta | Pristupačnost umjesto ručne teme, jer je tema već u PULSE-77. Sedam priča s kriterijima, 2 iteracije, grane po parovima, kostur i testovi prihvatljivosti za XP-1, XP-2 i XP-5. Upozorio da je prva grana `xp-radionica/dev` napravljena iz `PULSE-59`, a ne iz `dev` | Prihvatili smo opseg, 2 iteracije i podjelu po parovima. Granu `xp-radionica/dev` napravili smo ponovno iz `dev`. PULSE-77 nismo dirali |
| 13:24 | Par 1 (vozila Lucija) | Kostur | 3 | Claude Code: pripremi kostur iz zipa na `xp-radionica/dev`, ali ništa ne commitaj | Kopirao 7 datoteka kostura, pokrenuo build i unit testove: 169/169 zeleno. Izgled aplikacije je isti, jer su boje samo preseljene u `ColorSchemes.kt` | Pregledali smo datoteke i sami commitali u dva commita, kako piše u planu |
| 13:24 | Par 3 (Ana) | XP-3 | 3 | Pronađi ikone bez opisa, klikabilne bez teksta i naslove bez heading() u ui/users i ui/shell; ništa ne mijenjaj | Popis: 6 ukrasnih ikona (ispravno), SaveButton i UserRow bez oznake, 3 naslova bez heading(), 2 spinnera bez opisa | Prihvatili stavke 1–4, odbili opcionalne i bočne napomene (izvan priče). Sve nalaze ručno provjerili u kodu, točni su |
| 13:28 | Par 1 (vozila Lucija) | Kostur | 3 | Push iz GitSwampa odbijen: „refusing to allow an OAuth App to create or update workflow ... without `workflow` scope” | Objasnio da GitSwamp nema dopuštenje `workflow` za `.github/workflows/`, upozorio da ne radimo force push i predložio `git push origin xp-radionica/dev` iz terminala | Push iz terminala je prošao i workflow „XP radionica” se pokrenuo na GitHub Actions (zeleno, 2 min 19 s) |
| 13:28 | Par 3 (Ana) | XP-3 | 3 | Napiši samo 6 Compose UI testova iz kriterija, bez koda | UsersScreenReaderTest, 6 testova | Pokrenuli na emulatoru: 6/6 crveno na assertu (pravi razlog). Commit RED lokalno |
| 13:35 | Par 1 (vozila Lucija) | Plan rada | 3 | Kako raditi s AI-jem da poštujemo razinu 3 i da tim zna obraniti kod | Predložio da AI prvo objasni formulu i predvidi brojke, a tek onda piše kod. Svaku brojku koju AI tvrdi provjerava test. Na kraju AI postavlja pitanja, a mi odgovaramo sami | Prihvatili smo taj ritam za XP-1 i dodali dva pravila u Pravila |
| 13:41 | Par 3 (Ana) | XP-3 | 3 | GREEN: najmanja promjena za stavke 1–4, ne diraj testove | heading(), onClickLabel, contentDescription u 5 datoteka + 2 stringa | 6/6 zeleno, pregledali diff, push i PR |
| 13:44 | Par 1 (vozila Lucija) | XP-1 | 3 | Claude Code: napravi granu `xp-radionica/visoki-kontrast` iz `xp-radionica/dev` i kopiraj testove prihvatljivosti za XP-1 | Granu i 4 datoteke: `ContrastRatioTest` (8), `PayPulseColorSchemeTest` (6), `HighContrastThemeTest` (3, emulator) i prazan `contrastRatio` s `TODO("XP-1")`. Potvrdio da se sve prevodi, da 14 novih testova pada i da ostalih 169 prolazi | Testove smo commitali prije koda (crveno). Jan preuzima kao vozač |
| 13:52 | Par 1 (vozio Jan) | XP-1 | 3 | Claude: implementiraj `contrastRatio` po WCAG 2.1, bez biblioteka i bez mijenjanja testova, i objasni formulu | Funkcija s relativnom svjetlinom: sRGB kanal pretvara u linearnu svjetlost (prag 0,04045, eksponent 2,4) i vraća omjer (svjetlija + 0,05) / (tamnija + 0,05). Uz kod je dao objašnjenje u 3 rečenice | Jan je radio na laptopu bez Android Studija, pa je testove pokretao GitHub Actions. Nakon ovog commita CI je bio crven točno kako smo predvidjeli: 183 testa, 2 pala (`highContrastLight_` i `highContrastDark_everyTextColorHasAtLeastAaaContrast`), jer sheme još nisu postojale. `ContrastRatioTest` je prošao 8/8. Formulu smo usporedili s definicijom relativne svjetline u WCAG 2.1 |
| 13:54 | Par 1 (vozio Jan) | XP-1 | 3 | Claude: predloži visokokontrastne boje u PayPulse crvenoj, gdje svaki tekst ima barem 7:1 prema svojoj pozadini, i za svaki par izračunaj omjer | 6 novih boja u `Color.kt` i dvije sheme u `ColorSchemes.kt`. Prema AI-ju je najmanji omjer 9,08 u svijetloj i 12,35 u tamnoj. Za tamnu pozadinu ponudio dvije opcije, `#000000` ili `#121212` | Jan je odlučio: čisto crna pozadina, jer daje najveći kontrast i štedi bateriju na OLED zaslonima. Boja greške ostaje ista kao glavna crvena. Rubove (`outline`) namjerno ne diramo, to je XP-4. Omjere nismo prihvatili na riječ: nakon commita sa shemama CI je zelen, svih 183 unit testa prolazi, uključujući `PayPulseColorSchemeTest` 6/6. `HighContrastThemeTest` je na emulatoru pokrenula Lucija: 3/3 zeleno |
| 13:54 | Par 1 (vozio Jan) | XP-1 | 3 | Claude: kako vidjeti visoki kontrast prije nego Par 2 spoji prekidač u aplikaciji | Preview `TransactionsScreen` u visokom kontrastu, svijetli i tamni, u `TransactionsPreviews.kt` | Prihvaćeno. Prevođenje je potvrdio CI (zeleno), a izgled je u Android Studiju provjerila Lucija i napravila screenshot za prezentaciju |
| 14:04 | Par 3 (Ana) | XP-6 | 4 | Dali agentu 3 kriterija; korak 1 analiza, korak 2 samo testovi | TransactionRowScreenReaderTest, 3 testa; sam predvidio 1 crven i 2 zelena | Pokrenuli: točno 1 crven (redoslijed), 2 zelena. Commit RED |
| 14:06 | Par 3 (Ana) | XP-6 | 4 | Korak 3 i 4: najmanja promjena + objašnjenje | Jedna linija u TransactionRow.kt (redoslijed listOfNotNull) + 4 rizika | 3/3 zeleno, pregledali diff, push |

## Gdje je AI pogriješio

| Vrijeme | Par | Što je AI predložio | Kako smo otkrili da ne valja | Kako smo ispravili |
|---|---|---|---|---|
| 43 | Ana | Da test i kod idu u isti commit, bez RED commita | XP pravilo traži vidljiv RED commit u povijesti | Odbili, napravili zaseban RED commit 0c68fc0 |
| 39 | Ana | Nije bio siguran označava li M3 top bar naslov kao heading (sam je to naveo) | Test XP3_form_top_bar_title_is_heading je pao | Pretpostavka provjerena testom, izmjena ostala |
| 13:02 | Par 1 | Claude je u planu rekao da će sam napraviti granu `xp-radionica/visoki-kontrast` | Push mu je odbijen, jer njegova sesija nema pravo pisanja u naš repozitorij | Granu smo napravili sami u GitSwampu |
| 13:19 | Svi | U planiranju predložio XP-6 i XP-7 bez provjere postojećeg koda | Ana je pregledala kod: TransactionRow i grafovi već imaju opise | XP-6 smanjen na 1 bod, XP-7 izbačen |
| 13:24 | Par 3 (Ana) | Nije bio siguran označava li M3 top bar naslov kao heading (sam je to naveo) | Test XP3_form_top_bar_title_is_heading je pao | Pretpostavka provjerena testom, izmjena ostala |
| 13:28 | Par 1 | Upute za kostur pretpostavljale su da GitSwamp može pushati `.github/workflows/xp-radionica.yml` | GitHub je odbio push bez dopuštenja `workflow` | Push iz terminala, bez force pusha (vidi Zapisi, 13:28) |
| 13:28 | Par 3 (Ana) | Da test i kod idu u isti commit, bez RED commita | XP pravilo traži vidljiv RED commit u povijesti | Odbili, napravili zaseban RED commit 0c68fc0 |
| 13:33 | Svi | Predložak AI_LOG-a imao je jednu zajedničku tablicu za sve parove, iako parovi rade na odvojenim granama | Claude je to sam primijetio kad je pisao upute za merge, jer će se redovi različitih parova sudarati | Konflikte u AI_LOG-u rješavamo pri mergeu na GitHubu i zadržavamo redove svih parova |

Ograničenje koje smo znali unaprijed: Claude je kostur i testove pisao bez mogućnosti da ih prevede, jer u njegovom okruženju nema Android SDK-a. Zato je Lucija prije prvog commita napravila build i pokrenula sve postojeće testove (169/169).

## Priča na razini 4: XP-6

1. Kriteriji koje smo dali agentu: redoslijed status → iznos → kartica → datum i vrijeme; "Otvori detalje" pri dvostrukom dodiru; kartica samo maskirana.
2. Testovi koje je agent napisao prvi, i jesu li bili crveni prije koda: 3 testa; crven samo test redoslijeda, druga dva zelena jer je to već postojalo. Test namjerno šalje cijeli broj kartice i provjerava da se čuju samo zadnje 4 znamenke.
3. Kod koji je agent napisao: jedna linija u TransactionRow.kt, promijenjen redoslijed u listOfNotNull.
4. Naša analiza: provjerili da se vizualno ništa ne mijenja (samo semantika); rizik: redak na Nadzornoj ploči (LatestTransactionRow) i dalje čita karticu prvu, to je kandidat za sljedeći korak.
5. Objašnjenje koda vlastitim riječima (Ana): TalkBack čita opis retka kao jednu rečenicu složenu od liste dijelova. Redoslijed u listi je redoslijed čitanja, pa sam status i iznos premjestila naprijed jer korisnik prvo želi znati je li plaćanje prošlo.

## Zaključak tima

- Gdje nam je AI najviše pomogao:
- Gdje nam je najviše odmogao:
- Što bismo sljedeći put napravili drugačije:

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
| Par 2, Ennio i Marija | Ennio: Claude Code (Claude Opus 5.5). Marija: upisati alat i model | 3 |
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
| 13:35 | Par 2 (Ennio) | Plan rada | 3 | Claude Code: prema uputama za Par 2 predloži što tko radi u kojem bloku | Podjelu po blokovima od 15 minuta: prvo podatkovni sloj i ViewModel s unit testovima, zatim ekran, navigacija, `MainActivity` i test na emulatoru. Upozorio da testova iz uputa (`DataStoreAccessibilitySettingsRepositoryTest`, `AccessibilitySettingsScreenTest`) još nema, da projekt nema Robolectric pa repozitorij mora primati `DataStore` kroz konstruktor, i da za `DrawerItem` treba ikona koje nema | Prihvatili smo podjelu. Ennio vozi prvi blok, Marija drugi. Testove pišemo sami, prije koda |
| 13:40 | Par 2 (vozio Ennio) | XP-2 | 3 | Claude Code: napiši `DataStoreAccessibilitySettingsRepositoryTest` prije koda | Test sa 7 slučajeva: početno stanje, svaka postavka zasebno, obje uključene, ponovno isključivanje, spremanje pod ključem `larger_text` te postavke koje ostaju kad se nad istom datotekom otvori novi DataStore (kriterij 3) | Test nije prolazio ni kompilaciju dok klasa nije postojala (crveno). Pročitali smo ga i prihvatili |
| 13:41 | Par 3 (Ana) | XP-3 | 3 | GREEN: najmanja promjena za stavke 1–4, ne diraj testove | heading(), onClickLabel, contentDescription u 5 datoteka + 2 stringa | 6/6 zeleno, pregledali diff, push i PR |
| 13:44 | Par 1 (vozila Lucija) | XP-1 | 3 | Claude Code: napravi granu `xp-radionica/visoki-kontrast` iz `xp-radionica/dev` i kopiraj testove prihvatljivosti za XP-1 | Granu i 4 datoteke: `ContrastRatioTest` (8), `PayPulseColorSchemeTest` (6), `HighContrastThemeTest` (3, emulator) i prazan `contrastRatio` s `TODO("XP-1")`. Potvrdio da se sve prevodi, da 14 novih testova pada i da ostalih 169 prolazi | Testove smo commitali prije koda (crveno). Jan preuzima kao vozač |
| 13:45 | Par 2 (vozio Ennio) | XP-2 | 3 | Claude Code: napiši `DataStoreAccessibilitySettingsRepository` i liniju u `AppContainer` pa pokreni testove | Repozitorij s glavnim konstruktorom koji prima `DataStore` (za test) i sporednim koji prima `Context` (za `AppContainer`), uz `ReplaceFileCorruptionHandler` kao u `EncryptedTokenStorage`. Test 7/7 zeleno, ukupno 176 unit testova | Kod smo usporedili s `EncryptedTokenStorage` i prihvatili. Lokalni build tražio je JDK 25 zbog `gradle-daemon-jvm.properties`, pa je Claude Gradleu dao JDK iz Android Studija, bez mijenjanja datoteka u repozitoriju i bez skidanja novog JDK-a |
| 13:51 | Par 2 (vozio Ennio) | XP-2 | 3 | Claude Code: `AccessibilitySettingsViewModelTest` po uzoru na `MerchantsViewModelTest` i `AccessibilitySettingsViewModel` sa `stateIn` i `Factory` | ViewModel, `StubAccessibilitySettingsRepository` i 6 testova, među njima i da se promjena u repozitoriju odmah vidi u ViewModelu (kriterij 2). Svih 182 unit testa zeleno | Commitali smo u 13:51. Test nije bio crven prije koda, vidi Gdje je AI pogriješio |
| 13:52 | Par 1 (vozio Jan) | XP-1 | 3 | Claude: implementiraj `contrastRatio` po WCAG 2.1, bez biblioteka i bez mijenjanja testova, i objasni formulu | Funkcija s relativnom svjetlinom: sRGB kanal pretvara u linearnu svjetlost (prag 0,04045, eksponent 2,4) i vraća omjer (svjetlija + 0,05) / (tamnija + 0,05). Uz kod je dao objašnjenje u 3 rečenice | Jan je radio na laptopu bez Android Studija, pa je testove pokretao GitHub Actions. Nakon ovog commita CI je bio crven točno kako smo predvidjeli: 183 testa, 2 pala (`highContrastLight_` i `highContrastDark_everyTextColorHasAtLeastAaaContrast`), jer sheme još nisu postojale. `ContrastRatioTest` je prošao 8/8. Formulu smo usporedili s definicijom relativne svjetline u WCAG 2.1 |
| 13:54 | Par 1 (vozio Jan) | XP-1 | 3 | Claude: predloži visokokontrastne boje u PayPulse crvenoj, gdje svaki tekst ima barem 7:1 prema svojoj pozadini, i za svaki par izračunaj omjer | 6 novih boja u `Color.kt` i dvije sheme u `ColorSchemes.kt`. Prema AI-ju je najmanji omjer 9,08 u svijetloj i 12,35 u tamnoj. Za tamnu pozadinu ponudio dvije opcije, `#000000` ili `#121212` | Jan je odlučio: čisto crna pozadina, jer daje najveći kontrast i štedi bateriju na OLED zaslonima. Boja greške ostaje ista kao glavna crvena. Rubove (`outline`) namjerno ne diramo, to je XP-4. Omjere nismo prihvatili na riječ: nakon commita sa shemama CI je zelen, svih 183 unit testa prolazi, uključujući `PayPulseColorSchemeTest` 6/6. `HighContrastThemeTest` je na emulatoru pokrenula Lucija: 3/3 zeleno |
| 13:54 | Par 1 (vozio Jan) | XP-1 | 3 | Claude: kako vidjeti visoki kontrast prije nego Par 2 spoji prekidač u aplikaciji | Preview `TransactionsScreen` u visokom kontrastu, svijetli i tamni, u `TransactionsPreviews.kt` | Prihvaćeno. Prevođenje je potvrdio CI (zeleno), a izgled je u Android Studiju provjerila Lucija i napravila screenshot za prezentaciju |
| 13:56 | Par 2 (vozila Marija) | XP-2 | 3 | Marija: upisati upit | `AccessibilitySettingsScreenTest` (4 testa, emulator): naslov ekrana se prikazuje, dodir na redak Veći tekst i na redak Visoki kontrast mijenja postavku, a svaki redak je jedan čvor s ulogom Switch, pa ga TalkBack čita kao jedan prekidač | Test smo commitali u 13:56, prije koda ekrana, pa nije mogao proći (crveno) |
| 13:57 | Par 2 (Ennio) | XP-6 | 3 | Claude Code: Ana pita smije li XP-6 svesti na redoslijed, jer se redak transakcije već čita kao jedna rečenica | Provjerio `TransactionRow.kt`: kriteriji 2 i 3 već su ispunjeni, a kriterij 1 nije, jer je kartica prva, a status zadnji. Predložio da test pokrije sva tri kriterija, jer za `TransactionRow` nije bilo nijednog testa, i da se smanjenje bodova upiše u tablicu Brzina | Prijedlog smo poslali Ani. Ona je napisala 3 testa, od kojih je crven bio samo test redoslijeda (vidi 14:04) |
| 14:00 | Par 2 (vozila Marija) | XP-2 | 3 | Marija: upisati upit | Ekran Pristupačnost: naslov označen kao heading, dva retka s `toggleable(role = Role.Switch)` i `Switch(onCheckedChange = null)` te kartica Pregled. Navigacija: `ShellRoute.Accessibility`, `DrawerItem.ACCESSIBILITY` s novom ikonom i string `navigation_accessibility`. `MainActivity` čita postavke iz repozitorija i predaje ih u `PayPulseTheme` | Commitali smo u 14:00. Postojeći Settings (PULSE-80) nismo dirali. Rezultat `AccessibilitySettingsScreenTest` na emulatoru: upisati |
| 14:09 | Par 1 (vozila Lucija) | XP-4 | 3 | Claude Code: napravi granu `xp-radionica/kontrast-rubova` iz `xp-radionica/dev` i test prihvatljivosti za XP-4 iz kriterija u `docs/xp-radionica.md` | Prvo je u kodu provjerio koje boje crtaju rubove: `outline` (OutlinedTextField), `onSurfaceVariant` (indikator TextFielda) i `outlineVariant` (razdjelnici i rubovi kartica). Zatim je napisao `BorderContrastTest` (4 testa): obične teme barem 3:1 za rubove polja, a visoki kontrast barem 4,5:1 za rubove i razdjelnike | Test smo pokrenuli prije koda: 2 od 4 pada, `outlineVariant` je 1,70:1 u svijetlom i 2,25:1 u tamnom visokom kontrastu. Obične teme već prolaze, a test ih zaključava. XP-4 je planiran na razini 2, a test je napisao AI, pa smo razinu označili kao 3 |
| 14:10 | Par 1 (vozio Jan) | XP-4 | 3 | Claude: napiši test prihvatljivosti za XP-4 iz kriterija u `docs/xp-radionica.md` | `NonTextContrastTest` (4 testa), skoro isti kao Lucijin `BorderContrastTest` | Jan ga je pushao minutu nakon Lucije, a da nije znao da ona radi isto. Duplikat smo uočili pri pregledu grane, vidi Gdje je AI pogriješio |
| 14:13 | Par 1 (vozio Jan) | XP-4 | 3 | Claude: koje boje rubova i razdjelnika u visokom kontrastu prolaze barem 4,5:1, a da ostanu u stilu postojećih sivih | `outline` Grey900 i `outlineVariant` Grey700 u svijetloj shemi, `outline` Grey100 i nova `Grey400` u tamnoj. Prema AI-ju razdjelnici imaju 6,39:1 i 7,84:1 | Prihvaćeno tek kad je CI bio zelen: 187 testova, 0 palo. Prije toga je bio crven na točno 2 testa za razdjelnike u visokom kontrastu (1,70:1 i 2,25:1). Obične teme nismo dirali |
| 14:16 | Par 1 (vozio Jan) | XP-4 | 3 | Refaktor: dva testa provjeravaju isto | Claude je usporedio oba testa: Lucijin `BorderContrastTest` je širi, jer provjerava i `onSurfaceVariant` (indikator polja i ikone) | Obrisali smo `NonTextContrastTest` i zadržali `BorderContrastTest`. Testovi i dalje prolaze |
| 14:04 | Par 3 (Ana) | XP-6 | 4 | Dali agentu 3 kriterija; korak 1 analiza, korak 2 samo testovi | TransactionRowScreenReaderTest, 3 testa; sam predvidio 1 crven i 2 zelena | Pokrenuli: točno 1 crven (redoslijed), 2 zelena. Commit RED |
| 14:06 | Par 3 (Ana) | XP-6 | 4 | Korak 3 i 4: najmanja promjena + objašnjenje | Jedna linija u TransactionRow.kt (redoslijed listOfNotNull) + 4 rizika | 3/3 zeleno, pregledali diff, push |
| 14:16 | Par 2 (Ennio) | XP-2 | 3 | Claude Code: je li grana `xp-radionica/postavke-pristupacnosti` spremna za merge prema uputama i definiciji gotovog | Kod prati upute točku po točku, grana nema konflikata s `dev` u kojem je XP-1, svih 182 unit testa prolazi lokalno. Upozorio da fale zapisi Para 2 u AI_LOG-u, tablica Brzina i ručna provjera kriterija 2 i 3 na emulatoru, jer CI ne pokreće `androidTest` | PR #41 spojen je u `xp-radionica/dev` u 14:22. Zapise Para 2 upisali smo naknadno. Ručna provjera kriterija 2 i 3 na emulatoru: upisati rezultat |
| 14:25 | Par 2 (Ennio) | Merge | 3 | Claude Code: kako riješiti konflikt u PR-u #42 (`xp-radionica/kontrast-rubova`) | Spojio je granu s `dev` samo u memoriji, bez diranja grana: konflikt je samo u `AI_LOG.md`, jer su obje grane dodale redove na kraj tablice Zapisi, a kod se spaja bez konflikta. Predložio da se zadrže oba bloka | Konflikt smo riješili tako da smo zadržali redove svih parova, prema pravilu iz 13:33 |

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
| 13:35 | Par 2 | Upute za Par 2 navodile su `DataStoreAccessibilitySettingsRepositoryTest` i `AccessibilitySettingsScreenTest` kao da već postoje u kosturu | Claude Code je pretražio repozitorij i tih testova nije bilo | Testove smo napisali sami, prije koda |
| 13:51 | Par 2 | Claude Code je `AccessibilitySettingsViewModelTest` i ViewModel napisao u istom koraku | Claude je to sam napisao u odgovoru: test nijednom nije pao zbog koda koji nedostaje | Test smo zadržali jer pokriva kriterije. Za XP-5 tražimo da AI prvo napiše test i pokaže da pada |
| 14:10 | Par 1 | Claude je Janu dao gotov test za XP-4, a nije provjerio radi li netko već na istoj grani. Ranije je čuo da Lucija trenutno ne radi pa je pretpostavio da je tako i dalje | Dva gotovo ista testa na istoj grani u razmaku od minute, uočeno pri pregledu povijesti grane | Zadržali smo širi test, a duplikat obrisali u refaktor commitu. Dogovor: prije nego AI krene na priču, par javi tko vozi |

Ograničenje koje smo znali unaprijed: Claude je kostur i testove pisao bez mogućnosti da ih prevede, jer u njegovom okruženju nema Android SDK-a. Zato je Lucija prije prvog commita napravila build i pokrenula sve postojeće testove (169/169).

## Priča na razini 4: XP-6

1. Kriteriji koje smo dali agentu: redoslijed status → iznos → kartica → datum i vrijeme; "Otvori detalje" pri dvostrukom dodiru; kartica samo maskirana.
2. Testovi koje je agent napisao prvi, i jesu li bili crveni prije koda: 3 testa; crven samo test redoslijeda, druga dva zelena jer je to već postojalo. Test namjerno šalje cijeli broj kartice i provjerava da se čuju samo zadnje 4 znamenke.
3. Kod koji je agent napisao: jedna linija u TransactionRow.kt, promijenjen redoslijed u listOfNotNull.
4. Naša analiza: provjerili da se vizualno ništa ne mijenja (samo semantika); rizik: redak na Nadzornoj ploči (LatestTransactionRow) i dalje čita karticu prvu, to je kandidat za sljedeći korak.
5. Objašnjenje koda vlastitim riječima (Ana): TalkBack čita opis retka kao jednu rečenicu složenu od liste dijelova. Redoslijed u listi je redoslijed čitanja, pa sam status i iznos premjestila naprijed jer korisnik prvo želi znati je li plaćanje prošlo.

## Zaključak tima

- Gdje nam je AI najviše pomogao: savjetovanje tokom planiranja, arhitekturalni prijedlozi
- Gdje nam je najviše odmogao: preambiciozna, prekompleksna implementacija
- Što bismo sljedeći put napravili drugačije: više pripreme oko teoretskih temelja kako bismo lakše i brže izveli planiranje samog sustava

# XP^AI radionica: pristupačnost u PayPulse Android aplikaciji

Tim Srećkovici, 25. 11. 2026.

## Funkcionalnost i vrijednost

Iz kataloga smo odabrali 7, Pristupačnost i tamna tema.

PayPulse koriste trgovci i njihovo osoblje, često na blagajni ili na terenu: na jakom suncu, navečer i na manjim mobitelima. Dio osoblja slabije vidi, a dio koristi TalkBack. Zato dodajemo tri stvari:

- visoki kontrast
- veći tekst
- opise za čitač zaslona

Ništa od toga nije u prijavi projekta ni u Jira planu.

Tamna tema koja prati postavke sustava već postoji (`isSystemInDarkTheme()` u `PayPulseTheme`). Ručni odabir teme i jezika planiran je u PULSE-77 i u ovoj radionici ga ne diramo. Visoki kontrast zato radimo u svijetloj i u tamnoj varijanti, a koja se koristi ovisi o temi sustava.

## Parovi i grane

Sve grane izlaze iz `xp-radionica/dev` i u nju se vraćaju kad testovi prolaze.

| Par | Članovi | Iteracija 1 | Iteracija 2 |
|---|---|---|---|
| Par 1 | Jan, Lucija, AI agent | `xp-radionica/visoki-kontrast` (XP-1) | `xp-radionica/kontrast-rubova` (XP-4) |
| Par 2 | Ennio, Marija, AI agent | `xp-radionica/postavke-pristupacnosti` (XP-2) | `xp-radionica/veci-tekst` (XP-5) |
| Par 3 | Ana, AI agent | `xp-radionica/opisi-za-citac-zaslona` (XP-3) | `xp-radionica/redak-transakcije-citac` (XP-6) |

Uloge vozač i navigator mijenjamo svakih 15 minuta. AI agent je treći član para.

## Plan izdanja: dvije iteracije

| Vrijeme | Što |
|---|---|
| 0 do 15 | Planiranje: priče, procjene, kostur na `xp-radionica/dev` |
| 15 do 45 | Iteracija 1: XP-1, XP-2, XP-3, tag `iteracija-1` |
| 45 do 50 | Mjerenje brzine i prilagodba plana |
| 50 do 80 | Iteracija 2: XP-4, XP-5, XP-6, tag `iteracija-2` |
| 80 do 90 | Završni merge, testovi, tag `v1.0` |

Odabrali smo dvije iteracije umjesto tri iz dva razloga. Android build i testovi na emulatoru traju po nekoliko minuta. Prije svakog taga treba spojiti tri grane.

## Korisničke priče

Procjene su u bodovima 1, 2, 3 i 5.

### XP-1 Visokokontrastne boje (3 boda, Par 1, iteracija 1, AI razina 3)

Kao slabovidni korisnik želim visokokontrastne boje, kako bih iznose i statuse mogao pročitati i na jakom suncu.

1. Zadano: uključen Visoki kontrast i svijetla tema sustava. Kada: otvorim bilo koji ekran. Tada: svaki tekst ima kontrast prema svojoj pozadini barem 7:1.
2. Zadano: uključen Visoki kontrast i tamna tema sustava. Kada: pokrenem aplikaciju. Tada: koristi se tamna visokokontrastna tema i kontrast teksta je barem 7:1.
3. Zadano: Visoki kontrast je isključen. Kada: koristim svijetlu ili tamnu temu sustava. Tada: boje su kao prije i kontrast teksta je barem 4,5:1.

Testovi: `ContrastRatioTest`, `PayPulseColorSchemeTest`, `HighContrastThemeTest`

### XP-2 Ekran Pristupačnost s postavkama koje se pamte (3 boda, Par 2, iteracija 1, AI razina 3)

Kao korisnik želim na jednom mjestu uključiti Veći tekst i Visoki kontrast, i da ih aplikacija zapamti.

1. Zadano: prijavljen sam. Kada: u izborniku odaberem Pristupačnost. Tada: vidim isključene prekidače Veći tekst i Visoki kontrast te karticu Pregled.
2. Zadano: nalazim se na ekranu Pristupačnost. Kada: uključim Visoki kontrast. Tada: cijela aplikacija odmah mijenja boje, bez ponovnog pokretanja.
3. Zadano: uključio sam obje postavke. Kada: zatvorim i ponovno otvorim aplikaciju. Tada: obje su i dalje uključene.

Testovi: `DataStoreAccessibilitySettingsRepositoryTest`, `AccessibilitySettingsScreenTest`, `AccessibilitySettingsViewModelTest`

### XP-3 Opisani gumbi i ikone za čitač zaslona (3 boda, Par 3, iteracija 1, AI razina 3)

Kao korisnik čitača zaslona želim da su gumbi i ikone opisani, kako bih znao što radi svaki element.

1. Zadano: uključen TalkBack. Kada: prođem kroz ekrane Nadzorna ploča, Trgovci i Transakcije. Tada: svaki element koji se može dodirnuti izgovara svoj naziv.
2. Zadano: ekran ima ikone. Kada: čitač dođe do njih. Tada: ikone koje nose značenje (status, greška, upozorenje) imaju opis, a ukrasne ikone čitač preskače.
3. Zadano: otvoren je bilo koji glavni ekran. Kada: skačem po naslovima. Tada: naslovi ekrana i odjeljaka označeni su kao naslovi.

### XP-4 Vidljivi rubovi i ikone (2 boda, Par 1, iteracija 2, AI razina 2)

Kao slabovidni korisnik želim jasno vidjeti rubove polja i ikone, a ne samo tekst.

1. Zadano: obična svijetla ili tamna tema. Kada: otvorim ekran s poljem za unos. Tada: rub polja ima kontrast prema pozadini barem 3:1.
2. Zadano: uključen Visoki kontrast. Kada: otvorim isti ekran. Tada: rubovi polja i razdjelnici imaju kontrast barem 4,5:1.

### XP-5 Veći tekst (3 boda, Par 2, iteracija 2, AI razina 3)

Kao slabovidni korisnik želim veći tekst u aplikaciji, bez mijenjanja veličine fonta na cijelom mobitelu.

1. Zadano: veličina fonta sustava 100 %. Kada: uključim Veći tekst. Tada: sav tekst je 30 % veći.
2. Zadano: veličina fonta sustava 150 %. Kada: uključim Veći tekst. Tada: tekst je 195 %.
3. Zadano: veličina fonta sustava 200 %. Kada: uključim Veći tekst. Tada: tekst ostaje 200 %, da se ekrani ne raspadnu.

Testovi: `LargerTextTest`, `FontScaleTest`

### XP-6 Redak transakcije kao jedna rečenica (3 boda, Par 3, iteracija 2, AI razina 4)

Kao korisnik čitača zaslona želim da se redak transakcije pročita kao jedna rečenica, kako bih brzo prošao kroz popis.

1. Zadano: uključen TalkBack i otvoren popis transakcija. Kada: fokusiram redak. Tada: čujem jednu poruku sa statusom, iznosom, karticom te datumom i vremenom.
2. Zadano: redak je fokusiran. Kada: dvaput dodirnem. Tada: otvaraju se detalji transakcije, a čitač prije toga izgovara da se otvaraju detalji.
3. Zadano: redak s karticom. Kada: čitač ga pročita. Tada: broj kartice izgovara se samo maskiran, kao na ekranu.

### XP-7 Opis grafova na nadzornoj ploči (2 boda, rezerva)

Kao korisnik čitača zaslona želim čuti sažetak grafa, kako bih znao promet bez gledanja stupaca.

1. Zadano: nadzorna ploča s podacima. Kada: fokusiram graf. Tada: čujem naziv grafa i iznose za svaku karticu.
2. Zadano: nema podataka. Kada: fokusiram graf. Tada: čujem da nema podataka za odabrano razdoblje.

## Brzina

| Iteracija | Planirano | Ostvareno | Napomena |
|---|---|---|---|
| 1 | 9 (XP-1, XP-2, XP-3) | | |
| 2 | 8 (XP-4, XP-5, XP-6) + 2 rezerva | | |

Pravilo: ako je brzina u iteraciji 1 manja od 9, iteracija 2 dobiva najviše toliko bodova. Prvi ispada XP-4.

## Definicija gotovog za radionicu

- testovi prihvatljivosti priče prolaze
- svi unit testovi prolaze lokalno i na GitHub Actions
- grana je spojena u `xp-radionica/dev`
- ključni AI upiti i odluke upisani su u `AI_LOG.md`

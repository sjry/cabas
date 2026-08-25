# Notes pour les agents

Complément au [README](README.md), qui reste la référence sur le **format des recettes** et
l'usage de l'application. Ce fichier-ci décrit comment travailler dans le dépôt : commandes,
conventions, pièges d'environnement, et façon de vérifier une modification d'interface.

Le dépôt est **public** : ni chemin absolu, ni nom d'appareil, ni secret dans un fichier
versionné — documentation comprise. Ce qui dépend du poste se décrit comme une condition
(« si `/tmp` est monté `noexec`… »), pas comme un fait.

## Tenir README.md et AGENTS.md à jour

**C'est une partie du travail, pas une étape optionnelle.** Avant de conclure une tâche,
relire ces deux fichiers et corriger ce que la modification vient de rendre faux ou
incomplet. Une documentation qui décrit un état révolu coûte plus cher que pas de
documentation du tout : l'agent suivant lui fait confiance.

- **[README.md](README.md)** — ce que voit l'utilisateur : format des recettes, comportement
  des écrans, commandes de compilation, prérequis machine.
- **AGENTS.md** (ce fichier) — ce que doit savoir un agent : conventions, règles de domaine,
  procédures de vérification, pièges rencontrés. Un piège qui a coûté du temps se note ici,
  avec sa cause, pour ne pas être payé deux fois.

Ces mises à jour font partie du même commit que la modification qui les motive.

## L'application en une phrase

Cabas est une application Android (Kotlin, Compose, Room) qui parse des recettes markdown
écrites **pour 1 personne**, laisse composer un menu avec un nombre de convives par recette,
et en déduit une liste de courses.

## Carte du dépôt

```
app/src/main/java/com/sjarry/cabas/
  parser/    RecipeParser, IngredientUnit, IngredientCategory, QuantityFormatter
             — Kotlin pur, sans Android, testé
  data/      Room (AppDatabase, entities/, dao/), RecipeRepository, MenuRepository,
             ShoppingListBuilder, RecipeSearch et MenuDraw (logique pure),
             SettingsStore (DataStore)
  ui/        Navigation.kt + un dossier par écran (recipes, detail, menu, shopping),
             common/ pour les composables et le formatage partagés
app/src/test/            tests JUnit JVM (parseur, formatage, liste de courses, recherche,
                         tirage au sort)
app/schemas/             schémas Room exportés (3.json) — versionnés, à committer
exemples/                quatre recettes markdown d'exemple
```

## Commandes

```bash
export ANDROID_HOME="$HOME/Android/Sdk"   # si local.properties est absent
export JAVA_HOME=/chemin/vers/jdk-21      # si le java du PATH est antérieur à 17
./gradlew test            # tests unitaires JVM
./gradlew assembleDebug   # APK debug
./gradlew installDebug    # installe sur l'appareil connecté
```

**Ces deux variables ne sont pas décoratives**, et les deux pannes qu'elles évitent
arrivent dès la configuration, avant le moindre compilateur :

- `local.properties` n'étant pas versionné, il peut manquer sur un dépôt fraîchement cloné :
  Gradle échoue alors sur `SDK location not found`, y compris pour `./gradlew test`.
  Exporter `ANDROID_HOME` suffit, sans créer le fichier.
- Le plugin Android exige un JDK 17+ et refuse de se charger sinon, avec
  `Android Gradle plugin requires Java 17 to run`. Beaucoup de distributions ont encore un
  JDK plus ancien dans le PATH ; Android Studio embarque un JBR utilisable
  (`<install-android-studio>/jbr`), sinon n'importe quel JDK 21.

Vérifier avant de conclure à un bug : `java -version` et `echo $ANDROID_HOME`.

## Ce que les tests couvrent — et ne couvrent pas

70 tests JUnit, tous en JVM pure : `RecipeParserTest`, `QuantityFormatterTest`,
`ShoppingListBuilderTest`, `IngredientCategoryTest`, `RecipeSearchTest`, `MenuDrawTest`.
**Aucun test instrumenté**
(`app/src/androidTest` n'existe pas, la seule dépendance de test est `junit`).
`./gradlew connectedAndroidTest` n'a donc rien à exécuter : toute modification
d'interface se vérifie à la main sur l'émulateur.

Toute logique nouvelle qui peut vivre dans `parser/` ou dans `ShoppingListBuilder` doit y
vivre, précisément pour rester testable sans appareil.

## Conventions

- **Tout est en français** : chaînes de l'interface, KDoc, commentaires, noms de tests.
  Le code (identifiants, types) reste en anglais.
- Les commentaires expliquent le **pourquoi**, pas le quoi. Voir `switchToTab` dans
  [Navigation.kt](app/src/main/java/com/sjarry/cabas/ui/Navigation.kt) ou la note sur
  `kotlin.daemon.jvmargs` dans `gradle.properties` : ce sont les modèles à suivre.
- Pas de framework d'injection. Les ViewModels sont construits dans
  [AppViewModelProvider](app/src/main/java/com/sjarry/cabas/ui/AppViewModelProvider.kt),
  les dépendances viennent de `AppContainer` (`CabasApplication`).
- Chaque écran expose un `StateFlow` d'état via `stateIn(..., WhileSubscribed(5_000), ...)`
  et se collecte avec `collectAsStateWithLifecycle()`.
- Material 3, thème dans `ui/theme/`. Les composables partagés (`EmptyState`…) sont dans
  `ui/common/Components.kt`, les libellés (`quantityLabel`, `servingsLabel`) dans
  `ui/common/Formatting.kt` — s'en servir plutôt que de reformater sur place.

## Règles du domaine à ne pas casser

- **Les quantités sont stockées pour 1 personne**, en unité canonique (grammes,
  millilitres). La multiplication par le nombre de convives se fait **à l'affichage** :
  dans `ShoppingListBuilder` pour la liste de courses, dans `RecipeDetailScreen` pour le
  détail d'une recette. Ne jamais écrire une quantité mise à l'échelle en base.
- Un ingrédient sans quantité (`unspecified`) n'est jamais multiplié : il s'affiche `qs`.
- Les cases cochées de la liste de courses sont liées à une clé stable
  (`ShoppingListBuilder.keyOf`, nom normalisé + unité) pour survivre au recalcul.
  Cette clé étant partagée, cocher « sel » le coche dans **toutes** les recettes qui en
  demandent : c'est voulu, on ne l'achète qu'une fois.
- **Le rayon d'un article se déduit de son nom, jamais de la recette** : le sel est au
  même endroit quel que soit le plat. Le lexique est dans
  [IngredientCategory.kt](app/src/main/java/com/sjarry/cabas/parser/IngredientCategory.kt),
  où **le plus long mot-clé l'emporte** — c'est ce qui met « lait de coco » en épicerie
  et « lait » en crémerie. Un ingrédient annoncé surgelé court-circuite le lexique.
  Une correction de l'utilisateur (table `ingredient_categories`, clé = nom normalisé)
  l'emporte sur le lexique ; elle n'est jamais purgée avec le menu, contrairement aux
  cases cochées : elle décrit le magasin, pas les courses en cours.
- **Le rayon n'entre pas dans `keyOf`.** Corriger un rayon ne doit ni décocher l'article
  ni le dédoubler ; `ShoppingListBuilderTest` garde un test là-dessus.
- **Les vues « Rayon » et « A→Z » n'affichent que `ShoppingList.remaining`** ; les articles
  cochés partent dans `taken`, section « Pris (n) » repliable en bas de liste — dépliée par
  défaut, état dans le ViewModel, volontairement non persisté comme le choix de vue (qui
  s'ouvre sur « Rayon »). **La vue « Recette » fait exception** : elle rend `section.items`
  en entier, articles pris compris, barrés à leur place, sans section « Pris » (ils y
  figureraient deux fois) ni écran de fin quand tout est coché. Elle répond à « que demande
  ce plat ? », pas à « que reste-t-il à prendre ? ».
  `isComplete` exige une liste
  **non vide** : un menu vide n'est pas une liste terminée, il n'a rien à acheter — les
  deux cas ont chacun leur écran.
- Les trois onglets sont frères : tout passage de l'un à l'autre passe par
  `NavHostController.switchToTab`, jamais par un `navigate` direct. Les autres destinations
  (`recipe/{id}`, `menu/picker`) s'empilent par un `navigate` normal, et la barre du bas
  s'efface d'elle-même : `CabasApp` ne l'affiche que si la route courante est un onglet.
- **Le tri alphabétique des recettes se fait en Kotlin, au `Collator` français à
  `strength = PRIMARY`**, jamais en SQL. En SQLite, `COLLATE NOCASE` ne couvre que l'ASCII :
  « Éclair » se retrouverait après « Zucchini ». `RecipeSearch` et `ShoppingListBuilder`
  trient tous deux ainsi ; `RecipeDao.observeCandidates` ne porte donc pas d'`ORDER BY`.
- La recherche du sélecteur de menu porte sur le titre **et** les ingrédients, normalisés par
  `ShoppingListBuilder.normalizeName` — un seul normaliseur dans l'application, pas deux.
  Les noms d'ingrédients arrivent empaquetés par `GROUP_CONCAT(..., char(31))` : ce séparateur
  et `RecipeSearch.SEPARATOR` doivent rester identiques.
- **Le tirage au sort du menu ne propose jamais une recette déjà au menu**, et son `Random`
  est un paramètre de `MenuDraw.draw` : c'est ce qui rend un tirage rejouable dans un test,
  et c'est pourquoi l'exclusion se fait en Kotlin plutôt que dans la requête. Le dialogue
  plafonne le compteur au nombre de recettes hors menu (`MenuUiState.availableCount`) : un
  tirage rend donc toujours le nombre demandé, et il n'y a rien à expliquer après coup.
  `MenuRepository.addRandomRecipes` relit la base au moment du tirage plutôt que de croire
  l'état de l'écran, et réutilise `addRecipes`.
- **Le nombre de convives du dialogue de tirage vaut pour toutes les recettes tirées** : un
  tirage compose les repas d'une même table, il n'y a pas de convives par recette à ce
  moment-là. Il part de `MenuRepository.DEFAULT_SERVINGS` (comme le sélecteur, qui n'en
  propose pas le choix) et reste ajustable ensuite sur chaque carte.
- **Remplacer une recette du menu (`swapRecipe`) conserve le créneau** : mêmes `servings`, même
  `addedAt`, donc la carte se réécrit sur place au lieu de sauter en bas de liste — c'est ce qui
  rend l'action lisible sans confirmation. `done` repart à `false` : il décrivait l'autre plat.
  Les deux écritures passent par `db.withTransaction`, sinon le menu émet un état intermédiaire
  à une recette de moins et la carte clignote. La purge des cases cochées reste **hors**
  transaction, elle relit le menu par son `Flow`. Le dé d'une carte est désactivé quand
  `availableCount == 0` : sans confirmation, un appui sans effet serait indéchiffrable.
- Le détail d'une recette prend un argument de navigation optionnel `servings`
  (`recipe/{recipeId}?servings=N`, 1 par défaut). Ouvert depuis le **Menu**, il reçoit les
  convives de la carte ; ouvert depuis **Recettes**, il reste à 1 personne.

## Pièges d'interface rencontrés

- **`importedAt` est un horodatage de synchronisation, pas de création** : `RecipeRepository.upsert`
  le réécrit à chaque re-synchro du dossier. Aucune section « recettes récentes » ne peut s'y fier —
  après un *Re-synchroniser*, toutes les recettes sont « récentes ».
- **Pas de `Scaffold` dans un `Dialog`.** Un sélecteur plein écran a d'abord été tenté en
  `Dialog(usePlatformDefaultWidth = false)` : le `Scaffold` imbriqué mesure sa `bottomBar` à la
  hauteur des seuls encarts système et laisse les boutons déborder *sous* le bord de l'écran, avec
  ou sans `decorFitsSystemWindows` et `safeDrawingPadding`. Un écran de navigation à part entière
  (`menu/picker`) règle le problème et donne le geste de retour en prime. Si un jour un plein écran
  modal est vraiment nécessaire, poser une `Column` avec un `weight(1f)` sur la liste — jamais un
  `Scaffold`.
- **Une barre basse sous un champ de saisie ne cumule pas `padding(innerPadding)` et
  `imePadding()`** : clavier ouvert, on obtient la hauteur de la barre de navigation en vide
  inutile. Prendre le haut du `Scaffold` (`padding.calculateTopPadding()`) et, en bas,
  `windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))`, qui vaut déjà le
  plus grand des deux.
- Le Gboard d'un émulateur s'ouvre souvent en **clavier flottant**, qui ne recouvre rien :
  on ne peut pas y vérifier un encart de clavier. `pm clear
  com.google.android.inputmethod.latin` le remet ancré, mais fait réapparaître l'écran
  « Try out your stylus » qu'il faut fermer (*Cancel*) avant de retrouver le champ.

## Modifier la base

`AppDatabase` est en `version = 3`, `exportSchema = true`. Un changement de schéma impose
de monter la version, d'ajouter une `Migration` (voir `MIGRATION_1_2`) et de committer le
nouveau JSON de `app/schemas/`. `fallbackToDestructiveMigration()` est actif : une migration
oubliée efface silencieusement les données de l'utilisateur au lieu de planter.

## L'icône de l'application

Un cabas de courses rempli, entièrement vectoriel : **aucun PNG**, `minSdk = 26` garantit que
`mipmap-anydpi-v26/` est toujours la source. Trois fichiers, plus la couleur de fond :

```
drawable/ic_launcher_foreground.xml    le cabas, l'anse et les trois produits
drawable/ic_launcher_monochrome.xml    la même silhouette d'un seul ton (Android 13+)
mipmap-anydpi-v26/ic_launcher.xml      l'assemblage adaptatif
values/ic_launcher_background.xml      #2E7D5B, le vert de l'application
```

**Les contraintes de tracé ne sont pas négociables** — c'est ce qui décide si l'icône est
lisible sur un téléphone :

- Le canevas fait 108 dp mais le lanceur n'affiche que les **72 dp centraux**, et peut rogner
  jusqu'au **cercle de 66 dp**. Tout doit donc tenir dans un rayon de 33 dp autour de
  (54,54) — beaucoup de lanceurs, dont celui des AVD Pixel, utilisent le masque circulaire.
- Aucun trait sous **5 dp**, aucune forme sous **12 dp** : en dessous, la réduction à 28 px
  transforme le détail en bouillie. Un aperçu sur le canevas entier trompe complètement sur
  ce point, il faut regarder l'icône déjà rognée.
- **L'ordre des tracés est le dessin** : l'anse d'abord (elle passe derrière), puis les
  produits, puis le corps du cabas qui recouvre leur base. C'est ce qui donne l'impression
  qu'ils débordent du rebord. Les extrémités de l'anse retombent *sous* le rebord pour
  disparaître derrière le corps au lieu de faire deux bosses.
- La couche monochrome n'est pas la couche avant recoloriée : tout y étant d'un seul ton,
  deux formes qui se touchent n'en font plus qu'une. Elle a donc sa propre géométrie, avec
  des **vides explicites** (≈ 3,5 dp sous les produits, 4,5 dp sous l'anse) là où la couleur
  suffisait à séparer. Sans eux, contenu et cabas fusionnent en un couvercle festonné.

Pièges rencontrés :

- Le manifeste déclare `android:roundIcon="@mipmap/ic_launcher"`, **pas** `ic_launcher_round`.
  Le fichier `mipmap-anydpi-v26/ic_launcher_round.xml` n'est donc jamais lu : le garder
  synchronisé à la main, ou le supprimer, mais ne pas croire qu'on le modifie utilement.
- Les **icônes thématisées** ne s'activent pas sur un AVD Pixel : écrire `themed_icons` dans
  les préférences du lanceur (même avec `adb root` et le cache `app_icons.db` vidé) ne change
  rien, les icônes Google restent en couleur elles aussi. La couche monochrome se vérifie
  donc par rendu, pas sur l'appareil.
- Pour prévisualiser un tracé sans compiler, un navigateur en mode headless
  (`chromium --headless --screenshot`) sur un SVG reprenant les mêmes chemins fait très bien
  l'affaire. Attention si le navigateur est installé en **snap** ou en **flatpak** : il ne
  voit ni `/tmp` (qui lui est privé) ni les dossiers cachés du `$HOME`, et la capture échoue
  sur `Permission denied`. Travailler alors dans son propre dossier de données
  (`~/snap/<navigateur>/common/` par exemple).

## Vérifier une modification sur l'émulateur

L'interface se vérifie sur un AVD ; celui de référence est un Pixel 6 / API 35 / Google APIs.

```bash
export ANDROID_HOME="$HOME/Android/Sdk"             # = sdk.dir de local.properties
$ANDROID_HOME/emulator/emulator -list-avds          # récupérer le nom de l'AVD
$ANDROID_HOME/emulator/emulator -avd <avd> -no-snapshot-save -no-boot-anim &
$ANDROID_HOME/platform-tools/adb wait-for-device
$ANDROID_HOME/platform-tools/adb shell getprop sys.boot_completed   # attendre « 1 » (~20 s)
./gradlew installDebug
adb shell monkey -p com.sjarry.cabas -c android.intent.category.LAUNCHER 1
```

**Peupler l'application sans passer par l'interface.** L'import réel ouvre un sélecteur de
fichiers (SAF) pénible à piloter en `adb shell input`. Il est bien plus simple d'écrire
directement dans la base — l'APK debug est `debuggable`, donc `run-as` fonctionne :

```bash
adb push seed.sql /data/local/tmp/seed.sql && adb shell chmod 666 /data/local/tmp/seed.sql
adb shell am force-stop com.sjarry.cabas   # sinon Room réécrit par-dessus depuis son cache
adb shell 'run-as com.sjarry.cabas sqlite3 databases/cabas.db < /data/local/tmp/seed.sql'
```

Pour éprouver un écran de liste à l'échelle visée — le sélecteur de recettes ne devient
intéressant qu'à partir d'une centaine d'entrées — une CTE récursive suffit à en fabriquer autant :

```sql
INSERT INTO recipes (title, sourceUri, sourceFileName, rawMarkdown, importedAt)
WITH RECURSIVE n(i) AS (SELECT 1 UNION ALL SELECT i + 1 FROM n WHERE i < 150)
SELECT 'Recette ' || char(65 + (i % 26)) || ' n' || i, NULL, NULL, '# seed', 0 FROM n;
```

Y ajouter à la main un titre accentué (`Éclair au café`) et un titre non alphabétique
(`3 chocolats`) : ce sont eux qui révèlent les erreurs de tri et de regroupement.

Le passage par un fichier poussé évite les cauchemars de quoting : `adb shell run-as ...
"INSERT ..."` fait interpréter les parenthèses et les points-virgules par le shell du
téléphone. Attention aussi : le client `sqlite3` n'active pas `PRAGMA foreign_keys`, donc
un `DELETE FROM recipes` laisse des `ingredients` orphelins qui viendront se rattacher au
prochain `recipeId` réutilisé.

> **Avant de semer des données, prévenir ou repartir propre.** Écrire dans `cabas.db`
> détruit les recettes déjà présentes sur l'émulateur. `adb shell pm clear com.sjarry.cabas`
> remet l'application à zéro de façon explicite ; c'est préférable à un `DELETE` partiel.
> Attention à l'ordre après un `pm clear` : le fichier `cabas.db` n'existe plus, il faut
> **lancer l'application une fois** (Room le recrée) puis la `force-stop` avant de semer,
> sinon `sqlite3` écrit dans une base sans tables.

**Piloter et regarder l'écran.** `adb shell input tap X Y` en coordonnées appareil
(1080 × 2400 sur un Pixel 6 ; `adb shell wm size` pour un autre). Une capture brute dépasse
la limite de 2000 px de l'outil de lecture d'images : la réduire d'abord, par exemple avec
Pillow.

```python
png = subprocess.run([adb, "exec-out", "screencap", "-p"], capture_output=True).stdout
im = Image.open(io.BytesIO(png)); im.thumbnail((820, 1820)); im.save(out)
```

Pour convertir une coordonnée lue sur la capture réduite vers l'appareil : multiplier par
`2400 / hauteur_de_la_capture` (≈ 1,32 avec les valeurs ci-dessus).

## Pièges liés au poste de travail

- `No native library found for os.name=Linux` à la compilation signifie que `/tmp` est monté
  `noexec` : Room vérifie ses requêtes via *sqlite-jdbc*, qui a besoin d'extraire une
  bibliothèque native dans `java.io.tmpdir`. Donner alors un répertoire temporaire exécutable
  au démon Kotlin (`kotlin.daemon.jvmargs` avec `-Dorg.sqlite.tmpdir=` et `-Djava.io.tmpdir=`)
  **dans `~/.gradle/gradle.properties`**, jamais dans celui du dépôt : le chemin ne vaut que
  pour ce poste.
- `local.properties` (chemin du SDK), `keystore.properties` et `*.jks` ne sont **pas**
  versionnés et ne doivent jamais l'être. Plus généralement, aucun chemin absolu de poste
  ne doit entrer dans un fichier versionné, dépôt public oblige.
- Sans `keystore.properties`, la build release compile mais sort non signée — c'est voulu.
- Un APK debug et un APK release ne cohabitent pas sur un même appareil : signatures
  différentes, il faut désinstaller l'un avant d'installer l'autre.

## Distribution (CI GitHub Actions)

`.github/workflows/release.yml` construit et publie l'APK signé dans une release GitHub à
chaque tag `v*` ; l'utilisateur l'installe en ouvrant
`https://github.com/sjry/cabas/releases/latest` sur son téléphone. Détail de la procédure
et des secrets dans le README.

- `versionCode`/`versionName` sont surchargeables par propriétés Gradle
  (`-PversionCode= -PversionName=`) et valent 1 / « 1.0 » par défaut. La CI passe le numéro
  de build en `versionCode` : croissant, donc mise à jour installable par-dessus.
- Les secrets de signature sont dans l'**environment `prod`**, pas dans les secrets du
  dépôt : le job doit déclarer `environment: prod`, sinon `secrets.KEYSTORE_BASE64` arrive
  vide et la build échoue sur « Secret KEYSTORE_BASE64 absent ».
- La CI reconstitue `keystore.properties` depuis les secrets, exactement comme en local.
  Elle **échoue volontairement** si `KEYSTORE_BASE64` manque : un APK release non signé ne
  s'installe pas, mieux vaut un message clair qu'un artefact inutilisable.
- `$ANDROID_HOME/build-tools/*/apksigner` ne se glob pas : plusieurs versions de build-tools
  cohabitent souvent, en local comme sur les runners GitHub, et le second chemin serait passé
  en argument (`Unsupported command`). Sélectionner explicitement le plus récent
  (`find … | sort -V | tail -1`).

## Git

Branche `main`, remote `origin` (`github.com:sjry/cabas`), historique très court. Ne pas
committer ni pousser sans demande explicite. Vérifier que `app/schemas/` suit bien quand le
schéma bouge.

`app/build/` est ignoré (ligne ajoutée au `.gitignore` en août 2026, avec réécriture de
l'historique pour en purger les 2 560 fichiers qui y avaient été committés). `git status`
doit donc rester propre après une compilation : s'il ne l'est pas, c'est qu'un chemin
généré échappe encore au `.gitignore` — le corriger plutôt que de committer le bruit.

Les anciens commits d'avant la réécriture restent visibles sur GitHub via la référence de
la pull request #1 ; sans importance ici (des `.dex`, aucun secret), mais à savoir avant de
conclure que le dépôt distant est purgé.

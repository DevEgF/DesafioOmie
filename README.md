# Desafio Omie

App Android nativo de gestão de produtos e vendas, construído em Kotlin + Jetpack Compose. Além do CRUD de produtos e do fluxo de vendas, o projeto foi usado como base para adicionar uma camada completa de observabilidade (Firebase Analytics, Crashlytics, Remote Config, Performance Monitoring) e uma tela de **Developer Mode** para diagnóstico em campo.

## Sumário

- [O que o app faz](#o-que-o-app-faz)
- [Arquitetura escolhida](#arquitetura-escolhida)
- [Decisões tomadas e por quê](#decisões-tomadas-e-por-quê)
- [Como rodar o app](#como-rodar-o-app)
- [Testes, lint e cobertura](#testes-lint-e-cobertura)
- [CI/CD](#cicd)

## O que o app faz

- **Produtos**: cadastro, edição, listagem e exclusão.
- **Vendas**: criação de uma venda com múltiplos itens (produto + quantidade), cálculo automático de total; listagem de vendas na tela inicial com valor total agregado; detalhe de uma venda específica.
- **Observabilidade** (transparente para o usuário final, mas visível para o time via Firebase Console):
  - Toda navegação de tela gera um evento `screen_view` automaticamente.
  - Ações de negócio relevantes geram eventos próprios: `product_created`, `product_updated`, `product_deleted`, `product_save_failed`, `sale_item_included`, `sale_created`, `sale_save_failed`.
  - Exceções tratadas nos repositórios (falhas de banco, etc.) são reportadas ao Crashlytics como não-fatais, além de crashes fatais reais.
  - A navegação para o detalhe de uma venda é controlada por uma flag remota (`sale_detail_enabled`), configurável pelo Firebase Remote Config.
- **Developer Mode**: tela acessível por um ícone de bug na Home, pensada para QA/depuração em campo sem precisar de Logcat conectado:
  - Contagem de crashes já registrados no aparelho, uso de memória (RAM) e uso de armazenamento do app, cada um com uma barra de progresso visual.
  - Toggle para forçar `sale_detail_enabled` ligado/desligado **localmente nesse aparelho**, com indicador mostrando se o valor em uso é o override local ou o valor vindo do Firebase, e botão para limpar o override e voltar a obedecer o remoto.

## Arquitetura escolhida

### Clean Architecture por feature, camadas `domain` → `data` → `presentation`

Cada feature de negócio (`products`, `sales`) é um conjunto de módulos Gradle independentes:

```
feature/products/
  domain/         — modelos, contratos de repositório, use cases (não depende de Android/Room/Firebase)
  data/           — implementação do repositório (Room), mappers Entity ↔ Domain
  presentation/   — ViewModels, Compose screens, contratos MVI (State/Action/Event)

feature/sales/
  domain/ data/ presentation/   (mesma estrutura)
```

`domain` não depende de nada além de Kotlin puro + `core:domain`. `data` depende de `domain` + `core:database`. `presentation` depende de `domain` + `core:presentation`/`core:designsystem`. Isso significa que a lógica de negócio (validações, cálculo de total, regras de "o que é uma venda válida") pode ser testada sem Android, Room ou qualquer framework — só JVM puro.

### `core:*` — o que é compartilhado entre features

```
core/domain/          — Result<D, E> (dois parâmetros de tipo: sucesso + erro tipado),
                         DataError (enum de erros de validação/dados), e as interfaces de
                         observabilidade: AnalyticsTracker, RemoteConfigProvider,
                         CrashCounter, DeviceMetricsProvider
core/database/        — Room: DAOs e entities compartilhadas entre features
core/designsystem/     — tema Material3, tipografia, componentes visuais reutilizáveis
core/presentation/     — utilitários de UI (UiText para mensagens de erro localizáveis,
                         ObserveAsEvents para coletar SharedFlow de eventos de forma
                         lifecycle-aware, formatação de moeda)
core/analytics/        — implementação concreta do Firebase por trás das interfaces
                         de core:domain (ver seção de decisões abaixo)
```

### `feature/devtools/presentation` — Developer Mode

Módulo à parte, só com `presentation` (sem `domain`/`data` próprios) porque a tela não tem lógica de negócio nova — ela só **lê** as interfaces de diagnóstico já expostas em `core:domain` (`CrashCounter`, `DeviceMetricsProvider`, `RemoteConfigProvider`).

### MVI nos ViewModels

Todo ViewModel segue o mesmo contrato: `State` (dados imutáveis expostos via `StateFlow`), `Action` (o que a UI dispara), `Event` (efeitos únicos como navegação, expostos via `SharedFlow` e coletados com `ObserveAsEvents`). Isso deixa cada `ViewModelTest` seguindo o mesmo formato Given-When-Then, e a tela (`*Screen.kt`) fica burra — só renderiza `State` e encaminha `Action`.

### Diagrama de dependência (simplificado)

```
        app
         │
   ┌─────┼──────────────────────┐
   │      │                      │
feature:*:presentation   core:analytics
   │      │                      │
feature:*:domain ◄───── core:domain (interfaces)
   │
feature:*:data ──► core:database
```

**Regra dura do projeto**: `core:analytics` é o **único** módulo autorizado a importar `com.google.firebase.*`. Toda a lógica de negócio (ViewModels, use cases, repositórios) depende apenas das interfaces Firebase-agnósticas declaradas em `core:domain`.

## Decisões tomadas e por quê

### 1. Interfaces em `core:domain`, implementação em `core:analytics`

Em vez de qualquer ViewModel ou repositório importar `FirebaseAnalytics`/`FirebaseCrashlytics`/`FirebaseRemoteConfig` diretamente, existem quatro interfaces pequenas em `core:domain`:

```kotlin
interface AnalyticsTracker {
    fun logScreenView(screenName: String)
    fun logEvent(name: String, params: Map<String, Any?> = emptyMap())
    fun recordException(throwable: Throwable)
}
interface RemoteConfigProvider { fun isSaleDetailEnabled(): Boolean; ... }
interface CrashCounter { fun getCrashCount(): Int }
interface DeviceMetricsProvider { fun getAppMemoryUsageMb(): Long; fun getAppStorageUsageMb(): Long }
```

`core:analytics` implementa essas interfaces com o SDK real do Firebase e as expõe via Hilt (`@Binds`). **Por quê**: isso segue o mesmo padrão já usado no projeto para repositórios (interface em `domain`, implementação em `data`), mantém `domain`/`presentation` testáveis com mocks simples (sem precisar inicializar o SDK do Firebase em teste unitário), e isola o "raio de explosão" de uma eventual troca de provedor de analytics/crash reporting a um único módulo.

### 2. Contador de crash local via DataStore

O SDK do Crashlytics **não expõe uma API para ler quantos crashes já ocorreram** no aparelho — ele só permite enviar relatórios. Como o Developer Mode precisa mostrar essa contagem, ela é mantida manualmente: um `Int` em `DataStore<Preferences>`, incrementado em dois pontos:
- No boot do app (`OmieApplication.onCreate`), se `FirebaseCrashlytics.didCrashOnPreviousExecution()` retornar `true` (crash fatal na execução anterior).
- Toda vez que `AnalyticsTracker.recordException()` é chamado (exceções não-fatais capturadas nos repositórios).

### 3. `Result<D, E>` customizado em vez de `kotlin.Result`

O projeto já usava (antes desta feature) um `Result` próprio com dois parâmetros de tipo — sucesso (`D`) e erro tipado (`E : Error`) — em vez do `kotlin.Result<T>` da stdlib, que só carrega `Throwable`. Isso permite erros de domínio explícitos e exaustivos (`DataError.Validation.BLANK_PRODUCT_NAME`, `DataError.Local.UNKNOWN`, etc.), tratados com `when` sem `else` silencioso.

### 4. Remote Config com override local — e não simplesmente "confiar no Firebase"

A flag `sale_detail_enabled` é lida do Firebase Remote Config, mas o Developer Mode permite **sobrescrever esse valor localmente**, guardado no mesmo `DataStore` do contador de crash. A lógica de leitura é:

```
override local existe nesse aparelho?
  → sim: usa o override (ignora o Firebase)
  → não: usa o valor do Remote Config (ou o default local se o fetch falhar)
```

**Por quê**: o Remote Config real depende de rede e de propagação (pode levar minutos, e no pipeline de CI o `google-services.json` de teste nem aponta pra um projeto real). O override dá controle imediato e determinístico pra QA testar os dois estados da flag num aparelho específico, sem afetar outros usuários nem depender do Console. A tela deixa explícito qual dos dois valores está em vigor, pra não gerar confusão tipo "mudei no Firebase e não mudou nada" (quando na real era um override esquecido ligado).

### 5. `google-services.json` versionado como placeholder

O plugin `google-services` **quebra o build** se o arquivo não existir, então em vez de excluí-lo do controle de versão (o que quebraria qualquer clone novo do repo), existe um placeholder sintaticamente válido versionado (`project_id: omie-desafio-placeholder`, chaves falsas). O app builda e roda normalmente com ele — só as chamadas reais ao Firebase não vão a lugar nenhum. O arquivo real (com credenciais de verdade) nunca é commitado; no CI ele é gerado a partir de um GitHub Secret em base64 (ver seção de CI/CD).

### 6. TDD nos ViewModels e nas classes com lógica não-trivial

Toda mudança de comportamento (contador de crash, gate do Remote Config, eventos de analytics por ação) foi feita escrevendo o teste primeiro, confirmando que falhava pelo motivo certo (classe/método inexistente, não erro de digitação), implementando, e só então confirmando que passava. Os testes seguem o formato Given-When-Then no nome do método (nomes de teste em backticks, estilo `` `given X, when Y, then Z` ``), com JUnit 5 + MockK (para mocks) + Turbine (para testar `Flow`/`SharedFlow` de eventos).

### 7. Cobertura mínima de 85%, mas nem todo módulo entra na conta

`build.gradle.kts` (raiz) agrega cobertura via JaCoco só dos módulos com lógica de negócio real (`core:domain`, `feature:*:domain`, `feature:*:presentation`, `feature:devtools:presentation`). Módulos que são só wrappers finos de framework (`core:database`, `core:designsystem`, `feature:*:data`, e **`core:analytics`**) ficam de fora — não porque não sejam testados, mas porque testar unitariamente uma chamada direta a `FirebaseAnalytics.getInstance(context).logEvent(...)` não agrega cobertura de lógica de negócio, só infla o denominador.

## Como rodar o app

### Pré-requisitos

- Android Studio compatível com a AGP declarada em `gradle/libs.versions.toml`
- JDK 17 (Gradle 9.x exige)

### 1. Configurar o Firebase

O repositório já vem com um `app/google-services.json` placeholder — o app builda sem ele precisar ser trocado. Para usar Firebase de verdade:

1. Crie (ou abra) um projeto no [Firebase Console](https://console.firebase.google.com/).
2. Adicione um app Android com `applicationId = com.omie.desafio`.
3. Baixe o `google-services.json` gerado e substitua o arquivo em `app/google-services.json` (mesmo nome, mesmo caminho).
4. (Opcional, para o Remote Config funcionar de fato) crie o parâmetro `sale_detail_enabled` (tipo Boolean) em Remote Config → Publicar.

### 2. Build e instalação

```bash
./gradlew build                    # build completo de todos os módulos
./gradlew :app:assembleDebug       # gera só a APK debug
./gradlew :app:installDebug        # instala num emulador/dispositivo conectado
```

Ou abra o projeto direto no Android Studio e rode a configuração `app`.

### 3. Acessando o Developer Mode

Abra o app → toque no ícone de bug (🐛) na barra superior da tela inicial. Não depende de sensor nem gesto de shake — é um botão direto, acessível em qualquer build (debug ou release).

## Testes, lint e cobertura

```bash
./gradlew test                         # testes unitários de todos os módulos
./gradlew detekt                       # lint estático (Detekt)
./gradlew jacocoCoverageVerification   # cobertura agregada — falha se < 85%
```

O relatório HTML de cobertura fica em `build/reports/jacoco/aggregated/html/index.html` após rodar `jacocoCoverageVerification`.

## CI/CD

Dois workflows em `.github/workflows/`:

### `ci.yml` — gate de qualidade
Roda em todo `push` (qualquer branch) e em `pull_request` para `main`. Dois jobs em paralelo, sem depender de nenhum secret (gera um `google-services.json` placeholder próprio se não existir):
- **test**: `./gradlew test` + `./gradlew jacocoCoverageVerification`, sobe os relatórios como artifacts.
- **detekt**: `./gradlew detekt`, sobe o relatório como artifact.

### `firebase-distribution.yml` — distribuição
Dispara em push para `main` ou manualmente (aba Actions → Run workflow). Builda a APK debug (já assinada com o keystore de debug automático — sem precisar gerenciar keystore de release em CI), roda `test` + `detekt` de novo como segunda camada de segurança, e sobe pro Firebase App Distribution.

**Secrets necessários** (Settings → Secrets and variables → Actions):

| Secret | O que é | Como obter |
|---|---|---|
| `GOOGLE_SERVICES_JSON` | Conteúdo do `app/google-services.json` real, em base64 | `base64 -i app/google-services.json` |
| `FIREBASE_APP_ID` | ID do app Android no Firebase (`1:XXXXXXXXXX:android:XXXX...`) | Firebase Console → Configurações do projeto → seus apps |
| `FIREBASE_SERVICE_ACCOUNT` | JSON de uma service account com role de App Distribution Admin | Google Cloud Console → IAM & Admin → Service Accounts |

O workflow envia por padrão para o grupo de testers `testers` (Firebase Console → App Distribution → Testers & Groups) — troque o nome no YAML se preferir outro grupo.

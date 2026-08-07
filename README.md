# Desafio Omie

App Android (Kotlin + Jetpack Compose) de gestão de produtos e vendas, com observabilidade via Firebase e um Developer Mode para diagnóstico local.

## Arquitetura

Clean Architecture por feature, com MVI na camada de apresentação.

```
app/                    → navegação e wiring geral
core/
  domain/                → contratos (Result, DataError, interfaces de observabilidade)
  database/               → Room
  designsystem/           → tema e componentes visuais
  presentation/           → utilitários de UI compartilhados
  analytics/               → implementação Firebase das interfaces de core:domain
feature/
  products/{domain,data,presentation}
  sales/{domain,data,presentation}
  devtools/presentation/   → tela de Developer Mode
```

- **domain**: modelos, use cases, contratos de repositório — Kotlin puro, sem Android/Firebase.
- **data**: implementação dos repositórios (Room).
- **presentation**: ViewModels + telas Compose, seguindo o padrão **MVI** (`State`, `Action`, `Event` — evento é efeito único como navegação, coletado via `SharedFlow`).
- **core:analytics** é o único módulo que importa o SDK do Firebase. Todo o resto do app depende só das interfaces de `core:domain`, o que mantém a lógica de negócio testável com mocks simples.

## Trackers (Analytics/Crashlytics/Performance)

- **Analytics**: `screen_view` automático a cada navegação, mais eventos de negócio por ação (`product_created`, `sale_created`, `sale_item_included`, etc.), disparados pelos próprios ViewModels via `AnalyticsTracker`.
- **Crashlytics**: crashes fatais e exceções não-fatais (capturadas nos repositórios) são reportadas. Como o SDK não expõe uma contagem local de crashes, mantemos um contador próprio em `DataStore`, incrementado no boot (se `didCrashOnPreviousExecution()`) e a cada exceção reportada — exibido no Developer Mode.
- **Performance Monitoring**: automático, sem instrumentação manual.

## Feature Flag (Remote Config)

A flag `sale_detail_enabled` controla a navegação pro detalhe de uma venda, vinda do Firebase Remote Config. O Developer Mode permite **sobrescrever esse valor localmente no aparelho** (guardado em DataStore): se houver override, ele vence o valor remoto; um botão limpa o override e volta a obedecer o Firebase. A tela sempre mostra qual dos dois está em vigor no momento.

## Developer Mode

Acessível pelo ícone de bug (🐛) na Home. Mostra:
- Contagem de crashes, uso de memória e de armazenamento (com barra de progresso)
- Toggle da feature flag `sale_detail_enabled` + indicador de origem (override local vs. remoto)

## Rodando o projeto

```bash
./gradlew build                # build completo
./gradlew :app:installDebug    # instala num emulador/dispositivo
```

`app/google-services.json` já vem versionado apontando pro projeto Firebase real — não precisa configurar nada extra.

## Testes e qualidade

```bash
./gradlew test                         # testes unitários
./gradlew detekt                       # lint estático
./gradlew jacocoCoverageVerification   # cobertura agregada (mínimo 85%)
```

## CI/CD

- `.github/workflows/ci.yml` — roda `test` + `detekt` em todo push/PR.
- `.github/workflows/firebase-distribution.yml` — builda a APK debug e distribui via Firebase App Distribution (push em `main` ou disparo manual). Precisa dos secrets `FIREBASE_APP_ID` e `FIREBASE_SERVICE_ACCOUNT`.

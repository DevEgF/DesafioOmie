# Desafio Omie

App Android nativo (Kotlin, Jetpack Compose) para gestão de produtos e vendas, com observabilidade Firebase (Analytics, Crashlytics, Remote Config, Performance) e uma tela de Developer Mode para diagnóstico local.

## Stack

- **Kotlin** + **Jetpack Compose** (Material 3)
- **Arquitetura em camadas / Clean Architecture** por feature (`domain` → `data` → `presentation`), padrão **MVI** nos ViewModels (`State` / `Action` / `Event`)
- **Hilt** para injeção de dependência
- **Room** para persistência local
- **AndroidX DataStore (Preferences)** para armazenamento local leve (contador de crashes, overrides de feature flag)
- **Navigation Compose** com rotas type-safe (`kotlinx.serialization`)
- **Firebase**: Analytics, Crashlytics, Remote Config, Performance Monitoring
- **JUnit 5 + MockK + Turbine** para testes, **Detekt** para lint estático, **JaCoCo** para cobertura

## Estrutura dos módulos

```
app/                              — ponto de entrada, navegação, wiring geral
core/
  domain/                         — contratos Firebase-agnósticos (Result, DataError,
                                     AnalyticsTracker, RemoteConfigProvider, CrashCounter,
                                     DeviceMetricsProvider)
  database/                       — Room (DAOs, entities)
  designsystem/                   — tema, tipografia, componentes visuais compartilhados
  presentation/                   — utilitários de UI compartilhados (UiText, ObserveAsEvents, Money)
  analytics/                      — único módulo que importa o SDK do Firebase; implementa
                                     as interfaces de core:domain
feature/
  products/{domain,data,presentation}
  sales/{domain,data,presentation}
  devtools/presentation/          — tela de Developer Mode (sem domain/data próprios)
```

**Regra de dependência**: `core:analytics` é o único módulo autorizado a importar `com.google.firebase.*`. Todo o resto do app depende apenas das interfaces declaradas em `core:domain`.

## Funcionalidades

- Cadastro e listagem de produtos
- Registro de vendas (múltiplos itens, cálculo de total)
- Detalhe de venda (navegação controlada por feature flag remota)
- **Observabilidade**:
  - Eventos de tela automáticos (`screen_view` a cada navegação) + eventos de negócio (`product_created`, `sale_created`, etc.)
  - Relato de exceções fatais e não-fatais ao Crashlytics
  - Contador local de crashes (persistido via DataStore, já que o SDK do Crashlytics não expõe essa contagem)
  - Remote Config com a flag `sale_detail_enabled`
- **Developer Mode** (acessível pelo ícone de bug na tela inicial):
  - Contagem de crashes, uso de memória (RAM) e uso de armazenamento, com barras de progresso
  - Toggle local para forçar `sale_detail_enabled` ligado/desligado nesse aparelho, com indicador mostrando se o valor em uso é o override local ou o valor remoto do Firebase, e botão para resetar o override

## Rodando o projeto

### Pré-requisitos
- Android Studio (versão compatível com AGP definido em `gradle/libs.versions.toml`)
- JDK 11+

### Setup do Firebase
O arquivo `app/google-services.json` precisa existir para o build funcionar (os plugins `google-services`/`crashlytics`/`perf` exigem). Baixe o arquivo real do seu projeto no [Firebase Console](https://console.firebase.google.com/) (Android app com `applicationId = com.omie.desafio`) e substitua o arquivo em `app/google-services.json`.

### Build e testes

```bash
./gradlew build                        # build completo
./gradlew test                         # testes unitários de todos os módulos
./gradlew detekt                       # lint estático
./gradlew jacocoCoverageVerification   # cobertura agregada (mínimo 85%)
./gradlew :app:installDebug            # instala no emulador/dispositivo conectado
```

### Testando o Developer Mode
Abra o app, toque no ícone de bug (🐛) na barra superior da tela inicial. Não depende de sensores nem gestos — é um botão direto.

## CI/CD — distribuição via Firebase App Distribution

O workflow `.github/workflows/firebase-distribution.yml` builda a APK debug, roda testes + detekt, e sobe o resultado para o Firebase App Distribution. Ele dispara automaticamente em push para `main` ou manualmente (aba **Actions → Firebase App Distribution → Run workflow**).

### Secrets necessários (Settings → Secrets and variables → Actions)

| Secret | O que é | Como obter |
|---|---|---|
| `GOOGLE_SERVICES_JSON` | Conteúdo do `app/google-services.json` real, **em base64** | `base64 -i app/google-services.json` (ou `certutil -encode` no Windows, removendo as linhas `-----BEGIN/END-----`) |
| `FIREBASE_APP_ID` | ID do app Android no Firebase (formato `1:XXXXXXXXXX:android:XXXXXXXXXXXXXXXX`) | Firebase Console → Configurações do projeto → seus apps → App ID |
| `FIREBASE_SERVICE_ACCOUNT` | Conteúdo JSON de uma service account com a role **Firebase App Distribution Admin** (ou Editor) | Google Cloud Console → IAM & Admin → Service Accounts → criar chave JSON |

O workflow escreve o `GOOGLE_SERVICES_JSON` decodificado por cima do placeholder do repositório antes do build — o placeholder que já está versionado nunca precisa ser trocado manualmente, e o arquivo real nunca é commitado.

### Grupo de testers
O workflow envia para o grupo `testers` (parâmetro `groups` no step de upload). Crie esse grupo em Firebase Console → App Distribution → Testers & Groups, ou troque o nome no YAML para o grupo que preferir.

### Ajustes possíveis
- Trocar `assembleDebug` por `assembleRelease` exige configurar um `signingConfig` (keystore) — hoje o `release` buildType não está assinado.
- Trocar o gatilho `push: branches: [main]` por outro branch, ou remover e deixar só `workflow_dispatch` para disparo manual.

## Licença

Projeto de desafio técnico.

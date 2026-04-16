# Android Clean Scaffold

Template base para novos projetos Android seguindo **Clean Architecture + MVVM + Jetpack Compose + Hilt**.

---

## Stack Tecnológica

| Tecnologia | Finalidade |
|---|---|
| Kotlin | Linguagem principal |
| Jetpack Compose | UI declarativa |
| Hilt | Injeção de dependência |
| StateFlow / SharedFlow | Gerenciamento de estado |
| Navigation Compose | Navegação entre telas |
| Retrofit + OkHttp | Chamadas de API REST |
| Room | Persistência local |
| Coroutines | Programação assíncrona |
| Coil | Carregamento de imagens |

---

## Estrutura do Projeto

```
app/src/main/java/com/scaffold/app/
│
├── data/                          # Camada de Dados
│   ├── api/
│   │   ├── ApiService.kt          # Interface Retrofit com endpoints
│   │   └── ApiResult.kt           # Sealed class de resultado + safeApiCall
│   ├── di/
│   │   ├── NetworkModule.kt       # Provê Retrofit, OkHttp, ApiService
│   │   └── RepositoryModule.kt    # Bind de interfaces → implementações
│   ├── mappers/
│   │   ├── SampleItemMapper.kt    # Converte DTO → domínio
│   │   └── AuthMapper.kt
│   ├── models/
│   │   ├── SampleItemDto.kt       # Modelos Request/Response da API
│   │   └── AuthDto.kt
│   └── repositories/
│       ├── SampleRepositoryImpl.kt
│       └── AuthRepositoryImpl.kt
│
├── domain/                        # Camada de Domínio (sem dependências externas)
│   ├── models/
│   │   ├── SampleItem.kt          # Modelo de domínio genérico
│   │   └── Auth.kt                # User, Credentials, AuthResult
│   ├── repositories/
│   │   ├── SampleRepository.kt    # Interface do repositório
│   │   └── AuthRepository.kt
│   └── usecases/
│       ├── GetSampleItemsUseCase.kt
│       ├── LoginUseCase.kt
│       ├── ValidateEmailUseCase.kt
│       └── ValidatePasswordUseCase.kt
│
├── presentation/                  # Camada de Apresentação
│   └── ui/
│       ├── navigation/
│       │   ├── Screen.kt          # Sealed class com rotas
│       │   └── NavGraph.kt        # NavHost Compose
│       ├── splash/
│       │   └── SplashScreen.kt
│       ├── login/
│       │   ├── components/        # EmailField, PasswordField
│       │   ├── screens/           # LoginScreen
│       │   └── viewmodels/        # LoginViewModel, LoginUiState, LoginUiEvent
│       └── home/
│           ├── screens/           # HomeScreen
│           └── viewmodels/        # HomeViewModel, HomeUiState
│
├── ui/theme/                      # Tema global
│   ├── Color.kt
│   ├── Theme.kt                   # AppTheme
│   └── Type.kt
│
├── MainActivity.kt                # @AndroidEntryPoint
├── SplashActivity.kt
└── ScaffoldApp.kt                 # @HiltAndroidApp
```

---

## Como usar este scaffold

### 1. Renomear pacote

Substitua todas as ocorrências de `com.scaffold.app` pelo pacote do seu novo projeto:

```bash
# Exemplo com sed (macOS/Linux)
find . -type f -name "*.kt" -exec sed -i '' 's/com\.scaffold\.app/com.suaempresa.seuprojeto/g' {} +
find . -type f -name "*.xml" -exec sed -i '' 's/com\.scaffold\.app/com.suaempresa.seuprojeto/g' {} +
```

Altere também em `app/build.gradle.kts`:
```kotlin
namespace = "com.suaempresa.seuprojeto"
applicationId = "com.suaempresa.seuprojeto"
```

E em `settings.gradle.kts`:
```kotlin
rootProject.name = "SeuProjeto"
```

### 2. Configurar URL da API

Em `app/build.gradle.kts`:
```kotlin
buildConfigField("String", "API_BASE_URL", "\"https://sua-api.com/\"")
```

### 3. Criar uma nova Feature (padrão)

Siga sempre esta estrutura para cada nova feature:

```
presentation/ui/minha_feature/
├── components/          # Composables menores e reutilizáveis
├── screens/             # MinhaFeatureScreen.kt  
└── viewmodels/          # MinhaFeatureViewModel.kt + UiState + UiEvent

domain/
├── models/              # MinhaEntidade.kt
├── repositories/        # MinhaFeatureRepository.kt (interface)
└── usecases/            # GetMinhaFeatureUseCase.kt

data/
├── models/              # MinhaEntidadeDto.kt
├── mappers/             # MinhaEntidadeMapper.kt
└── repositories/        # MinhaFeatureRepositoryImpl.kt
```

Adicione o binding no `RepositoryModule.kt` e o endpoint no `ApiService.kt`.

### 4. Adicionar nova tela à navegação

Em `Screen.kt`:
```kotlin
object MinhaNovaFeature : Screen("minha_feature")
```

Em `NavGraph.kt`:
```kotlin
composable(Screen.MinhaNovaFeature.route) {
    MinhaNovaFeatureScreen(/* callbacks */)
}
```

---

## Padrões obrigatórios

### ViewModel
- Use `StateFlow` para estado da UI (`UiState`)
- Use `SharedFlow` para eventos únicos (`UiEvent`)
- Use `viewModelScope` para coroutines
- Anote com `@HiltViewModel`

```kotlin
@HiltViewModel
class MinhaViewModel @Inject constructor(
    private val useCase: MeuUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(MeuUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<MeuUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()
}
```

### Composable Screen
- Tela pública recebe ViewModel via `hiltViewModel()`
- Conteúdo interno é privado e recebe apenas dados/callbacks
- Sempre crie `@Preview` para o conteúdo interno

```kotlin
@Composable
fun MinhaScreen(
    onAction: () -> Unit,
    viewModel: MinhaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    MinhaContent(uiState = uiState, onAction = onAction)
}

@Composable
private fun MinhaContent(uiState: MeuUiState, onAction: () -> Unit) { ... }
```

### Use Cases
- Um use case = uma ação de negócio
- Anote dependências com `@Inject`
- Retorne `Flow` para streams, valor direto para operações únicas

### Repositório
- Interface no domínio, implementação na camada de dados
- Use `safeApiCall` para encapsular chamadas Retrofit
- Use `flowOn(Dispatchers.IO)` para operações de I/O

---

## Convenção de Commits

Siga o padrão Conventional Commits:

```
feat(login): implementa validação de senha
fix(home): corrige crash ao carregar lista vazia
refactor(auth): extrai lógica para use case
test(login): adiciona teste unitário para ValidateEmailUseCase
```

Tipos: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`, `perf`, `ci`, `build`, `revert`

---

## Testes

### Unitário (ViewModel)
```kotlin
@Test
fun `when login succeeds should emit NavigateToHome event`() = runTest {
    // Given
    coEvery { loginUseCase(any()) } returns flowOf(fakeAuthResult)

    // When
    viewModel.onLoginClick()

    // Then
    viewModel.uiEvent.test {
        assertEquals(LoginUiEvent.NavigateToHome, awaitItem())
    }
}
```

### Use Case
```kotlin
@Test
fun `validateEmail returns false for invalid email`() {
    val useCase = ValidateEmailUseCase()
    assertFalse(useCase("emailinvalido"))
    assertTrue(useCase("valido@email.com"))
}
```

---

## Checklist ao iniciar novo projeto a partir deste scaffold

- [ ] Renomear pacote em todos os arquivos `.kt` e `.xml`
- [ ] Atualizar `applicationId` e `namespace` no `build.gradle.kts`
- [ ] Atualizar `rootProject.name` no `settings.gradle.kts`
- [ ] Definir `API_BASE_URL` no `build.gradle.kts`
- [ ] Atualizar `app_name` em `strings.xml`
- [ ] Substituir modelos de exemplo (`SampleItem`) por modelos de domínio reais
- [ ] Configurar autenticação real no `AuthRepositoryImpl`
- [ ] Adicionar `google-services.json` se usar Firebase
- [ ] Atualizar ícones do launcher
- [ ] Remover / adaptar exemplos genéricos que não se aplicam ao projeto

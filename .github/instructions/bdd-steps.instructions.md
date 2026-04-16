---
applyTo: "**/.md"
description: "Instructions architecture development"
---

# Instruções para Implementação de BDD

Este guia define a sequência de steps para implementar features a partir de especificações BDD.

## Análise Inicial do BDD
1. Identifique os atores envolvidos
2. Liste as ações principais
3. Identifique os dados necessários
4. Mapeie os resultados esperados

## Sequência de Implementação

### Step 1 - Camada de Domínio
1. Criar modelo de domínio (Entity)
2. Definir interface do repositório
3. Implementar casos de uso
4. Definir exceções de domínio se necessário

```kotlin
// Domain Model
data class User(
    val id: String,
    val email: String,
    val password: String
)

// Repository Interface
interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
}

// Use Case
class LoginUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<User> =
        repository.login(email, password)
}
```

### Step 2 - Testes da Camada de Domínio
1. Criar testes unitários para casos de uso
2. Configurar mocks do repositório
3. Testar cenários de sucesso e erro

```kotlin
class LoginUseCaseTest {
    @Mock
    private lateinit var repository: AuthRepository
    private lateinit var useCase: LoginUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = LoginUseCase(repository)
    }

    @Test
    fun `when login is successful then return success result`() = runTest {
        // given
        val email = "test@example.com"
        val password = "password123"
        val user = User("1", email, password)
        whenever(repository.login(email, password))
            .thenReturn(Result.success(user))

        // when
        val result = useCase(email, password)

        // then
        assertTrue(result.isSuccess)
        assertEquals(user, result.getOrNull())
    }
}
```

### Step 3 - Camada de Dados
1. Criar modelos de dados (Request/Response)
2. Implementar serviços de API
3. Implementar repositório
4. Criar mappers entre camadas

```kotlin
// API Service
interface AuthService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
}

// Repository Implementation
class AuthRepositoryImpl @Inject constructor(
    private val service: AuthService,
    private val mapper: UserMapper
) : AuthRepository {
    override suspend fun login(email: String, password: String): Result<User> =
        try {
            val response = service.login(LoginRequest(email, password))
            Result.success(mapper.toDomain(response))
        } catch (e: Exception) {
            Result.failure(e)
        }
}
```

### Step 4 - Testes da Camada de Dados
1. Criar testes unitários para o repositório
2. Configurar mocks do serviço e mappers
3. Testar cenários de sucesso, erro e mapeamento

```kotlin
class AuthRepositoryImplTest {
    @Mock
    private lateinit var service: AuthService
    @Mock
    private lateinit var mapper: UserMapper
    private lateinit var repository: AuthRepositoryImpl

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = AuthRepositoryImpl(service, mapper)
    }

    @Test
    fun `when login service succeeds then return mapped user`() = runTest {
        // given
        val email = "test@example.com"
        val password = "password123"
        val response = LoginResponse(id = "1", email = email)
        val user = User("1", email, password)
        
        whenever(service.login(any())).thenReturn(response)
        whenever(mapper.toDomain(response)).thenReturn(user)

        // when
        val result = repository.login(email, password)

        // then
        assertTrue(result.isSuccess)
        assertEquals(user, result.getOrNull())
    }
}
```

### Step 5 - Camada de Apresentação (ViewModel)
1. Criar estados da UI
2. Implementar ViewModel
3. Definir eventos e ações
4. Implementar lógica de apresentação

```kotlin
sealed class LoginUiState {
    object Initial : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val user: User) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Initial)
    val uiState = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            loginUseCase(email, password)
                .onSuccess { user ->
                    _uiState.value = LoginUiState.Success(user)
                }
                .onFailure { error ->
                    _uiState.value = LoginUiState.Error(error.message.orEmpty())
                }
        }
    }
}
```

### Step 6 - Testes da Camada de Apresentação
1. Criar testes unitários para o ViewModel
2. Configurar mocks dos casos de uso
3. Testar fluxos de estados e eventos
4. Verificar tratamento de erros

```kotlin
class LoginViewModelTest {
    @Mock
    private lateinit var loginUseCase: LoginUseCase
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        viewModel = LoginViewModel(loginUseCase)
    }

    @Test
    fun `when login succeeds then emit success state`() = runTest {
        // given
        val email = "test@example.com"
        val password = "password123"
        val user = User("1", email, password)
        whenever(loginUseCase(email, password))
            .thenReturn(Result.success(user))

        // when
        viewModel.login(email, password)

        // then
        assertEquals(LoginUiState.Success(user), viewModel.uiState.value)
    }
}
```

### Step 7 - Camada de Apresentação (UI)
1. Implementar tela usando Compose
2. Conectar com ViewModel
3. Implementar tratamento de estados
4. Adicionar navegação

```kotlin
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onNavigateToHome: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LoginContent(
        uiState = uiState,
        onLoginClick = viewModel::login,
        onNavigateToHome = onNavigateToHome
    )
}
```

### Step 8 - Testes de Integração
1. Criar testes de integração entre camadas
2. Configurar TestRule para Hilt
3. Testar fluxo completo da feature

```kotlin
@HiltAndroidTest
class LoginIntegrationTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var repository: AuthRepository

    @Test
    fun loginFeatureTest() = runTest {
        // Test full flow from repository to UI
    }
}
```

## Considerações Importantes

1. **Ordem dos Steps**
   - Sempre implemente do domínio para fora
   - Testes unitários após cada camada
   - UI por último após tudo testado

2. **Testes**
   - Use mocks apropriadamente
   - Teste casos de sucesso e erro
   - Verifique estados e eventos
   - Mantenha testes independentes

3. **Clean Architecture**
   - Mantenha as camadas bem definidas
   - Use mappers entre camadas
   - Injeção de dependência apropriada
   - Respeite as regras de dependência

4. **Commits**
   - Um commit por step
   - Mensagens claras e descritivas
   - Inclua testes relacionados
   - Mantenha mudanças coesas

### 2. Conversão para Steps

Para cada feature do BDD, siga a sequência de steps abaixo:

## Step 1 - Camada de Domínio
```kotlin
// 1.1 Criar Entity
data class LoginEntity(
    val email: String,
    val password: String
)

// 1.2 Criar Repository Interface
interface LoginRepository {
    suspend fun login(credentials: LoginEntity): Result<UserEntity>
}

// 1.3 Criar Use Case
class LoginUseCase @Inject constructor(
    private val repository: LoginRepository
) {
    suspend operator fun invoke(credentials: LoginEntity): Result<UserEntity>
}
```

## Step 2 - Camada de Dados
```kotlin
// 2.1 Criar API Service
interface LoginApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}

// 2.2 Criar Data Models
data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val user: UserResponse
)

// 2.3 Criar Repository Implementation
class LoginRepositoryImpl @Inject constructor(
    private val api: LoginApi,
    private val mapper: LoginMapper
) : LoginRepository {
    override suspend fun login(credentials: LoginEntity): Result<UserEntity> =
        runCatching {
            val response = api.login(mapper.toRequest(credentials))
            mapper.toDomain(response)
        }
}
```

## Step 3 - Camada de Apresentação
```kotlin
// 3.1 Criar UI State
sealed class LoginUiState {
    object Initial : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val user: UserEntity) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

// 3.2 Criar ViewModel
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Initial)
    val uiState = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            loginUseCase(LoginEntity(email, password))
                .onSuccess { user ->
                    _uiState.value = LoginUiState.Success(user)
                }
                .onFailure { error ->
                    _uiState.value = LoginUiState.Error(error.message.orEmpty())
                }
        }
    }
}
```

## Step 4 - Camada de UI
```kotlin
// 4.1 Criar Composable Screen
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LoginContent(
        uiState = uiState,
        onLoginClick = viewModel::login,
        onLoginSuccess = onLoginSuccess
    )
}

// 4.2 Criar Content
@Composable
private fun LoginContent(
    uiState: LoginUiState,
    onLoginClick: (String, String) -> Unit,
    onLoginSuccess: () -> Unit
) {
    // Implementar UI conforme BDD
}
```

## Step 5 - Injeção de Dependência
```kotlin
// 5.1 Criar Module
@Module
@InstallIn(SingletonComponent::class)
object LoginModule {
    @Provides
    @Singleton
    fun provideLoginApi(retrofit: Retrofit): LoginApi =
        retrofit.create(LoginApi::class.java)

    @Provides
    @Singleton
    fun provideLoginRepository(
        api: LoginApi,
        mapper: LoginMapper
    ): LoginRepository = LoginRepositoryImpl(api, mapper)
}
```

## Step 6 - Testes
```kotlin
// 6.1 Testes de Use Case
class LoginUseCaseTest {
    @Test
    fun `when credentials are valid then return success`() {
        // Implement test based on BDD scenario
    }
}

// 6.2 Testes de ViewModel
class LoginViewModelTest {
    @Test
    fun `when login is successful then emit success state`() {
        // Implement test based on BDD scenario
    }
}
```

### Exemplo de Conversão

#### BDD Original
```gherkin
Cenário: Login bem-sucedido com email e senha
Dado que estou na tela de login
Quando eu digito "usuario@exemplo.com" no campo de email
E eu digito "senhaValida123" no campo de senha
E eu toco no botão de login
Então eu devo ser redirecionado para a tela inicial
```

#### Conversão em Steps

1. **Domain Layer**
   - Criar LoginEntity com email e senha
   - Criar LoginRepository interface com método login
   - Criar LoginUseCase para processar a autenticação

2. **Data Layer**
   - Criar LoginApi com endpoint POST /auth/login
   - Criar LoginRequest/Response models
   - Implementar LoginRepositoryImpl

3. **Presentation Layer**
   - Criar LoginUiState com estados Initial, Loading, Success, Error
   - Implementar LoginViewModel com login flow

4. **UI Layer**
   - Criar LoginScreen com form e validações
   - Implementar navegação para tela inicial após sucesso

5. **DI Setup**
   - Configurar módulo Hilt para injeção das dependências

6. **Testes**
   - Implementar testes unitários e de integração
   - Validar todos os cenários do BDD

### Boas Práticas

1. **Mapeamento de Cenários**
   - Cada cenário do BDD deve ter steps correspondentes
   - Mantenha rastreabilidade entre BDD e implementação
   - Documente decisões de implementação

2. **Implementação**
   - Siga a ordem das camadas (domain → data → presentation → ui)
   - Implemente um step por vez
   - Valide cada step antes de prosseguir

3. **Testes**
   - Escreva testes para cada camada
   - Use os cenários do BDD como base para testes
   - Mantenha cobertura de código alta

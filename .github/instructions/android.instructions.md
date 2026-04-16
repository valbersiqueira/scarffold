---
applyTo: "**"
description: "Instructions for Android development"
---

# Android Development Instructions

# Instruções de Desenvolvimento Android

## Chamadas Assíncronas com Coroutines

### Regras
1. Todas as operações assíncronas DEVEM ser implementadas usando Coroutines
2. Use `viewModelScope` para coroutines no ViewModel
3. Use `lifecycleScope` para coroutines em Activities/Fragments
4. Sempre defina o dispatcher apropriado para cada operação

### Exemplos de Implementação

```kotlin
class ExampleViewModel : ViewModel() {
    fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Operação de IO
                val result = repository.fetchData()
                // Atualização de UI no contexto Main
                withContext(Dispatchers.Main) {
                    // Atualizar estado
                }
            } catch (e: Exception) {
                // Tratamento de erro
            }
        }
    }
}
```

### Padrões a Seguir
- Use `withContext` para mudar de contexto
- Implemente tratamento de erros com try/catch
- Evite bloqueio de thread com `runBlocking`
- Prefira `async/await` para operações paralelas

## Navegação com Navigation Component

### Regras
1. Use SEMPRE o Navigation Component para navegação entre telas
2. Defina todos os destinos no navigation graph
3. Use Safe Args para passar dados entre destinos
4. Evite navegação programática direta

### Exemplo de Navigation Graph
```xml
<?xml version="1.0" encoding="utf-8"?>
<navigation xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto">
    
    <fragment
        android:id="@+id/loginFragment"
        android:name=".ui.login.LoginFragment">
        <action
            android:id="@+id/action_login_to_home"
            app:destination="@id/homeFragment"
            app:popUpTo="@id/loginFragment"
            app:popUpToInclusive="true" />
    </fragment>
</navigation>
```

### Exemplo de Navegação
```kotlin
// Use sempre desta forma
findNavController().navigate(
    LoginFragmentDirections.actionLoginToHome(userId = "123")
)
```

## Gerenciamento de Estado com StateFlow

### Regras
1. Use SEMPRE StateFlow/MutableStateFlow para gerenciar estados na UI
2. Defina estados imutáveis
3. Atualize estados apenas através do ViewModel
4. Use SharedFlow para eventos únicos (side-effects)

### Exemplo de Implementação

```kotlin
class LoginViewModel : ViewModel() {
    // Estado da UI
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Initial)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // Eventos únicos
    private val _uiEvent = MutableSharedFlow<LoginUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val result = repository.login(email, password)
                _uiState.update { 
                    LoginUiState.Success(user = result)
                }
                _uiEvent.emit(LoginUiEvent.NavigateToHome)
            } catch (e: Exception) {
                _uiState.update { 
                    LoginUiState.Error(message = e.message)
                }
            }
        }
    }
}

// Estado da UI
sealed class LoginUiState {
    object Initial : LoginUiState()
    data class Success(val user: User) : LoginUiState()
    data class Error(val message: String?) : LoginUiState()
}

// Eventos únicos
sealed class LoginUiEvent {
    object NavigateToHome : LoginUiEvent()
}
```

### Coleta de Estado na UI
```kotlin
class LoginFragment : Fragment() {
    private val viewModel: LoginViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Coletar estado
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                updateUi(state)
            }
        }

        // Coletar eventos
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiEvent.collect { event ->
                handleEvent(event)
            }
        }
    }
}
```

## Injeção de Dependência com Hilt

### Regras
1. Use SEMPRE Hilt para injeção de dependência
2. Cada módulo deve ter um único propósito
3. Use escopos apropriados (@Singleton, @ActivityScoped, etc.)
4. Evite injeção manual com Constructor Injection quando possível

### Configuração do Hilt
```kotlin
// Application
@HiltAndroidApp
class MoneyMindApp : Application()

// Activity
@AndroidEntryPoint
class MainActivity : AppCompatActivity()

// Fragment
@AndroidEntryPoint
class LoginFragment : Fragment()

// ViewModel
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel()
```

### Módulos e Providers
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor())
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {
    @Provides
    fun provideLoginUseCase(
        repository: UserRepository
    ): LoginUseCase = LoginUseCase(repository)
}
```

### Boas Práticas
1. Use `@Binds` para interfaces
2. Use `@Provides` para classes externas
3. Separe módulos por funcionalidade
4. Use qualificadores (@Named, @Qualifier) quando necessário

### Exemplo de Qualificadores
```kotlin
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthInterceptorOkHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class OtherInterceptorOkHttpClient

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @AuthInterceptorOkHttpClient
    @Provides
    @Singleton
    fun provideAuthOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor())
            .build()
    }

    @OtherInterceptorOkHttpClient
    @Provides
    @Singleton
    fun provideOtherOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(OtherInterceptor())
            .build()
    }
}
```

## Configuração do Retrofit e Chamadas de API

### Regras
1. Use SEMPRE Retrofit para chamadas de API REST
2. Implemente interceptores para tratamentos comuns
3. Configure timeout apropriado para as chamadas
4. Use coroutines com chamadas suspensas
5. Implemente tratamento de erros consistente

### Configuração Básica

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(): Interceptor {
        return Interceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer ${TokenManager.getToken()}")
                .addHeader("Content-Type", "application/json")
                .build()
            chain.proceed(request)
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: Interceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService =
        retrofit.create(ApiService::class.java)
}
```

### Definição de Serviços

```kotlin
interface ApiService {
    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: String): Response<UserResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("transactions")
    suspend fun getTransactions(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<PagedResponse<TransactionResponse>>

    @Multipart
    @POST("documents")
    suspend fun uploadDocument(
        @Part file: MultipartBody.Part,
        @Part("type") type: RequestBody
    ): Response<DocumentResponse>
}
```

### Tratamento de Erros

```kotlin
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val code: Int, val message: String?) : ApiResult<Nothing>()
    data class Exception(val e: Throwable) : ApiResult<Nothing>()
}

suspend fun <T> safeApiCall(call: suspend () -> Response<T>): ApiResult<T> {
    return try {
        val response = call()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                ApiResult.Success(body)
            } else {
                ApiResult.Error(
                    code = response.code(),
                    message = "Response body is null"
                )
            }
        } else {
            ApiResult.Error(
                code = response.code(),
                message = response.errorBody()?.string()
            )
        }
    } catch (e: Exception) {
        ApiResult.Exception(e)
    }
}
```

### Exemplo de Uso em Repository

```kotlin
class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : UserRepository {
    override suspend fun login(
        email: String,
        password: String
    ): Flow<ApiResult<LoginResponse>> = flow {
        emit(safeApiCall {
            apiService.login(LoginRequest(email, password))
        })
    }.flowOn(Dispatchers.IO)

    override suspend fun uploadDocument(
        file: File,
        type: String
    ): Flow<ApiResult<DocumentResponse>> = flow {
        // Criar MultipartBody.Part do arquivo
        val requestFile = file.asRequestBody("multipart/form-data".toMediaType())
        val filePart = MultipartBody.Part.createFormData(
            "file",
            file.name,
            requestFile
        )
        
        // Criar RequestBody para o tipo
        val typeBody = type.toRequestBody("text/plain".toMediaType())
        
        emit(safeApiCall {
            apiService.uploadDocument(filePart, typeBody)
        })
    }.flowOn(Dispatchers.IO)
}
```

### Boas Práticas

1. **Interceptores**
   - Implemente interceptor de autenticação
   - Use logging interceptor apenas em debug
   - Adicione interceptor para reconexão em falhas
   - Implemente cache quando apropriado

2. **Timeout e Cache**
   - Configure timeouts adequados por ambiente
   - Implemente cache offline quando necessário
   - Use diferentes timeouts para diferentes endpoints
   - Configure retry policy para falhas temporárias

3. **Serialização**
   - Use @SerializedName para mapping seguro
   - Defina valores default para campos nullable
   - Implemente adapters customizados quando necessário
   - Mantenha modelos de API separados dos modelos de domínio

4. **Tratamento de Erros**
   - Implemente tratamento consistente de erros
   - Use sealed class para representar resultados
   - Trate erros específicos da API
   - Propague erros de forma apropriada

5. **Testing**
   - Mock respostas da API em testes
   - Teste diferentes cenários de erro
   - Use MockWebServer para testes de integração
   - Implemente testes para interceptors

### Exemplo de Testes

```kotlin
@Test
fun `when login successful should return success result`() = runTest {
    // Given
    val response = LoginResponse(token = "token123")
    coEvery { apiService.login(any()) } returns Response.success(response)

    // When
    val result = repository.login("email@test.com", "password")
        .first()

    // Then
    assertTrue(result is ApiResult.Success)
    assertEquals("token123", (result as ApiResult.Success).data.token)
}

@Test
fun `when login fails should return error result`() = runTest {
    // Given
    coEvery { apiService.login(any()) } returns Response.error(
        401,
        "Unauthorized".toResponseBody()
    )

    // When
    val result = repository.login("email@test.com", "password")
        .first()

    // Then
    assertTrue(result is ApiResult.Error)
    assertEquals(401, (result as ApiResult.Error).code)
}
```

## Jetpack Compose

### Estrutura de Composables

#### Regras
1. Siga o padrão de nomenclatura:
   - Composables públicos com prefixo do recurso (Ex: `LoginScreen`, `ProfileCard`)
   - Composables privados com prefixo auxiliar (Ex: `LoginContent`, `ProfileHeader`)
2. Organize Composables em arquivos por feature
3. Mantenha Composables pequenos e reutilizáveis
4. Use modificadores como parâmetros

#### Exemplo de Estrutura
```kotlin
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    uiState: LoginUiState,
    onLoginClick: (String, String) -> Unit
) {
    LoginContent(
        modifier = modifier,
        uiState = uiState,
        onLoginClick = onLoginClick
    )
}

@Composable
private fun LoginContent(
    modifier: Modifier = Modifier,
    uiState: LoginUiState,
    onLoginClick: (String, String) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LoginHeader()
        LoginForm(
            onLoginClick = onLoginClick,
            enabled = !uiState.isLoading
        )
        if (uiState.isLoading) {
            LoginLoadingIndicator()
        }
    }
}
```

### Gerenciamento de Estado

#### Regras
1. Estado deve ser hoisted (elevado) para o composable pai apropriado
2. Use `remember` para estado local
3. Use `rememberSaveable` para estado que sobrevive à recriação
4. Prefira estado imutável

#### Exemplo de Estado
```kotlin
@Composable
fun LoginForm(
    onLoginClick: (String, String) -> Unit,
    enabled: Boolean
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    
    Column {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            enabled = enabled,
            label = { Text("Email") }
        )
        
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            enabled = enabled,
            label = { Text("Senha") }
        )
        
        Button(
            onClick = { onLoginClick(email, password) },
            enabled = enabled && email.isNotEmpty() && password.isNotEmpty()
        ) {
            Text("Login")
        }
    }
}
```

### Otimização de Performance

#### Regras
1. Use `remember` para cálculos pesados
2. Evite alocações desnecessárias em composables
3. Use `LaunchedEffect` para efeitos colaterais
4. Minimize recomposições desnecessárias

#### Exemplo de Otimizações
```kotlin
@Composable
fun UserList(
    users: List<User>,
    onUserClick: (User) -> Unit
) {
    // Lembre-se da função de clique para evitar recriações
    val onUserClickRemembered = remember(onUserClick) { onUserClick }
    
    // Calcule dados processados apenas quando necessário
    val sortedUsers by remember(users) {
        derivedStateOf { users.sortedBy { it.name } }
    }
    
    LazyColumn {
        items(
            items = sortedUsers,
            key = { it.id } // Use key para otimizar atualizações
        ) { user ->
            UserItem(
                user = user,
                onClick = { onUserClickRemembered(user) }
            )
        }
    }
}

@Composable
private fun UserItem(
    user: User,
    onClick: () -> Unit
) {
    // Use constantes para valores fixos
    val paddingValues = remember { PaddingValues(16.dp) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(paddingValues)
            .clickable(onClick = onClick)
    ) {
        // Conteúdo do item
    }
}
```

### Boas Práticas

1. **Composição**
   - Prefira composição sobre herança
   - Extraia componentes reutilizáveis
   - Use slots para conteúdo flexível
   - Mantenha a responsabilidade única

2. **Estado**
   - Use `rememberSaveable` para persistência
   - Implemente `Parcelable` para objetos complexos
   - Centralize estado no ViewModel
   - Use `collectAsState()` para Flows

3. **Performance**
   - Use `key` em listas
   - Evite lambda recreations
   - Minimize o número de composables
   - Use Modifier.composed para efeitos complexos

4. **UI/UX**
   - Siga o Material Design 3
   - Implemente animações com AnimatedVisibility
   - Use estados de carregamento e erro
   - Forneça feedback visual para ações

### Exemplo de Preview
```kotlin
@Preview(showBackground = true)
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun LoginScreenPreview() {
    MoneyMindTheme {
        LoginScreen(
            uiState = LoginUiState(),
            onLoginClick = { _, _ -> }
        )
    }
}
```


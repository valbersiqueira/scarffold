---
applyTo: "**"
description: "Instructions architecture development"
---

# Instruções de Arquitetura

## Clean Architecture + MVVM

### Estrutura do Projeto
```
app/
├── data/           # Camada de Dados
│   ├── api/        # Serviços de API
│   ├── db/         # Banco de dados local
│   ├── di/         # Injeção de dependência da camada
│   ├── mappers/    # Conversores de modelos
│   ├── models/     # Modelos de dados
│   └── repositories/ # Implementações dos repositórios
│
├── domain/         # Camada de Domínio
│   ├── models/     # Modelos de domínio
│   ├── repositories/ # Interfaces dos repositórios
│   └── usecases/   # Casos de uso
│
├── presentation/   # Camada de Apresentação
│   ├── ui/         # Componentes de UI
│   │   ├── feature1/
│   │   │   ├── components/ # Componentes específicos
│   │   │   ├── screens/    # Telas
│   │   │   └── viewmodels/ # ViewModels
│   │   └── shared/    # Componentes compartilhados
│   └── utils/     # Utilitários da UI
│
└── di/            # Injeção de dependência do app
```

### Camada de Dados (Data Layer)

#### Regras
1. Responsável pela implementação concreta dos repositórios
2. Contém todas as fontes de dados (API, banco de dados local)
3. Implementa a lógica de cache e sincronização
4. Define os modelos de dados (Request e Response)

#### Exemplo de Implementação
```kotlin
// Data Model (Request)
data class UserRequest(
    val id: String,
    val name: String,
    val email: String
)

// Data Model (Response)
data class UserResponse(
    val id: String,
    val name: String,
    val email: String
)

// Repository Implementation
class UserRepositoryImpl @Inject constructor(
    private val api: UserApi,
    private val db: UserDao,
    private val mapper: UserMapper
) : UserRepository {
    override suspend fun getUser(id: String): Flow<User> = flow {
        // Primeiro tenta do banco local
        db.getUser(id)?.let { 
            emit(mapper.toDomain(it))
        }
        
        // Atualiza do servidor
        try {
            val remoteUser = api.getUser(id)
            db.insertUser(remoteUser)
            emit(mapper.toDomain(remoteUser))
        } catch (e: Exception) {
            throw e
        }
    }
}
```

### Camada de Domínio (Domain Layer)

#### Regras
1. Contém toda a lógica de negócio
2. Define interfaces dos repositórios
3. Contém casos de uso independentes de frameworks
4. Define modelos de domínio puros

#### Exemplo de Implementação
```kotlin
// Domain Model
data class User(
    val id: String,
    val name: String,
    val email: String
)

// Repository Interface
interface UserRepository {
    suspend fun getUser(id: String): Flow<User>
}

// Use Case
class GetUserUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(id: String): Flow<User> =
        repository.getUser(id)
}
```

### Camada de Apresentação (Presentation Layer)

#### Regras
1. Implementa o padrão MVVM
2. ViewModels não conhecem a View
3. Estados imutáveis via StateFlow
4. Eventos únicos via SharedFlow
5. Um ViewModel por tela

#### Exemplo de Implementação
```kotlin
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Initial)
    val uiState = _uiState.asStateFlow()

    fun login(credentials: LoginCredentials) {
        viewModelScope.launch {
            _uiState.update { LoginUiState.Loading }
            try {
                val result = loginUseCase(credentials)
                _uiState.update { LoginUiState.Success(result) }
            } catch (e: Exception) {
                _uiState.update { LoginUiState.Error(e.message) }
            }
        }
    }
}

sealed class LoginUiState {
    object Initial : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val user: User) : LoginUiState()
    data class Error(val message: String?) : LoginUiState()
}
```

### Injeção de Dependência (DI Layer)

#### Regras
1. Use Hilt para injeção de dependência
2. Módulos separados por camada
3. Escopos apropriados para cada dependência
4. Bindings claros e explícitos

#### Exemplo de Implementação
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    @Singleton
    fun provideUserRepository(
        api: UserApi,
        db: UserDatabase,
        mapper: UserMapper
    ): UserRepository = UserRepositoryImpl(api, db, mapper)
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

## Boas Práticas

### Clean Architecture
1. Dependências apontam para dentro (domínio)
2. Camada de domínio não depende de nada externo
3. Use casos representam uma única ação
4. Mapeamento entre camadas via mappers

### MVVM
1. ViewModel não tem referência à View
2. View observa estados do ViewModel
3. Lógica de negócio nos use cases, não no ViewModel
4. Estados representam toda a UI

### Geral
1. Princípio da Responsabilidade Única
2. Injeção de dependência para acoplamento fraco
3. Testes unitários por camada
4. Documentação clara de interfaces



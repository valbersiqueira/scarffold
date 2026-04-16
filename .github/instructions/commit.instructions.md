---
applyTo: "**"
description: "Instructions for Git commit best practices"
---

# Instruções para Commits

## Padrões de Commit

### Estrutura da Mensagem
```
<tipo>[escopo opcional]: <descrição>

[corpo opcional]

[rodapé(s) opcional(is)]
```

### Tipos de Commit
- `feat`: Nova funcionalidade
- `fix`: Correção de bug
- `docs`: Alterações em documentação
- `style`: Formatação, ponto e vírgula faltando; Não altera o código
- `refactor`: Refatoração de código
- `test`: Adicionando testes, refatorando testes
- `chore`: Atualizando tasks do Gradle, configurações, etc
- `perf`: Alterações relacionadas a performance
- `ci`: Alterações em arquivos de CI
- `build`: Alterações que afetam o sistema de build
- `revert`: Reverte um commit anterior

### Regras para Commits

1. **Título do Commit**
   - Deve ser claro e conciso
   - Máximo de 50 caracteres
   - Use o tempo verbal no imperativo
   - Primeira letra maiúscula
   - Sem ponto final

2. **Corpo do Commit**
   - Deve explicar "o quê" e "por quê" vs. "como"
   - Máximo de 72 caracteres por linha
   - Separe título do corpo com uma linha em branco
   - Use parágrafos para melhor legibilidade

3. **Rodapé do Commit**
   - Use para referenciar issues
   - Identificar breaking changes
   - Múltiplos rodapés são separados por uma linha

### Exemplos de Bons Commits

```
feat(auth): Implementa autenticação via Google

Adiciona suporte para login social usando Google OAuth2.
- Integração com Firebase Auth
- Persistência do token de acesso
- Tratamento de refresh token

Resolve: #123
```

```
fix(login): Corrige validação de senha

A validação anterior permitia senhas sem caracteres especiais,
violando os requisitos de segurança definidos.

Breaking change: senhas antigas precisarão ser atualizadas
```

```
refactor(api): Migra chamadas para Retrofit

Substitui implementação antiga usando HttpURLConnection por Retrofit
para melhor manutenibilidade e redução de código boilerplate.

Performance: reduz tempo de resposta em 30%
```

## Boas Práticas

### 1. Commits Atômicos
- Um commit por alteração lógica
- Facilita review e rollback
- Mantém histórico limpo e compreensível

### 2. Branches
- Use feature branches para novas funcionalidades
- Mantenha branches atualizadas com a main
- Delete branches após merge

### 3. Commits Frequentes
- Commit early, commit often
- Evite commits gigantes
- Mantenha alterações relacionadas juntas

### 4. Mensagens Claras
- Seja descritivo mas conciso
- Use voz imperativa
- Inclua contexto quando necessário

### 5. Revisão Antes do Commit
- Use `git diff` para revisar alterações
- Verifique arquivos não versionados
- Confirme se não há informações sensíveis

## Workflow Recomendado

1. **Iniciando uma Feature**
   ```bash
   git checkout -b feature/nome-da-feature
   ```

2. **Durante o Desenvolvimento**
   ```bash
   git add -p  # Revise as alterações por hunks
   git commit  # Use mensagem seguindo o padrão
   ```

3. **Antes do Push**
   ```bash
   git pull --rebase origin main  # Mantenha branch atualizada
   git push origin feature/nome-da-feature
   ```

4. **Após o Merge**
   ```bash
   git checkout main
   git pull
   git branch -d feature/nome-da-feature
   ```

## Commits Após Modificações

### Passo a Passo

1. **Revise suas Alterações**
   ```bash
   git status  # Veja quais arquivos foram modificados
   git diff    # Revise as alterações em detalhes
   ```

2. **Agrupe Alterações Logicamente**
   - Identifique alterações relacionadas
   - Separe em commits diferentes se necessário
   - Use `git add` seletivamente

3. **Prepare o Commit**
   ```bash
   # Para adicionar arquivos específicos
   git add caminho/do/arquivo1.kt caminho/do/arquivo2.kt

   # Para adicionar alterações específicas em um arquivo
   git add -p caminho/do/arquivo.kt

   # Para remover arquivos adicionados por engano
   git reset caminho/do/arquivo.kt
   ```

4. **Verifique o que será Commitado**
   ```bash
   git status              # Veja o status atual
   git diff --cached       # Revise o que será commitado
   ```

5. **Faça o Commit**
   ```bash
   # Commit com mensagem curta
   git commit -m "feat(login): Adiciona validação de senha"

   # Commit com mensagem detalhada
   git commit
   ```
   
   Exemplo de mensagem detalhada no editor:
   ```
   feat(login): Adiciona validação de senha

   Implementa requisitos de segurança para senhas:
   - Mínimo de 8 caracteres
   - Pelo menos uma letra maiúscula
   - Pelo menos um número
   - Pelo menos um caractere especial

   Issue: #456
   ```

### Dicas para Bons Commits

1. **Verifique Antes de Commitar**
   - Remova prints de debug
   - Verifique formatação do código
   - Certifique-se que os testes passam
   - Não inclua arquivos temporários

2. **Organize as Alterações**
   - Um commit por funcionalidade
   - Separe refatorações de novas features
   - Mantenha commits relacionados juntos
   - Use commits atômicos

3. **Mensagens Efetivas**
   - Comece com o tipo adequado
   - Use escopo quando relevante
   - Descreva o que e por que, não como
   - Referencie issues relacionadas

### Correções Comuns

1. **Alterar Último Commit**
   ```bash
   # Adicionar mais alterações ao último commit
   git add arquivo-esquecido.kt
   git commit --amend

   # Apenas alterar a mensagem do último commit
   git commit --amend -m "feat(auth): Mensagem corrigida"
   ```

2. **Desfazer Commits**
   ```bash
   # Reverter commit mantendo histórico
   git revert <hash-do-commit>

   # Desfazer último commit mantendo alterações
   git reset --soft HEAD^

   # Desfazer commits e alterações (cuidado!)
   git reset --hard HEAD^
   ```

3. **Reorganizar Commits**
   ```bash
   # Reorganizar últimos commits
   git rebase -i HEAD~3

   # Juntar commits
   git rebase -i HEAD~2
   # Mude 'pick' para 'squash' nos commits a serem unidos
   ```

### Checklist Final

- [ ] Alterações são coesas e relacionadas
- [ ] Código está formatado e testado
- [ ] Mensagem segue o padrão estabelecido
- [ ] Issues relacionadas estão referenciadas
- [ ] Não há informações sensíveis
- [ ] Arquivos temporários foram removidos
- [ ] Branch está atualizada com main

## Anti-padrões

### Evite:
1. Mensagens vagas como "Fix bug" ou "Update"
2. Commits com múltiplas alterações não relacionadas
3. Commits sem descrição adequada
4. Push direto na main/develop
5. Commits com arquivos temporários
6. Mensagens sem contexto do que foi alterado

### Exemplos de Commits Ruins
```
# Muito vago
fix: bug fix

# Múltiplas alterações não relacionadas
feat: add login and update database and fix crash

# Sem contexto
chore: update dependencies

# Mensagem não descritiva
style: changes
```

## Ferramentas Recomendadas

1. **Commitlint**
   - Valida mensagens de commit
   - Garante padrão consistente
   - Pode ser integrado com husky

2. **Commitizen**
   - CLI para formatar mensagens
   - Guia interativo para commits
   - Garante padrão Conventional Commits

3. **Husky**
   - Git hooks made easy
   - Valida commits antes do push
   - Executa testes automaticamente

### Configuração do Commitlint
```json
{
  "extends": ["@commitlint/config-conventional"],
  "rules": {
    "type-enum": [
      2,
      "always",
      [
        "feat",
        "fix",
        "docs",
        "style",
        "refactor",
        "test",
        "chore",
        "perf",
        "ci",
        "build",
        "revert"
      ]
    ],
    "type-case": [2, "always", "lower"],
    "type-empty": [2, "never"],
    "scope-case": [2, "always", "lower"],
    "subject-empty": [2, "never"],
    "subject-full-stop": [2, "never", "."],
    "header-max-length": [2, "always", 50]
  }
}
```

### Configuração do Husky
```json
{
  "husky": {
    "hooks": {
      "commit-msg": "commitlint -E HUSKY_GIT_PARAMS",
      "pre-commit": "lint-staged",
      "pre-push": "./gradlew test"
    }
  }
}
```

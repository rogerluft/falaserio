# 🔐 Guia de Assinatura de Release - FalaSério

## 📋 Passo 1: Criar Keystore (APENAS UMA VEZ!)

Execute o comando abaixo no terminal. **GUARDE A SENHA COM SEGURANÇA!**

```bash
keytool -genkey -v -keystore falaserio-release.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias falaserio
```

**Informações solicitadas:**
- **Senha do keystore:** Escolha uma senha forte (ex: `F@l@S3r!0#2026`)
- **Nome e sobrenome:** WebStorage Tecnologia
- **Unidade organizacional:** Desenvolvimento
- **Organização:** WebStorage
- **Cidade:** [Sua cidade]
- **Estado:** [Seu estado]
- **Código do país:** BR
- **Senha da chave:** Use a mesma senha do keystore

⚠️ **CRÍTICO: Fazer backup do arquivo `.jks` e guardar as senhas!**  
Se perder, **NUNCA** poderá atualizar o app na Play Store!

---

## 📂 Passo 2: Mover Keystore para Local Seguro

```bash
# Criar pasta de keystores (fora do projeto Git!)
mkdir -p ~/keystores

# Mover o arquivo
mv falaserio-release.jks ~/keystores/

# Verificar
ls -la ~/keystores/
```

---

## 🔑 Passo 3: Configurar Variáveis de Ambiente

### Linux/Mac (adicionar ao `~/.bashrc` ou `~/.zshrc`):

```bash
export FALASERIO_KEYSTORE_PATH="$HOME/keystores/falaserio-release.jks"
export FALASERIO_KEYSTORE_PASSWORD="SUA_SENHA_AQUI"
export FALASERIO_KEY_ALIAS="falaserio"
export FALASERIO_KEY_PASSWORD="SUA_SENHA_AQUI"
```

Depois execute: `source ~/.bashrc`

### Windows (PowerShell):

```powershell
[System.Environment]::SetEnvironmentVariable("FALASERIO_KEYSTORE_PATH", "$env:USERPROFILE\keystores\falaserio-release.jks", "User")
[System.Environment]::SetEnvironmentVariable("FALASERIO_KEYSTORE_PASSWORD", "SUA_SENHA_AQUI", "User")
[System.Environment]::SetEnvironmentVariable("FALASERIO_KEY_ALIAS", "falaserio", "User")
[System.Environment]::SetEnvironmentVariable("FALASERIO_KEY_PASSWORD", "SUA_SENHA_AQUI", "User")
```

---

## 🚀 Passo 4: Build de Release

```bash
# Limpar build anterior
./gradlew clean

# Build release (assinado automaticamente)
./gradlew assembleRelease

# APK estará em:
# app/build/outputs/apk/release/app-release.apk
```

---

## ✅ Passo 5: Verificar Assinatura

```bash
# Verificar se APK está assinado corretamente
jarsigner -verify -verbose -certs app/build/outputs/apk/release/app-release.apk

# Deve aparecer: "jar verified."
```

---

## 📦 Passo 6: Gerar App Bundle (Recomendado para Play Store)

```bash
# App Bundle é mais eficiente que APK
./gradlew bundleRelease

# AAB estará em:
# app/build/outputs/bundle/release/app-release.aab
```

---

## 🔒 Segurança: O que NÃO fazer

❌ **NUNCA** comitar o arquivo `.jks` no Git  
❌ **NUNCA** comitar senhas em código  
❌ **NUNCA** compartilhar keystore publicamente  
✅ Fazer backup do keystore em local seguro (cloud criptografado, HD externo)  
✅ Guardar senhas em gerenciador de senhas  

---

## 🆘 Se perder o keystore:

⚠️ **PROBLEMA GRAVE!** Não será possível:
- Atualizar o app na Play Store
- Manter o mesmo applicationId

**Soluções:**
1. **Prevenir:** Fazer múltiplos backups agora
2. **Se perdeu:** Terá que publicar como app novo (perde usuários)

---

## 📝 Checklist de Segurança

- [ ] Keystore criado e salvo em `~/keystores/`
- [ ] Backup do keystore em local seguro (cloud/HD externo)
- [ ] Senhas salvas em gerenciador de senhas
- [ ] Variáveis de ambiente configuradas
- [ ] Teste de build release bem-sucedido
- [ ] `.jks` está no `.gitignore` (já configurado)
- [ ] APK assinado verificado com `jarsigner`

---

**Data de criação:** [Preencher ao criar keystore]  
**Senha do keystore:** [Salvar em gerenciador de senhas]  
**Alias:** falaserio  
**Validade:** 10.000 dias (~27 anos)

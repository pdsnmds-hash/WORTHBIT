# WorthBit — подпись в Codemagic

Приложение: com.rainbow.priceperuse, модуль app, версия 1.5 / code 10, minSdk 21, compileSdk/targetSdk 36, Java 17. VersionCode перед загрузкой увеличивают, если 10 уже использован в Play. SDK и идентификатор при настройке CI не менялись.

## 1. Одноразовый ключ

Пользователь подтвердил, что приложение ещё не публиковалось. Корневой codemagic.yaml сейчас содержит ТОЛЬКО генератор, workflow worthbit-create-upload-key-once. Никакого release-ключа на компьютере не создавалось; имеющиеся debug-ключи сохранены.

В Codemagic создайте группу worthbit_key_generation с двумя Secret-переменными NEW_KEYSTORE_PASSWORD и NEW_KEY_PASSWORD. Пароли сохраните в менеджере паролей, не в репозитории. Subject сертификата CN=WorthBit; организация и страна не выдумываются.

Подключите Git-репозиторий и один раз запустите генератор. Скачайте generated-signing/worthbit-upload.jks и generated-signing/upload_certificate.pem. Сохраните защищённую резервную копию JKS и паролей. Повторный запуск создаст другой ключ даже с тем же alias.

## 2. Identity

Team settings → codemagic.yaml settings → Code signing identities → Android keystores:

- файл: worthbit-upload.jks;
- Keystore password: значение NEW_KEYSTORE_PASSWORD;
- Key alias: upload;
- Key password: значение NEW_KEY_PASSWORD;
- Reference name: worthbit_upload.

Alias внутри JKS и Reference name в Codemagic — разные значения. Генерация JKS уже создаёт upload key. App signing key Google Play — отдельный ключ, для нового приложения его можно поручить Google.

## 3. Обычная сборка

После загрузки identity замените корневой YAML:

```sh
cp docs/codemagic/codemagic.yaml codemagic.yaml
git add codemagic.yaml
git commit -m "ci: enable signed bundle build"
git push
```

Workflow worthbit-aab использует identity worthbit_upload. prepare_signing.py создаёт release/signing/upload-keystore.jks и release/upload-key.properties с правами 600; storeFile разрешается относительно корня проекта. Перезапись существующих файлов запрещена. В debug ключ не требуется. Release без signing properties завершается ошибкой, а не выдаёт незаметно неподписанный пакет.

Сборка выполняет аудит Git, выбор новейшего установленного sdkmanager по Pkg.Revision, установку platforms;android-36 и build-tools;35.0.0 (фактическая версия Build Tools по умолчанию у AGP 8.13.2), unit tests, lint, bundleRelease и проверку AAB. Артефакты: app-release.aab, отчёты lint и unit tests. R8 выключен, mapping.txt не создаётся. Обычный workflow не выдаёт JKS и не публикует приложение в Play.

Приложение не содержит нативных .so: DEX универсален для armeabi-v7a, arm64-v8a, x86 и x86_64. Verifier допускает отсутствие native libraries; если они появятся, требует все четыре ABI.

## Git

В этой папке на момент подготовки не было .git. Remote и облачный проект не создавались. Для нового репозитория:

```sh
git init
python3 docs/codemagic/audit_repository.py
git add .
git diff --cached --stat
python3 docs/codemagic/audit_repository.py
git commit -m "ci: prepare WorthBit signing in Codemagic"
# После добавления вашего remote:
git push -u origin HEAD
```

Перед git add аудит проверяет будущий набор файлов во временном Git index. После инициализации Git проверяет отслеживаемые и новые неигнорируемые файлы; запуск после git add также обязателен. Ignore не убирает уже отслеживаемые файлы. Рабочие архивы и отчёты предыдущих этапов оставлены локально, но исключены из Git. Gradle/Kotlin metadata и файлы JAR-подписи — необходимые технические данные, они сохраняются.

## Проверка и границы

local-validation.json содержит результаты локальных проверок. Для повтора helper-проверок установите PyYAML в отдельное Python-окружение и запустите JAVA_HOME=<JDK17> python3 docs/codemagic/validate_local.py. Обычная CI-сборка PyYAML не требует. Облачный запуск, генерация реального upload key и регистрация в Play не выполнялись. Источники примеров NEW LOOK и HUN_SAFORA по путям шаблона недоступны; найдена и прочитана копия этого процесса в локальном проекте my-crua (docs/codemagic/). Удалены настройки чужого приложения; runtime/Firebase secrets WorthBit не нужны.

Официальная документация:
- https://docs.codemagic.io/yaml-code-signing/signing-android/
- https://docs.codemagic.io/yaml-quick-start/building-a-native-android-app/
- https://developer.android.com/studio/publish/app-signing
